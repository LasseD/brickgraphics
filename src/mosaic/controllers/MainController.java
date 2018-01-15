package mosaic.controllers;

import io.*;

import java.awt.image.*;
import java.io.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.parsers.*;
import mosaic.io.*;
import mosaic.rendering.Pipeline;
import mosaic.rendering.RenderingProgressBar;
import mosaic.ui.*;
import mosaic.ui.dialogs.PrintDialog;
import mosaic.ui.dialogs.ToBricksTypeFilterDialog;

/**
 * @author LD
 */
public class MainController implements ModelHandler<BrickGraphicsState> {
	public static final String APP_NAME = "LD Digital Mosaic Creator";
	public static final String APP_NAME_SHORT = "LDDMC";
	public static final String LOG_FILE_NAME = "lddmc.log";
	public static final String STATE_FILE_NAME = "lddmc.kvm";
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 9;
	public static final int VERSION_MICRO = 4;
	public static final String APP_VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_MICRO;
	public static final String HELP_URL = "http://c-mt.dk/software/lddmc/help";
	
	private Model<BrickGraphicsState> model;
	private Pipeline pipeline;
	private List<ChangeListener> listeners;

	private MagnifierController magnifierController;
	private UIController uiController;
	private ColorController colorController;
	private PrintController printController;
	private ToBricksController toBricksController;
	private OptionsController optionsController;	
	
	private MainWindow mw;
	private SaveDialog saveDialog;
	private PrintDialog printDialog;
	private ToBricksTypeFilterDialog toBricksTypeFilterDialog;
	private ColorLegend legend;
	
	// Image (for model state):
	private String imageFileName;
	private DataFile imageDataFile;
	private File mosaicFile;

	private MainController() {		
		listeners = new ArrayList<ChangeListener>();		
		mosaicFile = null;

		try {
			Log.initializeLog(LOG_FILE_NAME);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "The log file " + LOG_FILE_NAME + " could not be opened for writing.\nLDDMC might not have sufficient permissions.\nLog messages are written to console if available.\nThe error message:\n" + e1.getMessage(), "Failed to open/create log file", JOptionPane.WARNING_MESSAGE);
		}
		long startTime = System.currentTimeMillis();
		Log.log("Initiating components");
		model = new Model<BrickGraphicsState>(STATE_FILE_NAME, BrickGraphicsState.class);
		
		RenderingProgressBar renderingProgressBar = new RenderingProgressBar();
		pipeline = new Pipeline(renderingProgressBar);
		Log.log("Model file loaded");
		handleModelChange(model);
		
		// Set up controllers:
		optionsController = new OptionsController(model);
		colorController = new ColorController(model);
		uiController = new UIController(model);
		magnifierController = new MagnifierController(model, uiController);
		toBricksController = new ToBricksController(this, model);
		printController = new PrintController(model, this, pipeline);		
		Log.log("Created controllers after " + (System.currentTimeMillis()-startTime) + "ms.");

		// Set up UI:
		legend = new ColorLegend(this, pipeline);
		mw = new MainWindow(this, model, pipeline, renderingProgressBar);
		printController.setMainWindow(mw);
		legend.setBrickedView(mw.getBrickedView());
		listeners.add(mw);
		toBricksController.initiateUI(mw);
		optionsController.initiateOptionsDialog(mw);
		model.addModelHandler(this); // Make sure the load of image file is late in the process when loading a model.
		Log.log("LDDMC main window operational after " + (System.currentTimeMillis()-startTime) + "ms.");

		if(colorController.usesBackupColors()) {
			JOptionPane.showMessageDialog(mw, "The file " + ColorSheetParser.COLORS_FILE + " could not be read.\nBackup colors are used.\nTo get all the functionality of this program, please make sure that the file exists and that the program is allowed to read the file.\nThis also applies to the other files and folders of the program.", "Error reading file", JOptionPane.WARNING_MESSAGE);
		}
		
		if(!imageDataFile.isValid()) {
			try {
				MosaicIO.load(this, new File(imageFileName));
			}
			catch (Exception e) {
				Log.log(e);
				Action openAction = MosaicIO.createOpenAction(this, mw);
				openAction.actionPerformed(null);
			}				
		}
		else
			notifyListeners(model);
		
		saveDialog = new SaveDialog(mw);
		printDialog = new PrintDialog(mw, printController, colorController, magnifierController, pipeline);
		toBricksTypeFilterDialog = new ToBricksTypeFilterDialog(toBricksController, mw);
		
		pipeline.start();
		Log.log("MainController initiated.");
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					Log.log(e);
				}
				new MainController();
			}
		});
	}
	
	private void notifyListeners(Object source) {
		ChangeEvent e = new ChangeEvent(source);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	public void setImage(BufferedImage image, File imageFile) throws IOException {
		if(imageFile == null) {
			imageDataFile = new DataFile(image);
			imageFileName = "New Image";
		}
		else {
			imageDataFile = new DataFile(imageFile);		
			imageFileName = imageFile.getCanonicalPath();			
		}
		mosaicFile = null;
		pipeline.setStartImage(image);
		notifyListeners(this);
	}
	
	public void loadMosaicFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		model.loadFrom(br);
		br.close();
		fis.close();
		
		mosaicFile = file;
		notifyListeners(model);
	}
	
	public File getMosaicFile() {
		return mosaicFile;
	}
	
	public void setMosaicFile(File file) {
		this.mosaicFile = file;
	}
	
	public File showSaveDialog(String saveMessage, FileFilter... filters) {		
		return saveDialog.showSaveDialog(saveMessage, filters);
	}
	
	public void showToBricksTypeFilterDialog() {
		toBricksTypeFilterDialog.pack();
		toBricksTypeFilterDialog.setVisible(true);
	}
	
	public File getFile() {
		if(imageFileName == null)
			return null;
		return new File(imageFileName);
	}
	
	public Model<BrickGraphicsState> getModel() {
		return model;
	}
	
	public ColorController getColorController() {
		return colorController;
	}
	
	public PrintController getPrintController() {
		return printController;
	}
	
	public MagnifierController getMagnifierController() {
		return magnifierController;
	}

	public UIController getUIController() {
		return uiController;
	}
	
	public OptionsController getOptionsController() {
		return optionsController;
	}

	public ToBricksController getToBricksController() {
		return toBricksController;
	}

	public ColorLegend getLegend() {
		return legend;
	}
	
	public PrintDialog getPrintDialog() {
		return printDialog;
	}
	
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		imageFileName = (String)model.get(BrickGraphicsState.ImageFileName);
		imageDataFile = (DataFile)model.get(BrickGraphicsState.ImageFile);
		if(imageDataFile.isValid()) {
			try {
				BufferedImage image = MosaicIO.removeAlpha(ImageIO.read(imageDataFile.fakeStream()));
				pipeline.setStartImage(image);
			} catch (IOException e) {
				Log.log(e);
			}			
		}
		else
			Log.log("INVALID Image data file!");
	}

	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.ImageFileName, imageFileName);
		model.set(BrickGraphicsState.ImageFile, imageDataFile);
	}
}
