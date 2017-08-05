package mosaic.ui.actions;

import icon.*;
import java.awt.event.*;
import javax.swing.*;
import mosaic.controllers.*;
import mosaic.ui.dialogs.ColorChooserDialog;

public class ToggleColorDistributionChart extends AbstractAction {
	public static final String NAME = "Color chart";
	
	private UIController uc;
	private ColorChooserDialog colorChooser;
	
	public ToggleColorDistributionChart(UIController uc, ColorChooserDialog colorChooser) {
		this.uc = uc;
		this.colorChooser = colorChooser;

		putValue(Action.SHORT_DESCRIPTION, "Toggle the color chart in the color chooser.");
		putValue(Action.SMALL_ICON, Icons.colorDistributionChart(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.colorDistributionChart(Icons.SIZE_LARGE));
		putValue(Action.NAME, NAME);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		uc.flipViewColorDistributionChart();
		colorChooser.halfPack();
	}
}
