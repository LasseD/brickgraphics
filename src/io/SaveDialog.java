package io;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import mosaic.io.MosaicIO;

/**
 * Save dialog with additional "are you sure" message when file is being overwritten.
 * @author LD
 */
public class SaveDialog {
	private Component toModalize;
	private JFileChooser fileChooser;
	
	public SaveDialog(Component toModalize) {
		this.toModalize = toModalize;
		fileChooser = new JFileChooser(new File("."));
		fileChooser.setMultiSelectionEnabled(false);
	}
	
	public void setParentFolder(File folder) {
		fileChooser.setCurrentDirectory(folder);
	}
	
	/**
	 * Shows a simple save dialog that asks user to overwrite existing file.
	 * @param saveMessage
	 * @return null if no file selected
	 */
	public File showSaveDialog(String saveMessage, FileFilter... filters) {
		for(FileFilter ff : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(ff);
		for(FileFilter ff : filters)
			fileChooser.addChoosableFileFilter(ff);
	
		int retVal = fileChooser.showSaveDialog(toModalize);
		if(retVal != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File file = fileChooser.getSelectedFile();
		FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser.getFileFilter();
		String type = fileFilter.getExtensions()[0];
		file = MosaicIO.ensureSuffix(file, type);
		if(file.exists()) {
			int ret = JOptionPane.showConfirmDialog(toModalize, "Warning. The file \n" + file.getName() + "\nalready exists. Overwrite?", "File already exists", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ret != JOptionPane.OK_OPTION)
				return null;
		}
		
		return file;
	}
}
