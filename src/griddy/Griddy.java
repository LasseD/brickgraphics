package griddy;

import io.Log;
import io.Model;
import io.ModelChangeListener;
import io.ModelHandler;
import icon.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import griddy.actions.*;
import griddy.zoom.*;
import javax.swing.*;

import griddy.grid.*;
import griddy.io.GriddyState;
import griddy.rulers.*;
import griddy.zoom.Zoom;

public class Griddy extends JFrame implements ModelHandler<GriddyState>, WindowListener, ModelChangeListener {
	public static final String APP_NAME = "Griddy - The grid overlay program";
	public static final String APP_NAME_SHORT = "Griddy";
	public static final String LOG_FILE_NAME = "griddy.log";
	public static final String STATE_FILE_NAME = "griddy.kvm";
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 9;
	public static final int VERSION_MICRO = 3;
	public static final String APP_VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_MICRO;
	public static final String HELP_URL = "http://c-mt.dk/software/griddy/help";
	
	private BufferedImage image;
	private final Model<GriddyState> model;
	private final DisplayArea displayArea;
	private final Zoom zoom; // factor
	private final JScrollPane scrollPane;
	private final Ruler measurer;
	private BorderRuler scaleToolHorizontal, scaleToolVertical;

	public Griddy() {
		super(APP_NAME_SHORT);
		try {
			Log.initializeLog(LOG_FILE_NAME);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "The log file " + LOG_FILE_NAME + " could not be opened for writing.\nLDDMC might not have sufficient permissions.\nLog messages are written to console if available.\nThe error message:\n" + e1.getMessage(), "Failed to open/create log file", JOptionPane.WARNING_MESSAGE);
		}

		setIconImage(Icons.get(32, "icon", "GRIDDY").getImage());
		model = new Model<GriddyState>(STATE_FILE_NAME, GriddyState.class);
		zoom = new Zoom(model);
		displayArea = new DisplayArea(model, zoom); // will add itself as zoom listener.
		scaleToolHorizontal = new BorderRuler(true);
		scaleToolVertical = new BorderRuler(false);
		measurer = new Ruler(zoom, scaleToolHorizontal, scaleToolVertical);

		displayArea.addDisplayComponent(scaleToolHorizontal.makeDisplayLineComponent());
		displayArea.addDisplayComponent(scaleToolVertical.makeDisplayLineComponent());
		displayArea.addDisplayComponent(measurer);
		
		displayArea.addMouseListener(measurer);
		displayArea.addMouseMotionListener(measurer);
		measurer.addListener(new RulerListener() {			
			@Override
			public void rulerChanged(Ruler ruler) {
				displayArea.repaint();
			}
		});
		
		zoom.addZoomListener(scaleToolHorizontal);
		zoom.addZoomListener(scaleToolVertical);
		
		scaleToolHorizontal.addScaleListener(displayArea.getGrid(), true);
		scaleToolHorizontal.addScaleListener(scaleToolVertical, false);
		scaleToolHorizontal.addScaleListener(displayArea, true);
		
		scaleToolVertical.addScaleListener(displayArea.getGrid(), true);
		scaleToolVertical.addScaleListener(displayArea, true);
		scaleToolVertical.addScaleListener(scaleToolHorizontal, false);

		model.addModelHandler(this);
		
		GridDialog gridDialog = new GridDialog(this, displayArea.getGrid());
		Action openGridDialogAction = gridDialog.makeShowOptionsDialogAction(this, displayArea.getGrid());
		
		addWindowListener(this);
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
				int state = Griddy.this.getExtendedState();
				if((state | Frame.NORMAL) == Frame.NORMAL) {
					model.set(GriddyState.MainWindowPlacement, Griddy.this.getBounds());
				}
				else if((state | Frame.MAXIMIZED_HORIZ) == Frame.MAXIMIZED_HORIZ) {
					Rectangle inModel = (Rectangle)model.get(GriddyState.MainWindowPlacement);
					Rectangle bounds = Griddy.this.getBounds();
					inModel.y = bounds.y;
					inModel.height = bounds.height;
				}
				else if((state | Frame.MAXIMIZED_VERT) == Frame.MAXIMIZED_VERT) {
					Rectangle inModel = (Rectangle)model.get(GriddyState.MainWindowPlacement);
					Rectangle bounds = Griddy.this.getBounds();
					inModel.x = bounds.x;
					inModel.width = bounds.width;
				}
			}
		});
		Action openAction = IOActions.createOpenAction(model, this);
		Action saveAction = IOActions.createSaveAction(model, this);
		Action saveAsAction = IOActions.createSaveAsAction(model, this);
		Action exportAction = IOActions.createExportAction(model, this);
		Action loadAction = IOActions.createLoadScreenAction(model, this);

		// components:
		try {
			File file = new File((String)model.get(GriddyState.ImageFileName));
			IOActions.load(this, model, file);
		}
		catch (IOException e) {
			openAction.actionPerformed(null);
		} catch (ClassCastException e) {
			e.printStackTrace();
			openAction.actionPerformed(null);
		}
		
		JPanel scalePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		scalePanel.add(scaleToolHorizontal.makeTextFieldsComponent());
		scalePanel.add(new JLabel("x"));
		scalePanel.add(scaleToolVertical.makeTextFieldsComponent());

		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(scalePanel, BorderLayout.WEST);
		lowerPanel.add(measurer.makeGUIComponents(scaleToolHorizontal, scaleToolVertical), BorderLayout.CENTER);
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(displayArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setColumnHeaderView(scaleToolHorizontal.makeRuler());
		scrollPane.setRowHeaderView(scaleToolVertical.makeRuler());
		zoom.addZoomListener(new ZoomListener() {			
			@Override
			public void zoomChanged(double newZoom, double zoomChangeFactor) {
				scrollPane.validate();
			}
		});
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(lowerPanel, BorderLayout.SOUTH);
		contentPane.add(zoom.makeGUI(this), BorderLayout.NORTH);
		
		// file menu:
		JMenu fileMenu = new JMenu("File");
		fileMenu.setDisplayedMnemonicIndex(0);
		fileMenu.setMnemonic('F');
		fileMenu.add(openAction);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(exportAction);
		fileMenu.add(loadAction);
		// edit menu:
		JMenu editMenu = new JMenu("Edit");
		editMenu.setDisplayedMnemonicIndex(0);
		editMenu.setMnemonic('E');
		editMenu.add(openGridDialogAction);
		
		// menu bar:
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		setJMenuBar(menuBar);
		
		setBounds((Rectangle)model.get(GriddyState.MainWindowPlacement));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
	}
	
	public BufferedImage getFinalImage() {
		return displayArea.getDisplayedImage();
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
				newWindow();
			}
		});
	}
	
	private static void newWindow() {
		Griddy mw = new Griddy();
		Rectangle placement = (Rectangle)mw.model.get(GriddyState.MainWindowPlacement);
		mw.setBounds(placement);
		mw.setVisible(true);
	}
	
	public void imageFileLoaded(BufferedImage image) {
		this.image = image;
		displayArea.setImage(image);
		if(scrollPane != null) // init.
			scrollPane.validate();
		File file = new File((String)model.get(GriddyState.ImageFileName));
		setTitle("Griddy - " + file.getName());
		repaint();
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public Dimension getDisplayAreaSize() {
		Dimension rect = scrollPane.getSize();
		return new Dimension(rect.width - scrollPane.getVerticalScrollBar().getWidth() - BorderRuler.RULER_WIDTH - 2, 
				             rect.height - scrollPane.getHorizontalScrollBar().getHeight() - BorderRuler.RULER_WIDTH - 2);
	}
	
	@Override
	public void save(Model<GriddyState> model) {
		//model.set(GriddyState.ScaleToolHorizontal, scaleToolHorizontal);
		//model.set(GriddyState.ScaleToolVertical, scaleToolVertical);
		model.set(GriddyState.MainWindowPlacement, getBounds());
	}

	@Override
	public void windowActivated(WindowEvent e) {}
	@Override
	public void windowClosed(WindowEvent e) {}

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

	@Override
	public void windowDeactivated(WindowEvent e) {}
	@Override
	public void windowDeiconified(WindowEvent e) {}
	@Override
	public void windowIconified(WindowEvent e) {}
	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void modelChanged(Object stateValue) {
		setBounds((Rectangle)model.get(GriddyState.MainWindowPlacement));
	}

	@Override
	public void handleModelChange(Model<GriddyState> model) {}
}
