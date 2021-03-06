package ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

/**
 * @author LD
 */
public class AboutDialog extends JDialog {
	public static final String NAME_STR = "About";
	public static final String FILE_NAME = "about.txt";

	private AboutDialog(JFrame parent, String programName, String version, Icon icon) {
		super(parent, "About " + programName, true);
		setLayout(new BorderLayout());
		JLabel topLabel = new JLabel(icon);		
		add(topLabel, BorderLayout.WEST);
		
		StringBuffer sb = new StringBuffer();
		sb.append("Software: " + programName + "\n");
		sb.append("Version: " + version + "\n\n");
		
		try {
			Scanner scanner = new Scanner(new File(FILE_NAME));
			while(scanner.hasNextLine()) {
				sb.append(scanner.nextLine() + "\n");
			}			
		}
		catch(IOException e) {
			sb.append("Author: Lasse Deleuran\n");
			sb.append("Contact: lassedeleuran@gmail.com\n");
			sb.append("Warning: " + FILE_NAME + " could not be read from the file system!\n");
		}
		
		JTextArea ta = new JTextArea(sb.toString());
		ta.setEditable(false);
		ta.setBackground(getBackground());
		add(ta, BorderLayout.SOUTH);
		
		pack();
		int x = Math.max(0, parent.getLocation().x+(parent.getWidth()-getWidth())/2);
		int y = Math.max(0, parent.getLocation().y+(parent.getHeight()-getHeight())/2);
		setLocation(x, y);
	}
	
	public static Action createAction(final JFrame parent, final String programName, final String version, final Icon icon, final Icon smallIcon) {
		Action a = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog d = new AboutDialog(parent, programName, version, icon);		
				d.setVisible(true);				
			}
		};

		a.putValue(Action.SHORT_DESCRIPTION, "Display the about dialog.");
		a.putValue(Action.NAME, NAME_STR);
		a.putValue(Action.SMALL_ICON, smallIcon);
		a.putValue(Action.LARGE_ICON_KEY, icon);
		a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		a.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, NAME_STR.indexOf('A'));
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));	
		return a;
	}
}
