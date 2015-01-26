package mosaic.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import mosaic.ui.ImagePreparingView;
import icon.*;

public class ToggleFilters extends AbstractAction {
	public static final String DISPLAY_NAME = "Filters";
	private ImagePreparingView view;
	
	public ToggleFilters(ImagePreparingView view) {
		this.view = view;

		putValue(SHORT_DESCRIPTION, "Toggle the filters.");
		putValue(SMALL_ICON, Icons.prepareFiltersEnable(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.prepareFiltersEnable(Icons.SIZE_LARGE));
		putValue(NAME, DISPLAY_NAME);
		putValue(MNEMONIC_KEY, (int)'i');
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY, DISPLAY_NAME.indexOf('i'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		view.switchFiltersEnabled();
	}
}
