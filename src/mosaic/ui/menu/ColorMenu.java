package mosaic.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosaic.controllers.ColorController;
import mosaic.controllers.ColorController.ShownName;
import mosaic.ui.ColorSettingsDialog;
import mosaic.ui.actions.ShowColorOptionsAction;

public class ColorMenu extends JMenu implements ChangeListener {
	private static final long serialVersionUID = -6315804592857888262L;
	private JMenu colorTextMenu;
	private ColorController cc;
	
	public ColorMenu(ColorSettingsDialog csd, final ColorController cc) {
		super("Colors");
		this.cc = cc;
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
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(shownID.displayName);
			item.setMnemonic(shownID.displayName.charAt(0));
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
		
		updateColorTextMenu();
	}
	
	private void updateColorTextMenu() {
		// Color names:
		ButtonGroup names = new ButtonGroup();
		for(final ColorController.ShownName shownName : ColorController.ShownName.values()) {
			if(shownName.displayName == null)
				continue;
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(shownName.displayName);
			item.setMnemonic(shownName.displayName.charAt(0));
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
		if(e.getSource() != null && e.getSource() != this)
			updateColorTextMenu();
	}
}
