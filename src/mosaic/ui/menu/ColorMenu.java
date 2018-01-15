package mosaic.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosaic.controllers.ColorController;
import mosaic.controllers.ColorController.ShownName;
import mosaic.ui.actions.ShowColorOptionsAction;
import mosaic.ui.dialogs.ColorSettingsDialog;

public class ColorMenu extends JMenu implements ChangeListener {
	private JMenu colorTextMenu;
	private ColorController cc;
	private String[] lastSeenLocalizedNames;
	private ArrayList<JRadioButtonMenuItem> colorNameButtons;
	private ArrayList<JRadioButtonMenuItem> colorIDButtons;
	
	public ColorMenu(ColorSettingsDialog csd, final ColorController cc) {
		super("Colors");
		this.cc = cc;
		lastSeenLocalizedNames = new String[]{};
		colorNameButtons = new ArrayList<JRadioButtonMenuItem>();
		colorIDButtons = new ArrayList<JRadioButtonMenuItem>();
		cc.addChangeListener(this);
		setDisplayedMnemonicIndex(1);
		setMnemonic('o');
		add(new ShowColorOptionsAction(csd));
		addSeparator();

		JMenu colorNumberMenu = new JMenu("Shown color number");
		colorNumberMenu.setMnemonic('S');
		colorNumberMenu.setDisplayedMnemonicIndex(0);
		
		// Color indices:
		ButtonGroup ids = new ButtonGroup();
		for(final ColorController.ShownID shownID : ColorController.ShownID.values()) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(shownID.toString());
			colorIDButtons.add(item);			
			item.setMnemonic(shownID.toString().charAt(0));
			item.setDisplayedMnemonicIndex(0);
			if(cc.getShownID() == shownID)
				item.setSelected(true);
			item.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShownID(shownID);
				}
			});
			ids.add(item);
			colorNumberMenu.add(item);
		}
		add(colorNumberMenu);

		colorTextMenu = new JMenu("Shown color text");
		colorTextMenu.setMnemonic('h');
		colorTextMenu.setDisplayedMnemonicIndex(1);
		add(colorTextMenu);
		
		updateColorTextMenu(true);
		updateSelectedItems();
	}
	
	private void updateSelectedItems() {
		// ID's:
		ColorController.ShownID shownID = cc.getShownID();
		colorIDButtons.get(shownID.ordinal()).setSelected(true);
		
		// Names:
		ColorController.ShownName shownName = cc.getShownName();
		if(cc.getShownName() != ShownName.LOCALIZED) {
			colorNameButtons.get(shownName.ordinal()).setSelected(true);
			return;
		}
		int i = ColorController.ShownName.values().length-1;
		for(final String localizationName : cc.getLocalizedFileNamesNoTXT()) {
			if(cc.getLocalizedFileNameNoTXT().equals(localizationName)) {
				colorNameButtons.get(i).setSelected(true);
				break;
			}
			++i;
		}		
	}
	
	private void updateColorTextMenu(boolean force) {
		// Only change if files have changed!
		boolean same = true;
		String[] newLocalizedNames = cc.getLocalizedFileNamesNoTXT();
		if(lastSeenLocalizedNames.length != newLocalizedNames.length)
			same = false;
		else {
			for(int i = 0; i < lastSeenLocalizedNames.length; ++i) {
				if(!lastSeenLocalizedNames[i].equals(newLocalizedNames[i])) {
					same = false;
					break;
				}
			}
		}
		if(same && !force)
			return;
		lastSeenLocalizedNames = newLocalizedNames;
		
		colorNameButtons.clear();
		colorTextMenu.removeAll(); // Reload everything because a new file in the translations directory might have been added.
		// Color names:
		ButtonGroup names = new ButtonGroup();
		for(final ColorController.ShownName shownName : ColorController.ShownName.values()) {
			if(shownName.toString() == null)
				continue;
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(shownName.toString());
			colorNameButtons.add(item);
			item.setMnemonic(shownName.toString().charAt(0));
			item.setDisplayedMnemonicIndex(0);
			if(cc.getShownName() == shownName)
				item.setSelected(true);
			item.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShownName(shownName);
				}
			});
			names.add(item);
			colorTextMenu.add(item);
		}
		for(final String localizationName : cc.getLocalizedFileNamesNoTXT()) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(localizationName);
			colorNameButtons.add(item);
			if(cc.getShownName() == ShownName.LOCALIZED && cc.getLocalizedFileNameNoTXT().equals(localizationName))
				item.setSelected(true);
			item.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					cc.setShownName(ShownName.LOCALIZED);
					cc.setLocalizedFileNameNoTXT(localizationName, ColorMenu.this);
				}
			});
			names.add(item);
			colorTextMenu.add(item);			
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateColorTextMenu(false);
		updateSelectedItems();
	}
}
