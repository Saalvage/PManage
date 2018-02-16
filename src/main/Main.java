package main;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import passwords.GeneratedPassword;
import passwords.TextPassword;

public class Main {
	
	private static final String INVALID_PASSWORD = "Invalid password!";
	
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				References.error(e);
			}
		});
		System.out.println("Weclome to PManage!");
		if (References.FILE.exists()) {
			// Logging in
			boolean correct = false;
			while (!correct) {
				try {
					References.pmPassword = new SecretKeySpec(References.SHA256.digest(new String(References.CONSOLE.readPassword("Please enter your master password: ")).getBytes(StandardCharsets.UTF_8)), "AES");
					References.AES.init(Cipher.DECRYPT_MODE, References.pmPassword);
					String[] s = new String(References.AES.doFinal(Files.readAllBytes(References.FILE.toPath())), StandardCharsets.UTF_8).split("\n");
					// 1d: lines
					// 2d: between spaces
					String[][] strings = new String[s.length][];
					for (int i = 0; i < s.length; i++) {
						strings[i] = s[i].split(" ");
					}
					if (strings[0][0].equals("abcdefghijklmnopqrstuvxyz1234567890")) {
						System.out.println("Welcome back!");
						correct = true;
						int[] fileVersion = stringArrToIntArr(strings[0][1].split("\\."));
						int[] appVersion = stringArrToIntArr(References.VERSION.split("\\."));
						if (fileVersion[0] > appVersion[0] || fileVersion[0] == appVersion[0] && fileVersion[1] > appVersion[1] || fileVersion[0] == appVersion[0] && fileVersion[1] == appVersion[1] && fileVersion[2] > appVersion[2]) {
							References.CONSOLE.readLine("The save file was saved using a newer version of PManage, to avoid potential data corruption or errors please update to the latest version of PManage");
							System.exit(0);
						}
						for (int i = 1; i < strings.length; i++) {
							if (strings[i].length == 3) {
								References.PASSWORDS.put(strings[i][0], new GeneratedPassword(Integer.valueOf(strings[i][1]), new BigInteger(strings[i][2]).toByteArray()));
							} else {
								References.PASSWORDS.put(strings[i][0], new TextPassword(strings[i][1]));
							}
						}
					} else {
						System.out.println(INVALID_PASSWORD);
					}
				} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
					System.out.println(INVALID_PASSWORD);
				}
			}
		} else {
			// First time setup
			References.FILE.getParentFile().mkdir();
			char[] pw = References.CONSOLE.readPassword("To get started please type in a master password! (note it down!): ");
			boolean correct = false;
			boolean firstTime = true;
			while (!correct) {
				if (!firstTime) {
					pw = References.CONSOLE.readPassword("Please type in a master password: ");
				}
				firstTime = false;
				if (Arrays.equals(References.CONSOLE.readPassword("Please confirm your input: "), pw)) {
					correct = true;
					References.pmPassword = new SecretKeySpec(References.SHA256.digest(new String(pw).getBytes(StandardCharsets.UTF_8)), "AES");
					References.writeToFile();
					System.out.println("You successfully set your master password! Type \"help\" to get a list of available commands");
				} else {
					System.out.println(References.PASSWORDS_DONT_MATCH);
				}
			}
		}
		// Start listening for commands
		while (true) {
			CommandHandler.handle(References.CONSOLE.readLine("> ").trim().split(" "));
		}
	}
	
	private static int[] stringArrToIntArr(String[] s) {
		int[] i = new int[s.length];
		for (int j = 0; j < s.length; j++) {
			i[j] = Integer.parseInt(s[j]);
		}
		return i;
	}

}