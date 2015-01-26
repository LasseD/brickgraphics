package mosaic.ui.menu;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import mosaic.controllers.*;
import mosaic.ui.ColorChooserDialog;
import mosaic.ui.actions.ToggleColorDistributionChart;

public class ColorChooserToolBar extends JToolBar {
	public ColorChooserToolBar(ColorController colorController, UIController uiController, ColorChooserDialog colorChooser) {
		super("Color chooser options", SwingConstants.HORIZONTAL);
		
		add(colorChooser.createPackAction());
		add(new ToggleColorDistributionChart(uiController, colorChooser));
		//addSeparator();
	}
}
