package klfr.sa2emu.cpuemulator;

import java.util.Arrays;
import java.util.regex.Pattern;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import klfr.sa2emu.cpuemulator.exceptions.CPUException;
import klfr.sa2emu.cpuemulator.exceptions.ReadForbiddenException;

/**
 * Der erste Ausgabebus, der sich nach der entsprechenden Konvention verhält.
 * Dieser Bus enthält einen LCD-Bildschirm zur Anzeige von Text und einen
 * Oled-Schwarzweiß-Bildschirm (theoretisch ersetzbar duch eine LED-Matrix) zur
 * Anzeige von sehr einfachen Grafiken.<br>
 * <br>
 * Der Bildschirm kann per Busbefehl aktiviert werden, indem das dritte Bit von
 * rechts gesetzt wird. Der LCD-Bildschirm wird mit dem zweiten Bit von rechts
 * aktiviert oder deaktiviert (Hintergrundbeleuchtung an/aus).<br>
 * <br>
 * Die Bildschirmzeilen können mit den Adressen 0x10 (erste Zeile) bis 0x1F
 * (letzte Zeile) angesprochen werden. Die Bits repräsentieren dabei die
 * einzelnen Pixel von links nach rechts (MSB = erstes Pixel, LSB = letztes
 * Pixel).<br>
 * Der LCD-Bildschirm kann mit den Adressen 0x20 (erstes Zeichen) bis 0x3F
 * (letztes Zeichen) angesprochen werden. Jede Adresse entspricht somit einem
 * der 32 Zeichen, die auf zwei Zeilen (2x16) verteilt sind. Ein Zeichen hat
 * logischerweise 8 Byte und somit wird der (Pseudo)"ASCII-Raum" des
 * UTF-16-Zeichenraums benutzt. Es kann zu Konvertierungsfehlern bei
 * {@code char -> byte} (und damit Anzeigefehlern) kommen, falls Zeichen
 * verwendet werden, die mehr als 1 Byte belegen. Um zu prüfen, ob ein Zeichen
 * nur 1 Byte belegt, kann es mit der Methode
 * {@code Character.isBpmCodePoint(int)} überprüft werden.
 * 
 * @author kleines Filmröllchen
 * @version 1.0 vom 17.06.2018
 * @see Character#isBmpCodePoint(int)
 */
public class StandardOutputBus implements OutputBus {

	private String lcd = "                \n                ";
	private boolean[][] bwscreen = new boolean[8][8];

	private boolean lcdon = true;
	private boolean bwscreenon = true;

	private int busaction = NONE;

	private RegisterBus bus;

	public StandardOutputBus(RegisterBus bus) {
		this.bus = bus;
	}

	/**
	 * Der Bildschirm kann per Busbefehl aktiviert werden, indem das dritte Bit von
	 * rechts gesetzt wird. Der LCD-Bildschirm wird mit dem zweiten Bit von rechts
	 * aktiviert oder deaktiviert (Hintergrundbeleuchtung an/aus).
	 */
	@Override
	public void processCommand(byte command) {
		// zweites Bit -> LCD an/aus
		lcdon = ((command >> 1) & 0x01) == 1;
		// drittes Bit -> Pixelbildschirm an/aus
		bwscreenon = ((command >> 2) & 0x01) == 1;
	}

	/**
	 * Die Bildschirmzeilen können mit den Adressen 0x10 (erste Zeile) bis 0x1F
	 * (letzte Zeile) angesprochen werden. Die Bits repräsentieren dabei die
	 * einzelnen Pixel von links nach rechts (MSB = erstes Pixel, LSB = letztes
	 * Pixel). Der LCD-Bildschirm kann mit den Adressen 0x20 (erstes Zeichen) bis
	 * 0x3F (letztes Zeichen) angesprochen werden. Jede Adresse entspricht somit
	 * einem der 32 Zeichen, die auf zwei Zeilen (2x16) verteilt sind. Ein Zeichen
	 * hat logischerweise 8 Byte und somit wird der (Pseudo)"ASCII-Raum" des
	 * UTF-16-Zeichenraums benutzt. Es kann zu Konvertierungsfehlern bei
	 * {@code char -> byte} (und damit Anzeigefehlern) kommen, falls Zeichen
	 * verwendet werden, die mehr als 1 Byte belegen. Um zu prüfen, ob ein Zeichen
	 * nur 1 Byte belegt, kann es mit der Methode
	 * {@code Character.isBpmCodePoint(int)} überprüft werden.
	 */
	@Override
	public void processData(byte data) {
		// Adresse für Bildschirm
		if (address.getValue() >= 0x10 && address.getValue() <= 0x1F) {
			// Reiheninformation aus Adresse
			byte rowIndex = (byte) (address.getValue() & 0x0F);
			boolean[] bwrow = bwscreen[rowIndex];
			for (byte i = 0; i < 8; ++i) {
				bwrow[i] = (data >> (8 - i) & 0x01) == 1;
			}
		} else if (address.getValue() <= 0x3F) {
			byte position = address.getValue();
			// Adressraum beginnt bei 0x20, muss aber bei 0x00 starten
			position -= 0x20;
			// für zweite Reihe (0x30-0x3F) muss die Position eins höher sein, da das
			// Zeilenumbruchzeichen übersprungen wird.
			if (address.getValue() >= 0x30)
				position++;

			// Konvertierung notwendig für Modifikation an bestimmter Stelle
			char[] chars = lcd.toCharArray();
			chars[position] = (char) (data & 0xFF);
			// verhindert Zeilenumbrüche, Kontrollzeichen (Backspace, Device Control, Null
			// Character ect.) u.ä.
			if (Character.isWhitespace(chars[position]) || Character.isISOControl(chars[position])
					|| !Character.isValidCodePoint(chars[position])) {
				chars[position] = ' ';
			}
			lcd = String.valueOf(chars);
		}
	}

	@Override
	public void paint(GraphicsContext g) {
		double width = g.getCanvas().getWidth(), height = g.getCanvas().getHeight(), startbw = 0,
				startlcd = height - (height / 3);
		// alles auf dem Panel löschen und Zeichnungseinstellungen
		g.clearRect(0, 0, width, height);
		g.setFont(Font.font("Courier New", height / 20));
		g.setStroke(Color.BLACK);

		// Errechnete Größe eines Pixels
		double pixelSize = startlcd / 8;
		if (bwscreenon) {
			// Bildschirm zeichnen
			for (int i = 0; i < 8; ++i) {
				for (int j = 0; j < 8; ++j) {
					if (bwscreen[i][j]) {
						g.fillRect(j * pixelSize, i * pixelSize + startbw, pixelSize, pixelSize);
					}
				}
			}
		}
		// Rand zeichnen
		g.setStroke(Color.DARKGRAY);
		g.rect(0, startbw, pixelSize * 8, pixelSize * 8);

		// LCD
		if (lcdon) {
			g.setStroke(Color.BLACK);
			g.fillText(lcd, 0, startlcd);
		}

		// Rand zeichnen
		g.setStroke(Color.DARKGRAY);
		g.rect(0, startlcd, pixelSize * 8, pixelSize * 8);
	}

	@Override
	public String consolePaint() {
		String bwscreen = System.lineSeparator() + System.lineSeparator();
		if (this.bwscreenon) {
			for (int i = 0; i < this.bwscreen.length; ++i) {
				for (int j = 0; j < this.bwscreen[0].length; ++j) {
					bwscreen += this.bwscreen[i][j] ? "█" : " ";
				}
				bwscreen += System.lineSeparator();
			}
		}
		return (lcdon ? Pattern.compile(" ").matcher(lcd).replaceAll("_") : "") + bwscreen + System.lineSeparator();
	}

	@Override
	public void reset() {
		address.setValue((byte) 0);
		lcd = "                \n                ";
		for (int i = 0; i < 8; ++i)
			Arrays.fill(bwscreen[i], false);
	}

	@Override
	public void setToTransmit() throws ReadForbiddenException {
		// tut nichts
	}

	@Override
	public void clock() throws CPUException {
		if (busaction == RECIEVE) {
			processData(bus.recieveFrom());
		}

		busaction = NONE;
	}

	@Override
	public boolean isActing() {
		return busaction != NONE;
	}

	@Override
	public int getBusaction() {
		return busaction;
	}

	@Override
	public void setToRecieve() {
		busaction = RECIEVE;
	}

}
