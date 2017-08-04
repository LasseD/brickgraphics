package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import icon.*;
import mosaic.controllers.MainController;

/**
 * @author LD
 */
public class ShowToBricksTypeFilterDialog extends AbstractAction {
	public static final String NAME_STR = "Construction techniques";
	private MainController controller;
	
	public ShowToBricksTypeFilterDialog(MainController controller) {
		this.controller = controller;
		putValue(Action.SHORT_DESCRIPTION, "Display the construction techniques filter dialog.");
		putValue(Action.NAME, NAME_STR);
		putValue(Action.SMALL_ICON, Icons.filterToBrickTypes(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.filterToBrickTypes(Icons.SIZE_LARGE));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F3);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('C'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controller.showToBricksTypeFilterDialog();
	}
}
