package main;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import passwords.GeneratedPassword;
import passwords.Password;
import passwords.TextPassword;

public class References {

	public static final Console CONSOLE = System.console();
	public static final Cipher AES = getAES();
	public static final MessageDigest SHA256 = getSHA256();
	public static final Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();
	public static final File FILE = new File("C:/Users/Admin/AppData/Roaming/PManage/password.pm");
	public static final String VERSION = "1.0.0";
	
	// Key used to en-/decrypt the password file; created from the hashed master password; 256 bits
	public static SecretKeySpec pmPassword;
	// Passwords mapped to their names
	public static final Map<String, Password> PASSWORDS = new HashMap<>();
	
	public static final String PASSWORDS_DONT_MATCH = "Entered passwords didn't match, try again";
	
	private static Cipher getAES() {
		try {
			return Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			error(e);
		}
		return null;
	}
	
	private static MessageDigest getSHA256() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			error(e);
		}
		return null;
	}
	
	public static void writeToFile() {
		StringBuilder sb = new StringBuilder("abcdefghijklmnopqrstuvxyz1234567890 ").append(References.VERSION).append('\n');
		for (Entry<String, Password> e : PASSWORDS.entrySet()) {
			if (e.getValue() instanceof GeneratedPassword) {
				GeneratedPassword gp = (GeneratedPassword) e.getValue();
				sb.append(e.getKey()).append(' ').append(gp.length).append(' ').append(new BigInteger(gp.seed)).append('\n');
			} else {
				TextPassword tp = (TextPassword) e.getValue();
				sb.append(e.getKey()).append(' ').append(tp.password).append('\n');
			}
		}
		try {
			AES.init(Cipher.ENCRYPT_MODE, pmPassword);
			Files.write(References.FILE.toPath(), AES.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8)));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
			error(e);
		}
	}
	
	public static void error(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String s = "OS: " + System.getProperty("os.name") + '\n' + sw.toString().trim();
		System.out.println(s);
		if (CONSOLE.readLine("The application has encountered an error and will now exit, I am extremely sorry about that!\nIf you want to help improve PManage please send the above error message to me@salvage.ga, I would greatly appreciate it!\nIf you want to copy the error to your clipboard press y and enter, otherwise just press enter\n").trim().toLowerCase().equals("y")) {
			CLIPBOARD.setContents(new StringSelection(s), null);
		}
		System.exit(1);
	}
	
}