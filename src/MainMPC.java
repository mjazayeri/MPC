import java.util.Scanner;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class MainMPC {

	public static void main(String[] args) {
		
		try {
			Party p1 = new Party(7, 1, 3, 0);
			Party p2 = new Party(7, 1, 3, 1);
			Party p3 = new Party(7, 1, 3, 2);
			
			int[] shares_a = p1.shareSecret("a"	, 5);
			p1.setShare("a", shares_a[p1.PARTY_ID]);
			p2.setShare("a", shares_a[p2.PARTY_ID]);
			p3.setShare("a", shares_a[p3.PARTY_ID]);
			
			int[] shares_b = p2.shareSecret("b"	, 1);
			p1.setShare("b", shares_b[p1.PARTY_ID]);
			p2.setShare("b", shares_b[p2.PARTY_ID]);
			p3.setShare("b", shares_b[p3.PARTY_ID]);
			
			int add1 = p1.addition("a", "b");
			int add2 = p2.addition("a", "b");
			int add3 = p3.addition("a", "b");
			
			System.out.println(p1.reconstructSecret(new int[] {1, 2}, new int[]{add1, add2}));
		} 
		catch (Exception e) {
		}
		
	}
}
