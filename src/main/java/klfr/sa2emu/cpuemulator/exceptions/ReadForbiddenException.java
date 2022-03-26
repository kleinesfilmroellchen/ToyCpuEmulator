package klfr.sa2emu.cpuemulator.exceptions;

/**
 * Geworfen, falls das Lesen aus einem Register verboten ist.
 * 
 * @version 1.0 vom 15.06.2018
 * @author kleines Filmröllchen
 */

public class ReadForbiddenException extends CPUException {

	private static final long serialVersionUID = -4662612065755252509L;

	public ReadForbiddenException(String string) {
		super(string);
	}

	public ReadForbiddenException() {
		super();
	}

	public ReadForbiddenException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ReadForbiddenException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ReadForbiddenException(Throwable arg0) {
		super(arg0);
	}
} // end of ReadForbiddenException