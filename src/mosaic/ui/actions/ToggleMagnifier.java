package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.MagnifierController;

public class ToggleMagnifier extends AbstractAction {
	private MagnifierController magnifier;
	
	public ToggleMagnifier(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Toggle the magnifier.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "zoom"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "zoom"));
		putValue(NAME, "Magnifier");
		putValue(MNEMONIC_KEY, KeyEvent.VK_M);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Magnifier".indexOf('M'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		magnifier.flipEnabled();
	}
}
