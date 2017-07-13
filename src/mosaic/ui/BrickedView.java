package mosaic.ui;

import transforms.*;
import transforms.ScaleTransform.ScaleQuality;
import io.*;
import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import colors.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineListener;
import bricks.ToBricksType;
import java.awt.event.*;

public class BrickedView extends JComponent implements ChangeListener, PipelineListener {
	private BufferedImage mosaicImage;
	private ToBricksTransform toBricksTransform; // Used by CAD accessing functions.
	private Pipeline pipeline;
	private ToBricksController toBricksController;
	private MagnifierController magnifierController;
	private ColorController colorController;
	private JComponent mainComponent;
	
	public BrickedView(MainController mc, Model<BrickGraphicsState> model, Pipeline pipeline) {
		this.pipeline = pipeline;
		magnifierController = mc.getMagnifierController();
		colorController = mc.getColorController();
		// UI:
		toBricksController = mc.getToBricksController();
		toBricksController.addChangeListener(magnifierController);
		magnifierController.addChangeListener(this);
		
		toBricksTransform = new ToBricksTransform(colorController.getColorChooserSelectedColors(), 
				toBricksController.getToBricksType(), 
				toBricksController.getPropagationPercentage(), 
				toBricksController.getConstructionWidthInBasicUnits(),
				toBricksController.getConstructionHeightInBasicUnits(),
				colorController);
		magnifierController.setTBTransform(toBricksTransform);

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI();
			}
		});
		
		pipeline.setToBricksTransform(toBricksTransform);
		pipeline.addMosaicImageListener(magnifierController);
		pipeline.addMosaicImageListener(this);
	}
	
	private void setUI() {
		setLayout(new BorderLayout());
		mainComponent = new JComponent() {
			private ScaleTransform scaler = new ScaleTransform("Constructed view", true, ScaleQuality.RetainColors);

			@Override 
			public void paintComponent(Graphics g) {				
				super.paintComponent(g);
				if(mosaicImage == null)
					return;

				Dimension size = getSize();
				scaler.setWidth(size.width-2);
				scaler.setHeight(size.height-2);
				
				BufferedImage shownImage = scaler.transform(mosaicImage);
				if(shownImage == null)
					return;
				magnifierController.setShownImageSize(new Dimension(shownImage.getWidth(), shownImage.getHeight()));
					
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
				g2.drawImage(shownImage, null, null);
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
	
	private void updateTransform(ToBricksController t) {
		toBricksTransform.setPropagationPercentage(t.getPropagationPercentage());
		toBricksTransform.setToBricksType(t.getToBricksType());
		toBricksTransform.setColors(colorController.getColorChooserSelectedColors());
		toBricksTransform.setBasicUnitSize(t.getConstructionWidthInBasicUnits(), t.getConstructionHeightInBasicUnits());
		pipeline.invalidate();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && e.getSource() instanceof ToBricksController) {
			updateTransform((ToBricksController)e.getSource());
		}
		repaint();
	}
	
	public LEGOColor.CountingLEGOColor[] getLegendColors() {
		if(toBricksTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
			return toBricksTransform.lastUsedColorCounts();				
		}
		return toBricksTransform.getMainTransform().lastUsedColorCounts();								
	}
	
	// Used by CAD exports
	public ToBricksTransform getToBricksTransform() {
		return toBricksTransform;
	}
	
	// Used by CAD exports
	public Dimension getBrickedSize() {
		return new Dimension(mosaicImage.getWidth(), mosaicImage.getHeight());
	}

	@Override
	public void imageChanged(BufferedImage image) {
		mosaicImage = image;
		repaint();
	}
}
