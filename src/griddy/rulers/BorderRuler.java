package griddy.rulers;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import griddy.*;
import griddy.zoom.*;

public class BorderRuler implements Serializable, BorderRulerListener, ZoomListener {
	public static final int RULER_WIDTH = 26;
	private static final long serialVersionUID = 1098773918689574302L;
	private boolean isHorizontal, isRulerLocked;
	private transient boolean isRulerActive;
	private double start, end, dist;
	private LengthType lengthType;
	private transient List<BorderRulerListener> scaleListeners, scaleListenersViews;
	private transient volatile boolean isSlowValid;
	
	public BorderRuler(boolean isHorizontal) {
		this.isHorizontal = isHorizontal;
		start = 100;
		end = 200;
		lengthType = LengthType.brickWidth;
		dist = 5;
		isRulerLocked = true;
	}
	
	public void conformTo(BorderRuler other) {
		assert(isHorizontal == other.isHorizontal);
		isSlowValid = false;
		start = other.start;
		end = other.end;
		dist = other.dist;
		lengthType = other.lengthType;
		isRulerLocked = other.isRulerLocked;
		fireStateChange(this, false);
	}
	
	public void addScaleListener(BorderRulerListener listener, boolean isView) {
		if(scaleListeners == null) {
			scaleListeners = new LinkedList<BorderRulerListener>();
			scaleListenersViews = new LinkedList<BorderRulerListener>();
		}
		scaleListeners.add(listener);
		if(isView)
			scaleListenersViews.add(listener);
	}
	
	public boolean isRulerActive() {
		return isRulerActive;
	}

	public boolean isHorizontal() {
		return isHorizontal;
	}
	
	private void fireStateChange(BorderRuler scaleTool, boolean onlyViews) {
		if(scaleListeners == null)
			return;
		if(onlyViews) {
			for(BorderRulerListener l : scaleListenersViews) {
				l.borderRulerChanged(scaleTool);
			}						
		}
		else {
			for(BorderRulerListener l : scaleListeners) {
				l.borderRulerChanged(scaleTool);
			}			
		}
	}
	
	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		if(end <= start)
			throw new IllegalArgumentException("end <= start");
		isSlowValid = false;
		this.start = start;
		fireStateChange(this, false);
	}

	public double getEnd() {
		return end;
	}

	public void setEnd(double end) {
		if(end <= start)
			throw new IllegalArgumentException("end <= start");
		isSlowValid = false;
		this.end = end;
		fireStateChange(this, false);
	}

	public double getDist() {
		return dist;
	}

	public void setDist(double dist) {
		if(dist <= 0)
			throw new IllegalArgumentException("dist <= 0");
		isSlowValid = false;
		this.dist = dist;
		fireStateChange(this, false);
	}

	public LengthType getLengthType() {
		return lengthType;
	}

	public void setLengthType(LengthType lengthType) {
		isSlowValid = false;
		this.lengthType = lengthType;
		fireStateChange(this, false);
	}

	public boolean isRulerLocked() {
		return isRulerLocked;
	}

	public void setRulerLocked(boolean isRulerLocked) {
		this.isRulerLocked = isRulerLocked;
		fireStateChange(this, true);
	}

	public DisplayComponent makeDisplayLineComponent() {
		return new DisplayComponent() {
			private static final long serialVersionUID = 1L;
			private static final int STROKE_PERIOD = 4;
			private final Stroke stroke1 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{STROKE_PERIOD, STROKE_PERIOD}, 0);
			private final Stroke stroke2 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1, new float[]{STROKE_PERIOD, STROKE_PERIOD}, STROKE_PERIOD);
			
			@Override
			public void drawSlow(BufferedImage baseImage, Graphics2D g2) {
				drawQuick(g2);
			}
			
			@Override
			public void drawQuick(Graphics2D g2) {
				if(!isRulerActive) {
					isSlowValid = true;
					return;					
				}

				Stroke tmpStroke = g2.getStroke();
				g2.setStroke(stroke1);
				g2.setColor(Color.BLACK);
				Rectangle rect = g2.getClipBounds();
				int start = (int)(Math.round(BorderRuler.this.start));
				int end = (int)(Math.round(BorderRuler.this.end));
				if(isHorizontal) {
					g2.drawLine(start, rect.y, start, rect.y+rect.height);
					g2.drawLine(end, rect.y, end, rect.y+rect.height);
					g2.setStroke(stroke2);
					g2.setColor(Color.WHITE);
					g2.drawLine(start, rect.y, start, rect.y+rect.height);
					g2.drawLine(end, rect.y, end, rect.y+rect.height);
				}
				else {
					g2.drawLine(rect.x, start, rect.x+rect.width, start);
					g2.drawLine(rect.x, end, rect.x+rect.width, end);
					g2.setStroke(stroke2);
					g2.setColor(Color.WHITE);
					g2.drawLine(rect.x, start, rect.x+rect.width, start);
					g2.drawLine(rect.x, end, rect.x+rect.width, end);
				}
				g2.setStroke(tmpStroke);
				isSlowValid = true;
			}

			@Override
			public boolean isSlowValid() {
				return isSlowValid;
			}
		};
	}
	
	private class WellBehaveComboBox extends JComboBox<LengthType> {
		private static final long serialVersionUID = -540200893271836013L;
		private boolean ignoreActions;
		private List<ActionListener> actionListeners;
		
		public WellBehaveComboBox(LengthType[] values) {
			super(values);
			ignoreActions = false;
			actionListeners = new LinkedList<ActionListener>();
			super.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(ignoreActions)
						return;
					for(ActionListener l : actionListeners)
						l.actionPerformed(e);
				}
			});
			setRenderer(new ListCellRenderer<LengthType>() {				
				@Override
				public Component getListCellRendererComponent(
						JList<? extends LengthType> list, LengthType value,
						int index, boolean isSelected, boolean cellHasFocus) {
					JComponent component = value.makeDisplayComponent();
					int width = Math.max(component.getPreferredSize().width, 32);
					component.setPreferredSize(new Dimension(width, 32));
					return component;
				}
			});
		}
		
		@Override
		public void addActionListener(ActionListener l) {
			actionListeners.add(l);
		}

		public void setSelectedItemNoNotify(Object o) {
			ignoreActions = true;
			setSelectedItem(o);
			ignoreActions = false;
		}
	}
	
	public JPanel makeTextFieldsComponent() {
		JPanel mainPanel = new JPanel(new FlowLayout());
		final JTextField numberField = new JTextField(String.format("%.2f", getDist()), 6);
		numberField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {				
					double d = Double.parseDouble(numberField.getText());
					if(d <= 0)
						numberField.setText(String.format("%.2f", getDist()));
					else
						setDist(d);
				}
				catch(Exception e) {
					numberField.setText(String.format("%.2f", getDist()));
				}
			}
		});
		
		final WellBehaveComboBox typeBox = new WellBehaveComboBox(LengthType.values());
		typeBox.setSelectedItemNoNotify(getLengthType());
		typeBox.setEditable(false);
		typeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ignore) {
				setLengthType((LengthType)typeBox.getSelectedItem());
			}
		});
		
		BorderRulerListener listener = new BorderRulerListener() {
			@Override
			public void borderRulerChanged(BorderRuler ignore) {
				numberField.setText(String.format("%.2f", getDist()));				
				typeBox.setSelectedItemNoNotify(getLengthType()); // fires event change!
			}
		};
		addScaleListener(listener, true);

		mainPanel.add(numberField);
		mainPanel.add(typeBox);		

		return mainPanel;
	}
	
	public Ruler makeRuler() {
		final Ruler ruler = new Ruler();

		BorderRulerListener listener = new BorderRulerListener() {
			@Override
			public void borderRulerChanged(BorderRuler ignore) {
				ruler.repaint();
			}
		};
		addScaleListener(listener, true);
		
		return ruler;
	}
	
	@Override
	public void borderRulerChanged(BorderRuler scaleTool) {
		if(scaleTool == this) {
			return;
		}
		isSlowValid = false;
		// update dist to match it:
		double theirSpan = (scaleTool.getEnd() - scaleTool.getStart()) / scaleTool.getLengthType().getUnitLength();
		double ourSpan = (getEnd() - getStart()) / getLengthType().getUnitLength();
		//System.out.println("Scale " + isHorizontal + " changes with " + (scaleTool.getDist() / theirSpan * ourSpan) + " from " + scaleTool.isHorizontal);
		dist = scaleTool.getDist() / theirSpan * ourSpan;
		
		fireStateChange(scaleTool, false);
	}

	@Override
	public void zoomChanged(double newZoom, double zoomChangeFactor) {
		isSlowValid = false;
		start *= zoomChangeFactor;
		end *= zoomChangeFactor;
		fireStateChange(this, true);
	}
	
	private enum PullPlace {
		None, Start, End, Both;
	}
	
	private class Ruler extends JPanel {
		private static final long serialVersionUID = 3699617908206283489L;
		private Point startPlace;		
		
		private PullPlace getPullType(Point p) {
			int tolerance = 3;
			int s = p.x;
			if(!isHorizontal) {
				s = p.y;
			}
			if(Math.abs(s-start) <= tolerance)
				return PullPlace.Start;
			if(Math.abs(s-end) <= tolerance)
				return PullPlace.End;
			if(s > start && s < end)
				return PullPlace.Both;
			return PullPlace.None;
		}

		@Override
		public Dimension getPreferredSize() {
			if(isHorizontal) {
				return new Dimension(super.getPreferredSize().width, RULER_WIDTH);
			}
			else {
				return new Dimension(RULER_WIDTH, super.getPreferredSize().height);
			}
		}		
		
		public Ruler() {
			addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent arg0) {
					startPlace = null;
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
					startPlace = e.getPoint();
				}
				
				@Override
				public void mouseExited(MouseEvent arg0) {
					setCursor(Cursor.getDefaultCursor());
					isRulerActive = false;
					startPlace = null;
					fireStateChange(BorderRuler.this, true);
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					updateCursor(e.getPoint());
					isRulerActive = true;
					fireStateChange(BorderRuler.this, true);
				}
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
					// NOP
				}
			});
			addMouseMotionListener(new MouseMotionListener() {				
				@Override
				public void mouseMoved(MouseEvent e) {
					updateCursor(e.getPoint());
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					isSlowValid = false;
					Point p = e.getPoint();
					if(startPlace == null)
						return;
					PullPlace pp = getPullType(startPlace);

					if(isHorizontal) {
						switch(pp) {
							case None:
								break;
							case Start:
								start = p.x;
								break;
							case End:
								end = p.x;
								break;
							case Both:
								int diff = - startPlace.x + p.x;
								start += diff;
								end += diff;
								break;
							default:
								throw new IllegalStateException();
						}
					}
					else {
						switch(pp) {
							case None:
								break;
							case Start:
								start = p.y;
								break;
							case End:
								end = p.y;
								break;
							case Both:
								int diff = - startPlace.y + p.y;
								start += diff;
								end += diff;
								break;
							default:
								throw new IllegalStateException();
						}
					}
					startPlace = p;
					ensureSanity();
					fireStateChange(BorderRuler.this, false);
					repaint();
				}
			});
		}
		
		private void ensureSanity() {
			int size = getWidth();
			if(!isHorizontal) {
				size = getHeight();
			}
			start = Math.max(0, start);
			start = Math.min(size-3, start);
			end = Math.max(2, end);
			end = Math.min(size-1, end);
			end = Math.max(start + 2, end);
		}

		private void updateCursor(Point p) {
			PullPlace pp = getPullType(p);
			int cursorType = Cursor.DEFAULT_CURSOR;
			if(isHorizontal) {
				switch(pp) {
					case None:
						break;
					case Start:
						cursorType = Cursor.W_RESIZE_CURSOR;
						break;
					case End:
						cursorType = Cursor.E_RESIZE_CURSOR;
						break;
					case Both:
						cursorType = Cursor.MOVE_CURSOR;
						break;
					default:
						throw new IllegalStateException();
				}
			}
			else {
				switch(pp) {
					case None:
						break;
					case Start:
						cursorType = Cursor.N_RESIZE_CURSOR;
						break;
					case End:
						cursorType = Cursor.S_RESIZE_CURSOR;
						break;
					case Both:
						cursorType = Cursor.MOVE_CURSOR;
						break;
					default:
						throw new IllegalStateException();
				}				
			}
			setCursor(Cursor.getPredefinedCursor(cursorType));
		}
		
		@Override
		public void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			Rectangle bounds = g2.getClipBounds();
			g2.setColor(Color.WHITE);
			g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
			g2.setColor(Color.BLACK);			
			int height = getHeight();
			int width = getWidth();
			int start = (int)(Math.round(BorderRuler.this.start));
			int end = (int)(Math.round(BorderRuler.this.end));
			if(isHorizontal) {
				g2.drawLine(start, 0, start, height);
				g2.drawLine(end, 0, end, height);
				int mid = height/2;
				g2.drawLine(start, mid, end, mid);
			}
			else {
				g2.drawLine(0, start, width, start);
				g2.drawLine(0, end, width, end);
				int mid = width/2;
				g2.drawLine(mid, start, mid, end);				
			}
		}
	}
}
