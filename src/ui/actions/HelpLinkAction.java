package ui.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import icon.*;

public class HelpLinkAction extends AbstractAction {
	public static final String NAME_STR = "On-line help";
	private JFrame owner;
	private String url;
	
	public HelpLinkAction(JFrame owner, String url) {
		this.owner = owner;
		this.url = url;

		putValue(SHORT_DESCRIPTION, "Open on-line help.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "help", "HELP"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "help", "HELP"));
		putValue(NAME, NAME_STR);
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('O'));
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
	    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
	        try {
	            desktop.browse(new URI(url));
	        } catch (Exception e2) {
	        	JOptionPane.showConfirmDialog(owner, "Error opening browser", "Failed to open your default browser.\nPlease visit " + url + " for help with this software.", JOptionPane.INFORMATION_MESSAGE);
	        }
	    }
    }
}
