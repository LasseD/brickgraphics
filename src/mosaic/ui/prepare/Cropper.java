package mosaic.ui.prepare;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.event.*;
import mosaic.io.*;
import java.util.*;
import java.util.List;
import io.*;

public class Cropper implements MouseListener, MouseMotionListener, ModelSaver<BrickGraphicsState> {
	public static final int TOLERANCE = 3;

	private Dimension mouseImage;
	private Drag state;
	private Rectangle2D.Double unitRect;
	private Point lastPress;
	private List<ChangeListener> listeners;
	private boolean enabled;
	
	public Cropper(Model<BrickGraphicsState> model) {
		model.addModelSaver(this);
		state = Drag.NONE;
		unitRect = (Rectangle2D.Double)model.get(BrickGraphicsState.PrepareCrop);		
		enabled = (Boolean)model.get(BrickGraphicsState.PrepareCropEnabled);
		lastPress = null;
		listeners = new LinkedList<ChangeListener>();
	}
	
	public Cursor getCursor() {
		return state.getCursor();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void switchEnabled() {
		enabled = !enabled;
		notifyListeners();
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public Rectangle getCrop(Dimension picture) {
		return getCrop(picture.width, picture.height);
	}
		
	public Rectangle getCrop(int pw, int ph) {
		int rx = (int)Math.round(unitRect.x*pw);
		rx = cut(rx, 0, pw);
		
		int ry = (int)Math.round(unitRect.y*ph);
		ry = cut(ry, 0, ph);
		
		int rw = (int)Math.round(unitRect.width*pw);
		rw = cut(rw, 0, pw-rx);
		
		int rh= (int)Math.round(unitRect.height*ph);
		rh = cut(rh, 0, ph-ry);

		return new Rectangle(rx, ry, rw, rh);
	}
	private int cut(int i, int min, int max) {
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
		g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 0f));
		g2.drawRect(rect.x-1, rect.y-2, rect.width+1, rect.height+3);
		g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 3f));
		g2.drawRect(rect.x-2, rect.y-1, rect.width+3, rect.height+1);
	}
	
	public BufferedImage pollute(BufferedImage in) {
		Rectangle rect = getCrop(in.getWidth(), in.getHeight());

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
	
	public void notifyListeners() {
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() > 1) {			
			switchEnabled();
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	
	public void mouseExited(MouseEvent e) {
		state = Drag.NONE;
		notifyListeners();
	}

	public void mousePressed(MouseEvent e) {
		lastPress = e.getPoint();
		lastPress.translate(-2, -2);
	}

	public void mouseReleased(MouseEvent e) {
		lastPress = null;
	}

	public void mouseDragged(MouseEvent e) {
		if(lastPress == null)
			return;
		Point p = e.getPoint();		
		p.translate(-2, -2);

		int diffX = p.x - lastPress.x;
		int diffY = p.y - lastPress.y;
		Rectangle r = getCrop(mouseImage);
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
		if(r.x + r.width >= mouseImage.width)
			r.x = mouseImage.width - r.width;
		if(r.y + r.height >= mouseImage.height)
			r.y = mouseImage.height - r.height;
		
		// fix unitRect:
		double ux = (double)r.x / mouseImage.width;
		double uy = (double)r.y / mouseImage.height;
		double uw = (double)r.width / mouseImage.width;
		double uh = (double)r.height / mouseImage.height;

		// fix unitRect for keeping scale/aspect ratio:
		double scaleWH = unitRect.width / unitRect.height;
		double scaleHW = unitRect.height / unitRect.width;
		if(scaleX) {
			double dX = (uy-unitRect.y)*scaleWH;
			ux = unitRect.x+dX;
			uw = unitRect.width-dX;
		}
		else if(scaleY) {
			double dY = (uw-unitRect.width)*scaleHW;
			uy = unitRect.y-dY;
			uh = unitRect.height+dY;			
		}
		else if(scaleW) {
			double dW = (uh-unitRect.height)*scaleWH;
			uw = unitRect.width+dW;			
		}
		else if(scaleH) {
			double dH = (uw-unitRect.width)*scaleHW;
			uh = unitRect.height+dH;			
		}

		unitRect = new Rectangle2D.Double(ux, uy, uw, uh);
		
		lastPress = p;
		notifyListeners();
	}

	public void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		p.translate(-2, -2);
		if(mouseImage != null && state != (state = Drag.intersecting(p, getCrop(mouseImage))))
			notifyListeners();
	}

	public void setMouseImage(BufferedImage image) {
		this.mouseImage = new Dimension(image.getWidth(), image.getHeight());
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareCrop, unitRect);
		model.set(BrickGraphicsState.PrepareCropEnabled, enabled);
	}
}
