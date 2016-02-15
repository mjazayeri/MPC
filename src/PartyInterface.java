import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PartyInterface extends Remote {
	
	public int getId() throws RemoteException;
	
	public boolean addition(String a, String b) throws RemoteException;
	
	public boolean multiplication(String a, String b) throws RemoteException;
	
	public boolean setShare(String name, int value) throws RemoteException;
	
}
