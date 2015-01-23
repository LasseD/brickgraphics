package mosaic.ui.actions;

import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import icon.*;
import io.Model;
import mosaic.io.BrickGraphicsState;
import mosaic.io.LXFPrinter;
import mosaic.io.MosaicIO;
import mosaic.ui.MainWindow;

public class ExportLXF extends AbstractAction {
	public static final String LXF_SUFFIX = "lxf";

	private MainWindow mw;	
	private JFileChooser fileChooser;
	
	public ExportLXF(final Model<BrickGraphicsState> currentModel, final MainWindow mw) {
		File currentImage = (File)currentModel.get(BrickGraphicsState.Image);
		fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("LDD model, ." + LXF_SUFFIX, LXF_SUFFIX));

		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setName("Export LDD file");
		fileChooser.setSelectedFile(currentImage);
		this.mw = mw;

		putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDD model.");
		putValue(Action.SMALL_ICON, Icons.get(16, "fileexport"));
		putValue(Action.LARGE_ICON_KEY, Icons.get(32, "fileexport"));
		putValue(Action.NAME, "Export mosaic to LDD");
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Export mosaic to LDD".indexOf('x'));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		File currentImage = mw.getFile();
		fileChooser.setCurrentDirectory(currentImage.getParentFile());
		
		int retVal = fileChooser.showDialog(mw, "Export mosaic to LDD");
		if(retVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser.getFileFilter();
			String type = fileFilter.getExtensions()[0];
			file = MosaicIO.ensureSuffix(file, type);
			
			try {
				LXFPrinter.printTo(mw, file);
				JOptionPane.showMessageDialog(mw, "File exported", "File exported", JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception e1) {
				String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}
