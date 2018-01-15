package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import icon.*;
import mosaic.ui.dialogs.ColorSettingsDialog;

/**
 * @author ld
 */
public class ShowColorOptionsAction extends AbstractAction {
	public static final String NAME_STR = "Color settings";
	private ColorSettingsDialog csd;
	
	public ShowColorOptionsAction(ColorSettingsDialog csd) {
		this.csd = csd;
		putValue(Action.SHORT_DESCRIPTION, "Display the color settings dialog.");
		putValue(Action.NAME, NAME_STR);
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "preferences", "COLORS"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "preferences", "COLORS"));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('C'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		csd.pack();
		csd.setVisible(true);
	}
}
