import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.*;
import java.time.LocalDateTime;
import java.util.Scanner;

import com.sun.javafx.print.PrintHelper;
import com.sun.xml.internal.ws.util.StringUtils;

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
			
			setServerConfigurations();
			
			party = new Party(prime, degree, partiesCount, partyId);


			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind("P"+ (partyId + 1), (PartyInterface)party);


		} 
		catch (Exception e) {
			System.out.println("exception:" + e.getMessage());
			return;
		}

		
		party.connectToOtherParties(readPartiesPort());
		
		runCommands();

	}

	private static void runCommands() { 
		String command = "";
		System.out.println(">Write a Command (send \"help\" for instructions): ");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			System.out.print(">");
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
					multiplication(params[1], params[2]);
					break;
				case"^":
					int power = Integer.parseInt(params[2]);
					String b = params[1];
					for (int i = 0; i < power - 1; i++) {
						party.multiplication(params[1], b, false);
						b = b + "*" + params[1];
					}
					break;
				case"get":
					System.out.println(party.shares.get(params[1]));
					break;
				case"find":
					party.reconstructSecret(params[1]);
					break;
				case"help":
					printHelp();
					break;
				default:
					System.out.println("Command Not Found");
					break;
				}
			} 
			catch (Exception e) {
				System.out.println(LocalDateTime.now().toString()+ " exception occured: " + e.getMessage());
			}
		}
	}
	
	private static void multiplication(String a, String b) throws Exception{
		if(checkIfIsNumber(a)) {
			party.multiplyByConstant(b, Integer.parseInt(a), false);
		}
		else if(checkIfIsNumber(b)) {
			party.multiplyByConstant(a, Integer.parseInt(b), false);
		}
		else {
			party.multiplication(a, b, false);
		}
	}
	
	private static boolean checkIfIsNumber(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} 
		catch (Exception e) {
			return false;
		}
	}
	private static void setServerConfigurations() {
		try {
			System.out.println(">Select a serverId from [0, 1, 2, 3, 4,...]");
			Scanner scanner = new Scanner(System.in);
			partyId = scanner.nextInt();
			
			System.out.println(">Enter Port: ");
			port = scanner.nextInt();
			
			System.out.println(">Enter the degree of polynomial (t): ");
			degree = scanner.nextInt();
			
			System.out.println(">Enter prime value (p): ");
			prime = scanner.nextInt();
			
			System.out.println(">Enter parties count (n): ");
			partiesCount = scanner.nextInt();
			
		} 
		catch (Exception e) {
			System.out.println("Exception occured in getting serverId. Error: " + e.getMessage());
		}
	}
	private static void printHelp() { 
		System.out.println("To share a value type: <share name value> and hit enter. ex: share x 3 ");
		System.out.println("To add two values type: <+ name1 name2> ex: + x1 x2 ");
		System.out.println("To multiply two values type: <* name1 name2> ex: * x1 x2 ");
		System.out.println("For power operation, type: <^ name1 c> ex: ^ x1 3 ");
		System.out.println("To multiply by constant type: <* c name> or <* name c> ex: * x1 2 or * 2 x1");
		System.out.println("To find the final result use <find name> ex: find a+b or find a*a");
		System.out.println("General example:");
		System.out.println("share x1 3");
		System.out.println("share x2 4");
		System.out.println("^ x1 3");
		System.out.println("* x2 2");
		System.out.println("+ x1*x1*x1 x2");
		System.out.println("find x1*x1*x1+x2 \"put no space in between\"");
		
	}
	private static int[] readPartiesPort() { 
		
		System.out.println();
		System.out.println();
		System.out.println(">If you are sure all the parties are up,");
		int[] ports = new int[partiesCount];

		for (int i = 0; i < ports.length; i++) {
			try {
				if(i != partyId) {
					System.out.println(">Type the port of the server with Id = " + i);
					Scanner scanner = new Scanner(System.in);
					ports[i] = scanner.nextInt();
				}
			}
			catch (Exception e) {
				i--;
			}
		}
		
		return ports;
	}
}