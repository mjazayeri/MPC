import java.util.Scanner;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class MainMPC {

	public static void main(String[] args) {
		
		try {
			Party p1 = new Party(7, 1, 3, 0);
			Party p2 = new Party(7, 1, 3, 1);
			Party p3 = new Party(7, 1, 3, 2);

			int[] shares_a = p1.shareSecret("a"	, 6);
			p1.setShare("a", shares_a[p1.PARTY_ID]);
			p2.setShare("a", shares_a[p2.PARTY_ID]);
			p3.setShare("a", shares_a[p3.PARTY_ID]);
			
			int[] shares_b = p2.shareSecret("b"	, 6);
			p1.setShare("b", shares_b[p1.PARTY_ID]);
			p2.setShare("b", shares_b[p2.PARTY_ID]);
			p3.setShare("b", shares_b[p3.PARTY_ID]);
			
//			int add1 = p1.addition("a", "b");
//			int add2 = p2.addition("a", "b");
//			int add3 = p3.addition("a", "b");
//			
			int mult1[] = p1.multiplication("a", "b");
			int mult2[] = p2.multiplication("a", "b");
			int mult3[] = p3.multiplication("a", "b");
			
			
			int m1 = p1.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[0], mult2[0], mult3[0]});
			int m2 = p2.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[1], mult2[1], mult3[1]});
			int m3 = p3.reconstructSecret(new int[] {1, 2, 3}, new int[]{mult1[2], mult2[2], mult3[2]});
			System.out.println(p3.reconstructSecret(new int[] {1, 2, 3}, new int[]{m1, m2, m3}));
		} 
		catch (Exception e) {
		}//5*3 1*2 4*1
		
	}
}
