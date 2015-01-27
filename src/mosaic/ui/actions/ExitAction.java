package mosaic.ui.actions;

import io.Log;
import io.Model;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import mosaic.io.BrickGraphicsState;
import icon.*;

public class ExitAction extends AbstractAction {
	public static final String DISPLAY_NAME = "Exit";
	private Model<BrickGraphicsState> model;
	
	public ExitAction(Model<BrickGraphicsState> model) {
		this.model = model;

		putValue(SHORT_DESCRIPTION, "Exits the program.");
		putValue(SMALL_ICON, Icons.exit(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.exit(Icons.SIZE_LARGE));
		putValue(NAME, DISPLAY_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));	
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			model.saveToFile();
		} catch (IOException e2) {
			Log.log(e2);
		}
		Log.close();
		System.exit(0);
	}
}
