package mosaic.ui.actions;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import icon.*;
import io.Log;
import mosaic.io.LDRPrinter;
import mosaic.ui.BrickedView;
import mosaic.ui.MainWindow;
import mosaic.controllers.*;

public class ExportLDR extends AbstractAction {
	public static final String LDR_SUFFIX = "ldr";

	private MainController mc;
	private MainWindow mw;
	
	public ExportLDR(final MainController mc, MainWindow mw) {
		this.mc = mc;
		this.mw = mw;

		putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDraw model.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "export_ldraw", "->LDRAW"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "export_ldraw", "->LDRAW"));
		putValue(Action.NAME, "Export mosaic to LDraw");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Export mosaic to LDraw".indexOf('E'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
	}

	private void saveLDR(BrickedView brickedView, File file) throws IOException {
		new LDRPrinter(mc, brickedView).printTo(file);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final FileFilter ff = new FileNameExtensionFilter("LDraw model, ." + LDR_SUFFIX, LDR_SUFFIX);
		File file = mc.showSaveDialog("Export mosaic to LDraw", ff);
		
		if(file != null) {			
			try {
				saveLDR(mw.getBrickedView(), file);
				JOptionPane.showMessageDialog(mw, "LDraw file exported sucessfully!", "File exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				Log.log(e1);
			}
		}
	}
}
