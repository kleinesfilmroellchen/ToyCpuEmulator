package klfr.sa2emu.cpuemulator;

import klfr.sa2emu.cpuemulator.exceptions.CPUException;

/**
 * Ein Register, dass Hilfemethoden für das Rechnen mit zwei weiteren Registern
 * ermöglicht.
 * 
 * @author kleines Filmröllchen
 * @since 1.0
 * @version 1.0
 */
public class ALURegister extends Register {

	public static final int ADDITION = 100;
	public static final int SUBTRACTION = 101;
	public static final int OR = 102;
	public static final int XOR = 103;
	public static final int AND = 104;
	public static final int NOT = 105;
	public static final int BITSHIFTLEFT = 106;
	public static final int BITSHIFTRIGHT = 107;

	/**
	 * Die Operation, die die ALU durchführt.
	 */
	private int operation = ADDITION;

	public ALURegister(boolean b, RegisterBus bus, String string) {
		super(b, bus, string);
	}

	public ALURegister(boolean isReadable, String string) {
		super(isReadable, string);
	}

	/**
	 * Stellt den Operanden ein, den diese ALU für die nächste Berechnung benutzt.
	 */
	public void setOperand(int operand) {
		if (operand != ADDITION && operand != SUBTRACTION && operand != OR && operand != XOR && operand != AND
				&& operand != NOT && operand != BITSHIFTLEFT
				&& operand != BITSHIFTRIGHT) {
			throw new IllegalArgumentException(
					"Argument to setOperand must be an operation constant.");
		}
		this.operation = operand;
	}

	/**
	 * Setzt den Wert des Registers und rechnet dabei mit den beiden Parametern
	 * entsprechend der eingestellten Rechenoperation.
	 * 
	 * @return Der Zustand des Flaggenregisters, der sich aus den durchgeführten
	 *         Operationen und deren Ergebnis ergibt.
	 */
	public byte setValue(Register reg1, Register reg2) {
		byte flags = 0x00;

		switch (operation) {
			case ADDITION:
				if (reg1.getValue() + reg2.getValue() > 0xff)
					flags |= 0b00000010;
				this.val = (byte) (reg1.getValue() + reg2.getValue());
				break;
			case SUBTRACTION:
				this.val = (byte) (reg1.getValue() - reg2.getValue());
				break;
			case OR:
				this.val = (byte) (reg1.getValue() | reg2.getValue());
				break;
			case XOR:
				this.val = (byte) (reg1.getValue() ^ reg2.getValue());
				break;
			case AND:
				this.val = (byte) (reg1.getValue() & reg2.getValue());
				break;
			case NOT:
				this.val = (byte) ~reg1.getValue();
				break;
			case BITSHIFTLEFT:
				this.val = (byte) (reg1.getValue() << 1);
				break;
			case BITSHIFTRIGHT:
				this.val = (byte) (reg1.getValue() >>> 1);
				break;
		}

		if (this.val == 0)
			flags |= 0b00000001;
		if ((this.val & 0x01) == 1)
			flags |= 0b00000100;

		return flags;
	}

	public void clock() throws CPUException {
		super.clock();
		operation = ADDITION;
	}

}
