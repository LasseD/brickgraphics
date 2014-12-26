package mosaic.ui.actions;

import io.Model;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import mosaic.io.BrickGraphicsState;
import ui.Icons;

public class ExitAction extends AbstractAction {
	private static final long serialVersionUID = 315079464003322198L;
	public static final String DISPLAY_NAME = "Exit";
	private Model<BrickGraphicsState> model;
	
	public ExitAction(Model<BrickGraphicsState> model) {
		this.model = model;

		putValue(SHORT_DESCRIPTION, "Exits the program.");
		putValue(SMALL_ICON, Icons.exit(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.exit(Icons.SIZE_LARGE));
		putValue(NAME, DISPLAY_NAME);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK));	
	}

	public void actionPerformed(ActionEvent e) {
		try {
			model.saveToFile();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		System.exit(0);
	}
}
