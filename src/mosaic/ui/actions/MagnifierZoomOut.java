package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import mosaic.ui.bricked.*;
import ui.*;

public class MagnifierZoomOut extends AbstractAction {
	private static final long serialVersionUID = -2580016026615732166L;
	private Magnifier magnifier;
	
	public MagnifierZoomOut(Magnifier magnifier) {
		this.magnifier = magnifier;

		putValue(SHORT_DESCRIPTION, "Zoom out from the magnifier.");
		putValue(SMALL_ICON, Icons.get(16, "zoom_out"));
		putValue(LARGE_ICON_KEY, Icons.get(32, "zoom_out"));
		putValue(NAME, "Zoom Out");
		putValue(MNEMONIC_KEY, KeyEvent.VK_O);
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Zoom Out".indexOf('O'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		magnifier.reduceSize();
	}
}
