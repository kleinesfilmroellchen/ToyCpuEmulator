package klfr.sa2emu.cpuemulator;

import klfr.sa2emu.cpuemulator.exceptions.*;

/**
 * Ein Bus, auf den mehrere Register schreiben und lesen und somit zum
 * Datenaustausch nutzen. Es darf nur ein Register gleichzeitig schreiben, da
 * sonst im echten Prozessor ein Kurzschluss entstehen könnte. Daher wird in
 * solchen Fällen ein Fehler geworfen
 * 
 * @author kleines Filmröllchen
 * @version 1.0 vom 22.6.2018
 * @since 1.0
 */
public class RegisterBus {

	private byte value = 0x00;
	private boolean hasValue = false;

	public RegisterBus() {
		hasValue = false;
		value = 0x00;
		// System.out.println("Invoked Register bus constructor: " + this.toString());
	}

	/**
	 * Mit dieser Methode kann ein Register von diesem Bus lesen.
	 * 
	 * @throws CPUException falls der Bus noch nicht beschrieben wurde.
	 */
	public byte recieveFrom() throws CPUException {
		if (!hasValue)
			throw new CPUException("Read from unwritten bus.");
		return value;
	}

	/**
	 * Simuliert einen Taktzyklus auf diesem Bus. <b>Achtung: muss als letzter Takt
	 * geschehen!</b>
	 */
	public void clock() {
		// System.out.println("Clocked bus " + this.toString());
		value = 0;
		hasValue = false;
	}

	/**
	 * Mit dieser Methode kann ein Register auf diesen Bus schreiben.
	 * 
	 * @param val Der Wert, den der Bus haben soll.
	 * @throws CPUException falls der Bus bereits beschrieben wird.
	 */
	public void transmitTo(byte val) throws CPUException {
		if (hasValue)
			throw new CPUException("Short circuit / two registers writing to same bus.");
		hasValue = true;
		value = val;
	}

	/**
	 * @return Ob der Bus bereits beschrieben ist oder beschrieben wird.
	 */
	public boolean hasValue() {
		return hasValue;
	}

}
