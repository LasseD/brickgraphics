package mosaic.io;

import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import io.*;
import icon.*;
import mosaic.controllers.MainController;
import mosaic.ui.MainWindow;

import java.util.*;

/**
 * @author LD
 */
public class MosaicIO {
	public static final String MOSAIC_SUFFIX = "kvm";
	public static final String[] HTML_SUFFIXES = {"htm", "html", "xhtml"};
	private static String[] IMG_SUFFIXES = null;

	public static void saveMosaic(Model<BrickGraphicsState> model, BufferedImage image, File file) throws IOException {
		if(image == null)
			throw new IllegalArgumentException("image null");
		model.saveToFile(file);
	}

	public static void saveImage(BufferedImage bricked, File file) throws IOException {
		ImageIO.write(bricked, suffix(file), file);		
	}

	public static void load(MainController mc, Model<BrickGraphicsState> changingModel, File file) throws IOException {
		FileType fileType = FileType.get(file);
		switch(fileType) {
		case mosaic:
			try {
				mc.loadMosaicFile(file);
			} catch (IOException e) {
				Log.log(e);
				return;
			}
			break;
		case img:
			BufferedImage read = ImageIO.read(file);
			//System.out.println(read);
			if(read.getType() == BufferedImage.TYPE_CUSTOM) {
				int w = read.getWidth();
				int h = read.getHeight();
				BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				int[] rgb = read.getRGB(0, 0, w, h, null, 0, w);
				copy.setRGB(0, 0, w, h, rgb, 0, w);
				read = copy;
			}
			mc.setImage(read, file);
			break;
		default:
			throw new IllegalStateException("Enum " + FileType.class + " broken: " + fileType);
		}
	}
	
	private static void ensureIMG_SUFFIXES() {
		if(IMG_SUFFIXES == null) 
			IMG_SUFFIXES = ImageIO.getReaderFileSuffixes();		
	}

	public static Action createOpenAction(final Model<BrickGraphicsState> currentModel, final MainController mc, final MainWindow mw) {
		final JFileChooser fileChooser = new JFileChooser();
		Action open = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File currentImage = mc.getFile();
				fileChooser.setCurrentDirectory(currentImage.getParentFile());
				List<String> suffixes = new LinkedList<String>();
				ensureIMG_SUFFIXES();
				for(String s : IMG_SUFFIXES)
					suffixes.add(s);
				suffixes.add(MOSAIC_SUFFIX);
				
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Images and mosaics", suffixes.toArray(new String[0]));
				fileChooser.setFileFilter(filter);
				fileChooser.setMultiSelectionEnabled(false);
				
				int retVal = fileChooser.showOpenDialog(mw);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						load(mc, currentModel, file);
					} catch (Exception e1) {
						String message = "An error ocurred while opening file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(mw, message, "Error when opening file", JOptionPane.ERROR_MESSAGE);
						Log.log(e1);
					}
				}
			}
		};

		open.putValue(Action.SHORT_DESCRIPTION, "Select picture to open and load.");
		open.putValue(Action.SMALL_ICON, Icons.get(16, "image"));
		open.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "image"));
		open.putValue(Action.NAME, "Open");
		open.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		open.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Open".indexOf('O'));
		open.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));

		return open;
	}

	public static Action createLDDXMLFileOpenAction(final JDialog parent, final JTextField tf) {
		final JFileChooser fileChooser = new JFileChooser();
		Action a = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ldraw.xml", "xml");
				fileChooser.setFileFilter(filter);
				fileChooser.setMultiSelectionEnabled(false);
				
				int retVal = fileChooser.showOpenDialog(parent);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					tf.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		};

		return a;
	}

	public static Action createHtmlFileOpenAction(final JDialog parent, final JTextField tf) {
		final JFileChooser fileChooser = new JFileChooser();
		Action a = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML file (website)", HTML_SUFFIXES);
				fileChooser.setFileFilter(filter);
				fileChooser.setMultiSelectionEnabled(false);
				
				int retVal = fileChooser.showOpenDialog(parent);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					tf.setText(fileChooser.getSelectedFile().getAbsolutePath());
				}
			}
		};

		return a;
	}

	public static Action createSaveAction(final Model<BrickGraphicsState> currentModel, final MainController mc, final MainWindow mw) {
		Action save = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = mc.getFile();
				file = ensureSuffix(file, MOSAIC_SUFFIX);
				try {
					saveMosaic(currentModel, mc.getInImage(), file);
				}
				catch(IOException ex) {
					String message = "An error ocurred while saving file " + file.getName() + "\n" + ex.getMessage();
					JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		save.putValue(Action.SHORT_DESCRIPTION, "Save the mosaic.");
		save.putValue(Action.SMALL_ICON, Icons.get(16, "filesave"));
		save.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "filesave"));
		save.putValue(Action.NAME, "Save");
		save.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		save.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Save".indexOf('S'));
		save.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

		return save;
	}
	
	public static File ensureSuffix(File file, String suffix) {
		if(!file.isDirectory() && suffix(file).equals(suffix)) {
			return file;
		}
		return new File(file.getParent(), file.getName() + "." + suffix);
	}

	public static Action createSaveAsAction(final Model<BrickGraphicsState> currentModel, final MainController mc, final MainWindow mw) {
		final FileFilter ff = new FileNameExtensionFilter("." + MOSAIC_SUFFIX, MOSAIC_SUFFIX);

		Action saveAs = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = mc.getSaveDialog().showSaveDialog("Save the mosaic file", ff);
				
				if(file != null) {
					file = ensureSuffix(file, MOSAIC_SUFFIX);
					
					try {
						saveMosaic(currentModel, mc.getInImage(), file);
						JOptionPane.showMessageDialog(mw,  "Mosaic file saved sucessfully!", "File saved",JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception e1) {
						String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(mw, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
					}	
				}
			}
		};
		saveAs.putValue(Action.SHORT_DESCRIPTION, "Save the mosaic to a given file.");
		saveAs.putValue(Action.SMALL_ICON, Icons.get(16, "filesaveas"));
		saveAs.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "filesaveas"));
		saveAs.putValue(Action.NAME, "Save As");
		saveAs.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		saveAs.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Save As".indexOf('A'));
		saveAs.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		
		return saveAs;
	}
	
	public static String suffix(File file) {
		if(file.isDirectory())
			throw new IllegalArgumentException("Directories don't have a suffix: " + file);
		String name = file.getName();
		int lastDotLocation = name.lastIndexOf('.');
		return name.substring(lastDotLocation+1);
	}

	private static enum FileType {
		mosaic, img;

		public static FileType get(File file) {
			String suffix = suffix(file).toLowerCase();
			if(suffix.equals(MOSAIC_SUFFIX)) {
				return mosaic;
			}
			else {
				ensureIMG_SUFFIXES();
				for(String s : IMG_SUFFIXES) {
					if(suffix.equals(s)) {
						return img;
					}
				}
				throw new IllegalArgumentException("Unknown file format: " + suffix);
			}
		}
	}
}
