package mosaic.ui.bricked;

import io.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import javax.swing.Action;
import javax.swing.event.*;
import colors.LEGOColor;
import transforms.ToBricksTransform;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.actions.*;
import bricks.ToBricksType;

/**
 * Functions: 
 *  - turn on/off, (Toolbar, Key ALT+M)
 *  - set width/height=blocksize (Toolbar, ALT+arrows)
 *  - show symbols/colors, (Toolbar, Key ALT+C)
 *  - relative size (Toolbar zoom, ALT+-)
 * State: 
 *  - keyLocked (enable direction buttons, toggle using mouse)
 *  - coreImage
 *  - corePosition (on core image!, move with mouse or keyboard - depends on keylocked)
 *  - shownImage
 *  - bounds (w. shown image and free space)
 * @author LD
 */
public class Magnifier implements MouseListener, MouseMotionListener, KeyListener, ModelSaver<BrickGraphicsState> {
	public static final double SIZE_INC = 0.05;

	// functions:
	private Action enabled, showColors;
	private Dimension blockSize;
	private double size;
	// state (not saved):
	private boolean keyLocked;
	private BufferedImage shownImage;
	private int w, h;
	private Point corePosition;
	private List<ChangeListener> listeners;
	private Corner corner;
	private ToBricksTransform tbTransform;
	private Dimension parentComponentSize;

	public Magnifier(Model<BrickGraphicsState> model) {
		enabled = new MagnifierEnable(this);
		showColors = new MagnifierShowColors(this);
		reloadModel(model);
		corePosition = new Point(0,0);
		model.addModelSaver(this);
		listeners = new LinkedList<ChangeListener>();
		corner = Corner.SE;
	}
	
	public Action getEnabledAction() {
		return enabled;
	}
	public Action getShowColorsAction() {
		return showColors;
	}
	
	private boolean isEnabled() {
		return (Boolean)enabled.getValue(Action.SELECTED_KEY);
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		enabled.putValue(Action.SELECTED_KEY, model.get(BrickGraphicsState.MagnifierShow));
		if(!isEnabled())
			keyLocked = false;
		showColors.putValue(Action.SELECTED_KEY, model.get(BrickGraphicsState.MagnifierShowColors));
		blockSize = (Dimension)model.get(BrickGraphicsState.MagnifierBlockSize);
		size = (Double)model.get(BrickGraphicsState.MagnifierSize);		
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MagnifierShow, isEnabled());
		model.set(BrickGraphicsState.MagnifierShowColors, showColors());
		model.set(BrickGraphicsState.MagnifierBlockSize, blockSize);
		model.set(BrickGraphicsState.MagnifierSize, size);
	}
	
	private void enable(boolean enable) {
		enabled.putValue(Action.SELECTED_KEY, enable);

		notifyListeners();
	}
	
	private boolean showColors() {
		return (Boolean)showColors.getValue(Action.SELECTED_KEY);
	}
	
	public Dimension getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(Dimension blockSize) {
		this.blockSize = blockSize;
		sanify();
		notifyListeners();
	}

	public void changeBlockSizeWidth(int change) {
		blockSize.width += change;
		sanify();
		notifyListeners();
	}

	public void changeBlockSizeHeight(int change) {
		blockSize.height += change;
		sanify();
		notifyListeners();
	}
	
	private int bound(int a, int min, int max) {
		if(max < min)
			throw new IllegalStateException("Can't bound in [" + min + "," + max + "]");
		if(a < min)
			return min;
		if(a > max)
			return max;
		return a;
	}
	private double bound(double a, double min, double max) {
		if(max < min)
			throw new IllegalStateException("Can't bound in [" + min + "," + max + "]");
		if(a < min)
			return min;
		if(a > max)
			return max;
		return a;
	}
	
	private void sanify() {
		if(shownImage == null)
			return;
		
		// blockSize:
		int maxBlockSizeWidth = w/tbTransform.getToBricksType().getUnitWidth();
		blockSize.width = bound(blockSize.width, 1, maxBlockSizeWidth);
		int maxBlockSizeHeight = h/tbTransform.getToBricksType().getUnitHeight();
		blockSize.height = bound(blockSize.height, 1, maxBlockSizeHeight);
		
		// size:
		size = bound(size, SIZE_INC, 1);
		
		// corePosition:
		int bw = getBlockUnitSizeWidth();
		int bh = getBlockUnitSizeHeight();
		corePosition.x = bound(corePosition.x, 0, w-bw);
		if(corePosition.x % bw != 0)
			corePosition.x = 0;
		corePosition.y = bound(corePosition.y, 0, h-bh);
		if(corePosition.y % bh != 0)
			corePosition.y = 0;		
	}
	
	public void setTBTransform(ToBricksTransform tbTransform) {
		this.tbTransform = tbTransform;
		sanify();
	}

	public void adChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners() {
		if(!isEnabled())
			keyLocked = false; // in case enabled has changed
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	public void setCoreImage(int w, int h) {
		this.w = w;
		this.h = h;
		sanify();
	}

	public void setShownImage(BufferedImage shownImage) {
		this.shownImage = shownImage;
	}
	
	public BufferedImage getShownImage() {
		return shownImage;
	}

	/**
	 * Assert shownImage is shown on canvas.
	 * @param canvas
	 */
	public void draw(Dimension canvas, Graphics2D g2, ColorLegend colorListener) {
		// draw "bg" image:
		g2.drawImage(shownImage, null, 0, 0);

		// draw highlighting rectangle:
		double scaleX = shownImage.getWidth() / (double)w;
		double scaleY = shownImage.getHeight() / (double)h;
		int x = (int)Math.round(corePosition.x * scaleX);
		int y = (int)Math.round(corePosition.y * scaleY);
		int w = (int)Math.round(getBlockUnitSizeWidth() * scaleX); 
		int h = (int)Math.round(getBlockUnitSizeHeight() * scaleY); 
		
		Rectangle highlightingRect = new Rectangle(x, y, w, h);
		g2.setColor(Color.BLACK);
		g2.draw(highlightingRect);
		if(!isEnabled())
			return;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		MagnifierGlass glass = new MagnifierGlass(canvas);
		Rectangle glassRect = glass.getBounds();
		// draw / / lines
		parentComponentSize = new Dimension((int)(canvas.width/scaleX), (int)(canvas.height/scaleY));
		updateCorner();
		
		if(corner == Corner.NW || corner == Corner.SE) {
			g2.drawLine(highlightingRect.x, 
					highlightingRect.y+highlightingRect.height, 
					glassRect.x, 
					glassRect.y+glassRect.height);
			g2.drawLine(highlightingRect.x+highlightingRect.width, 
					highlightingRect.y, 
					glassRect.x+glassRect.width, 
					glassRect.y);			
		}
		else {
			g2.drawLine(highlightingRect.x, 
					highlightingRect.y, 
					glassRect.x, 
					glassRect.y);
			g2.drawLine(highlightingRect.x+highlightingRect.width, 
					highlightingRect.y+highlightingRect.height, 
					glassRect.x+glassRect.width, 
					glassRect.y+glassRect.height);		
		}

		// draw blow up:
		glass.paint(g2, colorListener);
	}

	public void mouseClicked(MouseEvent e) {
		if(isEnabled()) {
			keyLocked = !keyLocked;
			notifyListeners();		
		}
		else if(e.getClickCount() > 1){
			enable(true);
		}
	}
	
	private void snapCoreToGrid(Point mouseOnShown) {
		int mouseOnCoreX = (int)(mouseOnShown.x / (double)shownImage.getWidth() * w);
		int mouseOnCoreY = (int)(mouseOnShown.y / (double)shownImage.getHeight() * h);

		corePosition = new Point(mouseOnCoreX, mouseOnCoreY);		

		snapCoreToGrid();
	}
	
	private void snapCoreToGrid() {
		int unitW = getBlockUnitSizeWidth();
		int unitH = getBlockUnitSizeHeight();
//System.out.println("Snap to grid: " + corePosition + " on " + w + "x" + h + " w. grid " + unitW + "x" + unitH);
		corePosition.x = bound(corePosition.x, 0, w-1);
		corePosition.y = bound(corePosition.y, 0, h-1);
		
		corePosition.x = (corePosition.x/unitW)*unitW;		
		corePosition.y = (corePosition.y/unitH)*unitH;		
	}
	
	private void updateCorner() {
		if(parentComponentSize != null) {
			boolean west = corePosition.x < parentComponentSize.getWidth()/2;
			boolean north = corePosition.y < parentComponentSize.getHeight()/2;
			corner = Corner.get(!north, !west);
		}
	}

	public void mouseMoved(MouseEvent e) {
		if(keyLocked)
			return;
		
		snapCoreToGrid(e.getPoint());
		updateCorner();
		
		notifyListeners();
	}
	
	private int getBlockUnitSizeWidth() {
		return tbTransform.getToBricksType().getUnitWidth()*blockSize.width;
	}
	private int getBlockUnitSizeHeight() {
		return tbTransform.getToBricksType().getUnitHeight()*blockSize.height;
	}

	/**
	 * right/left: wrap around
	 * up down: no wrap around
	 */
	public void keyPressed(KeyEvent e) {
		if(!keyLocked)
			return;
		int key = e.getKeyCode();
		int incX = getBlockUnitSizeWidth();
		int incY = getBlockUnitSizeHeight();
		
		switch(key) {
		case KeyEvent.VK_LEFT:
			if(corePosition.x <= 0) {
				corePosition.x = w-1;
				if(corePosition.y <= 0) {
					corePosition.y = h-1;
				}
				else {
					corePosition.y-=incY;
				}
			}
			else {
				corePosition.x-=incX;
			}
			break;
		case KeyEvent.VK_RIGHT:
			if(corePosition.x >= w-incX) {
				corePosition.x = 0;
				if(corePosition.y >= h-incY) {
					corePosition.y = 0;
				}
				else
					corePosition.y+=incY;
			}
			else {
				corePosition.x+=incX;
			}
			break;
		case KeyEvent.VK_UP:
			if(corePosition.y <= 0)
				corePosition.y = h - 1;
			else
				corePosition.y-=incY;
			break;
		case KeyEvent.VK_DOWN:
			if(corePosition.y >= h-incY)
				corePosition.y = 0;
			else
				corePosition.y+=incY;
			break;
		}

		snapCoreToGrid();
		updateCorner();
		
		notifyListeners();
	}
	
	public void addSize() {
		size += SIZE_INC;
		if(size > 1) 
			size = 1;
		notifyListeners();
	}
	public void reduceSize() {
		size -= SIZE_INC;
		if(size < SIZE_INC) // also min.
			size = SIZE_INC;
		notifyListeners();
	}

	private enum Corner {
		NE, NW, SE, SW;

		public static Corner get(boolean north, boolean west) {
			if(north) {
				if(west) {
					return NW;
				}
				else {
					return NE;
				}
			}
			else {
				if(west) {
					return SW;
				}
				else {
					return SE;
				}				
			}
		}
	}

	private class MagnifierGlass {
		public static final int BUTTON_WIDTH = 12;

		private Dimension canvas;

		public MagnifierGlass(Dimension canvas) {
			this.canvas = canvas;
		}

		private Rectangle up() {
			return new Rectangle(
					BUTTON_WIDTH, 
					0, 
					getSize().width-2*BUTTON_WIDTH,
					BUTTON_WIDTH);
		}

		private Rectangle left() {
			return new Rectangle(
					0,
					BUTTON_WIDTH, 
					BUTTON_WIDTH, 
					getSize().height-2*BUTTON_WIDTH);
		}

		private Rectangle down() {
			return new Rectangle(
					BUTTON_WIDTH, 
					getSize().height-BUTTON_WIDTH, 
					getSize().width-2*BUTTON_WIDTH,
					BUTTON_WIDTH);
		}

		private Rectangle right() {
			return new Rectangle(
					getSize().width-BUTTON_WIDTH,
					BUTTON_WIDTH, 
					BUTTON_WIDTH, 
					getSize().height-2*BUTTON_WIDTH);
		}

		public Dimension getSize() {
			if(getBlockUnitSizeHeight() < getBlockUnitSizeWidth()) {
				int w = (int)Math.round(canvas.width * size);
				int h = (int)Math.round((double)w*getBlockUnitSizeHeight()/getBlockUnitSizeWidth());
				return new Dimension(w, h); 				
			}
			else {
				int h = (int)Math.round(canvas.height * size);
				int w = (int)Math.round((double)h/getBlockUnitSizeHeight()*getBlockUnitSizeWidth());
				return new Dimension(w, h);		
			}
		}

		public Point getPosition() {
			Dimension size = getSize();

			switch(corner) {
			case NW:
				return new Point(0,0);
			case NE:
				return new Point(canvas.width-size.width, 0);
			case SW:
				return new Point(0, canvas.height-size.height);
			case SE:
				return new Point(canvas.width-size.width, 
						canvas.height-size.height);
			default:
				throw new IllegalStateException("Corner: " + corner);
			}
		}

		public Rectangle getBounds() {
			return new Rectangle(getPosition(), getSize());
		}

		/**
		 * Paints itself from starting point, so be ware of translates
		 * @param g2
		 */
		public void paint(Graphics2D g2, ColorLegend colorListener) {
			Point position = getPosition();
			Dimension size = getSize();

			g2.translate(position.x, position.y);
			g2.setColor(Color.BLACK);

			if(keyLocked) {
				// draw buttons
				Rectangle[] buttons = new Rectangle[]{up(), right(), down(), left()};
				int pSize = BUTTON_WIDTH-4;
				Polygon triangle = new Polygon(
						new int[]{-pSize/2, pSize/2, 0}, 
						new int[]{-pSize/2, -pSize/2, pSize/2}, 3);
				for(int i = 0; i < buttons.length; i++) {
					Rectangle button = buttons[i];
					g2.setColor(Color.WHITE);
					g2.fill(button);
					g2.setColor(Color.BLACK);
					g2.draw(button);

					int centerX = (int)button.getCenterX();
					int centerY = (int)button.getCenterY();
					g2.translate(centerX, centerY);
					g2.rotate(Math.PI/2*(i+2));
					g2.draw(triangle);					
					g2.rotate(-Math.PI/2*(i+2));					
					g2.translate(-centerX, -centerY);
				}
				// draw position:
				int posX = corePosition.x/getBlockUnitSizeWidth();
				int posY = corePosition.y/getBlockUnitSizeHeight();
				String txt = posX + "," + posY;
				Rectangle bounds = up();
				g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, BUTTON_WIDTH-2));
				g2.drawString(txt, bounds.x + 2, (int)bounds.getCenterY() + g2.getFontMetrics().getAscent()/2);

				size.width -= 2*BUTTON_WIDTH;
				size.height -= 2*BUTTON_WIDTH;
				g2.translate(BUTTON_WIDTH, BUTTON_WIDTH);
			}

			// draw magnified:
			int basicUnitWidth = tbTransform.getToBricksType().getUnitWidth();
			int basicUnitHeight = tbTransform.getToBricksType().getUnitHeight();
			Rectangle basicUnitRect = new Rectangle(corePosition.x, corePosition.y, 
					getBlockUnitSizeWidth(), getBlockUnitSizeHeight());
			Set<LEGOColor> used;
			if(tbTransform.getToBricksType() == ToBricksType.snot) {
				if(showColors())
					used = tbTransform.drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, size);
				else
					used = tbTransform.drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, size);
			}
			else {
				if(showColors())
					used = tbTransform.getMainTransform().drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, size);
				else
					used = tbTransform.getMainTransform().drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, size);
			}
			colorListener.highlight(used);
		}
	}

	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseDragged(MouseEvent arg0) {}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {
		//System.out.println(arg0);
	}
	public void mousePressed(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}
