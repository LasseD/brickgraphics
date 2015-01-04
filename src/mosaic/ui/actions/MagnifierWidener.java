package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.MagnifierController;

public class MagnifierWidener extends AbstractAction {
	private MagnifierController magnifier;
	
	public MagnifierWidener(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Widen the magnifier.");
		putValue(SMALL_ICON, Icons.wider(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.wider(Icons.SIZE_LARGE));
		putValue(NAME, "Wider");
		putValue(MNEMONIC_KEY, KeyEvent.VK_W);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Wider".indexOf('W'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		magnifier.changeSizeWidthInMosaicBlocks(1);
	}
}
