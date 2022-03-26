package klfr.sa2emu.cpuemulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import klfr.sa2emu.cpuemulator.exceptions.*;

/**
 * Simulation einer CPU nach der Simple Advanced 2 Architektur.
 * 
 * @author kleines Filmröllchen
 * @version 1.0
 * @since 1.0
 */
public class SA2_CPU {

	/**
	 * Koordinator der Buskommunikation; übernimmt die damit zusammenhängenden
	 * Aufgaben.
	 * 
	 * @author kleines Filmröllchen
	 */
	private class TransmissionCoordinator {
		private List<BusConnectable> transmitters;
		private List<BusConnectable> recievers;

		public TransmissionCoordinator() {
			transmitters = new ArrayList<>();
			recievers = new ArrayList<>();
		}

		/**
		 * Speichert das Register als auf den Bus übertragendes Register.
		 * 
		 * @throws ReadForbiddenException falls das Register nicht übertragen kann / aus
		 *                                ihm nicht gelesen werden kann.
		 */
		public void noteForTransmission(BusConnectable r) throws ReadForbiddenException {
			r.setToTransmit();
			transmitters.add(r);
		}

		/**
		 * Speichert das Register als vom Bus empfangendes BusConnectable.
		 */
		public void noteForReception(BusConnectable r) {
			r.setToRecieve();
			recievers.add(r);
		}

		/**
		 * Führt Lesen und Schreiben der Register aus.
		 * 
		 * @throws CPUException falls der Bus schon beschrieben wird.
		 */
		public void execute() throws CPUException {
			for (BusConnectable r : transmitters) {
				// System.out.println(r + " is a transmitter");
				r.clock();
			}
			for (BusConnectable r : recievers) {
				// System.out.println(r + " is a reciever");
				r.clock();
			}
			recievers.clear();
			transmitters.clear();
		}

		/**
		 * @return Ob gespeichert ist, ob der Bus benutzt wird.
		 */
		public boolean busIsUsed() {
			return !(recievers.isEmpty() && transmitters.isEmpty());
		}
	}

	//// Steuerlinien

	/** Befehlsregister aus */
	public static final long IO = 0b1000000000000000000000000000000000000000000000000000000000000000L;
	/** Befehlsregister ein */
	public static final long II = 0b0100000000000000000000000000000000000000000000000000000000000000L;
	/** Programmzähler aus */
	public static final long CO = 0b0010000000000000000000000000000000000000000000000000000000000000L;
	/** Programmzähler aktivieren/inkrementieren */
	public static final long CE = 0b0001000000000000000000000000000000000000000000000000000000000000L;
	/** Programmzähler ein/Sprung */
	public static final long CI = 0b0000100000000000000000000000000000000000000000000000000000000000L;
	/** Operandenregister aus */
	public static final long OPO = 0b0000010000000000000000000000000000000000000000000000000000000000L;
	/** Operandenregister ein */
	public static final long OPI = 0b0000001000000000000000000000000000000000000000000000000000000000L;
	/** Register A aus */
	public static final long AO = 0b0000000100000000000000000000000000000000000000000000000000000000L;
	/** Register A ein */
	public static final long AI = 0b0000000010000000000000000000000000000000000000000000000000000000L;
	/** Register B aus */
	public static final long BO = 0b0000000001000000000000000000000000000000000000000000000000000000L;
	/** Register B ein */
	public static final long BI = 0b0000000000100000000000000000000000000000000000000000000000000000L;
	/** Register X aus */
	public static final long XO = 0b0000000000010000000000000000000000000000000000000000000000000000L;
	/** Register X ein */
	public static final long XI = 0b0000000000001000000000000000000000000000000000000000000000000000L;
	/** Konstante "0x01" nach X schreiben */
	public static final long X1 = 0b0000000000000100000000000000000000000000000000000000000000000000L;
	/** Stackzeiger aus */
	public static final long SO = 0b0000000000000010000000000000000000000000000000000000000000000000L;
	/** Stackzeiger ein */
	public static final long SI = 0b0000000000000001000000000000000000000000000000000000000000000000L;
	/** Stackzeiger inkrementieren */
	public static final long SP1 = 0b0000000000000000100000000000000000000000000000000000000000000000L;
	/** Stackzeiger dekrementieren */
	public static final long SM1 = 0b0000000000000000010000000000000000000000000000000000000000000000L;
	/** ALU-Modus subtrahieren */
	public static final long SUB = 0b0000000000000000001000000000000000000000000000000000000000000000L;
	/** ALU-Modus logisches bitweises Oder */
	public static final long OR = 0b0000000000000000000100000000000000000000000000000000000000000000L;
	/** ALU-Modus logisches bitweises Exklusiv-Oder */
	public static final long XOR = 0b0000000000000000000010000000000000000000000000000000000000000000L;
	/** ALU-Modus logisches bitweises Und */
	public static final long AND = 0b0000000000000000000001000000000000000000000000000000000000000000L;
	/** ALU-Modus logisches bitweises Nicht */
	public static final long NOT = 0b0000000000000000000000100000000000000000000000000000000000000000L;
	/** ALU-Modus Bitshift nach rechts */
	public static final long BSR = 0b0000000000000000000000010000000000000000000000000000000000000000L;
	/** ALU-Modus Bitshift nach links */
	public static final long BSL = 0b0000000000000000000000001000000000000000000000000000000000000000L;
	/** ALU-Register aus und Flaggenregister ein */
	public static final long EO = 0b0000000000000000000000000100000000000000000000000000000000000000L;
	/** Ausgabebefehl ein */
	public static final long OCI = 0b0000000000000000000000000010000000000000000000000000000000000000L;
	/** Ausgabebusadresse ein */
	public static final long OAI = 0b0000000000000000000000000001000000000000000000000000000000000000L;
	/** Ausgabebusdaten ein */
	public static final long ODI = 0b0000000000000000000000000000100000000000000000000000000000000000L;
	/** Ausgaberegister (einfache Zahlenanzeige) ein */
	public static final long OI = 0b0000000000000000000000000000010000000000000000000000000000000000L;
	/** Speicheradressregister ein */
	public static final long MAI = 0b0000000000000000000000000000001000000000000000000000000000000000L;
	/** Speicher aus */
	public static final long MO = 0b0000000000000000000000000000000100000000000000000000000000000000L;
	/** Speicher ein */
	public static final long MI = 0b0000000000000000000000000000000001000000000000000000000000000000L;
	/** CPU anhalten (Halt) */
	public static final long HLT = 0b0000000000000000000000000000000000100000000000000000000000000000L;
	/** Mikrobefehlszähler zurücksetzen */
	public static final long MSR = 0b0000000000000000000000000000000000010000000000000000000000000000L;

	/**
	 * Zeigt an, ob die CPU momentan angehalten wird.
	 */
	public boolean halted = false;

	/**
	 * Startposition des Stackzählers; der Stack ist ein Empty Stack (Stackzeiger
	 * zeigt auf erste freie Stelle im Stack) und descending (Befüllen des Stacks
	 * hat eine Verringerung des Stackzeigers zur Folge).
	 */
	static final byte STACK_START = (byte) 0xFF;
	/** Maximale Größe des Stacks */
	static final byte STACK_SIZE = 16;

	public RegisterBus bus = new RegisterBus();
	public Register A = new Register(true, bus, "A");
	public Register B = new Register(true, bus, "B");
	public Register X = new Register(true, bus, "X");
	public Register SP = new Register(true, bus, "SP");
	public Register IP = new Register(false, bus, "IP");
	public Register OP = new Register(true, bus, "OP");
	public Register PC = new Register(true, bus, "PC");
	public ALURegister ALU = new ALURegister(true, bus, "ALU");
	public FlagRegister FR = new FlagRegister(false, bus, "FR");
	public OutputRegister OUT = new OutputRegister(false, bus, "OUT");
	public OutputBus OB;
	public RandomAccessMemory RAM = new RandomAccessMemory(bus);

	/** Der Zähler der Mikrobefehle, kann zwischen 0 und 7 liegen. */
	private byte microInstructionCounter = 0x00;
	/** Die momentanen Mikrobefehle, die die CPU abarbeitet. */
	private List<Long> currentMicroInstructions;
	/** Der momentan verarbeitete Mikrobefehl */
	private Long curMicroInstruction;

	private TransmissionCoordinator busController = new TransmissionCoordinator();
	/**
	 * Der letzte gespeicherte Zustand des Bus. Kann für Anzeige und Debugging
	 * genutzt werden.
	 */
	private byte lastBusVal;

	/**
	 * Erzeugt eine neue CPU-Simulation und benutzt einen Standard-Ausgabebus.
	 */
	public SA2_CPU() {
		this.OB = new StandardOutputBus(bus);
		reset();
	}

	/**
	 * Erzeugt eine neue CPU-Simulation.
	 * 
	 * @param ob Der Ausgabebus, den diese Simulation verwendet.
	 */
	public SA2_CPU(OutputBus ob) {
		this.OB = ob;
		reset();
	}

	/**
	 * Setzt die gesamte CPU zurück (einschließlich Arbeitsspeicher).
	 */
	@SuppressWarnings("static-access")
	public void reset() {
		final byte zero = 0;
		microInstructionCounter = zero;

		// Register
		A.setValue(zero);
		B.setValue(zero);
		X.setValue(zero);
		SP.setValue(STACK_START);
		IP.setValue(zero);
		OP.setValue(zero);
		PC.setValue(zero);
		FR.setValue(zero);
		OUT.setValue(zero);

		// Arbeitsspeicher
		byte[] newram = new byte[256];
		Arrays.fill(newram, zero);
		RAM.setCompleteMemory(newram);
		RAM.AddressPointer.setValue(zero);

		// Ausgabebus
		OB.reset();
		OB.address.setValue(zero);
		OB.processCommand(zero);

		// Kein Halt
		halted = false;
		curMicroInstruction = 0l;
	}

	/**
	 * Setzt den Speicher ab einer bestimmten Adresse mit den gegebenen Bytes. Tut
	 * nichts, falls das Array über das Ende des Speichers hinausgehen würde.
	 * 
	 * @param mem      Die Bytes, die zur Vorlage für den Speicher dienen.
	 * @param startLoc Die Startposition, ab der der Speicher beschrieben wird.
	 */
	public void setMemory(byte[] mem, byte startLoc) {
		if (mem.length + startLoc > RAM.size())
			return;

		for (byte i = 0; i < mem.length; ++i) {
			RAM.AddressPointer.setValue((byte) (i + startLoc));
			RAM.writeMemory(mem[i]);
		}
	}

	/**
	 * Schreibt ein Byte an der gegebenen Stelle.
	 * 
	 * @param mem Das zu schreibende Byte.
	 * @param loc Die Stelle im Speicher, an der geschrieben werden soll. Falls
	 *            dieser Wert zu hoch ist, wird nichts getan.
	 */
	public void setMemory(byte mem, byte loc) {
		if (loc >= RAM.size())
			return;
		RAM.AddressPointer.setValue(loc);
		RAM.writeMemory(mem);
	}

	/**
	 * Setzt den gesamten Speicher auf die gegebenen Werte. Tut nichts, falls der
	 * Speicher eine andere Größe als das gegebene Array hat.
	 * 
	 * @param mem
	 */
	public void setMemory(byte[] mem) {
		try {
			RAM.setCompleteMemory(mem);
		} catch (IllegalArgumentException e) {
			return;
		}
	}

	/**
	 * Stellt die Kontrolllinien ein und verarbeitet somit den nächsten Mikrobefehl.
	 */
	@SuppressWarnings("static-access")
	public void setControlLines() throws StackOverflowException, CPUException {
		System.out.println("Invoked SA2_CPU.setControlLines() with current mic = " + microInstructionCounter);
		if (halted)
			return;

		Long instructionToDo = 0l;
		if (microInstructionCounter < 4) {
			// Der Befehl ist noch nicht bekannt: Von-Neumann-Zyklus ausführen
			System.out.println("Executing Von-Neumann-Cycle at " + microInstructionCounter);
			instructionToDo = getVonNeumannCycle().get(microInstructionCounter);
		} else {
			// Beim ersten normalen Zyklus werden die neuen Mikrobefehle eingelesen
			if (microInstructionCounter == 4) {
				currentMicroInstructions = decodeInstruction(IP.getValue(), FR.getValue());
			}

			for (Long l : currentMicroInstructions) {
				System.out.printf("%64s%n", Long.toBinaryString(l));
			}
			instructionToDo = currentMicroInstructions.get(microInstructionCounter - 4);
		}

		// falls haltline, wird sofort aufgehört
		if ((instructionToDo & HLT) > 0) {
			halted = true;
			return;
		}

		//// Steuerlinien mit Merker oder unabhängigem Wert setzen
		// Eingabe/Ausgabe von Registern und sonstiges
		if ((instructionToDo & II) > 0)
			busController.noteForReception(IP);
		if ((instructionToDo & IO) > 0)
			busController.noteForTransmission(IP);
		if ((instructionToDo & CO) > 0)
			busController.noteForTransmission(PC);
		if ((instructionToDo & CI) > 0)
			busController.noteForReception(PC);
		if ((instructionToDo & CE) > 0)
			PC.setValue((byte) (PC.getValue() + 1));
		if ((instructionToDo & AO) > 0)
			busController.noteForTransmission(A);
		if ((instructionToDo & AI) > 0)
			busController.noteForReception(A);
		if ((instructionToDo & BO) > 0)
			busController.noteForTransmission(B);
		if ((instructionToDo & BI) > 0)
			busController.noteForReception(B);
		if ((instructionToDo & XO) > 0)
			busController.noteForTransmission(X);
		if ((instructionToDo & XI) > 0)
			busController.noteForReception(X);
		if ((instructionToDo & X1) > 0)
			X.setValue((byte) 0x01);
		if ((instructionToDo & OPO) > 0)
			busController.noteForTransmission(OP);
		if ((instructionToDo & OPI) > 0)
			busController.noteForReception(OP);
		if ((instructionToDo & SO) > 0)
			busController.noteForTransmission(SP);
		if ((instructionToDo & SI) > 0)
			busController.noteForReception(SP);
		if ((instructionToDo & SP1) > 0)
			SP.setValue((byte) (SP.getValue() + 1));
		if ((instructionToDo & SM1) > 0)
			SP.setValue((byte) (SP.getValue() - 1));

		/*
		 * if (Byte.toUnsignedInt(SP.getValue()) > STACK_START + STACK_SIZE) throw new
		 * StackOverflowException( "Stack got too large: " + SP.getValue() +
		 * " vs. max. " + (STACK_START + STACK_SIZE));
		 */

		// ALU-Register setzen
		if ((instructionToDo & SUB) > 0)
			ALU.setOperand(ALURegister.SUBTRACTION);
		else if ((instructionToDo & OR) > 0)
			ALU.setOperand(ALURegister.OR);
		else if ((instructionToDo & XOR) > 0)
			ALU.setOperand(ALURegister.XOR);
		else if ((instructionToDo & AND) > 0)
			ALU.setOperand(ALURegister.AND);
		else if ((instructionToDo & BSL) > 0)
			ALU.setOperand(ALURegister.BITSHIFTLEFT);
		else if ((instructionToDo & BSR) > 0)
			ALU.setOperand(ALURegister.BITSHIFTRIGHT);
		else
			ALU.setOperand(ALURegister.ADDITION);
		// ALU-Ausgabe
		if ((instructionToDo & EO) > 0)
			busController.noteForTransmission(ALU);
		// Normales Ausgaberegister
		if ((instructionToDo & OI) > 0)
			busController.noteForReception(OUT);
		// Speicherzugriff mit evtl. direktem Buszugriff
		if ((instructionToDo & MAI) > 0)
			busController.noteForReception(RAM.AddressPointer);
		if ((instructionToDo & MO) > 0)
			busController.noteForTransmission(RAM);
		if ((instructionToDo & MI) > 0)
			busController.noteForReception(RAM);

		// Ausgabebus
		if ((instructionToDo & OAI) > 0)
			busController.noteForReception(OB.address);
		if ((instructionToDo & ODI) > 0)
			busController.noteForReception(OB);

		//// ALU-Berechnung ausführen und Flaggenregister setzen
		byte flags = ALU.setValue(A, X);
		// nur setzen falls ALU verwendet wird
		if ((instructionToDo & EO) > 0)
			FR.setValue(flags);

		curMicroInstruction = instructionToDo;
	}

	/**
	 * Simuliert einen Taktzyklus; davor müssen die Steuerlinien richtig gesetzt
	 * sein.
	 */
	public void clock() throws StackOverflowException, CPUException {
		long instructionToDo = curMicroInstruction;

		//// Buskommunikation ausführen
		busController.execute();
		try {
			lastBusVal = bus.recieveFrom();
		} catch (CPUException e) {
			lastBusVal = 0;
		}

		// Ausgabebusbefehl
		if ((instructionToDo & OCI) > 0)
			OB.processCommand(bus.recieveFrom());

		//// Übrige Register und Busse clocken
		ALU.clock();
		FR.clock();
		bus.clock();

		++microInstructionCounter;
		microInstructionCounter %= 8;

		if ((instructionToDo & MSR) > 0)
			microInstructionCounter = 0;
	}

	/**
	 * Dekodiert einen Maschinenbefehl in eine Liste von maximal acht
	 * Mikrobefehlen.<br>
	 * Natürlich beachtet diese Methode die gesetzten Flaggen im Flaggenbyte.
	 */
	public static List<Long> decodeInstruction(byte instruction, byte flags) {
		List<Long> microinstructions = new ArrayList<Long>();
		switch (instruction) {
			case 0x01:
				// halt
				microinstructions.add(HLT);
				break;

			case 0x10:
				// lade aus dem Speicher in a
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | AI);
				break;
			case 0x11:
				// lade aus dem Speicher in b
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | BI);
				break;
			case 0x12:
				// lade direkt in a
				microinstructions.add(OPO | AI);
				break;
			case 0x13:
				// lade direkt in b
				microinstructions.add(OPO | BI);
				break;

			case 0x20:
				// speichere a im Hauptspeicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(AO | MI);
				break;
			case 0x21:
				// speichere b im Hauptspeicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(BO | MI);
				break;

			case 0x30:
				// addiere b
				microinstructions.add(BO | XI);
				microinstructions.add(EO | AI);
				break;
			case 0x31:
				// addiere x
				microinstructions.add(EO | AI);
				break;
			case 0x32:
				// addieren aus Speicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(EO | AI);
				break;
			case 0x33:
				// addieren aus Speicher mit b als Speicheradresse
				microinstructions.add(BO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(EO | AI);
				break;
			case 0x34:
				// addieren aus Speicher mit x als Speicheradresse
				microinstructions.add(XO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(SUB | EO | AI);
				break;

			case 0x40:
				// subtrahiere b
				microinstructions.add(BO | XI);
				microinstructions.add(SUB | EO | AI);
				break;
			case 0x41:
				// subtrahiere x
				microinstructions.add(SUB | EO | AI);
				break;
			case 0x42:
				// subtrahieren aus Speicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(SUB | EO | AI);
				break;
			case 0x43:
				// subtrahieren aus Speicher mit b als Speicheradresse
				microinstructions.add(BO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(SUB | EO | AI);
				break;
			case 0x44:
				// subtrahieren aus Speicher mit x als Speicheradresse
				microinstructions.add(XO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(SUB | EO | AI);
				break;

			case 0x50:
				// oder b
				microinstructions.add(BO | XI);
				microinstructions.add(OR | EO | AI);
				break;
			case 0x51:
				// oder x
				microinstructions.add(OR | EO | AI);
				break;
			case 0x52:
				// odern aus Speicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(OR | EO | AI);
				break;
			case 0x53:
				// odern aus Speicher mit b als Speicheradresse
				microinstructions.add(BO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(OR | EO | AI);
				break;
			case 0x54:
				// odern aus Speicher mit x als Speicheradresse
				microinstructions.add(XO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(OR | EO | AI);
				break;

			case 0x60:
				// exklusiv oder b
				microinstructions.add(BO | XI);
				microinstructions.add(XOR | EO | AI);
				break;
			case 0x61:
				// exklusiv oder x
				microinstructions.add(XOR | EO | AI);
				break;
			case 0x62:
				// exklusiv odern aus Speicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(XOR | EO | AI);
				break;
			case 0x63:
				// exklusiv odern aus Speicher mit b als Speicheradresse
				microinstructions.add(BO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(XOR | EO | AI);
				break;
			case 0x64:
				// exklusiv odern aus Speicher mit x als Speicheradresse
				microinstructions.add(XO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(XOR | EO | AI);
				break;

			case 0x70:
				// und b
				microinstructions.add(BO | XI);
				microinstructions.add(AND | EO | AI);
				break;
			case 0x71:
				// und x
				microinstructions.add(AND | EO | AI);
				break;
			case 0x72:
				// undn aus Speicher
				microinstructions.add(OPO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(AND | EO | AI);
				break;
			case 0x73:
				// undn aus Speicher mit b als Speicheradresse
				microinstructions.add(BO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(AND | EO | AI);
				break;
			case 0x74:
				// undn aus Speicher mit x als Speicheradresse
				microinstructions.add(XO | MAI);
				microinstructions.add(MO | XI);
				microinstructions.add(AND | EO | AI);
				break;

			case (byte) 0x81:
				// nicht a
				microinstructions.add(NOT | EO | AI);
				break;
			case (byte) 0x82:
				// bishift left a
				microinstructions.add(BSL | EO | AI);
				break;
			case (byte) 0x83:
				// bishift right a
				microinstructions.add(BSL | EO | AI);
				break;
			case (byte) 0x84:
				// increment a (add x und x=1)
				microinstructions.add(X1 | EO | AI);
				break;
			case (byte) 0x85:
				// decrement a
				microinstructions.add(X1 | SUB | EO | AI);
				break;

			case (byte) 0x90:
				// normale ausgabe
				microinstructions.add(AO | OI);
				break;
			case (byte) 0x91:
				// ausgabebefehl
				microinstructions.add(OPO | OCI);
				break;
			case (byte) 0x92:
				// ausgabeadresse
				microinstructions.add(OPO | OAI);
				break;
			case (byte) 0x93:
				// ausgabedaten
				microinstructions.add(OPO | ODI);
				break;

			case (byte) 0xa0:
				// unbedingter sprung
				microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa1:
				// bedingter sprung falls carry
				if (FlagRegister.carryFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa2:
				// bedingter sprung falls nicht carry
				if (!FlagRegister.carryFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa3:
				// bedingter sprung falls zero
				if (FlagRegister.zeroFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa4:
				// bedingter sprung falls nicht zero
				if (!FlagRegister.zeroFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa5:
				// bedingter sprung falls parity (ungerade)
				if (FlagRegister.parityFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa6:
				// bedingter sprung falls nicht parity (gerade)
				if (!FlagRegister.parityFlagSet(flags))
					microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa7:
				// rufe unterroutine auf (rücksprungadresse auf stack)
				microinstructions.add(SO | MAI);
				microinstructions.add(CO | MI | SM1);
				microinstructions.add(OPO | CI);
				break;
			case (byte) 0xa8:
				// rücksprung zur letzten rücksprungadresse auf stack
				microinstructions.add(SP1);
				microinstructions.add(SO | MAI);
				microinstructions.add(MO | CI);
				break;

			case (byte) 0xb0:
				// a auf stack speichern
				microinstructions.add(SO | MAI);
				microinstructions.add(AO | MI | SM1);
				break;
			case (byte) 0xb1:
				// letztes stackelement in a speichern
				microinstructions.add(SP1);
				microinstructions.add(SO | AI);
				break;
			case (byte) 0xb2:
				// b auf stack speichern
				microinstructions.add(SO | MAI);
				microinstructions.add(BO | MI | SM1);
				break;
			case (byte) 0xb3:
				// letztes stackelement in b speichern
				microinstructions.add(SP1);
				microinstructions.add(SO | BI);
				break;
			case (byte) 0xb4:
				// stack zurücksetzen, stackbeginn ist in op
				microinstructions.add(OPO | SI);
				break;

			case (byte) 0x23:
				// a nach b verschieben
				microinstructions.add(AO | BI);
				break;
			case (byte) 0x24:
				// a nach x verschieben
				microinstructions.add(AO | XI);
				break;
			case (byte) 0x25:
				// b nach a verschieben
				microinstructions.add(BO | AI);
				break;
			case (byte) 0x27:
				// b nach x verschieben
				microinstructions.add(BO | XI);
				break;
			case (byte) 0x28:
				// sp nach a verschieben
				microinstructions.add(SO | AI);
				break;
			case (byte) 0x29:
				// sp nach b verschieben
				microinstructions.add(SO | BI);
				break;
			case (byte) 0x2a:
				// sp nach x verschieben
				microinstructions.add(SO | XI);
				break;
			case (byte) 0x2b:
				// a und b tauschen (überschreibt x)
				microinstructions.add(AO | XI);
				microinstructions.add(BO | AI);
				microinstructions.add(XO | BI);
				break;
		} // end of decode

		// egal wie viele Befehle, am Ende wird der Mikrobefehlszähler zurückgesetzt, um
		// keine Zyklen zu verschwenden
		if (!microinstructions.isEmpty()) {
			microinstructions.set(microinstructions.size() - 1, microinstructions.get(microinstructions.size() - 1) | MSR);
		} else {
			microinstructions.add(MSR);
		}
		return microinstructions;
	}

	/**
	 * Gibt zurück, ob der gegebene Befehl ein Befehl ist, der einen Operanden
	 * benötigt.
	 */
	public static boolean isOperandInstruction(byte b) {
		int i = (int) b;
		// Meiste Arithmetik
		return i == 0x32 || i == 0x42 || i == 0x52 || i == 0x62 || i == 0x72 ||
		// Load und Store (ohne move)
				(i & 0xf0) == 0x10 || ((i & 0xf0) == 0x20 && i < 0x23) ||
				// Outputs (ohne standard output)
				((i & 0xf0) == 0x90 && i != -0x70) ||
				// Sprung/call (nur return ist eine Ausnahme)
				((i & 0xf0) == 0xa0 && i != -0x58);
	}

	/**
	 * Gibt den Von-Neumann-Zyklus der SA2-Architektur zurück.
	 */
	public static List<Long> getVonNeumannCycle() {
		// Von-Neumann-Mikrobefehle
		List<Long> vnmi = new ArrayList<Long>();
		// Von-Neumann-Zyklus
		vnmi.add(CO | MAI);
		vnmi.add(MO | II | CE);
		vnmi.add(CO | MAI);
		vnmi.add(MO | OPI | CE);

		return vnmi;
	}

	/**
	 * Setzt den Speicheradresszeiger zurück.
	 */
	public void resetMemoryPointer() {
		// System.out.println("Memory pointer reset");
		RAM.AddressPointer.setValue((byte) 0);
	}

	/**
	 * @return Ob der Bus der CPU zu diesem Zeitpunkt genutzt wird.
	 */
	public boolean busIsUsed() {
		return busController.busIsUsed();
	}

	/**
	 * @return Der letzte gespeicherte Zustand des Bus.
	 */
	public byte lastBusVal() {
		return this.lastBusVal;
	}

}
