package mosaic.ui.actions;

import java.awt.event.*;

import javax.swing.*;
import mosaic.ui.prepare.*;
import icon.*;

public class ToggleCrop extends AbstractAction {
	private ImagePreparingView view;
	
	public ToggleCrop(ImagePreparingView view) {
		this.view = view;

		putValue(SHORT_DESCRIPTION, "Toggle cropping.");
		putValue(SMALL_ICON, Icons.crop(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.crop(Icons.SIZE_LARGE));
		putValue(NAME, "Crop");
		putValue(MNEMONIC_KEY, (int)'R');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Crop".indexOf('r'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		view.switchCropState();
	}
}
