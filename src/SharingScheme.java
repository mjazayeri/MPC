import java.util.Random;

public class SharingScheme {
	
	public int prime;
	public int degree;
	
	public SharingScheme(int prime, int degree) {
		this.degree = degree;
		this.prime = prime;
		System.out.println("Class initialized:");
		System.out.println("Degree of polynomial: t = " + degree);
		System.out.println("Prime number: p = " + prime);
	}
	
	public int[] generateShares(int secret, int partyCount) {
		
		int[] coefficients = generateCoefficients(secret);
		int[] result = new int[partyCount];
		
		//printPolynomial(coefficients);
		for (int i = 0; i < partyCount; i++) {
			result[i] = calculatePolynomial(coefficients, i+1);
			System.out.println("P[" + (i+1) + "] --> " + result[i] );
		}
		
		return result;
	}
	
	public int moduloPrime(int num) { 
		int r = num % prime;
		r = r < 0 ? r + prime : r;
		return r;
	}
	
	public int[] generateRecombinationVector(int[] set) {
		int[] vector = new int[set.length];

		for (int i = 0; i < set.length; i++) {
			vector[i] = calculatePolynomial(delta(set, set[i]), 0);
		}
		
		return vector;
	}

	public int findSecret(int[] parties, int[] values) { 
		int secret = 0;
		int[] recombinationVector = generateRecombinationVector(parties);
		for (int i = 0; i < values.length; i++) {
			// parties[i] - 1 : turn the index into 0-based format
			secret += values[i] * recombinationVector[i];
		}
		
		return moduloPrime(secret);
	}
	
	public int lagrange(int[] parties, int[] values) {
		
		int secret = 0;
		for (int i = 0; i < parties.length; i++) {
			secret += (values[i] * calculatePolynomial(delta(parties, parties[i]), values[i]));
		}
		return moduloPrime(secret);
	}
	
	private int[] delta(int[] set, int i) {
		int[] result = { 1 };
		for(int j = 0; j < set.length; j++) {
			if( set[j] != i ) {
				int inv = inverse(i-set[j]);
				 int[] newCoefficients = {-1 * set[j] * inv, inv};
				 result = multiplyPolynomials(result, newCoefficients);
			}
		}
		moduloArray(result);
		return result;
	}
	
	private int[] multiplyPolynomials(int[] a, int[] b) { 
		int resultSize = a.length + b.length - 1;
		int[] result = new int[resultSize];
		
		for(int i = 0; i < a.length; i++) {
			for(int j = 0; j < b.length; j++) {
				result[i+j] += a[i] * b[j];
			}
		}
		
		return result;
	}
	
	private int[] generateCoefficients(int secret) {
		int[] coefficients = new int[degree+1];
		Random randomizer = new Random();
		coefficients[0] = secret;
		for (int i = 1; i <= degree; i++) {
			coefficients[i] = randomizer.nextInt(prime); 
		}
		return coefficients;
	}
	
	private int calculatePolynomial(int[] coefficients, int x) {
		int result = 0;
		int xPower = 1;
		for (int i = 0; i < coefficients.length; i++) {
			result += coefficients[i] * xPower;
			xPower *= x;
		}
		
		return moduloPrime(result);
	}
	
	private int inverse(int a) {
		for (int i = 0; i < prime; i++) {
			int r = i * a % prime;
			r = r < 0 ? r + prime : r;
			if( r  == 1)
				return i;
		}
		return 0;
	}
	
	private void moduloArray(int[] input) {
		for (int i = 0; i < input.length; i++) {
			int m = input[i] % prime;
			m = m < 0 ? m + prime : m;
			input[i] = m; 
			
		}
	}
	
	public void printPolynomial(int[] coefficients) {
		String s = "F(X) = [ " + coefficients[0] + " + ";
		for (int i = 1; i < coefficients.length - 1; i++) {
			s += coefficients[i] + "X ^ " + i + " + ";
		}
		int last = coefficients.length - 1;
		s += coefficients[last] + "X ^ " + last + "] MOD " + prime;
		System.out.println(s);
	}
	
	public static void main(String[] args) {
		SharingScheme scheme = new SharingScheme(5, 1);
		int[] x = scheme.generateCoefficients(1);
		int[] y = scheme.generateShares(6, 3);
		
		System.out.println(scheme.findSecret(new int[] { 1, 3}, new int[] {y[0], y[2]}));
		System.out.println(scheme.findSecret(new int[] { 2, 3}, new int[] {y[1], y[2]}));
		System.out.println(scheme.findSecret(new int[] { 1, 2}, new int[] {y[0], y[1]}));
	}
}
