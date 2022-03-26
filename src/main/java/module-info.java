/**
 * Collection of CPU Emulating classes and Applications.
 * @author kleines Filmröllchen
 */
module sa2emu {
	exports klfr.sa2emu.viewers;
	exports klfr.sa2emu.cpuemulator;

	requires transitive javafx.base;
	requires transitive javafx.controls;
	requires java.base;
	requires java.logging;
}
