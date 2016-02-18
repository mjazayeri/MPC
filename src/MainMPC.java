import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class MainMPC {

	private static Party party;
	private static int partyId;
	private static int prime;
	private static int degree;
	private static int partiesCount;

	private static String serverId = "";
	private static int port;

	public static void main(String[] args) {
		try {
			partyId = Integer.parseInt(args[0]);
			port = Integer.parseInt(args[1]);
			
			setServerConfigurations();
			// remote object created 
			party = new Party(prime, degree, partiesCount, partyId);


			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind("P"+ (partyId + 1), (PartyInterface)party);


		} 
		catch (Exception e) {
			//			new Logger(Constants.SERVER_LOG_PATH, serverId).WriteLog("serverMainException: " + e.getMessage());
			return;
		}

		
		party.connectToOtherParties(readPartiesPort());
		
		runCommands();

	}

	private static void runCommands() { 
		String command = "";
		System.out.println("Write a Command (send \"help\" for instructions): ");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				command = reader.readLine();
				String[] params = command.split(" ");
				
				switch(params[0]) {
				case"exit":
					break;
				case"share":
					int shareVal = Integer.parseInt(params[2]);
					party.shareSecret(params[1], shareVal);
					break;
				case"+":
					party.addition(params[1], params[2], false);
					break;
				case"*":
					party.multiplication(params[1], params[2], false);
					break;
				case"get":
					System.out.println(party.shares.get(params[1]));
					break;
				default:
					break;
				}
			} 
			catch (Exception e) {
				System.out.println(LocalDateTime.now().toString()+ " exception occured: " + e.getMessage());
			}
		}
	}
	
	private static void setServerConfigurations() {
		try {
			System.out.println("Select a serverId from [0, 1, 2, 3, 4,...]");
			Scanner scanner = new Scanner(System.in);
//			partyId = scanner.nextInt();
			
			System.out.println("Enter Port: ");
//			port = scanner.nextInt();
//			System.out.println("Enter the degree of polynomial (t): ");
//			degree = scanner.nextInt();
			degree = 1;
//			System.out.println("Enter prime value (p): ");
//			prime = scanner.nextInt();
			prime = 5;
//			System.out.println("Enter parties count (n): ");
//			partiesCount = scanner.nextInt();
			partiesCount = 2;
		} 
		catch (Exception e) {
			System.out.println("Exception occured in getting serverId. Error: " + e.getMessage());
		}
	}

	private static int[] readPartiesPort() { 
		
		System.out.println("If you are sure all the parties are up,");
		int[] ports = new int[partiesCount];
		try {
			for (int i = 0; i < ports.length; i++) {
				if(i != partyId) {
					System.out.println("type server_" + i + "'s port number:");
					Scanner scanner = new Scanner(System.in);
					ports[i] = scanner.nextInt();
				}
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
		return ports;
	}
}




//Party p1 = new Party(7, 1, 3, 0);
//Party p2 = new Party(7, 1, 3, 1);
//Party p3 = new Party(7, 1, 3, 2);

//int[] shares_a = p1.shareSecret("a"	, 6);
//p1.setShare("a", shares_a[p1.PARTY_ID]);
//p2.setShare("a", shares_a[p2.PARTY_ID]);
//p3.setShare("a", shares_a[p3.PARTY_ID]);
//
//int[] shares_b = p2.shareSecret("b"	, 6);
//p1.setShare("b", shares_b[p1.PARTY_ID]);
//p2.setShare("b", shares_b[p2.PARTY_ID]);
//p3.setShare("b", shares_b[p3.PARTY_ID]);
//
//int add1 = p1.addition("a", "b");
//int add2 = p2.addition("a", "b");
//int add3 = p3.addition("a", "b");
//
//int mult1[] = p1.multiplication("a", "b");
//int mult2[] = p2.multiplication("a", "b");
//int mult3[] = p3.multiplication("a", "b");
//
//int m1 = p1.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[0], mult2[0], mult3[0]});
//int m2 = p2.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[1], mult2[1], mult3[1]});
//int m3 = p3.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[2], mult2[2], mult3[2]});
//System.out.println(p3.reconstructSecret(new int[] {1, 2, 3}, new int[]{m1, m2, m3}));