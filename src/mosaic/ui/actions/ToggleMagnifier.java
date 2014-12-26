package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.Icons;
import mosaic.controllers.MagnifierController;

public class ToggleMagnifier extends AbstractAction {
	private static final long serialVersionUID = 4284266557973002161L;
	private MagnifierController magnifier;
	
	public ToggleMagnifier(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Toggle the magnifier.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "zoom"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "zoom"));
		putValue(NAME, "Magnifier");
		putValue(MNEMONIC_KEY, KeyEvent.VK_M);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Magnifier".indexOf('M'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.flipEnabled();
	}
}
