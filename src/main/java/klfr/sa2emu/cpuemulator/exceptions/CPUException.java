package klfr.sa2emu.cpuemulator.exceptions;

/**
 * Überklasse aller Fehler, die die CPU werfen kann. Werden im Normalfall direkt
 * behandelt. Dient dazu, solche virtuellen Fehler von tatsächlichen
 * Programmfehlern zu unterscheiden.
 * 
 * @author kleines Filmröllchen
 */
public class CPUException extends Exception {

	private static final long serialVersionUID = 1L;

	public CPUException() {
	}

	public CPUException(String arg0) {
		super(arg0);
	}

	public CPUException(Throwable arg0) {
		super(arg0);
	}

	public CPUException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public CPUException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
