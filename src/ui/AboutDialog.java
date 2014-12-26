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
 * @author ld
 */
public class AboutDialog extends JDialog {
	private static final long serialVersionUID = 7842563607059033693L;
	public static final String NAME_STR = "About";
	public static final String FILE_NAME = "about.txt";

	private AboutDialog(JFrame parent, String programName, String version, Icon icon) {
		super(parent, "About " + programName, true);
		setLayout(new BorderLayout());
		JLabel topLabel = new JLabel(icon);		
		add(topLabel, BorderLayout.WEST);
		
		StringBuffer sb = new StringBuffer();
		
		try {
			Scanner scanner = new Scanner(new File(FILE_NAME));
			while(scanner.hasNextLine()) {
				sb.append(scanner.nextLine() + "\n");
			}			
		}
		catch(IOException e) {
			sb.append("Software: " + programName + "\n");
			sb.append("Version: " + version + "\n");
			sb.append("Author: Lasse Deleuran\n");
			sb.append("Contact: lassedeleuran@gmail.com\n");
		}
		
		JTextArea ta = new JTextArea(sb.toString());
		ta.setEditable(false);
		ta.setBackground(getBackground());
		add(ta, BorderLayout.SOUTH);
		
		pack();
		setLocation(parent.getLocation().x+(parent.getWidth()-getWidth())/2, parent.getLocation().y+(parent.getHeight()-getHeight())/2);
	}
	
	public static Action createAction(final JFrame parent, final String programName, final String version, final Icon icon, final Icon smallIcon) {
		Action a = new AbstractAction() {
			private static final long serialVersionUID = -4753100136802594575L;

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
