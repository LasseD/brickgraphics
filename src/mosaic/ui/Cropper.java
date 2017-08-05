package mosaic.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.event.*;
import mosaic.io.*;

import java.util.*;
import java.util.List;
import io.*;

public class Cropper implements MouseListener, MouseMotionListener, ModelHandler<BrickGraphicsState> {
	public static final int TOLERANCE = 3;

	private Rectangle mouseImageRect;
	private Drag state;
	private Rectangle2D.Double cropRect;
	private Point lastPress;
	private List<ChangeListener> changeListeners, pointerIconListeners;
	private boolean enabled;
	
	public Cropper(Model<BrickGraphicsState> model) {
		model.addModelHandler(this);
		state = Drag.NONE;
		lastPress = null;
		changeListeners = new LinkedList<ChangeListener>();
		pointerIconListeners = new LinkedList<ChangeListener>();
		handleModelChange(model);
	}
	
	public Cursor getCursor() {
		return state.getCursor();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void switchEnabled() {
		enabled = !enabled;
		notifyChangeListeners();
	}
	
	public void addChangeListener(ChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void addPointerIconListener(ChangeListener listener) {
		pointerIconListeners.add(listener);
	}
	
	public float getWidthToHeight() {
		return (float)(cropRect.width/cropRect.height);
	}
	
	public Rectangle getCrop(Rectangle r) {
		return getCrop(r.x, r.y, r.width, r.height);
	}
		
	public Rectangle getCrop(int x, int y, int w, int h) {
		int rx = (int)Math.round(cropRect.x*w);
		rx = clamp(rx, 0, w);
		
		int ry = (int)Math.round(cropRect.y*h);
		ry = clamp(ry, 0, h);
		
		int rw = (int)Math.round(cropRect.width*w);
		rw = clamp(rw, 0, w-rx);
		
		int rh= (int)Math.round(cropRect.height*h);
		rh = clamp(rh, 0, h-ry);

		return new Rectangle(rx, ry, rw, rh);
	}
	private static int clamp(int i, int min, int max) {
		if(i < min)
			return min;
		if(i > max)
			return max;
		return i;
	}
	
	public static void drawCropHighlight(Graphics2D g2, Rectangle rect) {
		g2.setColor(Color.BLACK);
		g2.drawRect(rect.x-1, rect.y-1, rect.width+1, rect.height+1);
		g2.drawRect(rect.x-2, rect.y-2, rect.width+3, rect.height+3);
		g2.setColor(Color.WHITE);
		Stroke formerStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 0f));
		g2.drawRect(rect.x-1, rect.y-2, rect.width+1, rect.height+3);
		g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 3f));
		g2.drawRect(rect.x-2, rect.y-1, rect.width+3, rect.height+1);
		g2.setStroke(formerStroke);
	}
	
	public BufferedImage drawImageWithCropRectAndRedLines(BufferedImage in) {
		Rectangle rect = getCrop(0, 0, in.getWidth(), in.getHeight());

		int inW = in.getWidth();
		int outW = inW+4;
		int inH = in.getHeight();
		int outH = inH+4;
		
		BufferedImage polluted = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = (Graphics2D)polluted.getGraphics();
		g2.translate(2, 2);
		g2.drawImage(in, null, 0, 0);

		drawCropHighlight(g2, rect);

		g2.setStroke(new BasicStroke(1f));
		g2.setColor(Color.RED);
		switch(state) {
		case SW:
			g2.draw(new Line2D.Double(rect.x-1, 0, rect.x-1, inH));
			break;
		case SE:
			g2.draw(new Line2D.Double(0, rect.getMaxY(), inW, rect.getMaxY()));
			break;
		case NW:
			g2.draw(new Line2D.Double(0, rect.y-1, inW, rect.y-1));
			break;
		case NE:
			g2.draw(new Line2D.Double(rect.getMaxX(), 0, rect.getMaxX(), inH));
			break;
		case ALL:
		case E:
		case N:
		case NONE:
		case S:
		case W:
			break;
		}
		
		g2.translate(-2, -2);
		
		return polluted;
	}
	
	private enum Drag {
		N(Cursor.N_RESIZE_CURSOR), 
		NE(Cursor.NE_RESIZE_CURSOR), 
		NW(Cursor.NW_RESIZE_CURSOR), 
		S(Cursor.S_RESIZE_CURSOR), 
		SE(Cursor.SE_RESIZE_CURSOR), 
		SW(Cursor.SW_RESIZE_CURSOR), 
		E(Cursor.E_RESIZE_CURSOR), 
		W(Cursor.W_RESIZE_CURSOR),
		ALL(Cursor.MOVE_CURSOR),
		NONE(Cursor.DEFAULT_CURSOR);
		
		private Cursor cursor;
		private Drag(int cursorType) {
			cursor = Cursor.getPredefinedCursor(cursorType);
		}
		public Cursor getCursor() {
			return cursor;
		}
		public static Drag intersecting(Point p, Rectangle r) {
			Rectangle big = new Rectangle(r);
			big.grow(TOLERANCE, TOLERANCE);
			if(!big.contains(p)) {
				return NONE;
			}
			
			Rectangle small = new Rectangle(r);
			small.grow(-TOLERANCE, -TOLERANCE);
			if(small.contains(p)) {
				return ALL;
			}
			
			Rectangle left = new Rectangle(big.x, big.y, 2*TOLERANCE, big.height);
			Rectangle right = new Rectangle(left);
			right.translate(r.width, 0);
			
			Rectangle up = new Rectangle(big.x, big.y, big.width, 2*TOLERANCE);
			Rectangle down = new Rectangle(up);
			down.translate(0, r.height);
			
			if(left.contains(p)) {
				if(up.contains(p))
					return NW;
				if(down.contains(p))
					return SW;
				return W;
			}
			if(right.contains(p)) {
				if(up.contains(p))
					return NE;
				if(down.contains(p))
					return SE;
				return E;				
			}
			else if(up.contains(p)) {
				return N;
			}
			else {// down contains point:
				return S;
			}
		}
	}
	
	private void notifyChangeListeners() {
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener listener : changeListeners) {
			listener.stateChanged(e);
		}
	}

	private void notifyPointerIconListeners() {
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener listener : pointerIconListeners) {
			listener.stateChanged(e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() > 1) {			
			switchEnabled();
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {}
	
	@Override
	public void mouseExited(MouseEvent e) {
		state = Drag.NONE;
		notifyChangeListeners();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		lastPress = e.getPoint();
		lastPress.translate(-2, -2);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastPress = null;
		notifyChangeListeners();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(lastPress == null)
			return;
		Point p = e.getPoint();		
		p.translate(-2, -2);

		int diffX = p.x - lastPress.x;
		int diffY = p.y - lastPress.y;
		Rectangle r = getCrop(mouseImageRect);
		boolean scaleX = false;
		boolean scaleY = false;
		boolean scaleW = false;
		boolean scaleH = false;
		
		switch(state) {
		case NONE:
			break;
		case ALL:
			r.translate(diffX, diffY);
			break;

		case NW: // N press:
			scaleX = true;
		case N:
			r.y += diffY;
			r.height -= diffY;
			break;
		
		case SE: // S press:
			scaleW = true;
		case S:
			r.height += diffY;
			break;
		
		case SW: // W press:
			scaleH = true;
		case W:
			r.x += diffX;
			r.width -= diffX;
			break;
		
		case NE: // E press:
			scaleY = true;
		case E:
			r.width += diffX;
			break;
		default:
			throw new IllegalStateException("Enum Drag broken:" + state);
		}
		
		// fix r:
		if(r.height < 1)
			r.height = 1;
		if(r.width < 1)
			r.width = 1;
		if(r.x < 0)
			r.x = 0;
		if(r.y < 0)
			r.y = 0;
		if(r.x + r.width >= mouseImageRect.width)
			r.x = mouseImageRect.width - r.width;
		if(r.y + r.height >= mouseImageRect.height)
			r.y = mouseImageRect.height - r.height;
		
		// fix unitRect:
		double ux = (double)r.x / mouseImageRect.width;
		double uy = (double)r.y / mouseImageRect.height;
		double uw = (double)r.width / mouseImageRect.width;
		double uh = (double)r.height / mouseImageRect.height;

		// fix unitRect for keeping scale/aspect ratio:
		double scaleWH = cropRect.width / cropRect.height;
		double scaleHW = cropRect.height / cropRect.width;
		if(scaleX) {
			double dX = (uy-cropRect.y)*scaleWH;
			ux = cropRect.x+dX;
			uw = cropRect.width-dX;
		}
		else if(scaleY) {
			double dY = (uw-cropRect.width)*scaleHW;
			uy = cropRect.y-dY;
			uh = cropRect.height+dY;			
		}
		else if(scaleW) {
			double dW = (uh-cropRect.height)*scaleWH;
			uw = cropRect.width+dW;			
		}
		else if(scaleH) {
			double dH = (uw-cropRect.width)*scaleHW;
			uh = cropRect.height+dH;			
		}

		cropRect = new Rectangle2D.Double(ux, uy, uw, uh);
		
		lastPress = p;
		notifyChangeListeners();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(mouseImageRect == null)
			return;
		Point p = e.getPoint();
		p.translate(-2-mouseImageRect.x, -2);
		if(state != (state = Drag.intersecting(p, getCrop(mouseImageRect))))
			notifyPointerIconListeners();
	}

	public void setMouseImageRect(Rectangle r) {
		this.mouseImageRect = r;
	}

	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareCrop, cropRect);
		model.set(BrickGraphicsState.PrepareCropEnabled, enabled);
	}

	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		cropRect = (Rectangle2D.Double)model.get(BrickGraphicsState.PrepareCrop);		
		enabled = (Boolean)model.get(BrickGraphicsState.PrepareCropEnabled);
	}
}
