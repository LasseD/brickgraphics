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
import mosaic.ui.*;
import bricks.LDRPrinter;
import java.util.*;

/**
 * File formats: image.jpg, ...,
 * 	             image.ldr
 *               image.mosaic (real extension of img not shown), use Model.State.ImageType
 * @author ld
 */
public class MosaicIO {
	public static final String MOSAIC_SUFFIX = "mosaic";
	public static final String[] HTML_SUFFIXES = {"htm", "html", "xhtml"};
	public static final String LDR_SUFFIX = "ldr";
	private static String[] IMG_SUFFIXES = null;

	public static void saveMosaic(Model<BrickGraphicsState> model, BufferedImage image, File file) throws IOException {
		if(image == null)
			throw new IllegalArgumentException("image null");
		FileOutputStream fos = new FileOutputStream(file, false);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		model.saveTo(oos);
		ImageIO.write(image, suffix((File)model.get(BrickGraphicsState.Image)), fos);
		oos.close();
		fos.close();
	}

	public static void saveLDR(BrickedView brickedView, File file) throws IOException {
		new LDRPrinter(brickedView).printTo(file);		
	}

	public static void saveImage(BufferedImage bricked, File file) throws IOException {
		ImageIO.write(bricked, suffix(file), file);		
	}

	public static void load(MainWindow parent, Model<BrickGraphicsState> changingModel, File file) throws IOException, ClassCastException, ClassNotFoundException {
		FileType fileType = FileType.get(file);
		switch(fileType) {
		case mosaic:
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			changingModel.loadFrom(ois);
			BufferedImage img = ImageIO.read(fis);			
			ois.close();
			fis.close();
			parent.mosaicLoaded(img);
			break;
		case ldr:
			throw new IOException("Cannont load ldr files. Only export to them.");
		case img:
			changingModel.set(BrickGraphicsState.Image, file);
			//changingModel.set(BrickGraphicsState.ImageType, suffix(file));
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
			parent.mosaicLoaded(read);
			break;
		default:
			throw new IllegalStateException("Enum " + FileType.class + " broken: " + fileType);
		}
	}
	
	private static void ensureIMG_SUFFIXES() {
		if(IMG_SUFFIXES == null) 
			IMG_SUFFIXES = ImageIO.getReaderFileSuffixes();		
	}

	public static Action createOpenAction(final Model<BrickGraphicsState> currentModel, final MainWindow parent) {
		final JFileChooser fileChooser = new JFileChooser();
		Action open = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File currentImage = (File)currentModel.get(BrickGraphicsState.Image);
				fileChooser.setCurrentDirectory(currentImage.getParentFile());
				List<String> suffixes = new LinkedList<String>();
				ensureIMG_SUFFIXES();
				for(String s : IMG_SUFFIXES)
					suffixes.add(s);
				suffixes.add(MOSAIC_SUFFIX);
				
				FileNameExtensionFilter filter = new FileNameExtensionFilter("Images and mosaics", suffixes.toArray(new String[0]));
				fileChooser.setFileFilter(filter);
				fileChooser.setMultiSelectionEnabled(false);
				
				int retVal = fileChooser.showOpenDialog(parent);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						load(parent, currentModel, file);
					} catch (Exception e1) {
						String message = "An error ocurred while opening file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when opening file", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
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

	public static Action createHtmlFileOpenAction(final JDialog parent, final JTextField tf) {
		final JFileChooser fileChooser = new JFileChooser();
		Action a = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setCurrentDirectory(null);
				
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

	public static Action createSaveAction(final Model<BrickGraphicsState> currentModel, final MainWindow parent) {
		Action save = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				File file = (File)currentModel.get(BrickGraphicsState.Image);
				file = ensureSuffix(file, MOSAIC_SUFFIX);
				try {
					saveMosaic(currentModel, parent.getInImage(), file);
				}
				catch(IOException ex) {
					String message = "An error ocurred while saving file " + file.getName() + "\n" + ex.getMessage();
					JOptionPane.showMessageDialog(parent, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
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
	
	private static File ensureSuffix(File file, String suffix) {
		if(!file.isDirectory() && suffix(file).equals(suffix))
			return file;
		return new File(file.getParent(), file.getName() + "." + suffix);
	}

	public static Action createSaveAsAction(final Model<BrickGraphicsState> currentModel, final MainWindow parent) {
		File currentImage = (File)currentModel.get(BrickGraphicsState.Image);
		final JFileChooser fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("." + MOSAIC_SUFFIX, MOSAIC_SUFFIX));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setSelectedFile(ensureSuffix(currentImage, MOSAIC_SUFFIX));

		Action saveAs = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retVal = fileChooser.showSaveDialog(parent);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = ensureSuffix(fileChooser.getSelectedFile(), MOSAIC_SUFFIX);
					
					try {
						saveMosaic(currentModel, parent.getInImage(), file);
						currentModel.set(BrickGraphicsState.Image, file);
					} catch (Exception e1) {
						String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
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
	
	public static Action createExportAction(final Model<BrickGraphicsState> currentModel, final MainWindow parent) {
		File currentImage = (File)currentModel.get(BrickGraphicsState.Image);
		final JFileChooser fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		/*for(String imgType : ImageIO.getWriterFileSuffixes()) {
			if(imgType.equals("jpeg"))
				continue; // confuses when there is both jpeg and jpg.
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image, " + imgType, imgType));
		}*/
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Model, ." + LDR_SUFFIX, LDR_SUFFIX));

		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setName("Export");
		fileChooser.setSelectedFile(currentImage);

		Action export = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retVal = fileChooser.showDialog(parent, "Export mosaic to LDraw");
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser.getFileFilter();
					String type = fileFilter.getExtensions()[0];
					file = ensureSuffix(file, type);
					
					try {
						if(type.equals(LDR_SUFFIX)) {
							saveLDR(parent.getBrickedView(), file);
						}
						else {
							saveImage(parent.getFinalImage(), file);
						}
					} catch (Exception e1) {
						String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		};

		export.putValue(Action.SHORT_DESCRIPTION, "Export mosaic as an LDraw model.");
		export.putValue(Action.SMALL_ICON, Icons.get(16, "fileexport"));
		export.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "fileexport"));
		export.putValue(Action.NAME, "Export mosaic to LDraw");
		export.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		export.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Export mosaic to LDraw".indexOf('E'));
		export.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));

		return export;
	}

	public static String suffix(File file) {
		if(file.isDirectory())
			throw new IllegalArgumentException("Directories don't have a suffix: " + file);
		String name = file.getName();
		int lastDotLocation = name.lastIndexOf('.');
		return name.substring(lastDotLocation+1);
	}

	private static enum FileType {
		mosaic, ldr, img;

		public static FileType get(File file) {
			String suffix = suffix(file).toLowerCase();
			if(suffix.equals(MOSAIC_SUFFIX)) {
				return mosaic;
			}
			else if(suffix.equals(LDR_SUFFIX)) {
				return ldr;
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
