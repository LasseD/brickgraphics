package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.*;
import mosaic.ui.bricked.*;

public class MagnifierSlimmer extends AbstractAction {
	private static final long serialVersionUID = 3368715313888126878L;
	private Magnifier magnifier;
	
	public MagnifierSlimmer(Magnifier magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Slim the magnifier.");
		putValue(SMALL_ICON, Icons.slimmer(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.slimmer(Icons.SIZE_LARGE));
		putValue(NAME, "Slimmer");
		putValue(MNEMONIC_KEY, KeyEvent.VK_L);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Slimmer".indexOf('l'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.changeBlockSizeWidth(-1);
	}
}
