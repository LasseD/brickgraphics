package mosaic.ui.bricked;

import transforms.*;
import io.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.io.*;
import bricks.ToBricksType;
import java.awt.event.*;

public class BrickedView extends JComponent implements ChangeListener {
	private static final long serialVersionUID = -8531244887026144925L;
	private BufferedImage inImage, bricked;
	private ToBricksTransform toBricksTransform;
	private ToBricksToolBar toolBar;
	private Magnifier magnifier;
	private ColorLegend legend;
	
	public BrickedView(JFrame parent, Model<BrickGraphicsState> model) {
		// UI:
		toolBar = new ToBricksToolBar(parent, model);		
		toolBar.addChangeListener(this);
		magnifier = new Magnifier(model);
		magnifier.adChangeListener(this);
		legend = new ColorLegend(parent, magnifier.getEnabledAction(), magnifier.getShowColorsAction());

		setLayout(new BorderLayout());
		add(toolBar, BorderLayout.NORTH);
		JComponent mainComponent = new JComponent() {
			private static final long serialVersionUID = 5749886635907597779L;
			private ScaleTransform scaler = new ScaleTransform(
					ScaleTransform.Type.bounded, 
					AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

			public @Override void paintComponent(Graphics g) {				
				super.paintComponent(g);
				Dimension size = getSize();
				scaler.setWidth(size.width);
				scaler.setHeight(size.height);
				
				magnifier.setShownImage(scaler.transform(bricked));			
				magnifier.draw(size, (Graphics2D)g, legend);
			}
		};
		mainComponent.addMouseListener(magnifier);
		mainComponent.addMouseMotionListener(magnifier);
		addKeyListener(magnifier);
		mainComponent.setFocusable(true);
		mainComponent.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				grabFocus();
			}

			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
		});
		add(mainComponent, BorderLayout.CENTER);
		
		updateTransform(toolBar);
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		magnifier.reloadModel(model);
		toolBar.reloadModel(model);
	}
	
	public void setImage(BufferedImage image) {
		if(image == null)
			throw new NullPointerException();
		inImage = image;
		bricked = toBricksTransform.transform(image);
		magnifier.setCoreImage(bricked.getWidth(), bricked.getHeight());
		
		toolBar.imageUpdated(image.getWidth(), image.getHeight());
		updateTransform(toolBar);
	}
	
	private void updateTransform(ToBricksToolBar toolBar) {
		if(toBricksTransform == null) {
			toBricksTransform = new ToBricksTransform(toolBar.getColors(), 
					toolBar.getToBricksType(), toolBar.getHalfToneType(), false);			
			magnifier.setTBTransform(toBricksTransform);
		}
		else {
			toBricksTransform.setToBricksType(toolBar.getToBricksType());
			toBricksTransform.setHalfToneType(toolBar.getHalfToneType());
			toBricksTransform.setColors(toolBar.getColors(), false);
		}
		toBricksTransform.setBasicUnitSize(toolBar.getBasicWidth(), toolBar.getBasicHeight());		
	}

	public void stateChanged(ChangeEvent e) {
		if(e.getSource() instanceof ToBricksToolBar) {
			ToBricksToolBar toolBar = (ToBricksToolBar)e.getSource();
			updateTransform(toolBar);
			bricked = toBricksTransform.transform(inImage);
			magnifier.setTBTransform(toBricksTransform);
			magnifier.setCoreImage(bricked.getWidth(), bricked.getHeight());
			if(toBricksTransform.getToBricksType() == ToBricksType.snot) {
				legend.setColors(toBricksTransform.lastUsedColors());				
			}
			else {
				legend.setColors(toBricksTransform.getMainTransform().lastUsedColors());								
			}
		}
		repaint();
	}
	
	public ToBricksTransform getToBricksTransform() {
		return toBricksTransform;
	}

	public Magnifier getMagnifier() {
		return magnifier;
	}
	
	public ColorLegend getColorLegend() {
		return legend;
	}
	
	public Dimension getBrickedSize() {
		return new Dimension(bricked.getWidth(), bricked.getHeight());
	}

	public ToBricksToolBar getToolBar() {
		return toolBar;
	}
}
