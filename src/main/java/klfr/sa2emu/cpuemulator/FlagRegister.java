package klfr.sa2emu.cpuemulator;

/**
 * Ein Flaggenregister. Flaggenregister enthalten Informationen über den Zustand
 * des Computers oder des letzten Befehls. Basierend auf den im Flaggenregister
 * enthaltenen Flaggen werden dann verschiedene Entscheidungen getroffen.<br>
 * Diese Klasse bietet Funktionalität, um schnell auf einzelne Flaggen
 * zuzugreifen.
 * 
 * @author kleines Filmröllchen
 * @version 1.0 vom 17.06.2018
 */
public class FlagRegister extends Register {

	public FlagRegister(boolean isReadable, RegisterBus bus, String string) {
		super(isReadable, bus, string);
	}

	public FlagRegister(boolean isReadable) {
		super(isReadable, null);
	}

	public FlagRegister(byte val, boolean isReadable, RegisterBus bus) {
		super(val, isReadable, bus, null);
	}

	/**
	 * @return Ob die Nullflagge gesetzt ist. Dies passiert, falls bei der letzten
	 *         arithmetischen Operation Null als Ergebnis herauskam.
	 */
	public boolean zeroFlagSet() {
		return (val & 0x01) == 1;
	}

	/**
	 * @return Ob die Carryflagge gesetzt ist. Dies passiert, falls bei der letzten
	 *         arithmetischen Operation ein Überlauf des Ergebnisregisters erfolgte.
	 */
	public boolean carryFlagSet() {
		return (val >> 1 & 0x01) == 1;
	}

	/**
	 * @return Ob die Parityflagge gesetzt ist. Dies passiert, falls bei der letzten
	 *         arithmetischen Operation das Ergebnis ungerade war (letztes Bit = 1).
	 */
	public boolean parityFlagSet() {
		return (val >> 2 & 0x01) == 1;
	}

	/**
	 * @return Ob die Nullflagge im gegebenen Byte gesetzt ist. Dies passiert, falls
	 *         bei der letzten arithmetischen Operation Null als Ergebnis herauskam.
	 */
	public static boolean zeroFlagSet(byte val) {
		return (val & 0x01) == 1;
	}

	/**
	 * @return Ob die Carryflagge im gegebenen Byte gesetzt ist. Dies passiert,
	 *         falls bei der letzten arithmetischen Operation ein Überlauf des
	 *         Ergebnisregisters erfolgte.
	 */
	public static boolean carryFlagSet(byte val) {
		return (val >> 1 & 0x01) == 1;
	}

	/**
	 * @return Ob die Parityflagge im gegebenen Byte gesetzt ist. Dies passiert,
	 *         falls bei der letzten arithmetischen Operation das Ergebnis ungerade
	 *         war
	 *         (letztes Bit = 1).
	 */
	public static boolean parityFlagSet(byte val) {
		return (val >> 2 & 0x01) == 1;
	}

}
