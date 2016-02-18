import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import javax.print.attribute.standard.NumberOfDocuments;

public class Party extends UnicastRemoteObject implements PartyInterface{
	
	public HashMap<String, Integer> shares;
	private SharingScheme scheme;
	private PartyInterface[] parties;
	private int[] recombinationVector;
	
	private String neededToken = "";
	private int receivedToken = 0;
	
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
	
	public void connectToOtherParties(int[] ports) { 
		parties = new PartyInterface[NUMBER_OF_PARTIES - 1];
		
		int nextParty = (PARTY_ID +1) % NUMBER_OF_PARTIES;
		int j = 0;
		try {
			for (int i = 0; i < ports.length; i++) {
				if( i != PARTY_ID) {
					String partyName = "P" + ( nextParty + 1);
					Registry registry = LocateRegistry.getRegistry(ports[i]);
					parties[j++] = (PartyInterface)registry.lookup(partyName);
					nextParty = nextParty + 1 % NUMBER_OF_PARTIES;
				}
			}
			
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
		//parties.add
	}
	
	public  boolean shareSecret(String secretName, int secretValue) {
		try {
			int [] sharesOfSecret = scheme.generateShares(secretValue, NUMBER_OF_PARTIES);
			shares.put(secretName, sharesOfSecret[PARTY_ID]);
			
			int nextId = (PARTY_ID + 1) % NUMBER_OF_PARTIES;
			for (int i = 0; i < parties.length; i++) {
				int share = sharesOfSecret[nextId];
				parties[i].setShare(secretName, share);
				nextId = (nextId + 1) % NUMBER_OF_PARTIES;
			}
			return true;
		} 
		catch (Exception e) {
			System.out.println("shareSeceret, Exception:" + e.getMessage());
			return false;
		}
	}
	
	@Override
	public synchronized int getId() throws RemoteException { 
		return PARTY_ID;
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
	public boolean multiplication(String a, String b, boolean isPartyReq) throws RemoteException{
		System.out.println("called *");
		int firstVal = shares.get(a);
		int secondVal = shares.get(b);
		
		int multiplication = scheme.moduloPrime(firstVal * secondVal);
		if(!shareSecret(PARTY_ID + a + "*" + b, multiplication)) {
			System.out.println("Error occured in sharing the result of a * b");
			return false;
		}
		try {
			
			neededToken = a + "*" + b;
			receivedToken = 0;
			
			if(!isPartyReq) {
				System.out.println("Share mult result with others");
				for(int i = 0; i < parties.length; i++) { 
					if(!parties[i].multiplication(a, b, true)) {
						System.out.println("Error occured during other's multiplication");
						return false;
					}
				}
			}
			
			System.out.println("waiting for other's shares");
			while(receivedToken < NUMBER_OF_PARTIES - 1) {
				wait();
			}
			System.out.println("shares received");
			
			int multResult = 0;
			for (int i = 0; i < NUMBER_OF_PARTIES; i++) {
				int share = shares.get( i + a + "*" + b);
				multResult += recombinationVector[i] * share;
			}
			
			shares.put(a + "*" + b, scheme.moduloPrime(multResult));
		} catch (Exception e) {
			System.out.println("multiplication, Exception: ");e.printStackTrace();
		}
		return true;
		
	}
	
	@Override
	public synchronized boolean setShare(String shareName, int shareValue) throws RemoteException{
		try {
			
			if(!neededToken.equals("") && shareName.contains(neededToken)) {
				receivedToken++;
				notify();
			}
			shares.put(shareName, shareValue);
			return true;
		} 
		catch (Exception e) {
			System.out.println("setShare,Exception:" + e.getMessage());
			return false;
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
