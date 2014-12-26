package mosaic.ui.menu;

import io.Model;
import javax.swing.*;
import ui.*;
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
		fileMenu.add(MosaicIO.createExportAction(model, mw));
		fileMenu.add(PrintAction.createPrintAction(model, mw));
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
		helpMenu.add(AboutDialog.createAction(mw, MainWindow.APP_NAME, MainWindow.APP_VERSION, Icons.floydSteinberg(64), Icons.floydSteinberg(Icons.SIZE_SMALL)));
		
		// this menu bar:
		add(fileMenu);
		add(viewMenu);
		add(new ColorMenu(csd, mw.getColorController()));
		add(new MagnifierMenu(bv.getMagnifierController()));
		add(helpMenu);
	}
}
