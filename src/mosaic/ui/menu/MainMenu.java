package mosaic.ui.menu;

import io.Model;
import javax.swing.*;
import icon.*;
import ui.AboutDialog;
import ui.actions.*;
import mosaic.io.*;
import mosaic.ui.*;
import mosaic.ui.actions.*;
import mosaic.ui.prepare.*;

public class MainMenu extends JMenuBar {
	private static final long serialVersionUID = 3921145264492575680L;

	public MainMenu(MainWindow mw, Model<BrickGraphicsState> model, ColorSettingsDialog csd) {
		// File menu:
		JMenu fileMenu = new JMenu("File");
		fileMenu.setDisplayedMnemonicIndex(0);
		fileMenu.setMnemonic('F');
		fileMenu.add(MosaicIO.createOpenAction(model, mw));
		fileMenu.add(MosaicIO.createSaveAction(model, mw));
		fileMenu.add(MosaicIO.createSaveAsAction(model, mw));
		fileMenu.add(new ExportLDR(model, mw));
		fileMenu.add(new ExportLXF(model, mw));
		fileMenu.add(mw.getPrintController().createPrintAction());
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
		viewMenu.add(new ToggleColorChooser(bv.getToolBar().getColorChooser()));
		viewMenu.add(new ToggleMagnifier(bv.getMagnifierController()));

		//Help menu:
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setDisplayedMnemonicIndex(0);
		helpMenu.setMnemonic('H');
		helpMenu.add(new HelpLinkAction(mw, MainWindow.HELP_URL));
		helpMenu.addSeparator();
		String appName = MainWindow.APP_NAME + " (" + MainWindow.APP_NAME_SHORT + ")";
		String appVersion = MainWindow.APP_VERSION;
		helpMenu.add(AboutDialog.createAction(mw, appName, appVersion, Icons.floydSteinberg(64), Icons.floydSteinberg(Icons.SIZE_SMALL)));
		
		// this menu bar:
		add(fileMenu);
		add(viewMenu);
		add(new ColorMenu(csd, mw.getColorController()));
		add(new MagnifierMenu(bv.getMagnifierController()));
		add(helpMenu);
	}
}
