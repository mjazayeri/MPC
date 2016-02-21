import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PartyInterface extends Remote {
	
	public int getId() throws RemoteException;
	
	public boolean addition(String a, String b, boolean isPartyReq) throws RemoteException;
	
	public boolean multiplication(String a, String b, boolean isPartyReq) throws RemoteException;
	
	public boolean multiplyByConstant(String a, int constant, boolean isPartyReq) throws RemoteException;
	
	public boolean setShare(String name, int value) throws RemoteException;
	
	public int getShare(String name) throws RemoteException;
	
	public void computeMultiplication(String sharesTokenName) throws RemoteException;
	
}
