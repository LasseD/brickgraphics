package mosaic.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import ui.Icons;

import mosaic.io.BrickGraphicsState;

import io.*;

/**
 * Language - resets everything when changed (Text, Locale)
 * Restrict image size - for users who don't resize
 * annoying questions - for users generally
 * 
 * @author ld
 */
public class Dialogs extends JDialog {
	private static final long serialVersionUID = 140367208812353010L;
	private JCheckBox annoyingQuestions, restrictImageSize;
	private JComboBox language;
	private JTextField restrictWidth, restrictHeight;

	private boolean beforeQuestions, beforeRestrict;
	private Dimension beforeDims;
	private Locale beforeLanguage;
	
	private DialogListener okListener;
	
	public Dialogs(MainWindow parent) {
		super(parent, "Options", true);
		setIconImage(Icons.get(Icons.SIZE_LARGE, "preferences.png").getImage());
		
		annoyingQuestions = new JCheckBox("Enable popup messages");
		annoyingQuestions.setToolTipText("\"Are you sure?\", \"Existing file exists, overwrite?\", etc.");
		restrictWidth = new JTextField(4);
		restrictHeight = new JTextField(4);
		restrictImageSize = new JCheckBox("Set max picture size");
		restrictImageSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDimsFields();
			}
		});
		language = new JComboBox(Text.getAvailableLocales());
		final ListCellRenderer DefaultRenderer = language.getRenderer();
		language.setRenderer(new ListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				String s = ((Locale)value).getDisplayName();
				return DefaultRenderer.getListCellRendererComponent(list, s, index, isSelected, cellHasFocus);
			}
		});
		
		// Components:
		// size panel:
		JPanel sizePanel = new JPanel();
		GridBagLayout sizeLayout = new GridBagLayout();
		sizePanel.setLayout(sizeLayout);
		sizePanel.setBorder(new TitledBorder("Max picture size"));
		GridBagConstraints c = new GridBagConstraints();

		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		
  	   	c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        add(sizePanel, restrictImageSize, sizeLayout, c);

		c.fill = GridBagConstraints.VERTICAL;
  	   	c.weightx = 0.0;
  	   	c.ipadx = 3;
        c.gridwidth = 1;
        add(sizePanel, new JLabel("Width"), sizeLayout, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
        add(sizePanel, restrictWidth, sizeLayout, c);
        c.gridwidth = 1;
        add(sizePanel, new JLabel("Height"), sizeLayout, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
        add(sizePanel, restrictHeight, sizeLayout, c);
		
		// all components:	
		JPanel components = new JPanel();
		GridBagLayout l = new GridBagLayout();
		components.setLayout(l);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
  	   	c.ipadx = 3;
  	   	c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
        add(components, annoyingQuestions, l, c);
        add(components, sizePanel, l, c);
        c.gridwidth = 1;
        add(components, new JLabel("Language"), l, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
        add(components, language, l, c);

        // all:
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(components, BorderLayout.CENTER);
		contentPane.add(createOkCancelPanel(), BorderLayout.SOUTH);

		pack();
	}
	
	private void add(JPanel panel, JComponent component, GridBagLayout layout, GridBagConstraints c) {
		layout.addLayoutComponent(component, c);
		panel.add(component);
	}
	
	private void updateDimsFields() {
		boolean selected = restrictImageSize.isSelected();
		restrictWidth.setEnabled(selected);
		restrictHeight.setEnabled(selected);
	}
	
	public Action makeShowOptionsDialogAction(final Model<BrickGraphicsState> model, final DialogListener okListener) {
		Action a = new AbstractAction() {
			private static final long serialVersionUID = 3793246329441L;

			public void actionPerformed(ActionEvent e) {
				showOptionsDialog(model, okListener);
			}
		};

		a.putValue(Action.SHORT_DESCRIPTION, "Options");
		a.putValue(Action.LONG_DESCRIPTION, "Show options dialog.");
		a.putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "preferences"));
		a.putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "preferences"));
		a.putValue(Action.NAME, "Options");
		a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		a.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Options".indexOf('n'));
		a.putValue(Action.ACTION_COMMAND_KEY, "Options");
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));	
		
		return a;
	}
	
	public static boolean userAcceptsOKCancel(MainWindow parent, Model<BrickGraphicsState> model, String title, String message) {
		if(!(Boolean)model.get(BrickGraphicsState.AnnoyingQuestions))
			return true;
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
	}
	
	public void showOptionsDialog(Model<BrickGraphicsState> model, DialogListener okListener) {
		this.okListener = okListener;

		beforeQuestions = (Boolean)model.get(BrickGraphicsState.AnnoyingQuestions);
		annoyingQuestions.setSelected(beforeQuestions);
		beforeRestrict = (Boolean)model.get(BrickGraphicsState.ImageRestrictionEnabled);
		restrictImageSize.setSelected(beforeRestrict);
		beforeDims = (Dimension)model.get(BrickGraphicsState.ImageRestriction);
		restrictWidth.setText(String.valueOf(beforeDims.width));
		restrictHeight.setText(String.valueOf(beforeDims.height));
		beforeLanguage = (Locale)model.get(BrickGraphicsState.Language);
		language.setSelectedItem(beforeLanguage);

		updateDimsFields();
		setVisible(true);
	}
	
	private JPanel createOkCancelPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				
				Set<BrickGraphicsState> changed = new HashSet<BrickGraphicsState>();
				
				if(annoyingQuestions.isSelected() != beforeQuestions)
					changed.add(BrickGraphicsState.AnnoyingQuestions);
				if(restrictImageSize.isSelected() != beforeRestrict)
					changed.add(BrickGraphicsState.ImageRestrictionEnabled);
				if(!((Locale)language.getSelectedItem()).equals(beforeLanguage)) {
					System.out.println(beforeLanguage.getDisplayName() + "->" + ((Locale)language.getSelectedItem()).getDisplayName());
					System.out.println(beforeLanguage + "->" + language.getSelectedItem());
					changed.add(BrickGraphicsState.Language);	
				}
				
				try {
					int w = Integer.parseInt(restrictWidth.getText());
					int h = Integer.parseInt(restrictHeight.getText());
					if(w <= 0 || h <= 0) {
						throw new NumberFormatException();
					}
					if(!new Dimension(w, h).equals(beforeDims))
						changed.add(BrickGraphicsState.ImageRestriction);
					
					okListener.okPressed(changed);
				}
				catch(NumberFormatException e2) {
					if(annoyingQuestions.isSelected()) {
						JOptionPane.showMessageDialog(Dialogs.this, "Width and height must be positive integers", 
								"Input error", JOptionPane.ERROR_MESSAGE);
					}
					else {
						restrictWidth.setText(String.valueOf(beforeDims.width));
						restrictHeight.setText(String.valueOf(beforeDims.height));
					}
				}
			}
		});
		
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		panel.add(ok);
		panel.add(cancel);		
		return panel;
	}
	
	public boolean getAnnoyingQuestions() {
		return annoyingQuestions.isSelected();
	}
	public boolean getRestrictImageSize() {
		return restrictImageSize.isSelected();
	}
	public int getRestrictWidth() {
		return Integer.parseInt(restrictWidth.getText());
	}
	public int getRestrictHeight() {
		return Integer.parseInt(restrictHeight.getText());
	}
	public Locale getLanguage() {
		return (Locale)language.getSelectedItem();
	}
	
	public static interface DialogListener {
		void okPressed(Set<BrickGraphicsState> changed);
	}
}
