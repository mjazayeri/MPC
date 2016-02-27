import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

import javax.print.attribute.standard.NumberOfDocuments;

import com.sun.corba.se.impl.encoding.TypeCodeReader;

public class Party extends UnicastRemoteObject implements PartyInterface{
	
	public HashMap<String, Integer> shares;
	private SharingScheme scheme;
	private PartyInterface[] parties;
	private int[] recombinationVector;
	
	private String neededToken = "";
	private int receivedTokens = 0;
	
	public final int NUMBER_OF_PARTIES;
	
	//a number in [0 , NUMBER_OF_PARTIES]
	public final int PARTY_ID; 
		
	public Party(int prime, int degree, int partiesCount, int id) throws RemoteException{
		shares = new HashMap<>();
		scheme = new SharingScheme(prime, degree);
		PARTY_ID = id;
		
		NUMBER_OF_PARTIES = partiesCount;
		int[] setOfAllParties = new int[NUMBER_OF_PARTIES];
		for (int i = 0; i < setOfAllParties.length; i++) setOfAllParties[i] = i+1;
		recombinationVector = scheme.generateRecombinationVector(setOfAllParties);
	}
	
	public boolean connectToOtherParties(ArrayList<Address> addresses) { 
		parties = new PartyInterface[NUMBER_OF_PARTIES - 1];
		
		try {
			for (int i = 0; i < addresses.size(); i++) {
				Address addr = addresses.get(i);
				String partyName = "P" + (addr.id + 1);
				Registry registry = LocateRegistry.getRegistry(addr.IP, addr.port);
				parties[i] = (PartyInterface)registry.lookup(partyName);
			}
			return true;
		} 
		catch (Exception e) {
			System.out.println("connectToOtherParties: " + e.getMessage());
			return false;
		}
	}
	
	public synchronized boolean shareSecret(String secretName, int secretValue) {
		
		int j = 0;
		int [] sharesOfSecret = scheme.generateShares(secretValue, NUMBER_OF_PARTIES);
		if(sharesOfSecret == null) 
			return false;
		
		try {
			for (int i = 0; i < NUMBER_OF_PARTIES; i++) {
				if(i == PARTY_ID)
					setShare(secretName, sharesOfSecret[i]);
				else {
					if(!parties[j++].setShare(secretName, sharesOfSecret[i])) {
						System.out.println("Error Sharing secret of party"+ j);
						return false;
					}
				}
			}
			return true;
		} 
		catch (Exception e) {
			System.out.println("shareSeceret party"+j+", Exception:" + e.getMessage());
			return false;
		}
	}
	
	@Override
	public synchronized int getId() throws RemoteException { 
		return PARTY_ID;
	}
	
	@Override
	public boolean multiplyByConstant(String a, int constant, boolean isPartyReq) throws RemoteException {
		if(!shares.containsKey(a))
			return false;
		
		int secret = shares.get(a);
		secret = scheme.moduloPrime(constant * secret);
		shares.put(a, secret);
		
		if(!isPartyReq) {
			for (int i = 0; i < parties.length; i++) {
				parties[i].multiplyByConstant(a, constant, true);
			}
		}
		
		return true;
	}
	
	@Override
	public synchronized boolean addition(String a, String b, boolean isPartyReq) throws RemoteException {
			
		int firstVal = shares.get(a);
		int secondVal = shares.get(b);
		int addition = scheme.moduloPrime(firstVal + secondVal);
		
		shares.put(a + "+" + b, addition);
		if(isPartyReq)
			return true;
		
		for (int i = 0; i < parties.length; i++) {
			if(!parties[i].addition(a, b, true))
				return false;
		}
		System.out.println("party<" + PARTY_ID + "> a + b = " + addition);
		return true;
	}
	
	@Override
	public int getShare(String name) throws RemoteException {
		
		if(shares.containsKey(name))
			return shares.get(name);
		
		return -1;
	}
	
	@Override
	public boolean multiplication(String a, String b, boolean isPartyReq) throws RemoteException {
		int firstVal=0, secondVal=0;
		synchronized (this) {
			firstVal = shares.get(a);
			secondVal = shares.get(b);
		}
		try {
			int multiplication = scheme.moduloPrime(firstVal * secondVal);
			if(!shareSecret(PARTY_ID + a + "*" + b, multiplication)) {
				return false;
			}
			
			if(!isPartyReq) {
				for(int i = 0; i < parties.length; i++) { 
					if(!parties[i].multiplication(a, b, true)) {
						System.out.println("Error occured during other's multiplication");
						return false;
					}
				}
				computeMultiplication(a + "*" + b);
				for (int i = 0; i < parties.length; i++) {
					parties[i].computeMultiplication(a + "*" + b);
				}
			}

			return true;
		} 
		catch (Exception e) {
			System.out.println("multiplication, Exception: " + e.getLocalizedMessage());
			return false;
		}
	}
	
	@Override
	public synchronized boolean setShare(String shareName, int shareValue) throws RemoteException{
		try {
			
			shares.put(shareName, shareValue);
			return true;
		} 
		catch (Exception e) {
			System.out.println("Exception in setShare:" + e.getMessage());
			return false;
		}
	}
	
	@Override
	public void computeMultiplication(String sharesTokenName) throws RemoteException{
		
		receivedTokens = 0;
		try {
			int multResult = 0;
			for (int i = 0; i < NUMBER_OF_PARTIES; i++) {
				int share = shares.get( i + sharesTokenName);
				multResult += recombinationVector[i] * share;
			}
			
			shares.put(sharesTokenName, scheme.moduloPrime(multResult));
		} 
		catch (Exception e) {
			System.out.println("Exception in computing * result" + e.getMessage());
		}
	}

	public int reconstructSecret(String secretName) {

		int secret = 0;
		int[] partySet = new int[NUMBER_OF_PARTIES];
		int[] values = new int[NUMBER_OF_PARTIES];
		int j=0;
		try {
			for (int i = 0; i < NUMBER_OF_PARTIES; i++) {
				partySet[i] = i+1;
				if(i == PARTY_ID) {
					values[i] = shares.get(secretName);
					if(values[i] < 0) {
						System.out.println("party[" + (i+1) + "doesnt hold" + secretName);
						return -1;
					}
				}
				else
					values[i] = parties[j++].getShare(secretName);
			}

			secret = scheme.findSecret(partySet, values);
			System.out.println(secret);
			return secret;
		} 
		catch (Exception e) {
			System.out.println("Exception in reconstructSecret:" + e.getMessage());
			return -1;
		}
	}
	
	private void sleep() {
		try {
			Thread.sleep(500);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public int reconstructSecret(int[] parties, int[] sharedValues) {
		
		int secret = 0;
		try {
			return scheme.findSecret(parties, sharedValues);
		} catch (Exception e) {
			// TODO: write log
			return -1;
		}
		
	}

}
