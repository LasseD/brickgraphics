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
import mosaic.io.MosaicIO;
import mosaic.ui.BrickedView;
import mosaic.ui.MainWindow;

public class ExportLDR extends AbstractAction {
	public static final String LDR_SUFFIX = "ldr";

	private MainWindow mw;	
	private JFileChooser fileChooser;
	
	public ExportLDR(final Model<BrickGraphicsState> currentModel, final MainWindow mw) {
		File currentImage = (File)currentModel.get(BrickGraphicsState.Image);
		fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		/*for(String imgType : ImageIO.getWriterFileSuffixes()) {
			if(imgType.equals("jpeg"))
				continue; // confuses when there is both jpeg and jpg.
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image, " + imgType, imgType));
		}*/
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("LDraw model, ." + LDR_SUFFIX, LDR_SUFFIX));

		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setName("Export LDR file");
		fileChooser.setSelectedFile(currentImage);
		this.mw = mw;

		putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDraw model.");
		putValue(Action.SMALL_ICON, Icons.get(16, "fileexport"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(32, "fileexport"));
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
		int retVal = fileChooser.showDialog(mw, "Export mosaic to LDraw");
		if(retVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser.getFileFilter();
			String type = fileFilter.getExtensions()[0];
			file = MosaicIO.ensureSuffix(file, type);
			
			try {
				saveLDR(mw.getBrickedView(), file);
				JOptionPane.showMessageDialog(mw, "File exported", "File exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
