package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.*;
import mosaic.ui.bricked.*;

public class MagnifierTaller extends AbstractAction {
	private static final long serialVersionUID = -2597248879224054969L;
	private Magnifier magnifier;
	
	public MagnifierTaller(Magnifier magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Heighten the magnifier.");
		putValue(SMALL_ICON, Icons.taller(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.taller(Icons.SIZE_LARGE));
		putValue(NAME, "Taller");
		putValue(MNEMONIC_KEY, KeyEvent.VK_T);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Taller".indexOf('T'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.changeBlockSizeHeight(1);
	}
}
