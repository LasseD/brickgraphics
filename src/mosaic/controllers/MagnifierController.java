package mosaic.controllers;

import io.*;

import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.*;

import javax.swing.event.*;
import transforms.ToBricksTransform;
import mosaic.io.BrickGraphicsState;
import mosaic.rendering.PipelineMosaicListener;

/**
 * Functions: 
 *  - turn on/off
 *  - turn legend on/off
 *  - set width/height=size. One unit in width is one "block" in mosaic shown.
 *  - switch view of symbols/colors (matching legend)
 * @author LD
 */
public class MagnifierController implements ChangeListener, IChangeMonitor, KeyListener, ModelHandler<BrickGraphicsState>, PipelineMosaicListener {
	private List<ChangeListener> listeners; // such as GUI components (actual magnifier) and bricked view with rectangle.
	private UIController uiController;

	private Dimension sizeInMosaicBlocks; // with block size of core image as unit
	
	private Point corePositionInCoreUnits; // on core image - moved by core image block size.
	private Dimension coreImageSizeInCoreUnits; // to move mouse on
	private ToBricksTransform tbTransform; // with info for core image
	private Dimension shownImageSizeInPixels;

	public MagnifierController(Model<BrickGraphicsState> model, UIController uiController) {
		this.uiController = uiController;
		uiController.addChangeListener(this);
		listeners = new LinkedList<ChangeListener>();
		handleModelChange(model);
		corePositionInCoreUnits = new Point(0,0);
		model.addModelHandler(this);
	}
	
	public Dimension getSizeInMosaicBlocks() {
		return sizeInMosaicBlocks;
	}
	public Dimension getSizeInUnits() {
		return new Dimension(sizeInMosaicBlocks.width * tbTransform.getToBricksType().getUnitWidth(),
						     sizeInMosaicBlocks.height * tbTransform.getToBricksType().getUnitHeight());
	}

	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		sizeInMosaicBlocks = (Dimension)model.get(BrickGraphicsState.MagnifierSize);		
	}
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MagnifierSize, sizeInMosaicBlocks);
	}

	public void setWidthInMosaicBlocks(int w) {
		if(this.sizeInMosaicBlocks.width == w)
			return;
		this.sizeInMosaicBlocks.width = w;
		sanify();
		notifyListeners(null);
	}

	public void setHeightInMosaicBlocks(int h) {
		if(this.sizeInMosaicBlocks.height == h)
			return;
		this.sizeInMosaicBlocks.height = h;
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
		if(coreImageSizeInCoreUnits == null || tbTransform == null || sizeInMosaicBlocks == null)
			return;
		
		// sizeInMosaicBlocks:
		int maxBlockSizeWidth = coreImageSizeInCoreUnits.width/tbTransform.getToBricksType().getUnitWidth();
		sizeInMosaicBlocks.width = bound(sizeInMosaicBlocks.width, 1, maxBlockSizeWidth);
		int maxBlockSizeHeight = coreImageSizeInCoreUnits.height/tbTransform.getToBricksType().getUnitHeight();
		sizeInMosaicBlocks.height = bound(sizeInMosaicBlocks.height, 1, maxBlockSizeHeight);
		
		// corePosition:
		int bw = getMagnifierWidthInCoreUnits();
		int bh = getMagnifierHeightInCoreUnits();
		corePositionInCoreUnits.x = bound(corePositionInCoreUnits.x, 0, coreImageSizeInCoreUnits.width-1);
		if(corePositionInCoreUnits.x % bw != 0)
			corePositionInCoreUnits.x = 0;
		corePositionInCoreUnits.y = bound(corePositionInCoreUnits.y, 0, coreImageSizeInCoreUnits.height-1);
		if(corePositionInCoreUnits.y % bh != 0)
			corePositionInCoreUnits.y = 0;		
	}
	
	public void setTBTransform(ToBricksTransform tbTransform) {
		this.tbTransform = tbTransform;
		sanify();
	}

	@Override
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

	public int getMagnifierPage() {
		int x = corePositionInCoreUnits.x/getMagnifierWidthInCoreUnits();
		int y = corePositionInCoreUnits.y/getMagnifierHeightInCoreUnits();
		int w = (coreImageSizeInCoreUnits.width+getMagnifierWidthInCoreUnits()-1)/getMagnifierWidthInCoreUnits();
		return x + y*w;
	}

	public Dimension getCoreImageSizeInCoreUnits() {
		return coreImageSizeInCoreUnits;
	}
	
	public void setShownImageSize(Dimension shownImageSizeInPixels) {
		this.shownImageSizeInPixels = shownImageSizeInPixels;
		sanify();
	}
	
	public Dimension getShownImageSizeInPixels() {
		return shownImageSizeInPixels;
	}
	
	public void moveMagnifierRight() {
		moveMagnifier(KeyEvent.VK_RIGHT);
	}
	public void moveMagnifierLeft() {
		moveMagnifier(KeyEvent.VK_LEFT);
	}
	public void moveMagnifierUp() {
		moveMagnifier(KeyEvent.VK_UP);
	}
	public void moveMagnifierDown() {
		moveMagnifier(KeyEvent.VK_DOWN);
	}
	private void moveMagnifier(int keyEventVKDirection) {
		if(!uiController.showMagnifier())
			return;
		
		int incX = getMagnifierWidthInCoreUnits();
		int incY = getMagnifierHeightInCoreUnits();
		
		int w = coreImageSizeInCoreUnits.width;
		int h = coreImageSizeInCoreUnits.height;
		
		switch(keyEventVKDirection) {
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
		default:
			return; // Do not notify listeners.
		}				
		notifyListeners(null);
	}
	
	/**
	 * right/left: wrap around
	 * up down: no wrap around
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		moveMagnifier(e.getKeyCode());
	}
	
	@Override
	public void keyReleased(KeyEvent e) {}
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void stateChanged(ChangeEvent e) {
		notifyListeners(e);
	}

	@Override
	public void mosaicChanged(Dimension mosaicImageSize) {
		this.coreImageSizeInCoreUnits = mosaicImageSize;
		sanify();
	}
}
