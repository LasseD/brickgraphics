package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import icon.*;
import mosaic.ui.OptionsDialog;

/**
 * @author ld
 */
public class ShowOptionsAction extends AbstractAction {
	public static final String NAME_STR = "Settings";
	private OptionsDialog od;
	
	public ShowOptionsAction(OptionsDialog od) {
		this.od = od;
		putValue(Action.SHORT_DESCRIPTION, "Display the settings dialog.");
		putValue(Action.NAME, NAME_STR);
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "preferences"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "preferences"));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F2);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('S'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		od.pack();
		od.setVisible(true);
	}
}
