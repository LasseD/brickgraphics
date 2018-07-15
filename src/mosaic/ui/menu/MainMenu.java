package mosaic.ui.menu;

import javax.swing.*;
import icon.*;
import ui.AboutDialog;
import ui.actions.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.ui.*;
import mosaic.ui.actions.*;
import mosaic.ui.dialogs.ColorSettingsDialog;

public class MainMenu extends JMenuBar {
	public MainMenu(MainController mc, MainWindow mw, ColorSettingsDialog csd) {
		// File menu:
		JMenu fileMenu = new JMenu("File");
		fileMenu.setDisplayedMnemonicIndex(0);
		fileMenu.setMnemonic('F');
		fileMenu.add(MosaicIO.createOpenAction(mc, mw));
		fileMenu.add(MosaicIO.createSaveAction(mc, mw));
		fileMenu.add(MosaicIO.createSaveAsAction(mc, mw));
		fileMenu.add(new ExportLDR(mc, mw));
		fileMenu.add(new ExportLXF(mc, mw));
		fileMenu.add(MosaicIO.createSaveMosaicSnapshotAction(mc, mw));
		fileMenu.add(PrintController.createPrintAction(mc.getPrintDialog()));
		fileMenu.addSeparator();
		fileMenu.add(new ExitAction(mc.getModel()));

		// Edit menu:
		JMenu editMenu = new JMenu("Edit");
		editMenu.setDisplayedMnemonicIndex(0);
		editMenu.setMnemonic('E');
		editMenu.add(new ShowToBricksTypeFilterDialog(mc));
		editMenu.add(new ShowOptionsAction(mc.getOptionsController()));
		
		// View menu:
		JMenu viewMenu = new JMenu("View");
		viewMenu.setDisplayedMnemonicIndex(0);
		viewMenu.setMnemonic('V');
		ImagePreparingView ipv = mw.getImagePreparingView();
		viewMenu.add(new ToggleFilters(ipv));
		viewMenu.add(new ToggleCrop(ipv));
		viewMenu.add(new ToggleShowDividerLocationButton(mc.getToBricksController()));
		viewMenu.addSeparator();
		viewMenu.add(new ToggleColorChooser(mw.getColorChooser()));
		viewMenu.add(new ToggleMagnifier(mc.getUIController()));

		//Help menu:
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setDisplayedMnemonicIndex(0);
		helpMenu.setMnemonic('H');
		helpMenu.add(new HelpLinkAction(mw, MainController.HELP_URL));
		helpMenu.addSeparator();
		String appName = MainController.APP_NAME + " (" + MainController.APP_NAME_SHORT + ")";
		String appVersion = MainController.APP_VERSION;
		helpMenu.add(AboutDialog.createAction(mw, appName, appVersion, Icons.floydSteinberg(64), Icons.floydSteinberg(Icons.SIZE_SMALL)));
		
		// this menu bar:
		add(fileMenu);
		add(editMenu);
		add(viewMenu);
		add(new ColorMenu(csd, mc.getColorController()));
		add(new MagnifierMenu(mc.getMagnifierController(), mc.getUIController()));
		add(helpMenu);
	}
}
