package mosaic.ui;

import transforms.*;
import transforms.ScaleTransform.ScaleQuality;
import io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.*;
import mosaic.controllers.*;
import mosaic.io.*;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineMosaicListener;
import bricks.ToBricksType;
import java.awt.event.*;

public class BrickedView extends JComponent implements ChangeListener, PipelineMosaicListener {
	private Dimension mosaicImageSize;
	private ToBricksTransform toBricksTransform; // Used by CAD accessing functions.
	private Pipeline pipeline;
	private ToBricksController toBricksController;
	private MagnifierController magnifierController;
	private ColorController colorController;
	private ScaleTransform scaler; // Used for size calculations
	
	public BrickedView(MainController mc, Model<BrickGraphicsState> model, Pipeline pipeline) {
		this.pipeline = pipeline;
		scaler = new ScaleTransform("Constructed view", true, ScaleQuality.RetainColors);
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

		addMouseListener(magnifierController);
		addMouseMotionListener(magnifierController);
		addKeyListener(magnifierController);
		setFocusable(true);
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				grabFocus();
			}
		});		
		pipeline.setToBricksTransform(toBricksTransform);
		pipeline.addMosaicListener(magnifierController);
		pipeline.addMosaicListener(this);
	}
	
	@Override 
	public void paintComponent(Graphics g) {				
		super.paintComponent(g);
		if(mosaicImageSize == null)
			return;

		Dimension size = getSize();
		scaler.setWidth(size.width-2);
		scaler.setHeight(size.height-2);

		Dimension shownImageSize = scaler.getTransformedSize(mosaicImageSize);
		magnifierController.setShownImageSize(shownImageSize);
			
		Graphics2D g2 = (Graphics2D)g;
		g2.translate(1, 1); // make space for highlight rect

		// Perform actual drawing:
		if(getWidth() > shownImageSize.width) {
			// adjust to center!
			int xOffset = (getWidth()-shownImageSize.width)/2;
			g2.translate(xOffset, 0);
			magnifierController.setMouseOffset(xOffset+1, 1);
		}
		else
			magnifierController.setMouseOffset(1, 1);
		
		toBricksTransform.drawAll(g2, shownImageSize);

		magnifierController.drawHighlightRect(g2);
	}

	public LEGOColor.CountingLEGOColor[] getLegendColors() {
		if(toBricksTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
			return toBricksTransform.lastUsedColorCounts();				
		}
		return toBricksTransform.getMainTransform().lastUsedColorCounts();								
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
	
	// Used by CAD exports
	public ToBricksTransform getToBricksTransform() {
		return toBricksTransform;
	}
	
	// Used by CAD exports
	public Dimension getBrickedSize() {
		return mosaicImageSize;
	}

	@Override
	public void mosaicChanged(Dimension mosaicImageSize) {
		this.mosaicImageSize = mosaicImageSize;
		repaint();
	}
}
