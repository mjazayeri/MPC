import java.awt.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.rmi.registry.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class MainMPC {

	private static Party party;
	private static int partyId;
	private static String IP;
	private static int port;
	
	private static int prime;
	private static int degree;
	private static int partiesCount = 0;
	
	private static ArrayList<Address> addresses; 
	
	public static void main(String[] args) {
		try {
			
			setServerConfigurations();
			
			party = new Party(prime, degree, partiesCount, partyId);


			Registry registry = LocateRegistry.createRegistry(port);
			registry.rebind("P"+ (partyId + 1), (PartyInterface)party);
			
			System.out.println("If all parties are ready entery [Y]:");
			System.out.print(">");
			new Scanner(System.in).next();

		} 
		catch (Exception e) {
			System.out.println("exception:" + e.getMessage());
			return;
		}

		
		if(!party.connectToOtherParties(addresses))
			return;
		
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
		addresses = new ArrayList<Address>();
		try {
			System.out.println("Select a serverId from [0, 1, 2, 3, 4,...]");
			System.out.print(">");
			Scanner scanner = new Scanner(System.in);
			partyId = scanner.nextInt();
			
			boolean doSkip = false;
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("settings.config")))) { 
				String line;
				while((line = reader.readLine()) != null) { 
					Address addr = new Address();
					partiesCount++;
					
					String[] tokens = line.split(",");
					for (int i = 0; i < tokens.length; i++) {
						String[] items = tokens[i].split(":");
						switch (items[0]) {
						case "ID":
							addr.id = Integer.parseInt(items[1]);
							break;
						case "IP":
							addr.IP = items[1];
							break;
						case "PORT":
							addr.port = Integer.parseInt(items[1]);
							break;
						case "PRIME":
							if(!doSkip)
								prime = Integer.parseInt(items[1]);
							break;
						case "DEGREE":
							if(!doSkip)
								degree = Integer.parseInt(items[1]);
							break;
						default:
							break;
						}
					}
					
					if(addr.id == partyId) {
						IP = addr.IP;
						port = addr.port;
						doSkip = true;
					}
					else
						addresses.add(addr);
				}
			}
			catch(Exception e) {
				System.out.println("setServerConfigurations, Error:" + e.getMessage());
			}
		} 
		catch (Exception e) {
			
			System.out.println("setServerConfigurations. Error: " + e.getMessage());
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
}