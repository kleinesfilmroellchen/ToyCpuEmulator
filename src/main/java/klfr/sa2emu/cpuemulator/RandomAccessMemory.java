package klfr.sa2emu.cpuemulator;

import java.io.PrintStream;

import klfr.sa2emu.cpuemulator.exceptions.CPUException;
import klfr.sa2emu.cpuemulator.exceptions.ReadForbiddenException;

/**
 * Darstellung des Arbeitsspeichers.
 * 
 * @version 1.0 vom 15.06.2018
 * @author kleines Filmröllchen
 */

public class RandomAccessMemory implements BusConnectable {

	// Anfang Attribute
	private byte[] mem;
	public Register AddressPointer;

	private int busaction = NONE;
	private RegisterBus bus;
	// Ende Attribute

	// Anfang Methoden
	/**
	 * Erzeugt einen neuen Arbeitsspeicher mit der angegebenen Kapazität.
	 * 
	 * @param capacity
	 */
	public RandomAccessMemory(int capacity, RegisterBus bus) {
		mem = new byte[capacity];
		AddressPointer = new Register(true, bus, "MemoryAddressPointer");
		this.bus = bus;
	}

	/**
	 * Erzeugt einen neuen Arbeitsspeicher mit einer Kapazität von 256 Bytes.
	 */
	public RandomAccessMemory(RegisterBus bus) {
		this(256, bus);
	}

	/**
	 * Übernimmt für den gesamten Arbeitsspeicher den angegebenen Inhalt. Eine
	 * solche Operation ist im Computer nicht möglich!
	 * 
	 * @param mem Der neue, direkt zugewiesene Arbeitsspeicher.
	 * @throws IllegalArgumentException Falls der neue Speicherinhalt eine andere
	 *                                  Größe aufweist als der aktuelle Speicher.
	 *                                  für Größenänderungen sollte ein
	 *                                  neuer RandomAccessMemory verwendet werden.
	 */
	public void setCompleteMemory(byte[] mem) throws IllegalArgumentException {
		if (mem.length != this.mem.length) {
			throw new IllegalArgumentException(
					"Different-sized new memory content. Make sure the new memory content has the same size (array length).");
		}
		this.mem = mem;
	}

	/**
	 * Liest aus dem Arbeitsspeicher an der Stelle, an die das Adressregister zeigt.
	 * 
	 * @return Das Byte der entsprechenden Stelle.
	 */
	public byte readMemory() {
		return mem[Byte.toUnsignedInt(AddressPointer.getValue())];
	}

	/**
	 * Schreibt in den Arbeitsspeicher an die Stelle, an die das Addressregister
	 * zeigt.
	 */
	public void writeMemory(byte val) {
		mem[(int) AddressPointer.getValue()] = val;
	}

	public void clock() throws CPUException {
		if (busaction == TRANSMIT) {
			System.out.println(
					"Memory@" + this.hashCode() + " at " + Byte.toUnsignedInt(AddressPointer.getValue()) + " transmits.");
			bus.transmitTo(mem[Byte.toUnsignedInt(AddressPointer.getValue())]);
		}
		if (busaction == RECIEVE) {
			System.out.println("Memory@" + this.hashCode() + " at " + AddressPointer.getValue() + " recieves.");
			mem[AddressPointer.getValue()] = bus.recieveFrom();
		}
		busaction = NONE;
	}

	/**
	 * @return Die Größe (Anzahl der Bits) des Speichers.
	 */
	public int size() {
		return mem.length;
	}

	public boolean isActing() {
		return busaction != NONE;
	}

	@Override
	public void setToTransmit() throws ReadForbiddenException {
		busaction = TRANSMIT;
	}

	@Override
	public void setToRecieve() {
		busaction = RECIEVE;
	}

	@Override
	public int getBusaction() {
		return busaction;
	}

	public void printMemory(PrintStream out) {
		int i = 0;
		for (byte b : mem) {
			out.print(SA2_Assembler.stringifyHex(b) + " ");
			if (++i == 8)
				out.println();
			i %= 8;
		}

	}
} // end of RandomAccessMemory
