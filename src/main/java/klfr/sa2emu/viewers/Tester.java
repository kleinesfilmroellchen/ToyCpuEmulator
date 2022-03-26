package klfr.sa2emu.viewers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import klfr.sa2emu.cpuemulator.*;
import klfr.sa2emu.cpuemulator.exceptions.AssemblyError;

@SuppressWarnings("unused")
public class Tester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// SA2_Assembler assembler;
		// SA2_CPU cpu = new SA2_CPU();
		//
		// Scanner input = new Scanner(System.in);
		// System.out.print("Bitte geben Sie einen Dateinamen an: ");
		// File f = new File(input.next());
		// try {
		// assembler = new SA2_Assembler(f);
		// } catch (FileNotFoundException e) {
		// System.err.println("Datei existiert nicht oder ist ein Verzeichnis.");
		// } catch (IOException e) {
		// System.err.println("Fehler in Eingabe/Ausgabe.");
		// } catch (AssemblyError e) {
		// e.printStackTrace();
		// }
		//
		//
		//
		// input.close();

	}

	// Testet den Standard-Ausgabebus
	// zwei Zeilen separat
	// String teststring1 = "Zeilenumbrüche sind toll!";
	// String teststring2 = "Überschreiben!!";
	// OutputBus ob = new StandardOutputBus();
	// ob.processCommand((byte) 0x02);
	// int address = 0x20;
	// for (char c : teststring1.toCharArray()) {
	// ob.setAdress((byte) address++);
	// ob.processData((byte) c);
	// }
	// // in zweiter Zeile starten
	// address = 0x30;
	// for (char c : teststring2.toCharArray()) {
	// ob.setAdress((byte) address++);
	// ob.processData((byte) c);
	// }
	// System.out.println(ob.consolePaint());

}
