package griddy.actions;

import griddy.*;
import java.io.*;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import io.*;
import icon.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * File formats: image.jpg, ...,
 *               image.griddy (real extension of img not shown), use Model.State.ImageType
 * @author ld
 */
public class IOActions {
	public static final String GRIDDY_SUFFIX = "griddy";
	public static final String[] IMG_SUFFIXES = ImageIO.getReaderFileSuffixes();

	public static void saveGriddy(Model<GriddyState> model, BufferedImage image, File file) throws IOException {
		if(image == null)
			throw new IllegalArgumentException("image is null");
		model.saveToFile(file);
	}

	public static void saveImage(BufferedImage bricked, File file) throws IOException {
		ImageIO.write(bricked, suffix(file), file);		
	}

	public static void load(Griddy parent, Model<GriddyState> changingModel, File file) throws IOException, ClassCastException {
		FileType fileType = FileType.get(file);
		switch(fileType) {
		case griddy:
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			//changingModel.loadFrom(ois); // TODO: Update!
			String fileNameType = suffix((File)changingModel.get(GriddyState.Image));			
			ImageInputStream iis = ImageIO.createImageInputStream(fis);
			
			Iterator<ImageReader> imageReaders = ImageIO.getImageReadersByFormatName(fileNameType);
			if(!imageReaders.hasNext())
				throw new IOException("No reader for file type: " + fileNameType);				

			ImageReader imageReader = imageReaders.next();
			imageReader.setInput(iis, true);
			ImageReadParam param = imageReader.getDefaultReadParam();
			BufferedImage img = imageReader.read(0, param);
						
			if(img == null)
				throw new IOException("Image not correctly read!");

			iis.close();
			ois.close();
			fis.close();
			parent.imageFileLoaded(img);
			break;
		case img:
			changingModel.set(GriddyState.Image, file);
			BufferedImage read = ImageIO.read(file);

			if(read.getType() == BufferedImage.TYPE_CUSTOM) {
				int w = read.getWidth();
				int h = read.getHeight();
				BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
				int[] rgb = read.getRGB(0, 0, w, h, null, 0, w);
				copy.setRGB(0, 0, w, h, rgb, 0, w);
				read = copy;
			}
			parent.imageFileLoaded(read);
			break;
		default:
			throw new IllegalStateException("Enum " + FileType.class + " broken: " + fileType);
		}
	}

	public static Action createLoadScreenAction(final Model<GriddyState> model, final Griddy parent) {
		Action load = new AbstractAction() {
			private static final long serialVersionUID = -475310013680255L;

			@Override
			public void actionPerformed(ActionEvent e) {				
				try {
					Rectangle r = parent.getBounds();
					Rectangle offScreen = new Rectangle();
					offScreen.x = Toolkit.getDefaultToolkit().getScreenSize().width + 1;
					parent.setBounds(offScreen); // No size.
					Rectangle screen = new Rectangle();
					screen.width = Toolkit.getDefaultToolkit().getScreenSize().width;
					screen.height = Toolkit.getDefaultToolkit().getScreenSize().height;
					JOptionPane.showMessageDialog(parent, "Press OK to load screenshot.", "Load screenshot", JOptionPane.PLAIN_MESSAGE);
					BufferedImage screenImage = new Robot().createScreenCapture(screen);
					parent.setBounds(r); // old size.

					String timeString = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
					model.set(GriddyState.Image, new File("screenshot_" + timeString + ".png"));
					if(screenImage.getType() == BufferedImage.TYPE_CUSTOM) {
						int w = screenImage.getWidth();
						int h = screenImage.getHeight();
						BufferedImage copy = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
						int[] rgb = screenImage.getRGB(0, 0, w, h, null, 0, w);
						copy.setRGB(0, 0, w, h, rgb, 0, w);
						screenImage = copy;
					}
					parent.imageFileLoaded(screenImage);					
				} catch (AWTException e1) {
					String message = "An error ocurred while loading screenshot " + e1.getMessage();
					JOptionPane.showMessageDialog(parent, message, "Error when loading screenshot", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		};

		load.putValue(Action.SHORT_DESCRIPTION, "Load screenshot as it is behind Griddy");
//		load.putValue(Action.SMALL_ICON, Icons.get(16, "image"));
//		load.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "image"));
		load.putValue(Action.NAME, "Load screenshot");
		load.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		load.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Load".indexOf('L'));
		load.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));

		return load;
	}
	
	public static Action createOpenAction(final Model<GriddyState> currentModel, final Griddy parent) {
		File currentImage = (File)currentModel.get(GriddyState.Image);
		final JFileChooser fileChooser = new JFileChooser(currentImage.getParentFile());
		List<String> suffixes = new LinkedList<String>();
		for(String s : IMG_SUFFIXES)
			suffixes.add(s);
		suffixes.add(GRIDDY_SUFFIX);
		
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Images and Griddy files", suffixes.toArray(new String[0]));
		fileChooser.setFileFilter(filter);
		fileChooser.setMultiSelectionEnabled(false);

		Action open = new AbstractAction() {
			private static final long serialVersionUID = -475310013680255L;

			@Override
			public void actionPerformed(ActionEvent e) {
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

	public static Action createSaveAction(final Model<GriddyState> currentModel, final Griddy parent) {
		Action save = new AbstractAction() {
			private static final long serialVersionUID = -208104007018089L;

			@Override
			public void actionPerformed(ActionEvent e) {
				File file = (File)currentModel.get(GriddyState.Image);
				file = ensureSuffix(file, GRIDDY_SUFFIX);
				try {
					saveGriddy(currentModel, parent.getImage(), file);
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

	public static Action createSaveAsAction(final Model<GriddyState> currentModel, final Griddy parent) {
		File currentImage = (File)currentModel.get(GriddyState.Image);
		final JFileChooser fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("." + GRIDDY_SUFFIX, GRIDDY_SUFFIX));
		fileChooser.setMultiSelectionEnabled(false);

		Action saveAs = new AbstractAction() {
			private static final long serialVersionUID = -48023768855816648L;

			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setSelectedFile(ensureSuffix((File)currentModel.get(GriddyState.Image), GRIDDY_SUFFIX));
				int retVal = fileChooser.showSaveDialog(parent);
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = ensureSuffix(fileChooser.getSelectedFile(), GRIDDY_SUFFIX);
					
					try {
						saveGriddy(currentModel, parent.getImage(), file);
						currentModel.set(GriddyState.Image, file);
					} catch (Exception e1) {
						String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};

		saveAs.putValue(Action.SHORT_DESCRIPTION, "Save to a given file.");
		saveAs.putValue(Action.SMALL_ICON, Icons.get(16, "filesaveas"));
		saveAs.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "filesaveas"));
		saveAs.putValue(Action.NAME, "Save As");
		saveAs.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		saveAs.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Save As".indexOf('A'));
		saveAs.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));

		return saveAs;
	}
	
	public static Action createExportAction(final Model<GriddyState> currentModel, final Griddy parent) {
		File currentImage = (File)currentModel.get(GriddyState.Image);
		final JFileChooser fileChooser = new JFileChooser(currentImage.getParentFile());
		
		for(FileFilter filter : fileChooser.getChoosableFileFilters())
			fileChooser.removeChoosableFileFilter(filter);
		for(String imgType : ImageIO.getWriterFileSuffixes()) {
			if(imgType.equals("jpeg"))
				continue; // confuses when there is both jpeg and jpg.
			fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Image, " + imgType, imgType));
		}
		
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setName("Save Screenshot");
		fileChooser.setSelectedFile(currentImage);

		Action export = new AbstractAction() {
			private static final long serialVersionUID = 66031427278213561L;

			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.setSelectedFile(((File)currentModel.get(GriddyState.Image)));
				int retVal = fileChooser.showDialog(parent, "Save Screenshot");
				if(retVal == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					FileNameExtensionFilter fileFilter = (FileNameExtensionFilter)fileChooser.getFileFilter();
					String type = fileFilter.getExtensions()[0];
					file = ensureSuffix(file, type);
					
					try {
						saveImage(parent.getFinalImage(), file);
					} catch (Exception e1) {
						String message = "An error ocurred while saving file " + file.getName() + "\n" + e1.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when saving file", JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		};

		export.putValue(Action.SHORT_DESCRIPTION, "Save Screenshot.");
		export.putValue(Action.SMALL_ICON, Icons.get(16, "fileexport"));
		export.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "fileexport"));
		export.putValue(Action.NAME, "Save Screenshot");
		export.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		export.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Save Screenshot".indexOf('e'));
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
		griddy, img;

		public static FileType get(File file) {
			String suffix = suffix(file).toLowerCase();
			if(suffix.equals(GRIDDY_SUFFIX)) {
				return griddy;
			}
			else {
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
