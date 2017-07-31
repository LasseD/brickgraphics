package mosaic.ui.actions;

import icon.Icons;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import mosaic.controllers.ToBricksController;

public class ToggleShowDividerLocationButton extends AbstractAction {
	private ToBricksController tbc;	
	
	public ToggleShowDividerLocationButton(ToBricksController tbc) {
		this.tbc = tbc;

		putValue(SHORT_DESCRIPTION, "Show/hide the divider location button.");
		putValue(SMALL_ICON, Icons.dividerTriangles(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.dividerTriangles(Icons.SIZE_LARGE));		
		putValue(NAME, "Divider button");
		putValue(MNEMONIC_KEY, (int)'b');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, "Divider button".indexOf('b'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		tbc.toggleShowDividerLocationButton();
	}
}