package griddy.grid;

import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import griddy.*;
import griddy.rulers.*;

public class Grid implements DisplayComponent, BorderRulerListener {
	private static final long serialVersionUID = -8151492927599986064L;
	private Point gridStart;
	private SizeType sizeType;
	private List<GridLevel> gridLevels;
	private double gridScale;
	private transient Point mousePressed, currentTranslate;
	private volatile transient boolean isSlowValid;
	
	public Grid() {
		gridStart = new Point();
		sizeType = SizeType.plate;
		gridLevels = new LinkedList<GridLevel>();
		GridLevel level0 = new GridLevel(Color.BLACK, 1, 1, false);
		gridLevels.add(level0);
		GridLevel level1 = new GridLevel(Color.RED, 10, 10, false);
		gridLevels.add(level1);
		gridScale = 1;		
	}
	
	public void conformTo(Grid other) {
		isSlowValid = false;
		setGridStart(other.getGridStart());
		setSizeType(other.getSizeType());
		setGridScale(other.gridScale);
		gridLevels.clear();
		for(GridLevel otherLevel : other.gridLevels) {
			GridLevel levelCopy = new GridLevel(otherLevel.color, otherLevel.xSize, otherLevel.ySize, otherLevel.drawContrasting);
			gridLevels.add(levelCopy);	
		}
	}
	
	public Point getGridStart() {
		return new Point(gridStart);
	}

	public void setGridStart(Point gridStart) {
		isSlowValid = false;
		this.gridStart = gridStart;
	}
	
	public void setGridScale(double scale) {
		isSlowValid = false;
		gridScale = scale;
	}
	
	public void setSizeType(SizeType sizeType) {
		isSlowValid = false;
		this.sizeType = sizeType;
	}
	
	public SizeType getSizeType() {
		return sizeType;
	}
	
	public List<GridLevel> getGridLevels() {
		return gridLevels;
	}
	
	public void addGridLevel() {
		Color color = Color.getHSBColor((float)Math.random(), 1.0f, 1.0f);
		gridLevels.add(new GridLevel(color, 5, 5, false));
		isSlowValid = false;
	}
	
	public void removeGridLevel(GridLevel gridLevel) {
		isSlowValid = false;
		gridLevels.remove(gridLevel);
	}
	
	@Override
	public void borderRulerChanged(BorderRuler scaleTool) {
		isSlowValid = false;
		double span = scaleTool.getEnd() - scaleTool.getStart();
		gridScale = span/scaleTool.getDist() / scaleTool.getLengthType().getUnitLength();
	}
	
	@Override
	public void drawQuick(Graphics2D g2) {
		for(GridLevel l : gridLevels) {
			l.drawQuick(g2);
		}
	}

	@Override
	public void drawSlow(BufferedImage baseImage, Graphics2D g2) {
		isSlowValid = true;
		for(GridLevel l : gridLevels) {
			l.drawSlow(baseImage, g2);
		}
	}
	
	public void addMouserListeners(Component component) {
		component.addMouseListener(createMouseListener(component));
		component.addMouseMotionListener(createMouseMotionListener(component));
	}
	
	public MouseMotionListener createMouseMotionListener(final Component toRepaint) {
		return new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// nop
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(mousePressed == null || currentTranslate == null)
					return;
				isSlowValid = false;
				currentTranslate = new Point(e.getPoint().x - mousePressed.x, e.getPoint().y - mousePressed.y);
				toRepaint.repaint();
			}
		};
	}
	
	public MouseListener createMouseListener(final Component toRepaint) {
		return new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// nop
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// nop
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// nop
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mousePressed = e.getPoint();
				currentTranslate = new Point();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(mousePressed == null || currentTranslate == null)
					return;
				isSlowValid = false;
				
				int distX = e.getPoint().x - mousePressed.x;
				int distY = e.getPoint().y - mousePressed.y;
				
				gridStart.x += distX;
				gridStart.y += distY;

				mousePressed = null;
				currentTranslate = null;
				toRepaint.repaint();
			}
			
		};
	}
	
	/*
	 * For use in GridLevel only!
	 */
	private interface LineDrawer {
		void drawLine(Graphics2D g2, int x1, int y1, int x2, int y2);
	}
	
	public class GridLevel implements Serializable {
		private static final long serialVersionUID = 4536684168892876017L;
		private Color color;
		private int xSize, ySize;
		private boolean drawContrasting;

		public GridLevel(Color c, int x, int y, boolean dc) {
			color = c;
			xSize = x;
			ySize = y;
			drawContrasting = dc;
		}
		
		public boolean getDrawContrasting() {
			return drawContrasting;
		}
		
		public int getXSize() {
			return xSize;
		}
		
		public int getYSize() {
			return ySize;
		}
		
		public Color getColor() {
			return color;
		}
		
		public void setDrawContrasting(boolean drawContrasting) {
			this.drawContrasting = drawContrasting;
		}
		
		public void setXSize(int size) {
			if(size <= 0)
				throw new IllegalArgumentException("size <= 0");
			isSlowValid = false;
			this.xSize = size;
		}
		
		public void setYSize(int size) {
			if(size <= 0)
				throw new IllegalArgumentException("size <= 0");
			isSlowValid = false;
			this.ySize = size;
		}
		
		public void setColor(Color color) {
			isSlowValid = false;
			this.color = color;
		}		
		
		private void draw(Graphics2D g2, LineDrawer ld) {
			Rectangle bounds = g2.getClipBounds();
			double xDiff = gridScale * sizeType.width()*xSize;
			double startX = gridStart.x;
			if(currentTranslate != null)
				startX += currentTranslate.x;
			startX += (int)((bounds.x-startX)/xDiff)*xDiff;
			
			for(double x = startX; x < bounds.x + bounds.width; x += xDiff) {
				ld.drawLine(g2, (int)Math.round(x), bounds.y, 
						    (int)Math.round(x), bounds.y+bounds.height-1);
			}
			
			double yDiff = gridScale * sizeType.height()*ySize;
			double startY = gridStart.y;
			if(currentTranslate != null)
				startY += currentTranslate.y;
			startY += (int)((bounds.y-startY)/yDiff)*yDiff;

			for(double y = startY; y < bounds.y + bounds.height; y += yDiff) {
				ld.drawLine(g2, bounds.x, (int)Math.round(y), 
						    bounds.x+bounds.width-1, (int)Math.round(y));
			}	
		}
		
		public void drawQuick(Graphics2D g2) {
			g2.setColor(color);
			draw(g2, new LineDrawer(){
				@Override
				public void drawLine(Graphics2D g2, int x1, int y1, int x2, int y2) {					
					g2.drawLine(x1, y1, x2, y2);
				}				
			});		
		}
		
		private boolean nearer(int color, Color nearer, Color other) {
			Color c = new Color(color);
			int r = c.getRed();
			int g = c.getGreen();
			int b = c.getBlue();
			int distNearer = (r-nearer.getRed())*(r-nearer.getRed())+
							 (b-nearer.getBlue())*(b-nearer.getBlue())+
							 (g-nearer.getGreen())*(g-nearer.getGreen());
			int distOther = (r-other.getRed())*(r-other.getRed())+
							(b-other.getBlue())*(b-other.getBlue())+
							(g-other.getGreen())*(g-other.getGreen());
			return distNearer < distOther;
		}
		
		public void drawSlow(final BufferedImage baseImage, Graphics2D g2) {
			drawQuick(g2);
			if(!drawContrasting) {
				return;
			}
							
			final Color contrast = new Color(255-color.getRed(), 255-color.getGreen(), 255-color.getBlue());
			g2.setColor(contrast);
			draw(g2, new LineDrawer(){
				@Override
				public void drawLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
					int startContrast = -1;
					if(x1 == x2) {
						if(x1 < 0 || x1 >= baseImage.getWidth())
							return;
						for(int y = Math.max(0, y1); y < Math.min(baseImage.getHeight(), y2); y++) {
							boolean contrastNow = !nearer(baseImage.getRGB(x1, y), contrast, color);
							//System.out.println("Comparing " + new Color(baseImage.getRGB(x1, y)) + " on " + contrast + "/" + color + ":" +contrastNow);
							if(startContrast == -1) {
								if(contrastNow)
									startContrast = y;
							}
							else {
								if(!contrastNow) {
									g2.drawLine(x1, startContrast, x2, y-1);
									//System.out.println(x1 + " " +  startContrast + " " + x2 + " " + (y-1));
									startContrast = -1;
								}
							}
						}
						if(startContrast != -1) {
							g2.drawLine(x1, startContrast, x2, y2);
						}
					}
					else if(y1 == y2) {
						if(y1 < 0 || y1 >= baseImage.getHeight())
							return;
						for(int x = Math.max(0, x1); x < Math.min(baseImage.getWidth(), x2); x++) {
							boolean contrastNow = !nearer(baseImage.getRGB(x, y1), contrast, color);
							if(startContrast == -1) {
								if(contrastNow)
									startContrast = x;
							}
							else {
								if(!contrastNow) {
									g2.drawLine(startContrast, y1, x-1, y2);
									startContrast = -1;
								}
							}
						}
						if(startContrast != -1) {
							g2.drawLine(startContrast, y1, x2, y2);
						}						
					}
					else {
						throw new IllegalArgumentException("Not a rectilinear line!");
					}
				}			
			});
		}
	}

	@Override
	public boolean isSlowValid() {
		return isSlowValid;
	}

}
