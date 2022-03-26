package klfr.sa2emu.cpuemulator.exceptions;

/**
 * Geworfen, falls ein Assembler einen Fehler in seiner Eingabe beim
 * Kompilieren/Assemblen findet.
 * 
 * @author kleines Filmröllchen
 */
public class AssemblyError extends Exception {

	private static final long serialVersionUID = 1L;

	public AssemblyError() {
	}

	public AssemblyError(String arg0) {
		super(arg0);
	}

	public AssemblyError(Throwable arg0) {
		super(arg0);
	}

	public AssemblyError(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AssemblyError(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
