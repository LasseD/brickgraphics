package griddy;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import griddy.grid.*;
import griddy.io.GriddyState;
import griddy.zoom.*;
import griddy.rulers.*;
import io.*;

public class DisplayArea extends JPanel implements ModelHandler<GriddyState>, ZoomListener, BorderRulerListener, ModelChangeListener {
	private BufferedImage image;
	private double zoom; // factor
	private Grid grid;
	private List<DisplayComponent> displayComponents;
	
	public DisplayArea(Model<GriddyState> model, Zoom zoom) {
		this.zoom = zoom.getZoom();
		zoom.addZoomListener(this);
		
		grid = new Grid(model);
		model.addModelHandler(this);

		displayComponents = new LinkedList<DisplayComponent>();
		displayComponents.add(grid);
		
		grid.addMouserListeners(this);
		//setDoubleBuffered(true);
	}
	
	public Grid getGrid() {
		return grid;
	}
	
	public void addDisplayComponent(DisplayComponent c) {
		displayComponents.add(c);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Rectangle bounds = g2.getClipBounds();
		g2.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);

		AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);
		AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
		BufferedImage baseImage = op.createCompatibleDestImage(image, null);
		op.filter(image, baseImage);
		//BufferedImage outImage = op.createCompatibleDestImage(image, null);
		//g2.setClip(bounds);

		g2.drawImage(baseImage, null, 0, 0);
		for(DisplayComponent dc : displayComponents) {
			dc.draw(baseImage, g2);
		}
	}
	
	public BufferedImage getDisplayedImage() {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics g = bi.getGraphics();
		g.setClip(0, 0, w, h);
		paintComponent(g);
		return bi;
	}
	
	public void setImage(BufferedImage image) {
		if(image == null)
			throw new NullPointerException("Image is null");
		this.image = image;
		setPreferredSize(new Dimension((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom)));
		invalidate();
	}
	
	@Override
	public void zoomChanged(double newZoom, double zoomChangeFactor) {
		zoom = newZoom;
		// update grid start to new translate (image is scaled from 0,0)
		Point p = grid.getGridStart();
		p.x = (int)Math.round(p.x*zoomChangeFactor);
		p.y = (int)Math.round(p.y*zoomChangeFactor);
		grid.setGridStart(p);
		if(image == null)
			return;
		setPreferredSize(new Dimension((int)(image.getWidth()*zoom), (int)(image.getHeight()*zoom)));
		invalidate();
	}

	@Override
	public void save(Model<GriddyState> model) {
		//model.set(GriddyState.Grid, grid); // TODO: Add
	}

	@Override
	public void borderRulerChanged(BorderRuler ignore) {
		repaint();
	}

	@Override
	public void modelChanged(Object stateValue) {
		grid.conformTo((Grid)stateValue);
	}

	@Override
	public void handleModelChange(Model<GriddyState> model) {
		// TODO Auto-generated method stub
		
	}
}
