import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Party {//extends UnicastRemoteObject implements PartyInterface{
	
	private HashMap<String, Integer> shares;
	private SharingScheme scheme;
	
	public final int NUMBER_OF_PARTIES = 3;
	
	//a number in [0 , NUMBER_OF_PARTIES]
	public final int PARTY_ID; 
		
	public Party(int prime, int degree, int partiesCount, int id) throws RemoteException{
		shares = new HashMap<>();
		scheme = new SharingScheme(prime, degree, partiesCount);
		PARTY_ID = id;
	}
	
	public int[] shareSecret(String secretName, int secretValue) {
		try {
			int [] sharesOfSecret = scheme.generateShares(NUMBER_OF_PARTIES, secretValue);
			shares.put(secretName, sharesOfSecret[PARTY_ID]);
			
			//TODO: send this shares to each party;
			return sharesOfSecret;
		} 
		catch (Exception e) {
			//TODO: create a log file
			return null;
		}
	}
	
	//@Override
	public int addition(String a, String b) throws RemoteException {
			
		int firstVal = shares.remove(a);
		int secondVal = shares.remove(b);

		int addition = scheme.moduloPrime(firstVal + secondVal);
		
		//TODO: create shares and send each party's share
		
		shares.put(a + "+" + b, addition);
		System.out.println("party<" + PARTY_ID + "> a + b = " + addition);
		return addition;
	}
	
	//@Override
	public boolean multiplication(String a, String b) {
		int firstVal = shares.remove(a);
		int secondVal = shares.remove(b);
		
		int multiplication = scheme.moduloPrime(firstVal * secondVal);
		
		//TODO:	share the local value
		
		shares.put(a + "*" + b, multiplication);
		return true;
		
	}
	
	//@Override
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
			return scheme.laplase(parties, sharedValues);
		} catch (Exception e) {
			// TODO: write log
			return -1;
		}
		
	}

}
