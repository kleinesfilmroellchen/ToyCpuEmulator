package klfr.sa2emu.viewers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import klfr.sa2emu.cpuemulator.SA2_Assembler;
import klfr.sa2emu.cpuemulator.exceptions.AssemblyError;

/**
 * Einfacher Assembler, der eine gegebene Datei einliest und diese einem
 * SA2-Assembler übergibt.
 * Dabei werden der fertige Maschinencode sowie evtl. Fehler ausgegeben.
 * 
 * @author kleines Filmröllchen
 *
 */
public class SimpleAssembler {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.print("Bitte geben Sie einen Dateinamen an: ");
		File f = new File(input.next());
		try {
			SA2_Assembler assembler = new SA2_Assembler(f);
			assembler.printMachineCode(System.out);
		} catch (FileNotFoundException e) {
			System.err.println("Datei existiert nicht oder ist ein Verzeichnis.");
		} catch (IOException e) {
			System.err.println("Fehler in Eingabe/Ausgabe.");
		} catch (AssemblyError e) {
			e.printStackTrace();
		}

		input.close();
	}

}
