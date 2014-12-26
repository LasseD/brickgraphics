package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import mosaic.ui.*;
import ui.Icons;

public class ToggleColorChooser extends AbstractAction {
	private static final long serialVersionUID = 304212080553608L;
	private ColorChooserDialog cs;
	
	public ToggleColorChooser(ColorChooserDialog cs) {
		if(cs == null)
			throw new IllegalArgumentException();
		this.cs = cs;
		putValue(Action.SHORT_DESCRIPTION, "Toggle the color chooser.");
		putValue(Action.SMALL_ICON, Icons.colors(Icons.SIZE_SMALL));
		putValue(Action.LARGE_ICON_KEY, Icons.colors(Icons.SIZE_LARGE));
		putValue(Action.NAME, "Colors");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Colors".indexOf('C'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK));
	}

	public void actionPerformed(ActionEvent e) {
		cs.switchEnabled();
	}
}
