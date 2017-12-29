package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import mosaic.controllers.PrintController;
import mosaic.controllers.PrintController.ShowPosition;

public class TogglePositionDisplay extends AbstractAction {
	public static final String DISPLAY_NAME = "(x,y)";
	
	private PrintController pc;
	
	public TogglePositionDisplay(PrintController pc) {
		this.pc = pc;

		putValue(SHORT_DESCRIPTION, "Toggle the position display.");
		putValue(NAME, DISPLAY_NAME);
		putValue(MNEMONIC_KEY, (int)'y');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, DISPLAY_NAME.indexOf('y'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ShowPosition[] values = ShowPosition.values();
		pc.setShowPosition(values[(pc.getShowPosition().ordinal()+1)%values.length], this);
	}
}
