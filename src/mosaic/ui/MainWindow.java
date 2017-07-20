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
import mosaic.rendering.Pipeline;
import mosaic.rendering.RenderingProgressBar;
import mosaic.ui.menu.*;

public class MainWindow extends JFrame implements ChangeListener, ModelHandler<BrickGraphicsState> {
	private ImagePreparingView imagePreparingView;
	private BrickedView brickedView;
	private JSplitPane splitPane;
	private ColorChooserDialog colorChooserDialog;
	private MagnifierWindow magnifierWindow;
	private MainController mc;
	private Pipeline pipeline;
	private Rectangle lastNormalPlacement;

	public MainWindow(final MainController mc, final Model<BrickGraphicsState> model, 
			final Pipeline pipeline, RenderingProgressBar renderingProgressBar) {
		super(MainController.APP_NAME);
		this.mc = mc;
		this.pipeline = pipeline;
		model.addModelHandler(this);
		long startTime = System.currentTimeMillis();
		
		setVisible(true);

		imagePreparingView = new ImagePreparingView(model, mc.getOptionsController(), mc.getToBricksController(), pipeline);
		//imagePreparingView.addChangeListener(this);
		Log.log("Created left view after " + (System.currentTimeMillis()-startTime) + "ms.");
		
		brickedView = new BrickedView(mc, model, pipeline);
		Log.log("Created right view after " + (System.currentTimeMillis()-startTime) + "ms.");
		magnifierWindow = new MagnifierWindow(mc, this);
		Log.log("Created magnifier after " + (System.currentTimeMillis()-startTime) + "ms.");

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
					lastNormalPlacement = MainWindow.this.getBounds();
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
				pipeline.invalidate();
			}
		});

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		{
			// Preparing view tool bar:
			JPanel pPrepairToolBar = new JPanel();
			ImagePreparingToolBar prepareToolBar = imagePreparingView.getToolBar();
			pPrepairToolBar.add(prepareToolBar);
			cp.add(pPrepairToolBar, BorderLayout.WEST);			
		}
		{
			// Legend:
			JPanel pLegend = new JPanel(new BorderLayout());
			ColorLegend colorLegend = magnifierWindow.getLegend();
			pLegend.add(colorLegend, BorderLayout.CENTER);
			cp.add(pLegend, BorderLayout.EAST);
		}

		cp.add(splitPane, BorderLayout.CENTER);
		cp.add(renderingProgressBar, BorderLayout.SOUTH);

		handleModelChange(model);
		Log.log("LDDMC main window operational after " + (System.currentTimeMillis()-startTime) + "ms.");
	}
	
	public void finishUpRibbonMenuAndIcon() {
		colorChooserDialog = new ColorChooserDialog(mc, MainWindow.this); // Must be made before ribbon!
		Ribbon ribbon = new Ribbon(mc, MainWindow.this);
		mc.getToBricksController().addComponents(ribbon, mc);
		getContentPane().add(ribbon, BorderLayout.NORTH);
		ColorSettingsDialog csd = new ColorSettingsDialog(MainWindow.this, mc.getColorController());
		setJMenuBar(new MainMenu(mc, MainWindow.this, mc.getModel(), csd));
		setIconImage(Icons.get(32, "icon").getImage());		
		
		ribbon.setVisible(false);
		ribbon.setVisible(true);		
	}

	private int getMinDividerLocation() {
		return Math.max(32, imagePreparingView.getPreferredSize().width);
	}
	
	private int getMaxDividerLocation() {
		return splitPane.getSize().width - splitPane.getDividerSize() - Math.max(32, brickedView.getPreferredSize().width) - 2;
	}
	
	public ColorChooserDialog getColorChooser() {
		if(colorChooserDialog == null)
			throw new IllegalStateException();
		return colorChooserDialog;
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
		return mc.getMagnifierController().getCoreImageInCoreUnits();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(mc.getInImage() == null || e == null || e.getSource() == this)
			return;
		
		File file = mc.getFile();
		if(file == null)
			setTitle(MainController.APP_NAME);
		else
			setTitle(MainController.APP_NAME + " - " + file.getName());

		pipeline.setStartImage(mc.getInImage());						
		//brickedView.setImage(imagePreparingView.getPreparredImage());

		if(splitPane != null)
			repaint();
	}
	
	private void setBoundsSafe(Rectangle r) {
		if(r.x < 0)
			r.x = 0;
		if(r.y < 0)
			r.y = 0;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle defaultSize = (Rectangle)BrickGraphicsState.MainWindowPlacement.getDefaultValue();
		if(r.x + r.width > screenSize.width) {
			r.x = 0;
			r.width = Math.min(screenSize.width, defaultSize.width);
		}
		if(r.y + r.height > screenSize.height) {
			r.y = 0;
			r.height = Math.min(screenSize.height, defaultSize.height);
		}
			
		setBounds(r);		
	}
	
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		Rectangle placement = (Rectangle)model.get(BrickGraphicsState.MainWindowPlacement);		
		setBoundsSafe(placement);
		splitPane.setDividerLocation((Integer)model.get(BrickGraphicsState.MainWindowDividerLocation));
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MainWindowDividerLocation, splitPane.getDividerLocation());
		model.set(BrickGraphicsState.MainWindowPlacement, lastNormalPlacement);
	}
}
