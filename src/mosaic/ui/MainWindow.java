package mosaic.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.*;
import java.io.File;
import java.io.IOException;
import icon.Icons;
import io.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.*;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.menu.*;

public class MainWindow extends JFrame implements ChangeListener, ModelHandler<BrickGraphicsState> {
	private ImagePreparingView imagePreparingView;
	private BrickedView brickedView;
	private JSplitPane splitPane;
	private ColorChooserDialog colorChooser;
	private MainController mc;

	public MainWindow(final MainController mc, final Model<BrickGraphicsState> model) {
		super(MainController.APP_NAME);
		this.mc = mc;
		model.addModelHandler(this);
		long startTime = System.currentTimeMillis();
		
		setVisible(true);
		
		imagePreparingView = new ImagePreparingView(model);
		imagePreparingView.addChangeListener(this);
		Log.log("Created left view after " + (System.currentTimeMillis()-startTime) + "ms.");
		
		brickedView = new BrickedView(mc, model);
		Log.log("Created right view after " + (System.currentTimeMillis()-startTime) + "ms.");

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					model.saveToFile();
				}
				catch (IOException e2) {
					Log.log(e2);
				}
				Log.close();
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
				int dividerLocation = splitPane.getDividerLocation();
				imagePreparingView.setVisible(dividerLocation >= getMinDividerLocation());
				brickedView.setVisible(splitPane.getWidth() == 0 || dividerLocation <= getMaxDividerLocation());
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
				colorChooser = new ColorChooserDialog(mc, MainWindow.this); // Must be made before ribbon!
				Ribbon ribbon = new Ribbon(mc, MainWindow.this);
				brickedView.getToolBar().addComponents(ribbon, true);		
				add(ribbon, BorderLayout.NORTH);
				ColorSettingsDialog csd = new ColorSettingsDialog(MainWindow.this, mc.getColorController());
				setJMenuBar(new MainMenu(mc, MainWindow.this, model, csd));
				setIconImage(Icons.get(32, "icon").getImage());
			}
		});

		handleModelChange(model);
		Log.log("LDDMC main window operational after " + (System.currentTimeMillis()-startTime) + "ms.");
	}

	private int getMinDividerLocation() {
		return Math.max(32, imagePreparingView.getPreferredSize().width);
	}
	
	private int getMaxDividerLocation() {
		return splitPane.getSize().width - splitPane.getDividerSize() - Math.max(32, brickedView.getPreferredSize().width) - 2;
	}
	
	public ColorChooserDialog getColorChooser() {
		if(colorChooser == null)
			throw new IllegalStateException();
		return colorChooser;
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
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(mc.getInImage() == null || e == null || e.getSource() == this) {
			return;
		}
		
		File file = mc.getFile();
		if(file == null)
			setTitle(MainController.APP_NAME);
		else
			setTitle(MainController.APP_NAME + " - " + mc.getFile().getName());

		imagePreparingView.setImage(mc.getInImage(), this);						
		BufferedImage toBrick = imagePreparingView.getFullyPreparredImage();
		brickedView.setImage(toBrick);

		if(splitPane == null)
			return;
		repaint();
	}
	
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		Rectangle placement = (Rectangle)model.get(BrickGraphicsState.MainWindowPlacement);
		setBounds(placement);
		splitPane.setDividerLocation((Integer)model.get(BrickGraphicsState.MainWindowDividerLocation));
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MainWindowDividerLocation, splitPane.getDividerLocation());
		model.set(BrickGraphicsState.MainWindowPlacement, new Rectangle(getLocation().x, getLocation().y, getWidth(), getHeight()));
	}
}
