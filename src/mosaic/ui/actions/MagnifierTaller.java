package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.MagnifierController;

public class MagnifierTaller extends AbstractAction {
	private MagnifierController magnifier;
	
	public MagnifierTaller(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Heighten the magnifier.");
		putValue(SMALL_ICON, Icons.taller(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.taller(Icons.SIZE_LARGE));
		putValue(NAME, "Taller");
		putValue(MNEMONIC_KEY, KeyEvent.VK_E);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Taller".indexOf('e'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		magnifier.changeSizeHeightInMosaicBlocks(1);
	}
}
