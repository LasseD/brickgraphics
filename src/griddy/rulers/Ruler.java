package griddy.rulers;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import griddy.*;
import griddy.zoom.*;

public class Ruler implements DisplayComponent, MouseListener, MouseMotionListener, ZoomListener {
	private static final long serialVersionUID = 4846393336196503759L;
	private BorderRuler brh, brv; // callback vars
	
	private Point2D.Double p1, p2;
	private LengthType lengthType;
	private Color color;

	private List<RulerListener> listeners;
	private volatile transient boolean isSlowValid;
	private volatile transient Point tmp;
	
	public Ruler(Zoom zoom, BorderRuler brh, BorderRuler brv) {
		this.brh = brh;
		this.brv = brv;
		listeners = new LinkedList<RulerListener>();
		zoom.addZoomListener(this);
	}
	
	public LengthType getLengthType() {
		return lengthType;
	}
	
	public double getDist() {
		Point2D.Double p2 = this.p2;
		if(p2 == null)
			p2 = new Point2D.Double(tmp.x, tmp.y);
		double xDist = (p1.x - p2.x)/lengthType.getUnitLength() / brh.getDist()*brh.getLengthType().getUnitLength();
		double yDist = (p1.y - p2.y)/lengthType.getUnitLength() / brv.getDist()*brv.getLengthType().getUnitLength();
		double pixelDist = Math.sqrt(xDist*xDist + yDist*yDist);
		return pixelDist;
	}

	public void setLengthType(LengthType lengthType) {
		isSlowValid = false;
		this.lengthType = lengthType;
		fireChanges();
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		isSlowValid = false;
		this.color = color;
		fireChanges();
	}
	
	public void addListener(RulerListener l) {
		listeners.add(l);
	}
	
	private void fireChanges() {
		for(RulerListener l : listeners) {
			l.rulerChanged(this);
		}
	}
	
	private class ScaleToolLengthTypeView extends JLabel implements BorderRulerListener {
		private static final long serialVersionUID = 630740450183320327L;
		private LengthType lt;
		private BorderRuler st;
		
		public ScaleToolLengthTypeView(BorderRuler st) {
			this.st = st;
			this.lt = st.getLengthType();
			st.addScaleListener(this, true);
			if(lt.getIcon() == null) {
				setText(lt.getText());
			}
			else {
				setIcon(lt.getIcon());
			}			
		}

		@Override
		public void borderRulerChanged(BorderRuler ignore) {
			if(lt != st.getLengthType()) {
				lt = st.getLengthType();
				if(lt.getIcon() != null) {
					setText(null);
					setIcon(lt.getIcon());
				}
				else {
					setIcon(null);
					setText(lt.getText());
				}					
			}
		}
	}
	
	private String formatPoint(Point2D.Double p) {
		double x = brh.getDist() / (brh.getEnd()-brh.getStart()) * p.x;
		double y = brv.getDist() / (brv.getEnd()-brv.getStart()) * p.y;
		return String.format("(%.2f,%.2f)", x, y);
	}
	
	private class PointsLabel extends JLabel {
		private static final long serialVersionUID = 5560003153793883901L;

		public void update(BorderRuler sth, BorderRuler stv) {
			if(p1 == null) { // report position.
				if(tmp != null)
					setText(formatPoint(new Point2D.Double(tmp.x, tmp.y)));
				else
					setText("");
			}
			else {
				Point2D.Double p2 = Ruler.this.p2;
				if(p2 == null)
					p2 = new Point2D.Double(tmp.x, tmp.y);
				
				setText(formatPoint(p1));
			}			
		}
	}
	
	public Component makeGUIComponents(final BorderRuler sth, final BorderRuler stv) {
		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.add(new JLabel("("));
		panel.add(new ScaleToolLengthTypeView(sth));
		panel.add(new JLabel("x"));
		panel.add(new ScaleToolLengthTypeView(stv));
		panel.add(new JLabel(") :"));
		final PointsLabel label = new PointsLabel();
		panel.add(label);
		
		addListener(new RulerListener() {			
			@Override
			public void rulerChanged(Ruler ruler) {								
				label.update(sth, stv);
			}
		});
		BorderRulerListener listener = new BorderRulerListener() {			
			@Override
			public void borderRulerChanged(BorderRuler scaleTool) {
				label.update(sth, stv);				
			}
		};
		sth.addScaleListener(listener, false);
		sth.addScaleListener(listener, false);
		
		return panel;
	}
	
	private static final int DOT_RADIUS = 2;
	@Override
	public void drawQuick(Graphics2D g2) {
		isSlowValid = true;
		g2.setColor(color);
		if(p1 != null) {
			Ellipse2D.Double oval = new Ellipse2D.Double(p1.x-DOT_RADIUS, p1.y-DOT_RADIUS, DOT_RADIUS*2, DOT_RADIUS*2);
			g2.draw(oval);

			if(p2 != null || tmp != null) {
				Point2D.Double p = ((p2 == null) ? new Point2D.Double(tmp.x, tmp.y) : p2);
				oval = new Ellipse2D.Double(p.x-DOT_RADIUS, p.y-DOT_RADIUS, DOT_RADIUS*2, DOT_RADIUS*2);
				g2.draw(oval);
				Line2D.Double line = new Line2D.Double(p1.x, p1.y, p.x, p.y);
				g2.draw(line);
			}
		}
	}

	@Override
	public void drawSlow(BufferedImage baseImage, Graphics2D g2) {
		drawQuick(g2);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		isSlowValid = false;
		Point p = e.getPoint();
		if(p1 == null) {
			p1 = new Point2D.Double(p.x, p.y);			
		}
		else if(p2 == null)
			p2 = new Point2D.Double(p.x, p.y);
		else {
			p1 = p2 = null;
		}
		fireChanges();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		tmp = e.getPoint();
		fireChanges();
	}
	@Override
	public void mouseExited(MouseEvent e) {
		tmp = null;
		fireChanges();
	}
	@Override
	public void mousePressed(MouseEvent arg0) {}
	@Override
	public void mouseReleased(MouseEvent arg0) {}
	@Override
	public void mouseDragged(MouseEvent arg0) {}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(p1 != null && p2 == null)
			isSlowValid = false;
		tmp = e.getPoint();
		fireChanges();
	}

	@Override
	public void zoomChanged(double newZoom, double zoomChangeFactor) {
		isSlowValid = false;
		if(p1 != null) {
			p1.x *= zoomChangeFactor;
			p1.y *= zoomChangeFactor;
		}
		if(p2 != null) {
			p2.x *= zoomChangeFactor;
			p2.y *= zoomChangeFactor;
		}
	}

	@Override
	public boolean isSlowValid() {
		return isSlowValid;
	}
}
