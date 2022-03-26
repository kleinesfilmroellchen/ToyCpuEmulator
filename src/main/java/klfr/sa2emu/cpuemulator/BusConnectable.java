package klfr.sa2emu.cpuemulator;

import klfr.sa2emu.cpuemulator.exceptions.CPUException;
import klfr.sa2emu.cpuemulator.exceptions.ReadForbiddenException;

/**
 * Interface für alle Objekte, die in der Lage sind, sich mit einem RegisterBus
 * zu verbinden.
 * 
 * @author kleines Filmröllchen
 * @version 1.0
 */
public interface BusConnectable {

	/**
	 * Keine Busaktion.
	 */
	public final static int NONE = 0;
	/**
	 * Empfangende/Lesende Busaktion.
	 */
	public final static int RECIEVE = 1;
	/**
	 * Sendende/Schreibende Busaktion.
	 */
	public final static int TRANSMIT = 2;

	/**
	 * Merkt dieses Objekt für das Senden auf seinen Bus.
	 * 
	 * @throws ReadForbiddenException falls aus diesem Bus nicht gelesen werden
	 *                                kann.
	 */
	void setToTransmit() throws ReadForbiddenException;

	/**
	 * Merkt dieses Objekt für das Empfangen von seinem Bus.
	 */
	void setToRecieve();

	/**
	 * Simuliert einen Taktzyklus und liest oder schreibt von oder zu dem Bus. Ein
	 * Aufruf dieser Methode muss bewirken, dass direkt danach
	 * {@code getBusaction() == NONE} gilt.
	 * 
	 * @throws CPUException bei Fehlern mit der Ausführung der Aktionen.
	 */
	void clock() throws CPUException;

	/**
	 * Gibt zurück, ob dieses Objekt für die Interaktion mit dem Bus vorgemerkt ist.
	 * Äquivalent zu {@code getBusaction() != NONE}.
	 */
	boolean isActing();

	/**
	 * Gibt die momentan gemerkte Busaktion für dieses Objekt zurück.
	 * 
	 * @see BusConnectable#NONE
	 * @see BusConnectable#TRANSMIT
	 * @see BusConnectable#RECIEVE
	 */
	int getBusaction();
}
