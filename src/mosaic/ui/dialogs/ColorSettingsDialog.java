package mosaic.ui.dialogs;

import io.Log;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import ui.LividTextField;

import colors.parsers.ColorSheetParser;
import mosaic.controllers.ColorController;
import mosaic.io.MosaicIO;

/**
 * @author LD
 */
public class ColorSettingsDialog extends JDialog implements ChangeListener {
	private ColorController cc;
	private JTextField /*tfLoadRebrickableURL,*/ tfLoadRebrickableFile;
	private LividTextField tfFromYear, tfToYear, tfMinSets, tfMinParts;
	private JCheckBox cbShowMetallic, cbShowTransparent, cbShowOnlyLDD, cbShowOtherColorsGroup;
	private static final String DIALOG_TITLE = "Color Settings";

	public ColorSettingsDialog(JFrame parent, ColorController cc) {
		super(parent, DIALOG_TITLE, true);
		this.cc = cc;
		cc.addChangeListener(this);
		setLocation(parent.getLocation());
		
		setupUI();
	}
	
	private void setupUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel setupPanel = new JPanel();
		setupPanel.setLayout(new BoxLayout(setupPanel, BoxLayout.Y_AXIS));
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
		
		// Load options:
		/*{
			// Rebrickable URL:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update colors using Rebrickable.com"));
			tfLoadRebrickableURL = new JTextField(40);
			JButton loadURLButton = new JButton("Download");
			loadURLButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.loadColorsFromURL(tfLoadRebrickableURL.getText(), ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Latest colors successfully downloaded!", "Colors updated", JOptionPane.PLAIN_MESSAGE);
				}
			});
			titlePanel.add(tfLoadRebrickableURL);
			titlePanel.add(loadURLButton);
			setupPanel.add(titlePanel);
		}*/
		{
			// Rebrickable File:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Update colors using a colors file downloaded from https://rebrickable.com/colors"));
			tfLoadRebrickableFile = new JTextField(40);
			JButton findButton = new JButton("...");
			findButton.addActionListener(MosaicIO.createHtmlFileOpenAction(this, tfLoadRebrickableFile));
			JButton loadFileButton = new JButton("Load file");
			loadFileButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.loadColorsFromFile(tfLoadRebrickableFile.getText(), ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Latest colors successfully read from file!", "Colors updated", JOptionPane.PLAIN_MESSAGE);
				}
			});
			titlePanel.add(tfLoadRebrickableFile);
			titlePanel.add(findButton);
			titlePanel.add(loadFileButton);
			setupPanel.add(titlePanel);
		}
		/*{
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
		}*/
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
							Log.log(e1);
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
							Log.log(e1);
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
			setupPanel.add(titlePanel);
		}
		{
			// backup of colors.txt:
			JPanel titlePanel = new JPanel(new BorderLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Restore colors from the backup file " + ColorSheetParser.BACKUP_COLORS_FILE + " in case of import with undetected errors"));
			JPanel topPanel = new JPanel(new FlowLayout());
			JButton button1 = new JButton("Restore colors from backup file");
			button1.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = cc.reloadBackupColorsFile(ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, ColorSheetParser.COLORS_FILE + " restored from backup!", ColorSheetParser.COLORS_FILE + " restored", JOptionPane.PLAIN_MESSAGE);
					else
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Problem encountered while reloading " + ColorSheetParser.BACKUP_COLORS_FILE + ".", ColorSheetParser.BACKUP_COLORS_FILE + " not reloaded!", JOptionPane.WARNING_MESSAGE);
				}
			});
			topPanel.add(button1);
			topPanel.add(new JLabel("Notice: This replaces the current " + ColorSheetParser.COLORS_FILE + " file!"));
			titlePanel.add(topPanel, BorderLayout.NORTH);
			JPanel bottomPanel = new JPanel(new FlowLayout());
			JButton button2 = new JButton("Make new backup file");
			button2.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean ok = ColorController.copyColorsFileToBackup(ColorSettingsDialog.this);
					if(ok)
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, ColorSheetParser.COLORS_FILE + " backed up successfully!", ColorSheetParser.COLORS_FILE + " backed up", JOptionPane.PLAIN_MESSAGE);
					else
						JOptionPane.showMessageDialog(ColorSettingsDialog.this, "Problem encountered while overwriting " + ColorSheetParser.BACKUP_COLORS_FILE + ".", ColorSheetParser.BACKUP_COLORS_FILE + " not overwritten!", JOptionPane.WARNING_MESSAGE);
				}
			});
			bottomPanel.add(button2);
			bottomPanel.add(new JLabel("Notice: The old backup file will be deleted!"));
			titlePanel.add(bottomPanel, BorderLayout.SOUTH);
			setupPanel.add(titlePanel);
		}		
		
		// Filters:
		{
			// Year:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Show only colors active between these years"));
			tfFromYear = new LividTextField(5);
			tfToYear = new LividTextField(5);
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
			filterPanel.add(titlePanel);
		}
		{
			// Quantities:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Show only colors that have appeared in at least"));
			tfMinSets = new LividTextField(5);
			tfMinParts = new LividTextField(5);
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
			filterPanel.add(titlePanel);
		}
		{
			// Exclusions:
			JPanel titlePanel = new JPanel(new GridLayout(4, 1));
			titlePanel.setBorder(BorderFactory.createTitledBorder("Additional color filters"));
			cbShowMetallic = new JCheckBox("Show copper/silver/gold/metallic/chrome colors");
			cbShowMetallic.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowMetallic(cbShowMetallic.isSelected(), ColorSettingsDialog.this);
				}
			});
			titlePanel.add(cbShowMetallic);
			cbShowTransparent = new JCheckBox("Show transparent colors");
			cbShowTransparent.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowTransparent(cbShowTransparent.isSelected(), ColorSettingsDialog.this);
				}
			});
			titlePanel.add(cbShowTransparent);
			cbShowOtherColorsGroup = new JCheckBox("Show other colors group (contains all remaining colors)");
			cbShowOtherColorsGroup.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowOtherColorsGroup(cbShowOtherColorsGroup.isSelected(), ColorSettingsDialog.this);
				}
			});
			titlePanel.add(cbShowOtherColorsGroup);
			cbShowOnlyLDD = new JCheckBox("Show only colors available in LDD");
			cbShowOnlyLDD.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShowOnlyLDD(cbShowOnlyLDD.isSelected(), ColorSettingsDialog.this);
				}
			});
			titlePanel.add(cbShowOnlyLDD);
			filterPanel.add(titlePanel);			
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
			cp.add(panel, BorderLayout.SOUTH);
		}
		
		tabs.addTab("Setup", setupPanel);
		tabs.addTab("Filters", filterPanel);
		cp.add(tabs, BorderLayout.CENTER);
		
		stateChanged(null);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		setTitle(DIALOG_TITLE + " - " + cc.getFilteredColors().size() + " of " + cc.getUnfilteredSize() + " colors shown");
		if(e != null && e.getSource() == this)
			return;
		
		//tfLoadRebrickableURL.setText(cc.getLoadRebrickableURL());
		tfLoadRebrickableFile.setText(cc.getLoadRebrickableFile());
		
		String fy = "" + cc.getFromYear();
		if(!tfFromYear.getText().trim().equals(fy))
			tfFromYear.setText(fy);
		String ty = "" + cc.getToYear();
		if(!tfToYear.getText().trim().equals(ty))
			tfToYear.setText(ty);
		String ms = "" + cc.getMinSets();
		if(!tfMinSets.getText().trim().equals(ms))
			tfMinSets.setText(ms);
		String mp = "" + cc.getMinParts();
		if(!tfMinParts.getText().trim().equals(mp))
			tfMinParts.setText(mp);
		
		cbShowOnlyLDD.setSelected(cc.getShowOnlyLDD());
		cbShowTransparent.setSelected(cc.getShowTransparent());
		cbShowOtherColorsGroup.setSelected(cc.getShowOtherColorsGroup());
		cbShowMetallic.setSelected(cc.getShowMetallic());
	}
}
