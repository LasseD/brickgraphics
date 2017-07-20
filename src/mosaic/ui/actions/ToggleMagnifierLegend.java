package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.*;

public class ToggleMagnifierLegend extends AbstractAction {
	private UIController controller;
	
	public ToggleMagnifierLegend(UIController controller) {
		this.controller = controller;

		putValue(Action.SHORT_DESCRIPTION, "Toggle the legend.");
		putValue(Action.SMALL_ICON, Icons.colorLegend(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.colorLegend(Icons.SIZE_LARGE));
		putValue(Action.NAME, "Legend");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Legend".indexOf('L'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controller.flipLegendEnabled();
	}
}
