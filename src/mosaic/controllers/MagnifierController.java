package mosaic.controllers;

import io.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.event.*;
import transforms.ToBricksTransform;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.Cropper;

/**
 * Functions: 
 *  - turn on/off
 *  - turn legend on/off
 *  - set width/height=size. One unit in width is one "block" in mosaic shown.
 *  - switch view of symbols/colors (matching legend)
 * @author LD
 */
public class MagnifierController implements ChangeListener, MouseListener, MouseMotionListener, KeyListener, ModelSaver<BrickGraphicsState> {
	private List<ChangeListener> listeners; // such as GUI components (actual magnifier) and bricked view with rectangle.
	private UIController uiController;

	private Dimension sizeInMosaicBlocks; // with block size of core image as unit
	
	private Point corePositionInCoreUnits; // on core image - moved by core image block size.
	private BufferedImage coreImageInCoreUnits; // to move mouse on
	private ToBricksTransform tbTransform; // with info for core image
	private Dimension shownImageSizeInPixels;
	private Point mouseOffset;

	public MagnifierController(Model<BrickGraphicsState> model, UIController uiController) {
		this.uiController = uiController;
		uiController.addChangeListener(this);
		listeners = new LinkedList<ChangeListener>();
		reloadModel(model);
		corePositionInCoreUnits = new Point(0,0);
		model.addModelSaver(this);
		mouseOffset = new Point();
	}
	
	public Dimension getSizeInMosaicBlocks() {
		return sizeInMosaicBlocks;
	}
	public Dimension getSizeInUnits() {
		return new Dimension(sizeInMosaicBlocks.width * tbTransform.getToBricksType().getUnitWidth(),
						     sizeInMosaicBlocks.height * tbTransform.getToBricksType().getUnitHeight());
	}

	public void reloadModel(Model<BrickGraphicsState> model) {
		sizeInMosaicBlocks = (Dimension)model.get(BrickGraphicsState.MagnifierSize);		
	}
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MagnifierSize, sizeInMosaicBlocks);
	}

	public void updateColorsInMagnifier() {
		notifyListeners(null);
	}
	
	public void setSizeInMosaicBlocks(Dimension size) {
		this.sizeInMosaicBlocks = size;
		sanify();
		notifyListeners(null);
	}

	public void changeSizeWidthInMosaicBlocks(int change) {
		sizeInMosaicBlocks.width += change;
		sanify();
		notifyListeners(null);
	}

	public void changeSizeHeightInMosaicBlocks(int change) {
		sizeInMosaicBlocks.height += change;
		sanify();
		notifyListeners(null);
	}
	
	/**
	 * utility function.
	 */
	private static int bound(int a, int min, int max) {
		if(max < min)
			throw new IllegalStateException("Can't bound " + a + " in [" + min + "," + max + "]");
		if(a < min)
			return min;
		if(a > max)
			return max;
		return a;
	}
	
	private int getMagnifierWidthInCoreUnits() {
		return tbTransform.getToBricksType().getUnitWidth()*sizeInMosaicBlocks.width;
	}
	private int getMagnifierHeightInCoreUnits() {
		return tbTransform.getToBricksType().getUnitHeight()*sizeInMosaicBlocks.height;
	}
	public ToBricksTransform getTBTransform() {
		return tbTransform;
	}

	/**
	 * Sanifies the following parameters:
	 */
	private void sanify() {
		if(coreImageInCoreUnits == null || tbTransform == null || sizeInMosaicBlocks == null)
			return;
		
		// sizeInMosaicBlocks:
		int maxBlockSizeWidth = coreImageInCoreUnits.getWidth()/tbTransform.getToBricksType().getUnitWidth();
		sizeInMosaicBlocks.width = bound(sizeInMosaicBlocks.width, 1, maxBlockSizeWidth);
		int maxBlockSizeHeight = coreImageInCoreUnits.getHeight()/tbTransform.getToBricksType().getUnitHeight();
		sizeInMosaicBlocks.height = bound(sizeInMosaicBlocks.height, 1, maxBlockSizeHeight);
		
		// corePosition:
		int bw = getMagnifierWidthInCoreUnits();
		int bh = getMagnifierHeightInCoreUnits();
		corePositionInCoreUnits.x = bound(corePositionInCoreUnits.x, 0, coreImageInCoreUnits.getWidth()-1);
		if(corePositionInCoreUnits.x % bw != 0)
			corePositionInCoreUnits.x = 0;
		corePositionInCoreUnits.y = bound(corePositionInCoreUnits.y, 0, coreImageInCoreUnits.getHeight()-1);
		if(corePositionInCoreUnits.y % bh != 0)
			corePositionInCoreUnits.y = 0;		
	}
	
	public void setTBTransform(ToBricksTransform tbTransform) {
		this.tbTransform = tbTransform;
		sanify();
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners(ChangeEvent e) {
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	public String getDisplayPosition() {
		StringBuilder sb = new StringBuilder();
		sb.append("Magnifier at ");
		sb.append(corePositionInCoreUnits.x/getMagnifierWidthInCoreUnits()+1);
		sb.append(",");
		sb.append(corePositionInCoreUnits.y/getMagnifierHeightInCoreUnits()+1);
		sb.append(". Size (");
		sb.append(sizeInMosaicBlocks.width);
		sb.append(",");
		sb.append(sizeInMosaicBlocks.height);
		sb.append(")");
		return sb.toString();
	}
	
	public Rectangle getCoreRect() {
		return new Rectangle(corePositionInCoreUnits.x, corePositionInCoreUnits.y, getMagnifierWidthInCoreUnits(), getMagnifierHeightInCoreUnits());
	}

	public void setCoreImage(BufferedImage coreImage) {
		this.coreImageInCoreUnits = coreImage;
		sanify();
	}
	
	public BufferedImage getCoreImageInCoreUnits() {
		return coreImageInCoreUnits;
	}
	
	public void setMouseOffset(int x, int y) {
		mouseOffset = new Point(x, y);
	}

	public void setShownImage(BufferedImage shownImage) {
		if(shownImage == null)
			return;
		this.shownImageSizeInPixels = new Dimension(shownImage.getWidth(), shownImage.getHeight());
		sanify();
	}
	
	public Dimension getShownImageSizeInPixels() {
		return shownImageSizeInPixels;
	}

	private void snapCoreToGrid(Point mouseOnShown) {
		if(coreImageInCoreUnits == null || sizeInMosaicBlocks == null || shownImageSizeInPixels == null)
			return;
		int mouseOnCoreXInCoreUnits = (int)(mouseOnShown.x / (double)shownImageSizeInPixels.width * coreImageInCoreUnits.getWidth());
		int mouseOnCoreYInCoreUnits = (int)(mouseOnShown.y / (double)shownImageSizeInPixels.height * coreImageInCoreUnits.getHeight());

		corePositionInCoreUnits = new Point(mouseOnCoreXInCoreUnits, mouseOnCoreYInCoreUnits);

		snapCoreToGrid();
	}
	
	private void snapCoreToGrid() {
		int unitW = getMagnifierWidthInCoreUnits();
		int unitH = getMagnifierHeightInCoreUnits();

		corePositionInCoreUnits.x = bound(corePositionInCoreUnits.x, 0, coreImageInCoreUnits.getWidth()-1);
		corePositionInCoreUnits.y = bound(corePositionInCoreUnits.y, 0, coreImageInCoreUnits.getHeight()-1);
		
		corePositionInCoreUnits.x = (int)(corePositionInCoreUnits.x/(double)unitW)*unitW;
		corePositionInCoreUnits.y = (int)(corePositionInCoreUnits.y/(double)unitH)*unitH;		
	}
	
	/**
	 * Assert shownImage is shown on canvas.
	 * @param canvas
	 */
	public void drawHighlightRect(Graphics2D g2) {
		if(!uiController.showMagnifier() || coreImageInCoreUnits == null || shownImageSizeInPixels == null)
			return;
		// draw highlighting rectangle:
		double scaleX = shownImageSizeInPixels.width / (double)coreImageInCoreUnits.getWidth();
		double scaleY = shownImageSizeInPixels.height / (double)coreImageInCoreUnits.getHeight();
		int x = (int)Math.round(corePositionInCoreUnits.x * scaleX);
		int y = (int)Math.round(corePositionInCoreUnits.y * scaleY);
		int w = (int)Math.round(getMagnifierWidthInCoreUnits() * scaleX)-2; 
		int h = (int)Math.round(getMagnifierHeightInCoreUnits() * scaleY)-2; 
		
		Rectangle highlightingRect = new Rectangle(x, y, w, h);
		
		Cropper.drawCropHighlight(g2, highlightingRect);
	}
	
	/**
	 * Enable on double click:
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if(!uiController.showMagnifier() && e.getClickCount() > 1){
			uiController.flipShowMagnifier();
		}
	}
	
	/**
	 * right/left: wrap around
	 * up down: no wrap around
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		int incX = getMagnifierWidthInCoreUnits();
		int incY = getMagnifierHeightInCoreUnits();
		
		int w = coreImageInCoreUnits.getWidth();
		int h = coreImageInCoreUnits.getHeight();
		
		switch(key) {
		case KeyEvent.VK_LEFT:
			corePositionInCoreUnits.x-=incX;
			if(corePositionInCoreUnits.x < 0) {
				corePositionInCoreUnits.x = ((w-1)/incX)*incX;
			}
			break;
		case KeyEvent.VK_RIGHT:
			corePositionInCoreUnits.x+=incX;
			if(corePositionInCoreUnits.x >= w) {
				corePositionInCoreUnits.x = 0;
			}
			break;
		case KeyEvent.VK_UP:
			corePositionInCoreUnits.y-=incY;
			if(corePositionInCoreUnits.y < 0)
				corePositionInCoreUnits.y = ((h-1)/incY)*incY;
			break;
		case KeyEvent.VK_DOWN:
			corePositionInCoreUnits.y+=incY;
			if(corePositionInCoreUnits.y >= h)
				corePositionInCoreUnits.y = 0;
			break;
		}

		snapCoreToGrid();
		
		notifyListeners(null);
	}
	
	private void moveMouseToShownImage(Point p) {
		p.x-=mouseOffset.x;
		p.y-=mouseOffset.y;
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();
		moveMouseToShownImage(p);
		snapCoreToGrid(p);
		notifyListeners(null);
	}
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		moveMouseToShownImage(p);
		snapCoreToGrid(p);
		notifyListeners(null);
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		Point p = e.getPoint();
		moveMouseToShownImage(p);
		snapCoreToGrid(p);
		notifyListeners(null);
	}
	@Override
	public void mouseMoved(MouseEvent e) {}

	@Override
	public void stateChanged(ChangeEvent e) {
		notifyListeners(e);
	}	
}
