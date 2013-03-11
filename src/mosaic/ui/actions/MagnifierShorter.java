package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.*;
import mosaic.ui.bricked.*;

public class MagnifierShorter extends AbstractAction {
	private static final long serialVersionUID = -9197875075916954879L;
	private Magnifier magnifier;
	
	public MagnifierShorter(Magnifier magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Shorten the magnifier.");
		putValue(SMALL_ICON, Icons.shorter(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.shorter(Icons.SIZE_LARGE));
		putValue(NAME, "Shorter");
		putValue(MNEMONIC_KEY, KeyEvent.VK_H);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Shorter".indexOf('h'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.changeBlockSizeHeight(-1);
	}
}
