package klfr.sa2emu.cpuemulator.exceptions;

/**
 * Geworfen, falls der Stackpointer über die höchste mögliche Stelle hinausgeht.
 * 
 * @author malub
 */
public class StackOverflowException extends CPUException {

	private static final long serialVersionUID = 1L;

	public StackOverflowException() {
	}

	public StackOverflowException(String arg0) {
		super(arg0);
	}

	public StackOverflowException(Throwable arg0) {
		super(arg0);
	}

	public StackOverflowException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public StackOverflowException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
