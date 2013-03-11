package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import ui.*;
import mosaic.ui.bricked.*;

public class MagnifierZoomIn extends AbstractAction {
	private static final long serialVersionUID = 2424721476223688522L;
	private Magnifier magnifier;
	
	public MagnifierZoomIn(Magnifier magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Zoom in on the magnifier.");
		putValue(SMALL_ICON, Icons.get(16, "zoom_in"));
		putValue(LARGE_ICON_KEY, Icons.get(32, "zoom_in"));
		putValue(NAME, "Zoom In");
		putValue(MNEMONIC_KEY, KeyEvent.VK_I);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Zoom In".indexOf('I'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.addSize();
	}
}
