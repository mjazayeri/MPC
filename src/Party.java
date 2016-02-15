import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Party extends UnicastRemoteObject implements PartyInterface{
	
	private HashMap<String, Integer> shares;
	private SharingScheme scheme;
	PartyInterface[] parties;
	public final int NUMBER_OF_PARTIES = 3;
	
	//a number in [0 , NUMBER_OF_PARTIES]
	public final int PARTY_ID; 
		
	public Party(int prime, int degree, int partiesCount, int id) throws RemoteException{
		shares = new HashMap<>();
		scheme = new SharingScheme(prime, degree);
		PARTY_ID = id;
	}
	
	public void connectToOtherParties() { 
		parties = new PartyInterface[NUMBER_OF_PARTIES - 1];
		
		int othersId = PARTY_ID;
		try {
			for (int i = 0; i < parties.length; i++) {
				String partyName = "P" + (othersId++  % NUMBER_OF_PARTIES);
				Registry registry = LocateRegistry.getRegistry();
				parties[i] = (PartyInterface)registry.lookup(partyName);
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		//parties.add
	}
	
	public boolean shareSecret(String secretName, int secretValue) {
		try {
			int [] sharesOfSecret = scheme.generateShares(secretValue, NUMBER_OF_PARTIES);
			shares.put(secretName, sharesOfSecret[PARTY_ID]);
			
			int othersId = PARTY_ID;
			for (int i = 0; i < parties.length; i++) {
				int share = sharesOfSecret[othersId++ % NUMBER_OF_PARTIES];
				parties[i].setShare(secretName, share);
			}
			return true;
		} 
		catch (Exception e) {
			//TODO: create a log file
			return false;
		}
	}
	
	@Override
	public int getId() throws RemoteException { 
		return PARTY_ID;
	}
	
	@Override
	public boolean addition(String a, String b) throws RemoteException {
			
		int firstVal = shares.get(a);
		int secondVal = shares.get(b);
		int addition = scheme.moduloPrime(firstVal + secondVal);
		
		shares.put(a + "+" + b, addition);
		System.out.println("party<" + PARTY_ID + "> a + b = " + addition);
		return true;
	}
	
	@Override
	public boolean multiplication(String a, String b) {
		int firstVal = shares.get(a);
		int secondVal = shares.get(b);
		
		int multiplication = scheme.moduloPrime(firstVal * secondVal);
		String multName = a + "*" + b + ":" + PARTY_ID;
		if(shareSecret(multName, multiplication)) {
			return true;
		}
		
		shares.remove(multName);
		return true;
		
	}
	
	@Override
	public boolean setShare(String shareName, int shareValue) throws RemoteException{
		try {
			shares.put(shareName, shareValue);
			return true;
		} 
		catch (Exception e) {
			// TODO: write it on log file
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
