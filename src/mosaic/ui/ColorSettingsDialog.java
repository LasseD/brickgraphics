package mosaic.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.ColorGroup;
import colors.parsers.ColorSheetParser;
import mosaic.controllers.ColorController;
import mosaic.io.MosaicIO;

public class ColorSettingsDialog extends JDialog implements ChangeListener {
	private static final long serialVersionUID = -1273278817213052004L;
	private ColorController cc;
	private JTextField tfLoadURL, tfLoadFile, tfFromYear, tfToYear, tfMinSets, tfMinParts;
	private JCheckBox cbShowMetallic, cbShowTransparent;
	private static final String DIALOG_TITLE = "Color Settings";

	public ColorSettingsDialog(JFrame parent, ColorController cc) {
		super(parent, DIALOG_TITLE, true);
		this.cc = cc;
		cc.addChangeListener(this);
		setLocation(parent.getLocation());
		
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setupUI();
			}
		});
	}
	
	private void setupUI() {
		Container cp = getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
		
		// Load options:
		{
			// URL:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update colors using Rebrickable.com"));
			tfLoadURL = new JTextField(40);
			JButton loadURLButton = new JButton("Download");
			loadURLButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.loadColorsFromURL(tfLoadURL.getText(), ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Latest colors successfully downloaded!", "Colors updated", JOptionPane.PLAIN_MESSAGE);
				}
			});
			titlePanel.add(tfLoadURL);
			titlePanel.add(loadURLButton);
			cp.add(titlePanel);
		}
		{
			// File:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update colors using a colors file downloaded from Rebrickable.com"));
			tfLoadFile = new JTextField(40);
			JButton findButton = new JButton("...");
			findButton.addActionListener(MosaicIO.createHtmlFileOpenAction(this, tfLoadFile));
			JButton loadFileButton = new JButton("Load file");
			loadFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					boolean ok = cc.loadColorsFromFile(tfLoadFile.getText(), ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Latest colors successfully read from file!", "Colors updated", JOptionPane.PLAIN_MESSAGE);
				}
			});
			titlePanel.add(tfLoadFile);
			titlePanel.add(findButton);
			titlePanel.add(loadFileButton);
			cp.add(titlePanel);
		}
		{
			// colors.txt:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update colors from the local " + ColorSheetParser.COLORS_FILE +" file"));
			JButton loadButton = new JButton("Reload " + ColorSheetParser.COLORS_FILE);
			loadButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.reloadColorsFile(ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, ColorSheetParser.COLORS_FILE + " reloaded successfully!", ColorSheetParser.COLORS_FILE + " reloaded", JOptionPane.PLAIN_MESSAGE);
					else
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Problem encountered while reloading " + ColorSheetParser.COLORS_FILE + ". Using safe colors!", ColorSheetParser.COLORS_FILE + " reloaded", JOptionPane.WARNING_MESSAGE);
				}
			});
			titlePanel.add(loadButton);
			titlePanel.add(new JLabel("Notice: This file is overwritten when using the options above!"));
			cp.add(titlePanel);
		}
		{
			// color_groups.txt:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update color groups from the local " + ColorGroup.GROUPS_FILE + " file"));
			JButton loadButton = new JButton("Reload " + ColorGroup.GROUPS_FILE);
			loadButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.reloadColorGroups();
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, ColorGroup.GROUPS_FILE + " reloaded successfully!", ColorGroup.GROUPS_FILE + " reloaded", JOptionPane.PLAIN_MESSAGE);
					else
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Problem encountered while reloading " + ColorGroup.GROUPS_FILE + ". Using safe color groups!", ColorSheetParser.COLORS_FILE + " reloaded", JOptionPane.WARNING_MESSAGE);
				}
			});
			titlePanel.add(loadButton);
			titlePanel.add(new JLabel("This updates the color groups shown in the color chooser."));
			cp.add(titlePanel);
		}
		{
			// color translations in color_translations/*.txt:
			JPanel titlePanel = new JPanel(new BorderLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Add or update color translations"));
			JPanel topPanel = new JPanel(new FlowLayout());
			JPanel bottomPanel = new JPanel(new FlowLayout());

			{
				JButton button = new JButton("Reload");
				button.addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						int loaded;
						try {
							loaded = cc.reloadColorTranslations(ColorSettingsDialog.this, true);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Error while reloading translations: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
							return;
						}
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, (loaded==0?"No":""+loaded) + " translation(s) read!", "Translations reloaded", JOptionPane.PLAIN_MESSAGE);
					}
				});
				topPanel.add(new JLabel("Color translations are in the '" + File.separator + ColorController.COLOR_TRANSLATION_FOLDER_NAME + "' folder. Press 'reload' to reload them all "));
				topPanel.add(button);
			}
			{
				final JTextField tf = new JTextField(10);
				JButton button = new JButton("Create");
				button.addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						String fileName = tf.getText().endsWith(".txt") ? tf.getText() : tf.getText()+".txt";
						try {
							cc.createColorTranslationFile(fileName);
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Error while creating color translations file: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							e1.printStackTrace();
							return;
						}
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Color translation file " + fileName + " created.\nAdd it to LDDMC using 'Reload' once it has been filled.", "File created", JOptionPane.PLAIN_MESSAGE);
					}
				});
				bottomPanel.add(new JLabel("Create new color translation file. Name: "));
				bottomPanel.add(tf);
				bottomPanel.add(button);
			}

			titlePanel.add(topPanel, BorderLayout.NORTH);
			titlePanel.add(bottomPanel, BorderLayout.SOUTH);
			cp.add(titlePanel);
		}
		
		// Filters:
		{
			// Year:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Only show colors active between these years"));
			tfFromYear = new JTextField(5);
			tfToYear = new JTextField(5);
			ActionListener a = new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setYearRange(tfFromYear.getText(), tfToYear.getText(), ColorSettingsDialog.this);
				}
			};
			tfFromYear.addActionListener(a);
			tfToYear.addActionListener(a);
			titlePanel.add(tfFromYear);
			titlePanel.add(new JLabel("-"));
			titlePanel.add(tfToYear);
			cp.add(titlePanel);
		}
		{
			// Quantities:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Only show colors that have appeared in at least"));
			tfMinSets = new JTextField(5);
			tfMinParts = new JTextField(5);
			ActionListener a = new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setMinQuantities(tfMinSets.getText(), tfMinParts.getText(), ColorSettingsDialog.this);
				}
			};
			tfMinSets.addActionListener(a);
			tfMinParts.addActionListener(a);
			titlePanel.add(tfMinSets);
			titlePanel.add(new JLabel("sets and"));
			titlePanel.add(tfMinParts);
			titlePanel.add(new JLabel("parts (use 0 to include all)"));
			cp.add(titlePanel);
		}
		{
			// Exclusions:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Colors that do not make much sense to show"));
			cbShowMetallic = new JCheckBox("Show metallic colors");
			cbShowMetallic.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowMetallic(cbShowMetallic.isSelected(), ColorSettingsDialog.this);
				}
			});
			cbShowTransparent = new JCheckBox("Show transparent colors");
			cbShowTransparent.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowTransparent(cbShowTransparent.isSelected(), ColorSettingsDialog.this);
				}
			});
			titlePanel.add(cbShowMetallic);
			titlePanel.add(cbShowTransparent);
			cp.add(titlePanel);			
		}
		{
			// Close
			JPanel panel = new JPanel(new FlowLayout());
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			panel.add(closeButton);
			cp.add(panel);
		}
		
		stateChanged(null);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		setTitle(DIALOG_TITLE + " - " + cc.getFilteredColors().size() + " of " + cc.getUnfilteredSize() + " colors shown");
		if(e != null && e.getSource() == this)
			return;
		
		tfLoadURL.setText(cc.getLoadURL());
		tfLoadFile.setText(cc.getLoadFile());
		tfFromYear.setText("" + cc.getFromYear());
		tfToYear.setText("" + cc.getToYear());
		tfMinSets.setText("" + cc.getMinSets());
		tfMinParts.setText("" + cc.getMinParts());
		cbShowTransparent.setSelected(cc.getShowTransparent());
		cbShowMetallic.setSelected(cc.getShowMetallic());
	}
}
