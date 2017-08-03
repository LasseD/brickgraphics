package mosaic.ui.menu;

import javax.swing.*;
import ui.*;
import mosaic.controllers.*;
import mosaic.io.MosaicIO;
import mosaic.ui.*;
import mosaic.ui.actions.*;

public class Ribbon extends JToolBar {
	public Ribbon(MainController mc, MainWindow mw) {
		setFloatable(true);
		setLayout(new WrapLayout(0, 0));

		ImagePreparingView imagePreparingView = mw.getImagePreparingView();

		// Load & Save:
		add(MosaicIO.createOpenAction(mc, mw));
		add(MosaicIO.createSaveAction(mc, mw));
		addSeparator();
		
		// Toggles:
		add(new ToggleFilters(imagePreparingView));
		add(new ToggleCrop(imagePreparingView));
		add(new ToggleColorChooser(mw.getColorChooser()));
		add(new ToggleMagnifier(mc.getUIController()));
		add(new ToggleMagnifierLegend(mc.getUIController()));		
		add(ToggleMagnifierTotals.createHidingButton(mc.getUIController()));
		// TODO: Add magnifier buttons (wider, taller, lower, slimmer, toggle colors)
	}
}
