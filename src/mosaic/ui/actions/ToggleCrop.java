package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import mosaic.ui.prepare.*;
import ui.*;

public class ToggleCrop extends AbstractAction {
	private static final long serialVersionUID = 3048201246080553608L;
	private ImagePreparingView view;
	
	public ToggleCrop(ImagePreparingView view) {
		this.view = view;

		putValue(SHORT_DESCRIPTION, "Toggle cropping.");
		putValue(SMALL_ICON, Icons.crop(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.crop(Icons.SIZE_LARGE));
		putValue(NAME, "Crop");
		putValue(MNEMONIC_KEY, (int)'R');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Crop".indexOf('r'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		view.switchCropState();
	}
}
