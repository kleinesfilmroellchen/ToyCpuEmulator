package klfr.sa2emu.cpuemulator;

/**
 * Ein Ausgaberegister für die CPU. Hauptsächlich Wrapper für
 * Darstellungsmethoden.
 * 
 * @version 1.0 vom 15.06.2018
 * @author kleines Filmröllchen
 */

public class OutputRegister extends Register {

	public OutputRegister(boolean isReadable, String string) {
		super(isReadable, string);
	}

	public OutputRegister(boolean isReadable, RegisterBus bus, String string) {
		super(isReadable, bus, string);
	}

	public OutputRegister(byte val, boolean isReadable, RegisterBus bus, String string) {
		super(val, isReadable, bus, string);
	}

	// Anfang Attribute
	/** Definiert, ob die Anzeige in Dezimalzahlen geschieht. */
	public boolean isDec = true;
	// Ende Attribute

	// Anfang Methoden
	/**
	 * @return Wert des Registers in einem formatierten String als Dezimalzahl. Ob
	 *         die Anzeige dezimal ist oder nicht, spielt hier keine Rolle.
	 */
	public String dec() {
		return Long.toString(val, 10);
	}

	/**
	 * @return Wert des Registers in einem formatierten String als Hexadezimalzahl.
	 *         Ob die Anzeige dezimal ist oder nicht, spielt hier keine Rolle.
	 */
	public String hex() {
		return Long.toString(val, 16);
	}

	/**
	 * Gibt den Wert des Registers in einem formatierten String zurück.
	 * 
	 * @return Formatierte Dezimalzahl, falls {@code isDec == true}, andernfalls
	 *         formatierte Hexadezimalzahl.
	 */
	public String display() {
		return isDec ? dec() : hex();
	}
	// Ende Methoden
} // end of OutputRegister
