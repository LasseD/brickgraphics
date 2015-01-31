package mosaic.ui.menu;

import io.Model;
import javax.swing.*;
import icon.*;
import ui.AboutDialog;
import ui.actions.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.ui.*;
import mosaic.ui.actions.*;

public class MainMenu extends JMenuBar {
	private static final long serialVersionUID = 3921145264492575680L;

	public MainMenu(MainController mc, MainWindow mw, Model<BrickGraphicsState> model, ColorSettingsDialog csd) {
		// File menu:
		JMenu fileMenu = new JMenu("File");
		fileMenu.setDisplayedMnemonicIndex(0);
		fileMenu.setMnemonic('F');
		fileMenu.add(MosaicIO.createOpenAction(model, mc, mw));
		fileMenu.add(MosaicIO.createSaveAction(model, mc, mw));
		fileMenu.add(MosaicIO.createSaveAsAction(model, mc, mw));
		fileMenu.add(new ExportLDR(model, mc, mw));
		fileMenu.add(new ExportLXF(model, mc, mw));
		fileMenu.add(mc.getPrintController().createPrintAction());
		fileMenu.addSeparator();
		fileMenu.add(new ExitAction(model));

		// View menu:
		JMenu viewMenu = new JMenu("View");
		viewMenu.setDisplayedMnemonicIndex(0);
		viewMenu.setMnemonic('V');
		ImagePreparingView ipv = mw.getImagePreparingView();
		BrickedView bv = mw.getBrickedView();
		viewMenu.add(new ToggleCrop(ipv));
		viewMenu.add(new ToggleFilters(ipv));
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
		add(viewMenu);
		add(new ColorMenu(csd, mc.getColorController()));
		add(new MagnifierMenu(bv.getMagnifierController(), mc.getUIController()));
		add(helpMenu);
	}
}
