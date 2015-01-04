package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.MagnifierController;

public class ToggleMagnifierColors extends AbstractAction {
	private MagnifierController magnifier;

	public ToggleMagnifierColors(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Toggle colors in the magnifier.");
		putValue(SMALL_ICON, Icons.showColors(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.showColors(Icons.SIZE_LARGE));
		putValue(NAME, "Show Colors");
		putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Show Colors".indexOf('S'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));	
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		magnifier.flipShowColors();
	}
}
