package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.Icons;
import mosaic.controllers.MagnifierController;

public class ToggleMagnifierLegend extends AbstractAction {
	private static final long serialVersionUID = 4284266557973002161L;
	private MagnifierController magnifier;
	
	public ToggleMagnifierLegend(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(Action.SHORT_DESCRIPTION, "Toggle the legend in the magnifier.");
		putValue(Action.SMALL_ICON, Icons.colorLegend(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.colorLegend(Icons.SIZE_LARGE));
		putValue(Action.NAME, "Legend");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Legend".indexOf('L'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.flipLegendEnabled();
	}
}
