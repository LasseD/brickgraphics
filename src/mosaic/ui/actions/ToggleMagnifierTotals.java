package mosaic.ui.actions;

import icon.Icons;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosaic.controllers.*;

public class ToggleMagnifierTotals extends AbstractAction {
	public static final String NAME = "Totals";
	
	private UIController controller;
	
	public ToggleMagnifierTotals(UIController controller) {
		this.controller = controller;

		putValue(Action.SHORT_DESCRIPTION, "Toggle the totals in the legend.");
		putValue(Action.SMALL_ICON, Icons.totalsSymbol(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.totalsSymbol(Icons.SIZE_LARGE));
		putValue(Action.NAME, NAME);
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME.indexOf('a'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		controller.flipViewTotals();
	}
	
	public static JButton createHidingButton(final UIController controller) {
		final JButton ret = new JButton(new ToggleMagnifierTotals(controller));
		ret.setText(null);
		controller.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				ret.setVisible(controller.showLegend());
			}
		});
		ret.setVisible(controller.showLegend());
		return ret;
	}
}
