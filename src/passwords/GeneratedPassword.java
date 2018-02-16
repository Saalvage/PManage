package passwords;

import java.security.SecureRandom;

public class GeneratedPassword extends Password {

	// Set by the user
	public final int length;
	// Internal seed used to seed the generator generating the end password
	public final byte[] seed;
	
	// Used when generating a new password
	public GeneratedPassword(int length) {
		this.length = length;
		this.seed = new SecureRandom().generateSeed(64);
		generatePassword();
	}
	
	// Used when loading passwords from file
	public GeneratedPassword(int length, byte[] seed) {
		this.length = length;
		this.seed = seed;
		generatePassword();
	}
	
	private void generatePassword() {
		SecureRandom sr = new SecureRandom(seed);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.appendCodePoint(sr.nextInt(1114112));
		}
		password = sb.toString();
	}
	
}