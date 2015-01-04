package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import icon.*;
import mosaic.controllers.MagnifierController;

public class MagnifierSlimmer extends AbstractAction {
	private MagnifierController magnifier;
	
	public MagnifierSlimmer(MagnifierController magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Slim the magnifier.");
		putValue(SMALL_ICON, Icons.slimmer(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.slimmer(Icons.SIZE_LARGE));
		putValue(NAME, "Slimmer");
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Slimmer".indexOf('l'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		magnifier.changeSizeWidthInMosaicBlocks(-1);
	}
}
