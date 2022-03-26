package klfr.sa2emu.cpuemulator;

import klfr.sa2emu.cpuemulator.exceptions.*;

/**
 * Definition eines Registers in einer CPU. Ein Register kann 1 Byte (8 Bit)
 * Daten enthalten.
 * 
 * @version 1.0 vom 15.06.2018
 * @author kleines Filmröllchen
 */

public class Register implements BusConnectable {

	protected byte val;
	protected boolean read = false;
	/** Welche Busaktion das Register im nächsten Taktzyklus durchführt. */
	private int busaction;

	private RegisterBus bus;
	private String name;

	/**
	 * Erzeugt ein Register.
	 * 
	 * @param val        Der Anfangswert des Registers.
	 * @param isReadable Ob aus dem Register gelesen werden kann. Falls nicht, wird
	 *                   beim Lesen ein Fehler geworfen.
	 * @param bus        Der Bus, der von diesem Register zum Lesen und Schreiben
	 *                   benutzt
	 *                   wird.
	 * @see ReadForbiddenException
	 */
	public Register(byte val, boolean isReadable, RegisterBus bus, String name) {
		this.bus = bus;
		this.val = val;
		read = isReadable;
		this.name = name;
	}

	/**
	 * Erzeugt ein Register mit einer Anfangswert von 0.
	 * 
	 * @see Register#Register(byte, boolean, RegisterBus, String)
	 */
	public Register(boolean isReadable, RegisterBus bus, String name) {
		this((byte) 0x00, isReadable, bus, name);
	}

	/**
	 * Erzeugt ein Register mit einem "Dummybus".
	 * 
	 * @see Register#Register(boolean, RegisterBus, String)
	 */
	public Register(boolean isReadable, String name) {
		this((byte) 0x00, isReadable, new RegisterBus(), name);
	}

	// Anfang Methoden
	/**
	 * @return Ob aus dem Register gelesen werden kann.
	 */
	public boolean isReadable() {
		return read;
	}

	/**
	 * Gibt den Wert des Registers zurück.
	 */
	public byte getValue() {
		return val;
	}

	/**
	 * Setzt den Wert des Registers auf den Wert des Parameters.
	 */
	public void setValue(byte val) {
		this.val = val;
	}

	/**
	 * Setzt den Wert des Registers auf den Wert des (Register)Parameters.
	 */
	public void setValue(Register val) {
		this.val = val.getValue();
	}

	/**
	 * Merkt sich für das Register einen Schreibvorgang auf den Bus.
	 * 
	 * @throws ReadForbiddenException falls von diesem Register nicht gelesen werden
	 *                                kann.
	 */
	public void setToTransmit() throws ReadForbiddenException {
		if (!read)
			throw new ReadForbiddenException("Read from non-read register!");
		this.busaction = TRANSMIT;
	}

	/**
	 * Merkt sich für das Register einen Lesevorgang vom Bus.
	 */
	public void setToRecieve() {
		this.busaction = RECIEVE;
	}

	/**
	 * Simuliert einen Taktzyklus: Lesen oder Schreiben auf oder vom Bus.
	 * 
	 * @throws CPUException falls der Bus schon beschrieben wird.
	 */
	public void clock() throws CPUException {
		if (busaction == TRANSMIT) {
			System.out.println("Register " + this.toString() + " clocks and transmits.");
			bus.transmitTo(val);
		} else if (busaction == RECIEVE) {
			System.out.println("Register " + this.toString() + " clocks and recieves.");
			val = bus.recieveFrom();
		}
		this.busaction = NONE;
	}

	/** Gibt eine binäre Darstellung des Werts des Registers zurück. */
	public String getBinDisplay() {
		return Integer.toUnsignedString(val, 2);
	}

	public int getBusaction() {
		return this.busaction;
	}

	public boolean isActing() {
		return busaction != NONE;
	}

	public String toString() {
		return this.name + "@" + this.hashCode();
	}
}
