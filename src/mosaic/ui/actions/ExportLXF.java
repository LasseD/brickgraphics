package mosaic.ui.actions;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import mosaic.controllers.*;
import icon.*;
import io.Log;
import mosaic.io.LXFPrinter;
import mosaic.ui.MainWindow;

public class ExportLXF extends AbstractAction {
	public static final String LXF_SUFFIX = "lxf";

	private MainController mc;
	private MainWindow mw;
	
	public ExportLXF(final MainController mc, MainWindow mw) {
		this.mc = mc;
		this.mw = mw;

		putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDD model.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "export_ldd", "->LDD"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "export_ldd", "->LDD"));
		putValue(Action.NAME, "Export mosaic to LDD");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Export mosaic to LDD".indexOf('x'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final FileFilter ff = new FileNameExtensionFilter("LDD model, ." + LXF_SUFFIX, LXF_SUFFIX);
		File file = mc.showSaveDialog("Export mosaic to LDD", ff);
		
		if(file != null) {			
			try {
				LXFPrinter.printTo(mc, mw, file);
				JOptionPane.showMessageDialog(mw, "LDD file exported sucessfully!", "File exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				Log.log(e1);
			}
		}
	}
}
