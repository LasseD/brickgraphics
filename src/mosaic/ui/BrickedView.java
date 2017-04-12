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
import bricks.ToBricksType;
import java.awt.event.*;

public class BrickedView extends JComponent implements ChangeListener {
	private BufferedImage inImage, bricked;
	private ToBricksTransform toBricksTransform;
	private ToBricksController toBricksController;
	private MagnifierController magnifierController;
	private ColorController colorController;
	private JComponent mainComponent;
	
	public BrickedView(MainController mc, Model<BrickGraphicsState> model) {
		magnifierController = mc.getMagnifierController();
		colorController = mc.getColorController();
		// UI:
		toBricksController = mc.getToBricksController();
		toBricksController.addChangeListener(magnifierController);
		magnifierController.addChangeListener(this);

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI();
			}
		});
		
		toBricksTransform = new ToBricksTransform(colorController.getColorChooserSelectedColors(), 
				toBricksController.getToBricksType(), 
				toBricksController.getPropagationPercentage(), colorController);
		magnifierController.setTBTransform(toBricksTransform);
	}
	
	private void setUI() {
		setLayout(new BorderLayout());
		mainComponent = new JComponent() {
			private static final long serialVersionUID = 5749886635907597779L;
			private ScaleTransform scaler = new ScaleTransform(true, ScaleQuality.RetainColors);

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
	
	public void setImage(BufferedImage image) {
		if(image == null)
			throw new NullPointerException();
		inImage = image;
		bricked = toBricksTransform.transform(image);
		magnifierController.setCoreImage(bricked);
		
		toBricksController.imageUpdated(image.getWidth(), image.getHeight());
		updateTransform(toBricksController);
	}
	
	private void updateTransform(ToBricksController t) {
		toBricksTransform.setPropagationPercentage(t.getPropagationPercentage());
		toBricksTransform.setToBricksType(t.getToBricksType());
		toBricksTransform.setColors(colorController.getColorChooserSelectedColors());
		toBricksTransform.setBasicUnitSize(t.getBasicWidth(), t.getBasicHeight());		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && inImage != null && e.getSource() instanceof ToBricksController) {
			ToBricksController toolBar = (ToBricksController)e.getSource();
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
	
	public Dimension getBrickedSize() {
		return new Dimension(bricked.getWidth(), bricked.getHeight());
	}
}
