package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import icon.*;
import mosaic.controllers.OptionsController;
import mosaic.ui.dialogs.OptionsDialog;

/**
 * @author LD
 */
public class ShowOptionsAction extends AbstractAction {
	public static final String NAME_STR = "Settings";
	private OptionsController oc;
	
	public ShowOptionsAction(OptionsController oc) {
		this.oc = oc;
		putValue(Action.SHORT_DESCRIPTION, "Display the settings dialog.");
		putValue(Action.NAME, NAME_STR);
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "preferences", "SETTINGS"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "preferences", "SETTINGS"));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F2);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('S'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		OptionsDialog od = oc.getOptionsDialog();
		od.pack();
		od.setVisible(true);
	}
}
