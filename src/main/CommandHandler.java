package main;

import java.awt.Desktop;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import passwords.GeneratedPassword;
import passwords.TextPassword;

public abstract class CommandHandler {
	
	private static final String NO_PASSWORD_FOUND = "No password by the name of \"%s\" found, type \"list\" for a list of all available passwords\n";
	
	// All user commands go here
	private static final CommandHandler[] C_HANDLERS = {
		new CommandHandler("new", "Creates a new password under <name>, either randomly with the defined <length> or using the preset <password>", "<name> {<length>|<password>}", 2) {
			@Override
			protected void exec(String[] s) {
				if (References.PASSWORDS.containsKey(s[1])) {
					System.out.println("Password \"" + s[1] + "\" already exists, if you are trying to override it please delete it first");
				} else {
					if (isPositiveNumber(s[2])) {
						References.PASSWORDS.put(s[1], new GeneratedPassword(Integer.valueOf(s[2])));
					} else {
						References.PASSWORDS.put(s[1], new TextPassword(s[2]));
					}
					References.writeToFile();
					System.out.println("Successfully created password \"" + s[1] + '"');
				}
			}
		},
		new CommandHandler("get", "Copies password <name> to clipboard", "<name>", 1) {
			@Override
			protected void exec(String[] s) {
				if (References.PASSWORDS.containsKey(s[1])) {
					References.CLIPBOARD.setContents(new StringSelection(References.PASSWORDS.get(s[1]).password), null);
					System.out.println("Copied password to clipboard!");
				} else {
					System.out.printf(NO_PASSWORD_FOUND, s[1]);
				}
			}
		},
		new CommandHandler("rename", "Renames password <name from> to <name to>", "<name from> <name to>", 2) {
			@Override
			protected void exec(String[] s) {
				if (References.PASSWORDS.containsKey(s[1])) {
					References.PASSWORDS.put(s[2], References.PASSWORDS.get(s[1]));
					References.PASSWORDS.remove(s[1]);
				}
			}
		},
		new CommandHandler("delete", "Deletes password <name>", "<name>", 1) {
			@Override
			protected void exec(String[] s) {
				if (References.PASSWORDS.containsKey(s[1])) {
					String response = References.CONSOLE.readLine("Are you sure you want to delete password \"" + s[1] + "\"? (THIS CANNOT BE UNDONE) <y/n>\n").trim().toLowerCase();
					while (!response.equals("y") && !response.equals("n")) {
						response = References.CONSOLE.readLine("<y/n>\n").trim().toLowerCase();
					}
					if (response.equals("y")) {
						References.PASSWORDS.remove(s[1]);
						References.writeToFile();
						System.out.println("Successfully deleted password \"" + s[1] + '"');
					} else {
						System.out.println("Aborted password deletion");
					}
				} else {
					System.out.printf(NO_PASSWORD_FOUND, s[1]);
				}
			}
		},
		new CommandHandler("list", "Lists all available passwords") {
			@Override
			protected void exec(String[] s) {
				if (References.PASSWORDS.isEmpty()) {
					System.out.println("You currently have no passwords");
				} else {
					String output = References.PASSWORDS.keySet().toString();
					System.out.println(output.substring(1, output.length()-1));
				}
			}
		},
		new CommandHandler("change", "Initiates the master password change sequence") {
			@Override
			protected void exec(String[] s) {
				char[] pw = References.CONSOLE.readPassword("Please enter the new master password: ");
				if (Arrays.equals(References.CONSOLE.readPassword("Please confirm your new master password: "), pw)) {
					References.pmPassword = new SecretKeySpec(References.SHA256.digest(new String(pw).getBytes(StandardCharsets.UTF_8)), "AES");
					References.writeToFile();
					System.out.println("Successfully changed master password");
				} else {
					System.out.println(References.PASSWORDS_DONT_MATCH);
				}
			}
		},
		new CommandHandler("open", "Opens the path to the current password file") {
			@Override
			protected void exec(String[] s) {
				try {
					Desktop.getDesktop().open(References.FILE.getParentFile());
				} catch (IOException e) {
					References.error(e);
				}
			}
		},
		new CommandHandler("exit", "Exits the application (alternatively you can just close the window)") {
			@Override
			protected void exec(String[] s) {
				System.exit(0);
			}
		},
		new CommandHandler("help", "Displays all available commands with explanations") {
			@Override
			protected void exec(String[] s) {
				StringBuilder sb = new StringBuilder();
				for (CommandHandler ch : C_HANDLERS) {
					sb.append(ch.phrase).append(' ');
					if (ch.args != null) {
						sb.append(ch.args);
					}
					sb.append('\n').append('\t').append(ch.help).append('\n');
				}
				System.out.println(sb);
			}
		}
	};
	
	public static void handle(String[] s) {
		if (!s[0].equals("")) {
			boolean exists = false;
			for (CommandHandler ch : C_HANDLERS) {
				if (s[0].equals(ch.phrase)) {
					exists = true;
					boolean valid = false;
					if (ch.validArgs.length == 0 && s.length == 1) {
						valid = true;
					} else {
						for (int i : ch.validArgs) {
							if (s.length - 1 == i) {
								valid = true;
								break;
							}
						}
					}
					if (valid) {
						ch.exec(s);
					} else {
						String output = Arrays.toString(ch.validArgs);
						System.out.println("Invalid argument count " + (s.length - 1) + ", required: "+ (output.length() == 2 ? '0' : output.substring(1, output.length() - 1)));
					}
					break;
				}
			}
			if (!exists) {
				System.out.println("Unknown command, type \"help\" for a list of commands");
			}
		}
	}
	
	public static boolean isPositiveNumber(String s) {
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) < '0' || s.charAt(i) > '9') {
				return false;
			}
		}
		return true;
	}
	
	// Phrase under which a command is called
	private final String phrase;
	// Help phrase
	private final String help;
	// All valid arguments
	private final String args;
	// Array of allowed argument lengths
	private final int[] validArgs;
	
	// Used when a command has no arguments
	public CommandHandler(String phrase, String help) {
		this(phrase, help, null);
	}

	// Used when a command has arguments
	public CommandHandler(String phrase, String help, String args, int... validArgs) {
		this.phrase = phrase;
		this.help = help;
		this.args = args;
		this.validArgs = validArgs;
	}
	
	// Executed when a command is called; to be overridden by instances
	protected abstract void exec(String[] s);
	
}
