package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import mosaic.ui.*;
import icon.*;

public class ToggleCrop extends AbstractAction {
	public static final String DISPLAY_NAME = "Crop";
	private ImagePreparingView view;
	
	public ToggleCrop(ImagePreparingView view) {
		this.view = view;

		putValue(SHORT_DESCRIPTION, "Toggle cropping.");
		putValue(SMALL_ICON, Icons.crop(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.crop(Icons.SIZE_LARGE));
		putValue(NAME, DISPLAY_NAME);
		putValue(MNEMONIC_KEY, (int)'r');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, DISPLAY_NAME.indexOf('r'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		view.switchCropState();
	}
}
