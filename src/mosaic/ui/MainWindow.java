package mosaic.ui;

import icon.*;
import io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.controllers.PrintController;
import mosaic.ui.menu.*;
import mosaic.ui.prepare.*;

/**
 * @author ld
 */
public class MainWindow extends JFrame implements ChangeListener, ModelSaver<BrickGraphicsState> {
	public static final String APP_NAME = "LD Digital Mosaic Creator";
	public static final String APP_VERSION = "0.9.1";
	public static final String HELP_URL = "http://c-mt.dk/software/lddmc/help";
	
	private ImagePreparingView imagePreparingView;
	private BrickedView brickedView;
	private BufferedImage image;
	private JSplitPane splitPane;
	private Model<BrickGraphicsState> model;

	private MagnifierController magnifierController;
	private ColorController colorController;
	private PrintController printController;

	public MainWindow() {
		super(APP_NAME);
		long startTime = System.currentTimeMillis();
		System.out.println("Initiating components");
		model = new Model<BrickGraphicsState>("lddmc.state", BrickGraphicsState.class);
		model.addModelSaver(this);
		
		colorController = ColorController.instance(model);
		magnifierController = new MagnifierController(model);
		printController = new PrintController(model, this);
		
		System.out.println("Created controllers after " + (System.currentTimeMillis()-startTime) + "ms.");

		imagePreparingView = new ImagePreparingView(model);		
		imagePreparingView.addChangeListener(this);
		System.out.println("Created left view after " + (System.currentTimeMillis()-startTime) + "ms.");
		
		brickedView = new BrickedView(this, model, magnifierController, colorController);
		System.out.println("Created right view after " + (System.currentTimeMillis()-startTime) + "ms.");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					model.saveToFile();
				}
				catch (IOException e2) {
					e2.printStackTrace();
				}
				System.exit(0);
			}
		});
		getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			@Override
			public void ancestorMoved(HierarchyEvent e) {
				updateModel();				
			}
			@Override
			public void ancestorResized(HierarchyEvent e) {
				updateModel();				
			}			
			private void updateModel() {
				int state = MainWindow.this.getExtendedState();
				if((state | Frame.NORMAL) == Frame.NORMAL) {
					model.set(BrickGraphicsState.MainWindowPlacement, MainWindow.this.getBounds());
				}
				else if((state | Frame.MAXIMIZED_HORIZ) == Frame.MAXIMIZED_HORIZ) {
					Rectangle inModel = (Rectangle)model.get(BrickGraphicsState.MainWindowPlacement);
					Rectangle bounds = MainWindow.this.getBounds();
					inModel.y = bounds.y;
					inModel.height = bounds.height;
				}
				else if((state | Frame.MAXIMIZED_VERT) == Frame.MAXIMIZED_VERT) {
					Rectangle inModel = (Rectangle)model.get(BrickGraphicsState.MainWindowPlacement);
					Rectangle bounds = MainWindow.this.getBounds();
					inModel.x = bounds.x;
					inModel.width = bounds.width;
				}
			}
		});

		// in split pane:
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePreparingView, brickedView);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(16);
		splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				imagePreparingView.setVisible(splitPane.getDividerLocation() >= getMinDividerLocation());
				brickedView.setVisible(splitPane.getDividerLocation() <= getMaxDividerLocation());
				brickedView.getToolBar().update();
			}
		});
		
		setLayout(new BorderLayout());

		// Preparing view tool bar:
		JPanel pPrepairToolBar = new JPanel();
		ImagePreparingToolBar prepareToolBar = imagePreparingView.getToolBar();
		pPrepairToolBar.add(prepareToolBar);
		add(pPrepairToolBar, BorderLayout.WEST);

		add(splitPane, BorderLayout.CENTER);
		//add(Log.makeStatusBar(), BorderLayout.SOUTH);

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				ColorSettingsDialog csd = new ColorSettingsDialog(MainWindow.this, colorController);

				Ribbon ribbon = new Ribbon(MainWindow.this);
				brickedView.getToolBar().addComponents(ribbon, true);		
				add(ribbon, BorderLayout.NORTH);
				setJMenuBar(new MainMenu(MainWindow.this, model, csd));
				setIconImage(Icons.get(32, "icon").getImage());
				new MagnifierWindow(MainWindow.this, magnifierController, colorController); // Do your own thing little window.
			}
		});

		System.out.println("LDDMC main window operational after " + (System.currentTimeMillis()-startTime) + "ms.");
	}
	
	public int getMinDividerLocation() {
		return Math.max(32, imagePreparingView.getPreferredSize().width);
	}
	
	public int getMaxDividerLocation() {
		return splitPane.getSize().width - splitPane.getDividerSize() - Math.max(32, brickedView.getPreferredSize().width) - 2;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				MainWindow mw = newWindow();
				Action openAction = MosaicIO.createOpenAction(mw.model, mw);
				try {
					File file = (File)mw.model.get(BrickGraphicsState.Image);
					MosaicIO.load(mw, mw.model, file);
				}
				catch (Exception e) {
					e.printStackTrace();
					openAction.actionPerformed(null);
				} 			
			}
		});
	}
	
	private static MainWindow newWindow() {
		MainWindow mw = new MainWindow();
		Rectangle placement = (Rectangle)mw.model.get(BrickGraphicsState.MainWindowPlacement);
		mw.setBounds(placement);
		mw.setVisible(true);
		mw.splitPane.setDividerLocation((Integer)mw.model.get(BrickGraphicsState.MainWindowDividerLocation));
		return mw;
	}
	
	public void softReset() {
		try {
			model.saveToFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setVisible(false);
		dispose();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				newWindow();
			}
		});
	}

	public void mosaicLoaded(BufferedImage image) {
		this.image = image;
		imagePreparingView.loadModel(model);
		imagePreparingView.setImage(image);				
		
		BufferedImage toBrick = imagePreparingView.getFullyPreparredImage();

		brickedView.reloadModel(model);
		brickedView.setImage(toBrick);

		if(splitPane != null)
			repaint();
	}
	
	public String getFileName() {
		return ((File)model.get(BrickGraphicsState.Image)).getName();
	}

	public BufferedImage getInImage() {
		return image;
	}
	
	public BrickedView getBrickedView() {
		if(brickedView == null)
			throw new IllegalStateException();		
		return brickedView;
	}
	
	public ImagePreparingView getImagePreparingView() {
		if(imagePreparingView == null)
			throw new IllegalStateException();
		return imagePreparingView;
	}
	
	public JSplitPane getSplitPane() {
		if(splitPane == null)
			throw new IllegalStateException();
		return splitPane;
	}
	
	public BufferedImage getFinalImage() {
		return brickedView.getMagnifierController().getCoreImageInCoreUnits();
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

	@Override
	public void stateChanged(ChangeEvent e) {
		File file = (File)model.get(BrickGraphicsState.Image);
		setTitle(APP_NAME + " - " + file.getName());
		if(image == null)
			return;
		BufferedImage toBrick = imagePreparingView.getFullyPreparredImage();
		brickedView.setImage(toBrick);
		repaint();
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MainWindowDividerLocation, splitPane.getDividerLocation());
		model.set(BrickGraphicsState.MainWindowPlacement, new Rectangle(getLocation().x, getLocation().y, getWidth(), getHeight()));
	}
}
