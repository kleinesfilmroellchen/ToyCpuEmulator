package klfr.sa2emu.viewers;

// Java Stdlib Utility
import java.io.*;
import java.util.*;

// JavaFX
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import klfr.sa2emu.cpuemulator.*;
import klfr.sa2emu.cpuemulator.exceptions.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.event.*;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.canvas.*;
import javafx.animation.*;

/**
 * Eine grafische Benutzeroberfläche für einen Emulator der Simple Advanced 2
 * Architektur.<br>
 * für die Oberfläche wird JavaFX benutzt.<br>
 * <br>
 * Es sind Möglichkeiten zum Laden und Eingeben sowie Kompilieren von
 * Assemblycode verfügbar, sowie zur Modifikation des Arbeitsspeichers. Dann
 * kann die CPU ausgeführt werden: automatisch in verschiedenen
 * Geschwindigkeitsstufen, oder manuell mit 1-Schritt-Methode.
 * 
 * @version 1.0 from 25.06.2018
 * @author kleines Filmröllchen
 */

public class JavaFX_SA2Emulator extends Application {

	/**
	 * PrintStream, der ein Textfeld mit den Nachrichten beschreibt. Es ist nur die
	 * Verwendung des TextFieldPrintStream(TextField) - Konstruktors zulässig.
	 * 
	 * @author kleines Filmröllchen
	 * @see TextFieldPrintStream#TextFieldPrintStream(TextField)
	 */
	@SuppressWarnings("unused")
	private class TextFieldPrintStream extends PrintStream {

		protected TextInputControl tf;

		/**
		 * Einziger zulässiger Konstruktor.
		 * 
		 * @param tf Das Textfeld, welches dieser PrintStream zum Drucken verwendet.
		 * @throws FileNotFoundException weil baum (wird nicht geworfen)
		 */
		public TextFieldPrintStream(TextInputControl tf) throws FileNotFoundException {
			super("temp.dat");
			this.tf = tf;
		}

		public void print(String x) {
			tf.appendText(String.valueOf(x));
		}

		public void print(Object x) {
			this.print(String.valueOf(x));
		}

		public void print(boolean b) {
			this.print(Boolean.toString(b));
		}

		public void print(int b) {
			this.print(Integer.toString(b));
		}

		public void print(long b) {
			this.print(Long.toString(b));
		}

		public void print(float b) {
			this.print(Float.toString(b));
		}

		public void print(double b) {
			this.print(Double.toString(b));
		}

		public void println() {
			tf.appendText(System.lineSeparator());
		}

		public void println(String s) {
			tf.appendText(s + System.lineSeparator());
		}

		public void println(Object x) {
			this.println(String.valueOf(x));
		}

		public void println(boolean b) {
			this.println(Boolean.toString(b));
		}

		public void println(int b) {
			this.println(Integer.toString(b));
		}

		public void println(long b) {
			this.println(Long.toString(b));
		}

		public void println(float b) {
			this.println(Float.toString(b));
		}

		public void println(double b) {
			this.println(Double.toString(b));
		}

		public void println(short b) {
			this.println(Short.toString(b));
		}

		public void println(byte b) {
			this.println(Byte.toString(b));
		}
	}

	// TODO: Memory Table

	/// CPU und Assembler
	private SA2_CPU cpu = new SA2_CPU();
	@SuppressWarnings("unused")
	private SA2_Assembler assembler;

	/// Zeitkontrolle
	private Timeline cpuRunTimeline = new Timeline();

	/// Datei Eingabe/Ausgabe
	private static FileChooser asmFileChooser;
	private static FileChooser ramFileChooser;

	/// Schrift und Grafik
	private static Font monoFont;
	private static Font normalFont;
	private static List<Image> icons = new ArrayList<>();

	/// Grafikobjekte
	// private Label mpos = new Label();
	// Assembler und Speicher
	private TextArea assemblerTA = new TextArea();
	private TextArea compileErrorA = new TextArea();
	// CPU-Darstellung
	private TextField tfPC = new TextField();
	private TextField tfMAR = new TextField();
	private TextField tfRAM = new TextField();
	private TextField tfIP = new TextField();
	private TextField tfOP = new TextField();
	private TextField tfFR = new TextField();
	private TextField tfOUT = new TextField();
	private TextField tfA = new TextField();
	private TextField tfALU = new TextField();
	private TextField tfX = new TextField();
	private TextField tfSP = new TextField();
	private TextField tfB = new TextField();
	private Canvas bgCanvas = new Canvas();
	private Canvas outC = new Canvas();
	private Canvas outputBusC = new Canvas();
	private Label outputBusL = new Label();
	private HBox lowerSectionHb = new HBox();
	private VBox consSlidVb = new VBox();
	// Simulation
	private TextArea consoleTf = new TextArea();
	private Slider simspeedSl = new Slider(-1, 4, 2);
	// Ende Attribute

	@SuppressWarnings("hiding")
	public void start(Stage primaryStage) throws FileNotFoundException {
		AnchorPane root = new AnchorPane();
		Scene scene = new Scene(root, 759, 556);

		// Menüleiste
		MenuBar menuBar = new MenuBar();
		AnchorPane.setLeftAnchor(menuBar, 0d);
		AnchorPane.setTopAnchor(menuBar, 0d);
		AnchorPane.setRightAnchor(menuBar, 0d);
		root.getChildren().add(menuBar);

		Menu fileMenu = new Menu("Datei");
		Menu assemblerMenu = new Menu("Assembler");
		Menu simulationMenu = new Menu("Simulation");
		Menu ramMenu = new Menu("RAM");
		menuBar.getMenus().addAll(fileMenu, assemblerMenu, simulationMenu, ramMenu);

		MenuItem compile = new MenuItem("Assemblerbefehle kompilieren (Strg + K)");
		compile.setOnAction(action -> {
			String assembly = assemblerTA.getText();
			try {
				SA2_Assembler assembler = new SA2_Assembler(assembly);
				cpuRunTimeline.stop();
				cpu.halted = false;
				cpu.setMemory(assembler.getMachineCode(), (byte) 0);
				assembler.printMachineCode(System.out);
			} catch (AssemblyError e) {
				drawError(e.getMessage());
				return;
			}

			cpu.RAM.printMemory(System.out);
			cpu.resetMemoryPointer();

			drawMsg("Assembly erfolgreich!");

		});
		CheckMenuItem autoCompile = new CheckMenuItem("Automatisch kompilieren");
		assemblerMenu.getItems().addAll(autoCompile, compile);

		MenuItem loadAsm = new MenuItem("Assemblerbefehle öffnen (Strg + O)");
		loadAsm.setOnAction(action -> {
			asmFileChooser.setTitle("Assemblerdatei öffnen");
			File selected = asmFileChooser.showOpenDialog(primaryStage);
			try {
				Scanner s = new Scanner(selected);
				while (s.hasNextLine())
					assemblerTA.appendText(s.nextLine() + System.lineSeparator());
				s.close();
				if (autoCompile.isSelected()) {
					compile.fire();
				}
			} catch (FileNotFoundException e) {
				drawError("Fehler: Datei nicht gefunden.");
			}
		});

		MenuItem storeAsm = new MenuItem("Assemblerbefehle speichern (Strg + S)");
		storeAsm.setOnAction(action -> {
			asmFileChooser.setTitle("Assemblerdatei speichern");
			File selected = asmFileChooser.showSaveDialog(primaryStage);
			PrintStream filePrinter;
			try {
				filePrinter = new PrintStream(selected);
				filePrinter.print(assemblerTA.getText());
				filePrinter.close();
			} catch (FileNotFoundException e) {
				drawError(e.getMessage());
			}
		});
		MenuItem exit = new MenuItem("Schließen (Alt + F4)");
		exit.setOnAction(action -> Platform.exit());
		fileMenu.getItems().addAll(loadAsm, storeAsm, new SeparatorMenuItem(), exit);

		MenuItem oneStep = new MenuItem("Ein CPU-Schritt (Alt + 1)");
		oneStep.setOnAction(action -> {
			cpuRunTimeline.stop();

			cpuRunTimeline = new Timeline(new KeyFrame(Duration.ZERO, a -> {
				if (cpu.halted) {
					try {
						cpuRunTimeline.stop();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					executeStepPt1();
				} catch (CPUException e) {
					e.printStackTrace();
				}
			}), new KeyFrame(Duration.millis(500), a -> {
				try {
					executeStepPt2();
				} catch (CPUException e) {
					e.printStackTrace();
				}
			}));

			cpuRunTimeline.setCycleCount(1);
			cpuRunTimeline.play();
		});
		MenuItem play = new MenuItem("Simulation starten (Alt + 2)");
		play.setOnAction(action -> {
			Duration dnf = calcAnimationDuration();
			System.out.println(dnf.toString());

			cpuRunTimeline.stop();
			cpuRunTimeline = new Timeline(new KeyFrame(dnf, a -> {
				if (cpu.halted) {
					try {
						cpuRunTimeline.stop();
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				try {
					executeStepPt1();
				} catch (CPUException e) {
					e.printStackTrace();
				}
			}), new KeyFrame(dnf.add(dnf), a -> {
				try {
					executeStepPt2();
				} catch (CPUException e) {
					e.printStackTrace();
				}
			}));

			cpuRunTimeline.setCycleCount(Animation.INDEFINITE);
			cpuRunTimeline.play();
		});
		MenuItem stop = new MenuItem("Simulation anhalten (Alt + 3)");
		stop.setOnAction(action -> {
			cpuRunTimeline.stop();
		});
		MenuItem reset = new MenuItem("Programmzähler zurücksetzen (Alt + 4)");
		reset.setOnAction(action -> {
			cpuRunTimeline.stop();
			cpu.resetMemoryPointer();
		});
		simulationMenu.getItems().addAll(oneStep, play, stop, new SeparatorMenuItem(), reset);

		MenuItem loadRam = new MenuItem("Arbeitsspeicher laden (Strg + Alt + O)");
		loadRam.setOnAction(action -> {
			ramFileChooser.setTitle("Arbeitsspeicherabbild öffnen");
			File selected = ramFileChooser.showOpenDialog(primaryStage);
			try {
				readMemory(new Scanner(selected));
				cpu.resetMemoryPointer();
			} catch (FileNotFoundException e) {
				drawError("Fehler: Datei wurde nicht gefunden oder kann nicht gelesen werden.");
			} catch (NoSuchElementException e) {
				drawError("Fehler beim Lesen des Speicherabbilds. Lade nur unmodifizierte Speicherabbilde.");
			} catch (Exception e) {
				drawError("Unbekannter Fehler beim Lesen des Speicherabbilds.");
			}
		});
		MenuItem storeRam = new MenuItem("Arbeitsspeicher speichern (Strg + Alt + S)");
		storeRam.setOnAction(action -> {
			ramFileChooser.setTitle("Arbeitsspeicherabbild speichern");
			File selected = ramFileChooser.showSaveDialog(primaryStage);
			try {
				cpu.RAM.printMemory(new PrintStream(selected));
			} catch (FileNotFoundException e) {
				drawError("Fehler: Datei wurde nicht gefunden oder kann nicht gelesen werden.");
			}
		});
		MenuItem resetRam = new MenuItem("Arbeitsspeicher zurücksetzen (Alt + Shift + 4)");
		resetRam.setOnAction(action -> {
			cpuRunTimeline.stop();
			cpu.reset();
			cpu.resetMemoryPointer();
		});
		ramMenu.getItems().addAll(loadRam, storeRam, new SeparatorMenuItem(), resetRam);

		// Tastenkombinationen
		// String in der Form: "Modifier Modifier Taste"
		Map<String, MenuItem> keyCombinations = new HashMap<>();
		keyCombinations.put("CONTROL O", loadAsm);
		keyCombinations.put("CONTROL S", storeAsm);
		keyCombinations.put("CONTROL K", compile);
		keyCombinations.put("ALT DIGIT1", oneStep);
		keyCombinations.put("ALT DIGIT2", play);
		keyCombinations.put("ALT DIGIT3", stop);
		keyCombinations.put("ALT DIGIT4", reset);
		keyCombinations.put("ALT NUMPAD1", oneStep);
		keyCombinations.put("ALT NUMPAD2", play);
		keyCombinations.put("ALT NUMPAD3", stop);
		keyCombinations.put("ALT NUMPAD4", reset);
		keyCombinations.put("CONTROL ALT O", loadRam);
		keyCombinations.put("CONTROL ALT S", storeRam);
		keyCombinations.put("SHIFT ALT DIGIT4", resetRam);
		keyCombinations.put("SHIFT ALT NUMPAD4", resetRam);

		EventHandler<KeyEvent> tfInputBlocker = action -> {
			// Deaktivierung der Eingabe bei Escape
			if (action.getCode() == KeyCode.ESCAPE)
				root.requestFocus();
			action.consume();
		};
		EventHandler<KeyEvent> tfInputPasser = action -> {
			// Deaktivierung der Eingabe bei Escape
			if (action.getCode() == KeyCode.ESCAPE)
				root.requestFocus();
			// sicherstellen, dass das Ereignis weitergeleitet wird
			if (action.isConsumed())
				KeyEvent.fireEvent(((Node) action.getSource()).getParent(), action);
		};
		// erkennt Tastenkombinationen
		scene.setOnKeyPressed(action -> {
			System.out.println("Tastendruck: " + action.getCode().toString());
			// Nur Strg+ [] ist interessant
			if (action.isMetaDown() || action.isControlDown() || action.isAltDown()) {
				String keysPressed = ((action.isMetaDown() || action.isControlDown()) ? "CONTROL " : "")
						+ (action.isShiftDown() ? "SHIFT " : "") + (action.isAltDown() ? "ALT " : "")
						+ action.getCode().toString();
				if (keyCombinations.containsKey(keysPressed)) {
					MenuItem itemToFire = keyCombinations.get(keysPressed);
					itemToFire.fire();
					System.out.println("Fired Event on " + itemToFire.toString() + " with key combination " + keysPressed);
				}
			}
		});

		/// Assembler
		VBox assemblerVb = new VBox(8);
		AnchorPane.setLeftAnchor(assemblerVb, 8d);
		AnchorPane.setTopAnchor(assemblerVb, 30d);
		root.getChildren().add(assemblerVb);

		assemblerVb.getChildren().addAll(assemblerTA, compileErrorA);
		assemblerTA.setPrefSize(153, 289);
		assemblerTA.setFont(monoFont);
		assemblerTA.setPromptText("SA2-Assembler eingeben");
		assemblerTA.setFocusTraversable(true);
		assemblerTA.setOnKeyPressed(tfInputBlocker);
		compileErrorA.setPrefSize(153, 57);
		compileErrorA.setFont(monoFont);
		compileErrorA.setOnKeyPressed(tfInputBlocker);
		compileErrorA.setWrapText(true);
		compileErrorA.setEditable(false);

		// Schriftarten
		outputBusL.setFont(normalFont);

		AnchorPane.setRightAnchor(bgCanvas, 8d);
		AnchorPane.setTopAnchor(bgCanvas, 30d);
		bgCanvas.setWidth(root.getWidth() - assemblerVb.localToScene(assemblerVb.getBoundsInLocal()).getMaxX());
		bgCanvas.setHeight(321);
		bgCanvas.setOnKeyPressed(tfInputBlocker);
		root.getChildren().add(bgCanvas);

		VBox leftRegistersVb = new VBox(16);
		leftRegistersVb.setLayoutX(216);
		leftRegistersVb.setLayoutY(64);
		leftRegistersVb.getChildren().addAll(tfPC, tfMAR, tfRAM, tfIP, tfOP, tfOUT);
		leftRegistersVb.getChildren().forEach(node -> {
			node.setOnKeyPressed(tfInputPasser);
			((Region) node).setPrefSize(105, 33);
			((TextField) node).setFont(monoFont);
		});
		root.getChildren().add(leftRegistersVb);
		tfRAM.setPrefWidth(45);

		tfPC.setText("PC");
		tfMAR.setText("MAR");
		tfRAM.setText("RAM");
		tfIP.setText("IP");
		tfOP.setText("OP");
		tfOUT.setText("OUT");

		VBox rightRegistersVb = new VBox(16);
		rightRegistersVb.setLayoutX(400);
		rightRegistersVb.setLayoutY(64);
		rightRegistersVb.getChildren().addAll(tfA, tfALU, tfX, tfB, tfSP);
		rightRegistersVb.getChildren().forEach(node -> {
			node.setOnKeyPressed(tfInputPasser);
			((Region) node).setPrefSize(105, 33);
			((TextField) node).setFont(monoFont);
		});
		root.getChildren().add(rightRegistersVb);

		// Flaggenregister
		tfFR.setTranslateX(tfALU.localToScene(0, 0).getX() + 8);
		tfFR.setTranslateY(-tfALU.getHeight() - 8);
		tfFR.setPrefHeight(33);
		tfFR.setPrefWidth(97);
		tfFR.setFont(monoFont);
		tfFR.setText("FR");
		root.getChildren().add(tfFR);

		tfA.setText("A");
		tfALU.setText("ALU");
		tfX.setText("X");
		tfB.setText("B");
		tfSP.setText("SP");

		outC.setWidth(97);
		outC.setHeight(40);
		root.getChildren().add(outC);

		/// Untere Hälfte der CPU-Darstellung
		AnchorPane.setBottomAnchor(lowerSectionHb, 8d);
		AnchorPane.setRightAnchor(lowerSectionHb, 16d);
		lowerSectionHb.setPrefSize(392, 121);
		root.getChildren().add(lowerSectionHb);

		// Label und Bus
		outputBusL.setLayoutX(lowerSectionHb.localToScene(lowerSectionHb.getBoundsInLocal()).getMinX());
		outputBusL.setLayoutY(lowerSectionHb.localToScene(lowerSectionHb.getBoundsInLocal()).getMinY());
		outputBusL.setPrefSize(139, 25);
		outputBusL.setText("Ausgabebus");
		outputBusL.setContentDisplay(ContentDisplay.CENTER);
		outputBusL.setLabelFor(outputBusC);
		root.getChildren().add(outputBusL);
		outputBusC.setWidth(169);
		lowerSectionHb.getChildren().add(outputBusC);

		// Slider und Konsole
		simspeedSl.setPrefHeight(25);
		simspeedSl.setPrefWidth(230);
		simspeedSl.setBlockIncrement(1);
		simspeedSl.setSnapToTicks(true);
		simspeedSl.setMajorTickUnit(1);
		simspeedSl.setMinorTickCount(0);
		simspeedSl.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public Double fromString(String str) {
				return null;
			}

			@Override
			public String toString(Double d) {
				if (Math.round(d) == -1)
					return "Sofort";
				if (Math.round(d) == 0)
					return "Schnell";
				// System.out.println(Double.toString(calcAnimationDuration().toSeconds() *2));
				return String.format("%.2fs", calcAnimationDuration(d).toSeconds() * 2);
			}
		});

		// Mauslabel beim Verschieben des Sliders
		Label mouseLabel = new Label();
		mouseLabel.setPrefHeight(20);
		mouseLabel.setPrefWidth(170);
		mouseLabel.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		simspeedSl.setOnMouseDragged(ac -> {
			// System.out.printf("(%5.2f |%5.2f )%n", ac.getX(), ac.getY());
			mouseLabel.setText("Geschwindigkeit: " + simspeedSl.getLabelFormatter().toString(simspeedSl.getValue()));
			mouseLabel.setLayoutX(ac.getSceneX());
			mouseLabel.setLayoutY(ac.getSceneY() - simspeedSl.getHeight());
		});
		simspeedSl.setOnMousePressed(ac -> {
			mouseLabel.setText("Geschwindigkeit: " + simspeedSl.getLabelFormatter().toString(simspeedSl.getValue()));
			root.getChildren().add(mouseLabel);
			mouseLabel.setLayoutX(ac.getSceneX());
			mouseLabel.setLayoutY(ac.getSceneY() - simspeedSl.getHeight());
		});
		simspeedSl.setOnMouseReleased(ac -> {
			root.getChildren().remove(root.getChildren().size() - 1);
		});
		simspeedSl.setShowTickLabels(true);
		simspeedSl.setShowTickMarks(true);
		consSlidVb.getChildren().add(simspeedSl);
		consoleTf.setPrefHeight(121);
		consoleTf.setPrefWidth(200);
		consoleTf.setFont(monoFont);
		consoleTf.setEditable(false);
		System.setOut(new TextFieldPrintStream(consoleTf));
		System.setErr(new TextFieldPrintStream(consoleTf));
		consSlidVb.getChildren().add(consoleTf);
		lowerSectionHb.getChildren().add(consSlidVb);

		/// debug ficker
		// mpos.setPrefSize(100, 20);
		// mpos.setFont(normalFont);
		// root.setOnMouseMoved(ac -> {
		// mpos.setLayoutX(ac.getSceneX());
		// mpos.setLayoutY(ac.getSceneY()-20);
		// mpos.setText("x = " + ac.getSceneX() + " y = " + ac.getSceneY());
		// });
		// root.getChildren().add(mpos);

		//// Ende Komponenten

		primaryStage.setTitle("Simple Advanced Version 2 Emulator");
		primaryStage.getIcons().addAll(icons);
		primaryStage.setScene(scene);
		primaryStage.show();

		// stellt sicher, dass die CPU in jedem Frame gezeichnet wird.
		AnimationTimer sceneUpdater = new AnimationTimer() {
			// public long last;

			public void handle(long now) {
				drawCPU();

				// Dynamisches Layout hier
				bgCanvas.setWidth(root.getWidth() - assemblerVb.localToScene(assemblerVb.getBoundsInLocal()).getMaxX());

				outC.setLayoutX(tfOUT.localToScene(0, 0).getX());
				outC.setLayoutY(tfOUT.localToScene(0, 0).getY());
				outputBusL.setLayoutX(lowerSectionHb.localToScene(lowerSectionHb.getBoundsInLocal()).getMinX());
				outputBusL.setLayoutY(lowerSectionHb.localToScene(lowerSectionHb.getBoundsInLocal()).getMinY());

				tfFR.setTranslateX(tfALU.localToScene(0, 0).getX() + 8);
				tfFR.setTranslateY(-tfALU.getHeight() - 8);

				// System.out.println(1000000000f / (now - last));
				// last = now;
			}
		};
		sceneUpdater.start();

		cpu.reset();
	} // end of start

	public void readMemory(Scanner scan) throws NoSuchElementException {
		cpu.RAM.AddressPointer.setValue((byte) 0);
		while (scan.hasNextInt(16)) {
			cpu.RAM.AddressPointer.setValue((byte) (cpu.RAM.AddressPointer.getValue() + 1));
			cpu.RAM.writeMemory((byte) scan.nextInt(16));
		}
	}

	/**
	 * Führt den ersten Teil des Schritts, also das Setzen der Steuerlinien, aus.
	 * 
	 * @throws CPUException
	 * @throws StackOverflowException
	 */
	public void executeStepPt1() throws StackOverflowException, CPUException {
		System.out.println("--Step part 1--");
		cpu.setControlLines();
	}

	/**
	 * Führt den zweiten Teil des Schritts, also das Aktivieren der Datenübertragung
	 * auf dem Bus, aus.
	 * 
	 * @throws StackOverflowException
	 * @throws CPUException
	 */
	public void executeStepPt2() throws StackOverflowException, CPUException {
		System.out.println("--Step part 2--");
		cpu.clock();
	}

	/**
	 * Zeichnet alles von der CPU.
	 */
	public void drawCPU() {

		Point2D p = tfALU.getParent().getLocalToParentTransform()
				.deltaTransform(tfALU.getLayoutX() + tfALU.getTranslateX(), tfALU.getLayoutY() + tfALU.getWidth());
		tfFR.setLayoutX(p.getX() + tfALU.getWidth() + 8);
		tfFR.setLayoutY(p.getY());

		tfPC.setText("PC 0x" + hex(cpu.PC.getValue()));
		tfMAR.setText("MAR 0x" + hex(cpu.RAM.AddressPointer.getValue()));
		tfRAM.setText("RAM [0x" + hex(cpu.RAM.AddressPointer.getValue()) + "] 0x" + hex(cpu.RAM.readMemory()));
		tfIP.setText("IP 0x" + hex(cpu.IP.getValue()));
		tfOP.setText("OP 0x" + hex(cpu.OP.getValue()));
		tfA.setText("A 0x" + hex(cpu.A.getValue()));
		tfB.setText("B 0x" + hex(cpu.B.getValue()));
		tfX.setText("X 0x" + hex(cpu.X.getValue()));
		tfALU.setText("ALU 0x" + hex(cpu.ALU.getValue()));
		tfOUT.setText("OUT 0x" + hex(cpu.OUT.getValue()));
		tfFR.setText("FR 0x" + hex(cpu.FR.getValue()));
		tfSP.setText("SP 0x" + hex(cpu.SP.getValue()));
		tfFR.setTooltip(new Tooltip(flagTooltip()));

		// Busse
		GraphicsContext g = bgCanvas.getGraphicsContext2D();
		g.setFill(Color.WHITESMOKE);
		g.clearRect(0, 0, bgCanvas.getWidth(), bgCanvas.getHeight());

		drawBusGrid(g);

		GraphicsContext outG = outC.getGraphicsContext2D();
		outG.setFill(Color.MAROON);
		outG.fillRoundRect(0d, 0d, outC.getWidth(), outC.getHeight(), 2d, 2d);
		outG.setLineWidth(4);
		outG.strokeRoundRect(2, 2, outC.getWidth() - 2, outC.getHeight() - 2, 2, 2);
		outG.setFill(Color.RED);
		outG.setFont(Font.font(monoFont.getFamily(), FontWeight.BOLD, 25d));
		outG.fillText(cpu.OUT.display(), 5, (outC.getHeight() / 2) + (outG.getFont().getSize() / 2));

	}

	/**
	 * Zeichnet die Buslinien auf die Grafikoberfläche, auch abhängig von
	 * Ausgabe/Eingabe der Register.
	 */
	public void drawBusGrid(GraphicsContext g) {
		g.save();

		/// Einstellungen
		g.setFont(monoFont);
		if (cpu.halted) {
			g.setStroke(Color.RED);
			g.setFill(Color.RED);
		}
		g.setStroke(Color.BLACK);
		g.setFill(Color.BLACK);
		g.setLineWidth(6);
		g.setLineCap(StrokeLineCap.BUTT);

		// Busposition
		Bounds outb = tfOUT.localToScene(tfOUT.getBoundsInLocal());
		double busStartY = 0, busEndY = outb.getMinY() + (outb.getWidth() / 2)
				- (g.getCanvas().getLayoutY() + g.getCanvas().getTranslateY());

		// Bus-X-Position berechnen
		double leftRegisterEdge = tfPC.localToScene(tfPC.getBoundsInLocal()).getMaxX(),
				rightRegisterEdge = tfSP.localToScene(tfSP.getBoundsInLocal()).getMinX(),
				busX = g.getCanvas().sceneToLocal((leftRegisterEdge + rightRegisterEdge) / 2, 0).getX();

		/// Zugang der Register auf den Bus
		drawRegisterToBus(tfPC, g, cpu.PC.isActing(), busX, true);
		drawRegisterToBus(tfMAR, g, cpu.RAM.AddressPointer.isActing(), busX, true);
		drawRegisterToBus(tfRAM, g, cpu.RAM.isActing(), busX, true);
		drawRegisterToBus(tfIP, g, cpu.IP.isActing(), busX, true);
		drawRegisterToBus(tfOP, g, cpu.OP.isActing(), busX, true);
		drawRegisterToBus(tfOUT, g, cpu.OUT.isActing(), busX, true);
		drawRegisterToBus(tfA, g, cpu.A.isActing(), busX, false);
		drawRegisterToBus(tfB, g, cpu.B.isActing(), busX, false);
		drawRegisterToBus(tfX, g, cpu.X.isActing(), busX, false);
		drawRegisterToBus(tfALU, g, cpu.ALU.isActing(), busX, false);
		drawRegisterToBus(tfSP, g, cpu.SP.isActing(), busX, false);

		/// Bus selbst
		g.setLineWidth(1);
		g.strokeText("0x" + hex(cpu.lastBusVal()), busX - 20, busStartY + g.getFont().getSize() + 5);
		if (cpu.busIsUsed())
			g.setStroke(Color.CHARTREUSE);

		g.setLineWidth(6);
		g.strokeLine(busX, busStartY + 25, busX, busEndY);
		g.restore();

	}

	/**
	 * Zeichnet die Verbindungslinie zwischen Register und Bus.
	 * 
	 * @param tf        Das Grafikobjekt, welches das Register repräsentiert. Seine
	 *                  Position wird zur Positionierung der Linie genutzt.
	 * @param g         Der Grafikkontext, der zum Zeichnen genutzt wird. Sein
	 *                  Zustand ist
	 *                  hinterher genauso wie davor.
	 * @param isActive  Falls {@code true}, wird die Verbindungslinie in grün
	 *                  gezeichnet, um die Kommunikation des Registers mit dem Bus
	 *                  zu signalisieren.
	 * @param busX      Die X-Position der Buslinie auf der Canvas von g.
	 * @param fromRight Falls {@code true}, wird die Verbindungslinie auf der
	 *                  rechten Seite des Registers begonnen. Andernfalls wird auf
	 *                  der linken Seite
	 *                  begonnen.
	 */
	private static void drawRegisterToBus(Region tf, GraphicsContext g, boolean isActive, double busX,
			boolean fromRight) {
		// System.out.printf(
		// "Invoked JavaFX_SA2Emulator.drawRegisterToBus(Region, GraphicsContext,
		// isActive = %b, busX, fromRight = %b)%n",
		// isActive, fromRight);
		g.save();
		g.setStroke(Color.BLACK);

		Bounds tfPos = tf.localToScene(tf.getBoundsInLocal());

		double gx = g.getCanvas().getLayoutX() + g.getCanvas().getTranslateX(),
				gy = g.getCanvas().getLayoutY() + g.getCanvas().getTranslateY();

		double xstart = fromRight ? (tfPos.getMinX() + tfPos.getWidth() - gx) : (tfPos.getMinX() - gx),
				ystart = tfPos.getMinY() + (tfPos.getHeight() / 2) - gy;

		if (isActive) {
			System.out.println("Active Bus");
			g.setStroke(Color.CHARTREUSE);
		}

		g.strokeLine(xstart, ystart, busX, ystart);

		g.restore();
	}

	/**
	 * Zeichnet einen Fehler in das Fehlerfeld.
	 */
	public void drawError(String error) {
		compileErrorA.setStyle("-fx-text-fill: rgb(255, 5, 10);");
		compileErrorA.setText(error);
	}

	/**
	 * Zeichnet eine (normale) Nachricht in das Fehlerfeld.
	 */
	public void drawMsg(String msg) {
		compileErrorA.setStyle("-fx-text-fill: rgb(0, 0, 0);");
		compileErrorA.setText(msg);
	}

	/**
	 * @return Tooltip-Informationen über das Flaggenregister der CPU mit Newline am
	 *         Ende.
	 */
	public String flagTooltip() {

		String tooltip = "Flaggenregister:" + System.lineSeparator();
		if (cpu.FR.carryFlagSet())
			tooltip += "Übertrags/Carry-Flagge gesetzt." + System.lineSeparator();
		if (cpu.FR.zeroFlagSet())
			tooltip += "Nullflagge gesetzt." + System.lineSeparator();
		if (cpu.FR.parityFlagSet())
			tooltip += "Paritäts/Ungerade-Flagge gesetzt." + System.lineSeparator();
		return tooltip;
	}

	/**
	 * Berechnet die Animationszeit der CPU, die sich aus dem Wert des
	 * Geschwindigkeitssliders ergibt.
	 * 
	 * @return
	 */
	public Duration calcAnimationDuration() {
		return calcAnimationDuration(simspeedSl.getValue());
	}

	/**
	 * Berechnet die Animationszeit, die sich aus dem gegebenen Sliderwert
	 * errechnet.
	 * 
	 * @param d
	 * @return
	 */
	public Duration calcAnimationDuration(Double d) {
		Duration dnf = Duration.millis(Math.pow(10, d) / 2);
		if (d == -1)
			dnf = Duration.ONE;
		else if (d == 0)
			dnf = Duration.ZERO;

		// System.out.println("simspeed half=" + dnf.toString() + " d=" + d);

		return dnf;
	}

	/**
	 * @see main.java.klfr.sa2emu.cpuemulator.SA2_Assembler#stringifyHex(byte)
	 */
	private static String hex(byte b) {
		return SA2_Assembler.stringifyHex(b);
	}

	public static void main(String[] args) {

		List<ExtensionFilter> assemblyFileExtensions = new ArrayList<>();
		assemblyFileExtensions.add(new ExtensionFilter("Assemblerdatei", "*.asm", "*.asmbly", "*.assembly"));
		assemblyFileExtensions.add(new ExtensionFilter("Textdatei", "*.txt", "*.text", "*.dat"));
		assemblyFileExtensions.add(new ExtensionFilter("Alle Dateien", "*.*"));

		asmFileChooser = new FileChooser();
		asmFileChooser.setInitialDirectory(new File("."));
		asmFileChooser.setTitle("Assemblerdatei öffnen");
		asmFileChooser.getExtensionFilters().addAll(assemblyFileExtensions);
		asmFileChooser.setSelectedExtensionFilter(assemblyFileExtensions.get(0));

		List<ExtensionFilter> ramFileExtensions = new ArrayList<>();
		ramFileExtensions.add(new ExtensionFilter("Arbeitsspeicher", "*.ram", "*.mem", "*.memory"));
		ramFileExtensions.add(new ExtensionFilter("Textdatei", "*.txt", "*.text", "*.dat"));
		ramFileExtensions.add(new ExtensionFilter("Alle Dateien", "*.*"));

		ramFileChooser = new FileChooser();
		ramFileChooser.setInitialDirectory(new File("."));
		ramFileChooser.setTitle("Arbeitsspeicher öffnen");
		ramFileChooser.getExtensionFilters().addAll(ramFileExtensions);
		ramFileChooser.setSelectedExtensionFilter(ramFileExtensions.get(0));

		try {
			icons.add(new Image("/icon_size0.png"));
			icons.add(new Image("/icon_size1.png"));
			icons.add(new Image("/icon_size2.png"));
		} catch (IllegalArgumentException e) {
		}

		loadFonts();

		launch(args);
	}

	public static void loadFonts() {
		// falls "Candara" geladen werden kann
		if (!Font.font("Candara", 12).getFamily().equals(Font.getDefault().getFamily())) {
			normalFont = Font.font("Candara", 12);
		} else {
			normalFont = Font.font("Arial", 12);
		}

		if (!Font.font("Andale Mono", 10).getFamily().equals(Font.getDefault().getFamily())) {
			monoFont = Font.font("Andale Mono", 10);
		} else {
			monoFont = Font.font("Courier New", 10);
		}
	}

}
