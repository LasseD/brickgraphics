package mosaic.ui;

import transforms.*;
import io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.ui.menu.*;
import bricks.ToBricksType;
import java.awt.event.*;

public class BrickedView extends JComponent implements ChangeListener {
	private BufferedImage inImage, bricked;
	private ToBricksTransform toBricksTransform;
	private ToBricksTools toolBar;
	private MagnifierController magnifierController;
	private ColorController colorController;
	private JComponent mainComponent;
	
	public BrickedView(MainController mw, Model<BrickGraphicsState> model) {
		magnifierController = mw.getMagnifierController();
		colorController = mw.getColorController();
		// UI:		
		toolBar = new ToBricksTools(mw, model);
		toolBar.addChangeListener(magnifierController);
		magnifierController.addChangeListener(this);

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI();
			}
		});
		
		toBricksTransform = new ToBricksTransform(colorController.getColors(), 
				toolBar.getToBricksType(), toolBar.getHalfToneType(), toolBar.getPropagationPercentage(), colorController);			
		magnifierController.setTBTransform(toBricksTransform);
	}
	
	private void setUI() {
		setLayout(new BorderLayout());
		//add(toolBar, BorderLayout.NORTH);
		mainComponent = new JComponent() {
			private static final long serialVersionUID = 5749886635907597779L;
			private ScaleTransform scaler = new ScaleTransform(
					ScaleTransform.Type.bounded, 
					AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

			@Override 
			public void paintComponent(Graphics g) {				
				super.paintComponent(g);
				if(bricked == null)
					return;
				magnifierController.setCoreImage(bricked);

				Dimension size = getSize();
				scaler.setWidth(size.width-2);
				scaler.setHeight(size.height-2);
				
				BufferedImage shownImage = scaler.transform(bricked);
				magnifierController.setShownImage(shownImage);
				if(shownImage == null)
					return;
					
				Graphics2D g2 = (Graphics2D)g;
				g2.translate(1, 1); // make space for highlight rect

				// Perform actual drawing:
				if(getWidth() > shownImage.getWidth()) {
					// adjust to center!
					int xOffset = (getWidth()-shownImage.getWidth())/2;
					g2.translate(xOffset, 0);
					magnifierController.setMouseOffset(xOffset+1, 1);
				}
				else
					magnifierController.setMouseOffset(1, 1);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.drawImage(shownImage, null, 0, 0);
				magnifierController.drawHighlightRect(g2);
			}
		};
		mainComponent.addMouseListener(magnifierController);
		mainComponent.addMouseMotionListener(magnifierController);
		addKeyListener(magnifierController);
		mainComponent.setFocusable(true);
		mainComponent.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				grabFocus();
			}
		});
		add(mainComponent, BorderLayout.CENTER);		
	}
	
	public void setImage(BufferedImage image) {
		if(image == null)
			throw new NullPointerException();
		inImage = image;
		bricked = toBricksTransform.transform(image);
		magnifierController.setCoreImage(bricked);
		
		toolBar.imageUpdated(image.getWidth(), image.getHeight());
		updateTransform(toolBar);
	}
	
	private void updateTransform(ToBricksTools toolBar) {
		toBricksTransform.setPropagationPercentage(toolBar.getPropagationPercentage());
		toBricksTransform.setToBricksType(toolBar.getToBricksType());
		toBricksTransform.setHalfToneType(toolBar.getHalfToneType());
		toBricksTransform.setColors(colorController.getColors());
		toBricksTransform.setBasicUnitSize(toolBar.getBasicWidth(), toolBar.getBasicHeight());		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && inImage != null && e.getSource() instanceof ToBricksTools) {
			ToBricksTools toolBar = (ToBricksTools)e.getSource();
			updateTransform(toolBar);
			bricked = toBricksTransform.transform(inImage);
			magnifierController.setTBTransform(toBricksTransform);
			magnifierController.setCoreImage(bricked);
		}
		if(mainComponent != null)
			mainComponent.repaint();
	}
	
	public LEGOColor.CountingLEGOColor[] getLegendColors() {
		if(toBricksTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
			return toBricksTransform.lastUsedColorCounts();				
		}
		return toBricksTransform.getMainTransform().lastUsedColorCounts();								
	}
	
	public ToBricksTransform getToBricksTransform() {
		return toBricksTransform;
	}

	public MagnifierController getMagnifierController() {
		if(magnifierController == null)
			throw new IllegalStateException();
		return magnifierController;
	}
	
	public Dimension getBrickedSize() {
		return new Dimension(bricked.getWidth(), bricked.getHeight());
	}

	public ToBricksTools getToolBar() {
		return toolBar;
	}
}
