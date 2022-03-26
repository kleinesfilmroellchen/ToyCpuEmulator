package klfr.sa2emu.cpuemulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import klfr.sa2emu.cpuemulator.exceptions.AssemblyError;

/**
 * Assembler-Subprogramm, dass SA2-Assembly in SA2-Maschinensprache übersetzt.
 * 
 * @author malub
 */
public class SA2_Assembler {

	/** fertige Maschinensprache */
	private byte[] machineCode;

	/**
	 * Erzeugt einen neuen Assembler und verarbeitet die angegebenen Assemblybefehle
	 * zu Maschinenbefehlen.
	 * 
	 * @param assemblycode SA2-Assembly, die assembled werden soll.
	 * @throws AssemblyError
	 */
	public SA2_Assembler(String assemblycode) throws AssemblyError {
		this(new Scanner(assemblycode));
	}

	/**
	 * Erzeugt einen neuen Assembler und verarbeitet die angegebenen Assemblybefehle
	 * zu Maschinenbefehlen.
	 * 
	 * @param assemblycode Scanner, dessen Eingabe assembled werden soll.
	 */
	public SA2_Assembler(Scanner assemblyscanner) throws AssemblyError {
		assemblyscanner.reset();
		// First pass
		Dictionary<String, Integer> labelDict = new Hashtable<String, Integer>();
		StringBuilder firstPassBuilder = new StringBuilder();
		int linenumber = 0, realline = 0;
		while (assemblyscanner.hasNextLine()) {
			// Neue Zeile einlesen und Zeilennummer erhöhen
			linenumber++;
			realline++;
			Scanner linescanner = new Scanner(assemblyscanner.nextLine());
			// Basis 16
			linescanner.useRadix(10);
			// Zeile überspringen, falls leer
			if (!linescanner.hasNext()) {
				linenumber--;
				continue;
			}

			// Parsen einer Zeile
			String token = linescanner.next().toLowerCase(Locale.ENGLISH);
			// Kommentarzeile: alles danach ignorieren
			if (token.startsWith("/")) {
				linenumber--;
				continue;
			}

			// Label: speichern aber nicht in den second pass string
			if (token.startsWith(":")) {
				// ":" entfernen
				token = token.split(":")[1];
				labelDict.put(token, linenumber);
				try {
					token = linescanner.next();
				} catch (NoSuchElementException e) {
					linescanner.close();
					throw new AssemblyError(
							"Missing Element error in line " + realline + ": expected 'command' after label '" + token + "'.");
				}
				if (token.startsWith("/")) {
					linescanner.close();
					throw new AssemblyError(
							"Missing Element error in line " + realline + ": expected 'command' after label '" + token + "'.");
				}
			}
			// Befehl
			switch (token.toLowerCase(Locale.ENGLISH)) {
				case "load":
					/// Laden von Wert in Register
					firstPassBuilder.append("load ");

					// hole Operanden
					try {
						// für die Operanden sind Leerzeichen egal
						linescanner.useDelimiter("/+\\z*");
						token = linescanner.next().toLowerCase();
						linescanner.useDelimiter("\\p{javaWhitespace}+");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError(
								"Missing Element error in line " + realline + ": expected load origin after 'load' command.");
					}
					token = token.trim();
					token = token.replaceAll("\\s", "");
					// Zwei Teile des Befehls: Ursprung und Ziel
					String[] splitted = token.split(",|\\-\\>");
					// Java soll mich mal ficken -_-
					ArrayList<String> ip = new ArrayList<String>();
					for (String s : splitted) {
						if (!s.isEmpty() && s != null)
							ip.add(s);
					}
					String[] instructionParts = new String[ip.size()];
					for (int i = 0; i < ip.size(); ++i) {
						instructionParts[i] = ip.get(i);
					}
					// ende vom ficken

					if (instructionParts.length != 2) {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline
										+ ": expected exactly two identifiers after 'load' command.");
					}
					// falls der erste Teil des Befehls (Ursprungsaddresse) die korrekte Form hat
					// und eventuell ein Operationsliteral enthält das ausgerechnet wird
					if (instructionParts[0].matches("\\d+([\\+\\-]\\d+)?")) {
						// Direktes Laden des Werts, der beschrieben wird.
						try {
							// Operationsliteral kompilieren
							int value = compileOperationLiteral(instructionParts[0]);
							firstPassBuilder.append(stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else if (instructionParts[0].matches("\\$\\d+([\\+\\-]\\d+)?")) {
						// Laden aus Speicheradresse, die durch Operand beschrieben wird.
						try {
							// Operationsliteral ohne $ komplilieren
							int value = compileOperationLiteral(instructionParts[0].replace("$", ""));
							firstPassBuilder.append("$ " + stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'OP' or '$OP' after 'load' command.");

					} // ende von erstem Operand if
						// Zielregister muss nicht erkannt werden, darum kümmert sich der Second Pass
						// assembler
					firstPassBuilder.append(" " + instructionParts[1].replaceAll("\\s", ""));
					break; // end of load

				case "store":
					// Speichern im RAM
					firstPassBuilder.append("store ");

					// hole Operanden
					try {
						// für die Operanden sind Leerzeichen egal
						linescanner.useDelimiter("/+\\z*");
						token = linescanner.next().toLowerCase();
						linescanner.useDelimiter("\\p{javaWhitespace}+");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected store destination after 'store' command.");
					}

					token = token.trim();
					token = token.replaceAll("\\s", "");
					// Zwei Teile des Befehls: Ursprung und Ziel
					splitted = token.split("[,(\\-\\>)\\>]");
					// Java soll mich mal ficken -_-
					ip = new ArrayList<String>();
					for (String s : splitted) {
						if (!s.isEmpty() && s != null)
							ip.add(s);
					}
					instructionParts = new String[ip.size()];
					for (int i = 0; i < ip.size(); ++i) {
						instructionParts[i] = ip.get(i);
					}
					// ende vom ficken

					if (instructionParts.length != 2) {
						linescanner.close();
						throw new AssemblyError("Missing Element error in line " + realline
								+ ": expected exactly two identifiers after 'store' command.");
					}

					// erster Teil -> Register
					if (instructionParts[0].matches("[ab]")) {
						firstPassBuilder.append(instructionParts[0] + " ");
					} else {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'A' or 'B' after 'store' command.");
					}

					if (instructionParts[1].matches("\\$\\d+([\\+\\-]\\d+)?")) {
						// Speichern in Speicheradresse, die durch Operand beschrieben wird.
						try {
							// Operationsliteral ohne $ komplilieren
							int value = compileOperationLiteral(instructionParts[1].replace("$", ""));
							// $ hier nicht nötig, da immer Speichern in Adresse geschieht (Speichern in
							// Literal macht keinen Sinn)
							firstPassBuilder.append(stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else {
						linescanner.close();
						throw new AssemblyError("Syntax error in line " + realline
								+ ": expected '$OP' after register identifier in 'store' command.");

					} // ende von if
					break; // end of store

				case "add":
				case "sub":
				case "or":
				case "xor":
				case "and":
					// Arithmetisch-logische Operationen
					firstPassBuilder.append(token + " ");

					// hole Operanden
					try {
						// für die Operanden sind Leerzeichen egal
						linescanner.useDelimiter("/+\\z*");
						token = linescanner.next().toLowerCase();
						linescanner.useDelimiter("\\p{javaWhitespace}+");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError("Missing Element error in line " + realline
								+ ": expected declaration of second operand after 'arithmetic or logic operation' command.");
					}
					token = token.trim();
					token = token.replaceAll("\\s", "");
					if (token.equalsIgnoreCase("b") || token.equalsIgnoreCase("x") || token.equalsIgnoreCase("$b")
							|| token.equalsIgnoreCase("$x")) {
						// nur x/b/$y/$b
						firstPassBuilder.append(token);
					} else if (token.matches("\\$\\d+([\\+\\-]\\d+)?")) {
						// Operandenliteral mit Berechnung
						try {
							// Operationsliteral ohne $ komplilieren
							int value = compileOperationLiteral(token.replace("$", ""));
							firstPassBuilder.append("$ " + stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else if (token.matches("\\d+([\\+\\-]\\d+)?")) {
						// Operandenliteral mit Berechnung
						try {
							// Operationsliteral komplilieren
							int value = compileOperationLiteral(token);
							firstPassBuilder.append(stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else {
						linescanner.close();
						throw new AssemblyError("Syntax error in line " + realline
								+ ": expected 'B', 'X', '$OP', '$B' or '$X' after 'arithmetic or logic operation' command.");
					}
					break; // end of arithmetic/logic

				case "not":
				case "bitsr":
				case "bitsl":
				case "inc":
				case "dec":
					// Unäre Operationen (immer mit A ausgeführt) und Haltebefehl
					firstPassBuilder.append(token);
					break; // end of unary/halt

				case "out":
					// Ausgabe
					firstPassBuilder.append(token);
					// hole Ausgabemodi
					try {
						token = linescanner.next().toLowerCase();
					} catch (NoSuchElementException e) {
						// Passiert bei einfachem output
						firstPassBuilder.append(System.lineSeparator());
						continue;
					}

					if (token.matches("cmd|addr|dat")) {
						// gültiger Ausgabebefehl
						firstPassBuilder.append(" " + token);
						try {
							token = linescanner.next().toLowerCase();
							// Operationsliteral ohne $ komplilieren
							if (token.startsWith("$")) {
								int value = compileOperationLiteral(token.replace("$", ""));
								firstPassBuilder.append(" $ " + stringify(value));
							} else {
								int value = compileOperationLiteral(token);
								firstPassBuilder.append(" " + stringify(value));
							}
						} catch (NoSuchElementException e) {
							linescanner.close();
							throw new AssemblyError(
									"Syntax error in line " + realline
											+ ": expected '$OP' after 'output type' in 'out' command.");
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else if (!token.matches("/.*")) {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'output type' after 'out' command.");
					}
					break; // end of out

				case "jmp":
				case "jmpc":
				case "jmpnc":
				case "jmpz":
				case "jmpnz":
				case "jmpp":
				case "jmpnp":
				case "call":
					// Sprung (bedingt oder unbedingt)
					firstPassBuilder.append(token + " ");

					try {
						token = linescanner.next().toLowerCase();
						token.replaceAll("\\s", "");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline
										+ ": expected '$OP' or 'label' in 'jump' or 'call' command.");
					}

					if (token.matches("\\$\\d+([\\+\\-]\\d+)?")) {
						// Sprungadresse
						try {
							// Operationsliteral ohne $ komplilieren
							int value = compileOperationLiteral(token.replace("$", ""));
							firstPassBuilder.append("$ " + stringify(value));
						} catch (AssemblyError e) {
							String[] msg = e.getMessage().split(":");
							msg[0] = msg[0].concat(" in line " + realline + ":").concat(msg[1]);
							linescanner.close();
							throw new AssemblyError(msg[0]);
						}
					} else if (token.matches("\\w+")) {
						// Sprunglabel: wird nach dem Pass behandelt
						firstPassBuilder.append(token);
					} else {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline
										+ ": expected '$OP' or 'label' in 'jump' or 'call' command.");
					}
					break; // end of jump

				case "move":
					// Bewegen von Registern in andere Register
					firstPassBuilder.append(token + " ");

					// hole Registernamen
					try {
						// für die Operanden sind Leerzeichen egal
						linescanner.useDelimiter("/+\\z*");
						token = linescanner.next().toLowerCase();
						linescanner.useDelimiter("\\p{javaWhitespace}+");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError(
								"Missing Element error in line " + realline
										+ ": expected register names after 'move' command.");
					}
					token = token.replaceAll("\\s", "");
					// Zwei Teile des Befehls: Ursprung und Ziel
					splitted = token.split("[,(\\-\\>)\\>]");
					// Java soll mich mal ficken -_-
					ip = new ArrayList<String>();
					for (String s : splitted) {
						if (!s.isEmpty() && s != null)
							ip.add(s);
					}
					instructionParts = new String[ip.size()];
					for (int i = 0; i < ip.size(); ++i) {
						instructionParts[i] = ip.get(i);
					}
					// ende vom ficken

					if (instructionParts.length != 2) {
						linescanner.close();
						throw new AssemblyError("Syntax error in line " + realline
								+ ": expected exactly two register names after 'move' command.");
					}

					if (instructionParts[0].matches("a|b|sp")) {
						firstPassBuilder.append(instructionParts[0] + " ");
					} else {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'A', 'B' or 'SP' after 'move' command.");
					}
					if (instructionParts[1].matches("[abx(sp)]")) {
						firstPassBuilder.append(instructionParts[1]);
					} else {
						linescanner.close();
						throw new AssemblyError("Syntax error in line " + realline
								+ ": expected second 'A', 'X', 'B' or 'SP' after first 'register name' in 'move' command.");
					}

					break; // end of move

				case "push":
				case "pop":
					// Stackoperationen (mit einem Parameter)
					firstPassBuilder.append(token + " ");

					try {
						token = linescanner.next().toLowerCase();
						token.replaceAll("\\s", "");
					} catch (NoSuchElementException e) {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'A' or 'B' in 'push' or 'pop' command.");
					}

					if (token.matches("[abx]")) {
						// Push/Popregister
						firstPassBuilder.append(token);
					} else {
						linescanner.close();
						throw new AssemblyError(
								"Syntax error in line " + realline + ": expected 'A' or 'B' in 'push' or 'pop' command.");
					}
					break; // end of call/return/push/pop

				case "noop":
					break; // end of noop
				case "halt":
				case "swap":
				case "stkrest":
				case "return":
					// Operationen ohne Parameter
					firstPassBuilder.append(token);
					break; // end of halt/swap/stkrest/return

				default:
					linescanner.close();
					throw new AssemblyError(
							"Unknown command exception in line " + realline + ": expected 'command' or 'label' or 'comment'.");
			} // end of command parse

			firstPassBuilder.append(System.lineSeparator());
			linescanner.close();
		} // end of first pass parse

		// System.out.println(firstPassBuilder);

		assemblyscanner.close();

		// second pass
		Scanner firstPassScanner = new Scanner(firstPassBuilder.toString());
		List<Byte> instructions = new ArrayList<>();

		while (firstPassScanner.hasNextLine()) {
			Scanner lineS = new Scanner(firstPassScanner.nextLine());
			if (!lineS.hasNext())
				continue;
			String token = lineS.next();
			// Befehl, Operand
			byte ib = 0x00, ob = 0x00;

			switch (token) {
				case "load":
					ib = 0x10;

					token = lineS.next(); // Ursprung
					if (token.equals("$")) {
						token = lineS.next();
						ob = (byte) integer(token); // Memory-loads sind entweder 0x10 oder 0x11 (vorletztes Bit nicht
																// gesetzt)
					} else {
						ob = (byte) integer(token); // Literal-loads sind entweder 0x12 oder 0x13 (vorletztes Bit gesetzt)
						ib |= 0x02;
					}

					// Zielregister
					token = lineS.next();
					if (token.equals("b")) // die Fälle mit gesetztem letzten Bit sind Ladevorgänge in B
						ib += 1;
					break; // end of load

				case "store":
					ib = 0x20;

					token = lineS.next(); // Ursprungsregister
					if (token.equals("b")) {
						// vergleiche load
						ib += 1;
					}

					token = lineS.next();
					// Zieladdresse
					ob = (byte) integer(token);
					break; // end of store

				case "add":
				case "sub":
				case "and":
				case "or":
				case "xor":

					// Erster Teil des Befehls abhängig von Operation
					switch (token) {
						case "add":
							ib = 0x30;
							break;
						case "sub":
							ib = 0x40;
							break;
						case "or":
							ib = 0x50;
							break;
						case "xor":
							ib = 0x60;
							break;
						case "and":
							ib = 0x70;
							break;
					}

					// Zweiter Teil (und Operand) abhängig von weiteren Argument(en)
					token = lineS.next();
					switch (token) {
						case "b":
							ib |= 0x00;
							break;
						case "x":
							ib |= 0x01;
							break;
						case "$":
							ib |= 0x02;
							ob = (byte) integer(lineS.next());
							break;
						case "$b":
							ib |= 0x03;
							break;
						case "$x":
							ib |= 0x04;
							break;
					}
					break; // end of add

				case "not":
					ib = (byte) 0x81;
					break;
				case "bitsl":
					ib = (byte) 0x82;
					break;
				case "bitsr":
					ib = (byte) 0x83;
					break;
				case "inc":
					ib = (byte) 0x84;
					break;
				case "dec":
					ib = (byte) 0x85;
					break; // end of unary arithmetic

				case "out":
					ib = (byte) 0x90;
					try {
						token = lineS.next();
					} catch (NoSuchElementException e) {
						// erneut den einfach-Ausgabe-Fall abfangen
						break;
					}
					// erweiterte Ein/Ausgabe
					switch (token) {
						case "cmd":
							ib |= 0x01;
							break;
						case "addr":
							ib |= 0x02;
							break;
						case "dat":
							ib |= 0x03;
							break;
					}

					token = lineS.next();
					if (token.equals("$")) {
						// bringt die Werte von 0x91-0x93 auf 0x94-0x96
						ib += 3;
						token = lineS.next();
					}
					ob = (byte) integer(token);
					break; // end of out's

				case "jmp":
				case "jmpc":
				case "jmpnc":
				case "jmpz":
				case "jmpnz":
				case "jmpp":
				case "jmpnp":
				case "call":
					// Sprung oder Unterprogramm
					ib = (byte) 0xA0;

					switch (token) {
						case "jmpc":
							ib |= 0x01;
							break;
						case "jmpnc":
							ib |= 0x02;
							break;
						case "jmpz":
							ib |= 0x03;
							break;
						case "jmpnz":
							ib |= 0x04;
							break;
						case "jmpp":
							ib |= 0x05;
							break;
						case "jmpnp":
							ib |= 0x06;
							break;
						case "call":
							ib |= 0x07;
							break;
					}

					token = lineS.next();
					if (token.matches("\\w+")) {
						// Label: Lookup im Dictionary
						ob = labelDict.get(token).byteValue();
					} else {
						// Speicheradressenzeiger ignorieren
						if (token.equals("$"))
							token = lineS.next();
						ob = (byte) integer(token);
					}
					break; // end of jump's

				case "return":
					ib = (byte) 0xA8;
					break; // end of return
				case "move":
					ib = 0x20;
					token = lineS.next();
					switch (token) {
						case "a":
							ib |= 0x02;
							break;
						case "b":
							ib |= 0x05;
							break;
						case "sp":
							ib |= 0x08;
							break;
					}
					token = lineS.next();

					// Offset durch zweites Argument sorgt für logische Maschinencodes
					switch (token) {
						// a sorgt für keinen Offset
						case "b":
							ib += 1;
							break;
						case "x":
							ib += 2;
							break;
					}
					break; // end of move

				case "swap":
					ib = 0x2B;
					break;
				case "push":
				case "pop":
					ib = (byte) 0xB0;
					if (token.equals("pop"))
						ib += 1;
					token = lineS.next();

					// bei a passiert keine Erhöhung
					if (token.equals("b"))
						ib += 2;
					break; // end of push/pop

				case "stkrest":
					ib = (byte) 0xb4;
					// Somit weiß die CPU, wo der Stack anfängt
					ob = SA2_CPU.STACK_START;
					break;
				case "halt":
					ib = 0x01;
					break;
			} // end of line

			instructions.add(ib);
			instructions.add(ob);
			lineS.close();
		}

		firstPassScanner.close();

		// Speichern des Maschinencodes
		machineCode = new byte[instructions.size()];
		for (int i = 0; i < instructions.size(); ++i) {
			machineCode[i] = instructions.get(i);
		}

	} // end of main constructor

	/**
	 * Erzeugt einen neuen Assembler und verarbeitet die angegebenen Assemblybefehle
	 * zu Maschinenbefehlen.
	 * 
	 * @param assemblycode Datei, deren Inhalt assembled werden soll.
	 * @throws AssemblyError
	 */
	public SA2_Assembler(File assemblyFile) throws FileNotFoundException, IOException, AssemblyError {
		this(new Scanner(assemblyFile));
	}

	/**
	 * @return Die Maschinenbefehle, die dieser Assembler verarbeitet hat.
	 */
	public byte[] getMachineCode() {
		return machineCode;
	}

	/**
	 * Die Anzahl der Bytes, die die Maschinenbefehle dieses Assemblers einnehmen.
	 */
	public int machineCodeLength() {
		return machineCode.length;
	}

	/**
	 * Druckt den Maschinencode schön formatiert, zweizeilig und hexadezimal.
	 * 
	 * @param out Der Ausgabestrom, in den gedruckt wird.
	 */
	public void printMachineCode(PrintStream out) {
		byte end = 0;
		for (byte b : machineCode) {
			end++;
			out.print(stringifyHex(b));
			if (end > 1)
				out.println();
			else
				out.print(" ");
			end %= 2;
		}
	}

	/**
	 * Erzeugt eine Zahl aus einem dezimalen String; Leerzeichen werden ignoriert.
	 */
	private static int integer(String s) throws NumberFormatException {
		s = s.replaceAll("\\s", "");
		return Integer.parseInt(s);
	}

	/**
	 * Erzeugt einen dezimalen String aus einer Zahl.
	 */
	private static String stringify(int i) {
		return Integer.toString(i);
	}

	/**
	 * Erzeugt einen hexadezimalen String aus einem Byte. Vorzeichen sind egal.
	 */
	public static String stringifyHex(byte b) {
		String sb = Integer.toUnsignedString(b, 16);
		if (sb.length() > 2)
			sb = sb.substring(sb.length() - 2, sb.length());
		return sb;
	}

	/**
	 * Kompiliert ein Operationsliteral (Addition oder Subtraktion von zwei
	 * Operanden) und gibt den Integerwert zurück.
	 */
	private static int compileOperationLiteral(String operation) throws AssemblyError {
		operation = operation.trim();
		operation = operation.replaceAll("\\s", "");
		// Aufteilen in die beiden Operanden der Addition/Subtraktion
		String[] operands = operation.split("[\\+-]");
		// Die Operation, welche ausgeführt werden soll
		String operationType = operation.indexOf("+") > -1 ? "+" : (operation.indexOf("-") > -1 ? "-" : "+");

		if (operands.length == 1) {
			// einfaches übernehmen des Operanden
			return integer(operands[0]);
		} else if (operands.length == 2) {
			// Addition oder Subtraktion
			int val = integer(operands[0]);
			if (operationType.equalsIgnoreCase("+")) {
				val += integer(operands[1]);
			} else if (operationType.equalsIgnoreCase("-")) {
				val -= integer(operands[1]);
			} else {
				// sollte nicht passieren (und dann ist der Benutzer schuld)
				throw new AssemblyError("Unknown error: inline operation not '+' or '-'.");
			}
			return val;
		} else {
			throw new AssemblyError("Literal Operation error: inline operation with more than two operands.");
		}

	}
}
