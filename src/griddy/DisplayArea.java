package griddy;

import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import griddy.grid.*;
import griddy.zoom.*;
import griddy.rulers.*;
import io.*;

public class DisplayArea extends JPanel implements ModelSaver<GriddyState>, ZoomListener, BorderRulerListener, ModelChangeListener {
	private static final long serialVersionUID = 8055655787263638626L;
	private BufferedImage image;
	private double zoom; // factor
	private Grid grid;
	private List<DisplayComponent> displayComponents;
	
	public DisplayArea(Model<GriddyState> model, Zoom zoom) {
		this.zoom = zoom.getZoom();
		zoom.addZoomListener(this);
		
		grid = (Grid)model.get(GriddyState.Grid);
		model.addModelSaver(this);

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
	
	private class SlowDrawer implements Runnable {
		private transient boolean kill;
		private BufferedImage image;
		private transient BufferedImage drawnImage;
		private Rectangle graphicsBounds;
		
		public SlowDrawer(BufferedImage image, Graphics2D g2) {
			if(image == null)
				throw new IllegalArgumentException("Image is null");
			this.image = image;
			graphicsBounds = g2.getClipBounds();
			kill = false;
		}
		
		@Override
		public void run() {
			AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);
			AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
			if(kill) return;
			BufferedImage baseImage = op.createCompatibleDestImage(image, null);
			if(kill) return;
			op.filter(image, baseImage);
			if(kill) return;
			BufferedImage outImage = op.createCompatibleDestImage(image, null);
			if(kill) return;
			Graphics2D g2 = (Graphics2D)outImage.getGraphics();
			g2.setClip(graphicsBounds);

			g2.drawImage(baseImage, null, 0, 0);
			for(DisplayComponent dc : displayComponents) {
				if(kill) return;
				dc.drawSlow(baseImage, g2);
			}
			drawnImage = outImage;
			repaint();
		}
		
		public BufferedImage getDrawnImage() {
			return drawnImage;
		}
		
		public void kill() {
			kill = true;
		}
	}
	
	private SlowDrawer currentSlowDraw;
	@Override
	public void paintComponent(Graphics g) {
		boolean slowValid = true;
		for(DisplayComponent dc : displayComponents) {
			if(!dc.isSlowValid()) {
				slowValid = false;
				//System.out.println(dc + " broke :(");
				break;
			}
		}
		
		Graphics2D g2 = (Graphics2D)g;

		BufferedImage slowImage;
		if(slowValid && currentSlowDraw != null && (slowImage = currentSlowDraw.getDrawnImage()) != null) {			
			g2.drawImage(slowImage, null, 0, 0);
		}
		else {			
			Rectangle bounds = g2.getClipBounds();
			g2.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
			if(image != null) {
				AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);
				AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
				g2.drawImage(image, op, 0, 0);
			}
			for(DisplayComponent dc : displayComponents) {
				dc.drawQuick(g2);
			}

			if(currentSlowDraw != null)
				currentSlowDraw.kill();
			currentSlowDraw = new SlowDrawer(image, g2);
			new Thread(currentSlowDraw).start();			
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
		model.set(GriddyState.Grid, grid);
	}

	@Override
	public void borderRulerChanged(BorderRuler ignore) {
		repaint();
	}

	@Override
	public void modelChanged(Object stateValue) {
		grid.conformTo((Grid)stateValue);
	}
}
