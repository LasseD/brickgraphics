package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import mosaic.ui.dialogs.ColorChooserDialog;
import icon.*;

public class ToggleColorChooser extends AbstractAction {
	private ColorChooserDialog cs;
	
	public ToggleColorChooser(ColorChooserDialog cs) {
		this.cs = cs;
		putValue(Action.SHORT_DESCRIPTION, "Toggle the color chooser.");
		putValue(Action.SMALL_ICON, Icons.colorsChooserDialog(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.colorsChooserDialog(Icons.SIZE_LARGE));
		putValue(Action.NAME, "Colors");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Colors".indexOf('C'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		cs.switchEnabled();
	}
}
