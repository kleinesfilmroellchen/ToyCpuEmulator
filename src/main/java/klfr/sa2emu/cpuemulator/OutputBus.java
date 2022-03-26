package klfr.sa2emu.cpuemulator;

import javafx.scene.canvas.*;

/**
 * Vorlage für einen Ausgabebus. Es ist nicht vorgegeben, wie ein Ausgabebus
 * agiert. Einige Standardimplementationen werden vorgegeben.
 * 
 * @version 1.0 vom 15.06.2018
 * @author kleines Filmröllchen
 */

public interface OutputBus extends BusConnectable {

	public Register address = new Register(true, "Virtuelles Ausgabebusaddressregister");

	// Anfang Methoden
	/**
	 * Verarbeitet einen Befehl an die Ausgabegeräte. Befehle sind dauerhaft
	 * zugänglich und sollten wie Einstellungen genutzt werden.
	 */
	public void processCommand(byte command);

	/**
	 * Setzt den Bus zurück auf seinen "leeren" Ausgangszustand.
	 */
	public void reset();

	/**
	 * Zeichnet die Peripherie/Ausgabegeräte mithilfe des gegebenen
	 * Grafik-Kontextes.
	 * 
	 * @see OutputBus#consolePaint()
	 */
	public void paint(GraphicsContext g);

	/**
	 * Zeichnet "in die Konsole" - gibt einen fertig formatierten Text zurück, der
	 * in einer (neuen Zeile in einer) Konsole angezeigt werden kann und ebenfalls
	 * die Peripheriegeräte darstellt.
	 * 
	 * @see OutputBus#paint(JPanel)
	 */
	public String consolePaint();

	/**
	 * Verarbeitet die Daten in Abhängigkeit von der Adresse.
	 */
	void processData(byte data);
} // end of OutputBus
