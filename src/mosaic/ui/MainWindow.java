package mosaic.ui;

import io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;

import ui.Icons;

import mosaic.io.BrickGraphicsState;
import mosaic.io.MosaicIO;
import mosaic.ui.bricked.*;
import mosaic.ui.prepare.*;

/**
 * Java Hints: 142,355 sec.
 * First own impl: 1-3 sec. (Use this...)
 *
 * Left: Size, crop, (sharpness, gamma, brightness, contrast, saturation)
 *		 file:
 *		 v open 
 *		 s save (brickType, widthType, width, heightType, height, colors)
 *		 - print instructions
 *		 Magnifier:
 *		 v show
 *		 v set size
 *		 v color codes
 *		 help:
 *		 s Manual (Open link)
 *		 s about (JDialog w. JLabel w. text)
 */
public class MainWindow extends JFrame implements ChangeListener, ModelSaver<BrickGraphicsState>, Dialogs.DialogListener, WindowListener {
	private static final long serialVersionUID = 4819662761398560128L;
	private BrickedView brickedView;
	private ImagePreparingView imagePreparingView;
	private BufferedImage image;
	private JSplitPane splitPane;
	private Model<BrickGraphicsState> model;

	public MainWindow() {
		super("Brick Graphics Mosaic");
		System.out.println("Initiating components");
		setIconImage(Icons.get(32, "icon").getImage());
		model = new Model<BrickGraphicsState>("BrickGraphics.state", BrickGraphicsState.class);
		addWindowListener(this);
		getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener(){
			public void ancestorMoved(HierarchyEvent e) {
				updateModel();				
			}
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
		Action openAction = MosaicIO.createOpenAction(model, this);
		Action saveAction = MosaicIO.createSaveAction(model, this);
		Action saveAsAction = MosaicIO.createSaveAsAction(model, this);
		Action exportAction = MosaicIO.createExportAction(model, this);

		// components:
		try {
			File file = (File)model.get(BrickGraphicsState.Image);
			MosaicIO.load(this, model, file);
		}
		catch (IOException e) {
			openAction.actionPerformed(null);
		} catch (ClassCastException e) {
			e.printStackTrace();
			openAction.actionPerformed(null);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			openAction.actionPerformed(null);
		}
		imagePreparingView.addChangeListener(this);
		model.addModelSaver(this);
		
		// in split pane:
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePreparingView, brickedView);

		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(null, Icons.get(32, "image_w"), null);
		tabbedPane.add(splitPane, Icons.get(32, "image_c"));
		tabbedPane.addTab(null, Icons.get(32, "image_e"), null);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int index = tabbedPane.getSelectedIndex();
				switch(index) {
				case 0:
					imagePreparingView.setVisible(true);
					brickedView.setVisible(false);					
					break;
				case 1:
					splitPane.setDividerLocation(0.6);
					imagePreparingView.setVisible(true);
					brickedView.setVisible(true);
					break;
				case 2:
					imagePreparingView.setVisible(false);
					brickedView.setVisible(true);
					break;
				default:
					throw new IllegalStateException("Pane " + index + " selected.");
				}
			}
		});
		tabbedPane.setSelectedIndex(1);
	
		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
		add(Log.makeStatusBar(), BorderLayout.SOUTH);

		// file menu:
		JMenu fileMenu = new JMenu("File");
		fileMenu.setDisplayedMnemonicIndex(0);
		fileMenu.setMnemonic('F');
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(exportAction);
		// View menu:
		JMenu viewMenu = new JMenu("View");
		viewMenu.setDisplayedMnemonicIndex(0);
		viewMenu.setMnemonic('V');
		viewMenu.add(new JCheckBoxMenuItem(brickedView.getToolBar().getColorChooser().getOnOffAction()));
		viewMenu.add(new JCheckBoxMenuItem(brickedView.getColorLegend().getOnOffAction()));
		viewMenu.add(new JCheckBoxMenuItem(imagePreparingView.getCropEnabledAction()));
		viewMenu.add(new Dialogs(this).makeShowOptionsDialogAction(model, this));
		// menu bar:
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(new MagnifierFileMenu(brickedView.getMagnifier()));
		this.setJMenuBar(menuBar);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				newWindow();
			}
		});
	}
	
	private static void newWindow() {
		MainWindow mw = new MainWindow();
		Rectangle placement = (Rectangle)mw.model.get(BrickGraphicsState.MainWindowPlacement);
		mw.setBounds(placement);
		mw.setVisible(true);
		mw.splitPane.setDividerLocation((Integer)mw.model.get(BrickGraphicsState.MainWindowDividerLocation));
		mw.brickedView.getColorLegend().propertyChange(null);
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
			public void run() {
				newWindow();
			}
		});
	}

	public void mosaicLoaded(BufferedImage image) {
		if(image == null)
			throw new IllegalArgumentException("image is null");
		
		this.image = image;
		if(imagePreparingView == null)
			imagePreparingView = new ImagePreparingView(image, model);
		else {
			imagePreparingView.loadModel(model);
			imagePreparingView.setImage(image);				
		}
		BufferedImage toBrick = imagePreparingView.getFullyPreparredImage();

		if(brickedView == null) {
			brickedView = new BrickedView(this, model);			
		}
		else {
			brickedView.reloadModel(model);
		}
		brickedView.setImage(toBrick);

		if(splitPane != null)
			repaint();
	}

	public void stateChanged(ChangeEvent e) {
		File file = (File)model.get(BrickGraphicsState.Image);
		setTitle("Brick Graphics Mosaic - " + file.getName());
		BufferedImage toBrick = imagePreparingView.getFullyPreparredImage();
		brickedView.setImage(toBrick);
		repaint();
	}
	
	public BufferedImage getInImage() {
		return image;
	}
	
	public BrickedView getBrickedView() {
		return brickedView;
	}
	
	public BufferedImage getFinalImage() {
		return brickedView.getMagnifier().getShownImage();
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MainWindowDividerLocation, splitPane.getDividerLocation());
	}

	public void okPressed(Set<BrickGraphicsState> changed) {
		boolean prepareReloaded = false;
		for(BrickGraphicsState state : changed) {
			switch(state) {
			case AnnoyingQuestions:
				// volatile state.
				break;
			case ImageRestriction:
			case ImageRestrictionEnabled:
				if(!prepareReloaded)
					imagePreparingView.loadModel(model);
				prepareReloaded = true;
				break;
			case Language:
				softReset();
				return;
			default: 
				throw new IllegalStateException("Enum broken: " + state);
			}
		}
	}

	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {
		try {
			model.saveToFile();
		}
		catch (IOException e2) {
			e2.printStackTrace();
		}
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}
