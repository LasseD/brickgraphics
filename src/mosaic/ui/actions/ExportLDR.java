package mosaic.ui.actions;

import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import icon.*;
import io.Model;
import mosaic.io.BrickGraphicsState;
import mosaic.io.LDRPrinter;
import mosaic.ui.BrickedView;
import mosaic.ui.MainWindow;

public class ExportLDR extends AbstractAction {
	public static final String LDR_SUFFIX = "ldr";

	private MainWindow mw;	
	
	public ExportLDR(final Model<BrickGraphicsState> currentModel, final MainWindow mw) {
		this.mw = mw;

		putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDraw model.");
		putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "export_ldraw"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "export_ldraw"));
		putValue(Action.NAME, "Export mosaic to LDraw");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Export mosaic to LDraw".indexOf('E'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
	}

	private static void saveLDR(BrickedView brickedView, File file) throws IOException {
		new LDRPrinter(brickedView).printTo(file);		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final FileFilter ff = new FileNameExtensionFilter("LDraw model, ." + LDR_SUFFIX, LDR_SUFFIX);
		File file = mw.getSaveDialog().showSaveDialog("Export mosaic to LDraw", ff);
		
		if(file != null) {			
			try {
				saveLDR(mw.getBrickedView(), file);
				JOptionPane.showMessageDialog(mw, "LDraw file exported sucessfully!", "File exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
