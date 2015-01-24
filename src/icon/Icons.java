package icon;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import java.util.Random;
import colors.*;
import mosaic.controllers.ColorController;
import mosaic.ui.prepare.*;

public class Icons {
	public static final int SIZE_LARGE = 32;
	public static final int SIZE_SMALL = 16;
	public static final Color DISABLED_COLOR = Color.LIGHT_GRAY;
	private static ColorGroup[] colorGroups = ColorGroup.generateBackupColorGroups();

	public static void colorControllerLoaded(ColorController cc) {
		colorGroups = cc.getColorGroupsFromDisk();			
	}
	
	public static ImageIcon get(int size, String image) {
		String fileName = "icons" + File.separator + size + "x" + size + File.separator + image + ".png";
		ImageIcon icon = new ImageIcon(fileName);
		return icon;
	}

	private static Color randomColor(Object... seeds) {
		int hash = 0;
		for(Object o : seeds)
			hash += o.hashCode();
		ColorGroup group = colorGroups[Math.abs(hash) % colorGroups.length];
		LEGOColor[] colors = group.getColors();
		return colors[Math.abs(hash) % colors.length].getRGB();
	}

	public static Icon colors(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				int w = 8;
				for(int y = 0; w*y < Math.min(colorGroups.length*w, size); y++) {
					LEGOColor[] colors = colorGroups[y].getColors();
					for(int x = 0; w*x < Math.min(colors.length*w, size); x++) {
						g2.setColor(colors[x].getRGB());
						g2.fillRect(w*x, w*y, w, w);
						g2.setColor(Color.WHITE);
						g2.fillRect(w*x+w/4, w*y+w/4, w/2, w/2);
						g2.setColor(Color.BLACK);
						g2.drawRect(w*x+w/4, w*y+w/4, w/2, w/2);						
					}
				}
			}
		};
	}
	
	public static Icon dividerTriangles(int size) {
		return new HalfWidthIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				// Left triangle:
				int[] xs = new int[]{0, getIconWidth(), getIconWidth()};
				int[] ys = new int[]{getIconHeight()/4, 0, getIconHeight()/2};
				g2.fillPolygon(xs,  ys, 3);
				
				// Right triangle:
				xs = new int[]{0, 0, getIconWidth()};
				ys = new int[]{getIconHeight(), getIconHeight()/2, getIconHeight()*3/4};
				g2.fillPolygon(xs,  ys, 3);
			}
		};				
	}
	
	public static Icon leftTriangle(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, size, size};
				int[] ys = new int[]{size/2, 0, size};
				g2.fillPolygon(xs,  ys, 3);
			}
		};		
	}
	
	public static Icon rightTriangle(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, 0, size};
				int[] ys = new int[]{0, size, size/2};
				g2.fillPolygon(xs,  ys, 3);
			}
		};		
	}
	
	public static Icon exit(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.RED);
				g2.fillRect(0,  0, size, size);
				g2.setColor(Color.BLACK);
				g2.drawRect(0,  0, size, size);
				int xMin = size*2/10;
				int xMax = size-xMin;
				g2.drawLine(xMin, xMin, xMax, xMax);
				g2.drawLine(xMin, xMax, xMax, xMin);
			}
		};		
	}
	
	public static Icon prepareFiltersEnable(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.translate(size/2, 0);
				sharpness(size/2).paint(g2);
				g2.translate(0, size/2);
				plus(size/2).paint(g2);
				g2.translate(-size/2, 0);
				gamma(size/2).paint(g2);
				g2.translate(0, -size/2);
			}
		};				
	}
	
	public static Icon showColors(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillRect(0,  0, size, size);
				
				final int ROWS = 4;
				int w = size/ROWS;
				for(int y = 0; w*y < size; y++) {
					for(int x = 0; x < ROWS-y-1; x++) {
						g2.setColor(randomColor(getClass(), 2*y, x));
						g2.fillRect(w*x, w*y, w, w);
						g2.setColor(Color.BLACK);
						g2.drawRect(w*x, w*y, w, w);						
					}
					g2.setColor(randomColor(getClass(), y));
					g2.fillPolygon(new int[]{w*(ROWS-y-1), w*(ROWS-y-1), w*(ROWS-y)}, new int[]{w*(y+1), w*y, w*y}, 3);
				}
				// draw black lines:
				g2.setColor(Color.BLACK);
				g2.drawLine(0,  size, size, 0);
				for(int y = 0; w*y < size; y++) {
					for(int x = 0; w*x < size; x++) {
						g2.drawRect(w*x, w*y, w, w);						
					}
				}
			}
		};
	}
	
	public static ToBricksIcon brickFromSide() {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				g2.setColor(Color.RED);
				BrickIconMeasure m = new BrickIconMeasure(size);	
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					m = new BrickIconMeasure((size-1)*(size-1)/(m.brickHeight+m.studHeight+1));
					baseX = size - m.brickWidth - 1;
					baseY = m.studHeight;
					drawVerticalMeasure(g2, baseX-3, baseY, baseY + m.brickHeight);
					break;
				case MeasureWidth:
					m = new BrickIconMeasure(size*(size-4)/(m.brickHeight+m.studHeight)-1);
					baseX = m.mid - m.brickWidth/2;
					baseY = m.studHeight;
					drawHorizontalMeasure(g2, baseX, baseX+m.brickWidth, size-3);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = m.mid-m.brickWidth/2;
					baseY = m.mid-(m.brickHeight+m.studHeight)/2+m.studHeight;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}				
				
				// brick base and stud:
				g2.fillRect(baseX, baseY, m.brickWidth, m.brickHeight);
				g2.fillRect(baseX + (m.brickWidth-m.studWidth)/2, baseY-m.studHeight, m.studWidth, m.studHeight);

				g2.setColor(Color.BLACK);
				// brick base and stud:
				g2.drawRect(baseX, baseY, m.brickWidth, m.brickHeight);
				g2.drawRect(baseX + (m.brickWidth-m.studWidth)/2, baseY-m.studHeight, m.studWidth, m.studHeight);
			}
		};
	}

	public static ToBricksIcon plateFromSide() {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				g2.setColor(Color.RED);
				BrickIconMeasure m = new BrickIconMeasure(size);
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					// size*M=brickWidth, x*M=size-4 => brickWidth/size = (size-4)/x => x = ...
					m = new BrickIconMeasure((size-5)*size/m.brickWidth);
					baseX = 4;
					baseY = size/2 - (m.studHeight+m.plateHeight)/2 + m.studHeight;
					drawVerticalMeasure(g2, 1, baseY, baseY + m.plateHeight);
					break;
				case MeasureWidth:
					m = new BrickIconMeasure(size*size/m.brickWidth);
					baseX = 0;
					baseY = size-5 - m.plateHeight;
					drawHorizontalMeasure(g2, 0, size-1, size-2);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = m.mid-m.brickWidth/2;
					baseY = m.mid-(m.plateHeight+m.studHeight)/2+m.studHeight;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}				
				
				// brick base and stud:
				g2.fillRect(baseX, baseY, m.brickWidth, m.plateHeight);
				g2.fillRect(baseX + (m.brickWidth-m.studWidth)/2, baseY-m.studHeight, m.studWidth, m.studHeight);

				g2.setColor(Color.BLACK);
				// brick base and stud:
				g2.drawRect(baseX, baseY, m.brickWidth, m.plateHeight);
				g2.drawRect(baseX + (m.brickWidth-m.studWidth)/2, baseY-m.studHeight, m.studWidth, m.studHeight);
			}
		};
	}

	public static ToBricksIcon tileFromTop() {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				g2.setColor(Color.RED);
				
				BrickIconMeasure m = new BrickIconMeasure(size);	
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					m = new BrickIconMeasure(2*(size-5)-1);
					baseX = 4;
					baseY = 0;
					drawVerticalMeasure(g2, 1, 0, m.brickWidth);
					break;
				case MeasureWidth:
					m = new BrickIconMeasure(2*(size-5)-1);
					baseX = 4;
					baseY = 0;
					drawHorizontalMeasure(g2, baseX, baseX + m.brickWidth, size-3);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = m.mid-m.brickWidth/2;
					baseY = m.mid-m.brickWidth/2;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}				
				
				g2.fillRect(baseX, baseY, m.brickWidth, m.brickWidth);
				g2.setColor(Color.BLACK);
				g2.drawRect(baseX, baseY, m.brickWidth, m.brickWidth);
			}
		};
	}

	public static ToBricksIcon studFromTop(final int bricksWide) {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				g2.setColor(Color.RED);
				
				BrickIconMeasure m = type.isMeasure() ?  new BrickIconMeasure(2*(size-5)/bricksWide) : new BrickIconMeasure(size);	
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					baseX = 4;
					baseY = 0;
					drawVerticalMeasure(g2, 1, 0, m.brickWidth*bricksWide);
					break;
				case MeasureWidth:
					baseX = 4;
					baseY = 0;
					drawHorizontalMeasure(g2, 4, 4+m.brickWidth*bricksWide, size-3);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = m.mid-m.brickWidth/2*bricksWide;
					baseY = m.mid-m.brickWidth/2*bricksWide;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}				
				
				g2.fillRect(baseX, baseY, m.brickWidth*bricksWide, m.brickWidth*bricksWide);
				g2.setColor(Color.BLACK);
				g2.drawRect(baseX, baseY, m.brickWidth*bricksWide, m.brickWidth*bricksWide);
				// studs:
				for(int x = 0; x < bricksWide; ++x) {
					int indentX = baseX + x*m.brickWidth + (m.brickWidth-m.studWidth)/2;
					for(int y = 0; y < bricksWide; ++y) {
						int indentY = baseY + y*m.brickWidth + (m.brickWidth-m.studWidth)/2;
						g2.drawOval(indentX, indentY, m.studWidth, m.studWidth);
					}
				}
			}
		};
	}

	public static ToBricksIcon snot() {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				g2.setColor(Color.RED);
				
				BrickIconMeasure m = type.isMeasure() ?  new BrickIconMeasure(size-5) : new BrickIconMeasure(size);	
				int baseX = 0;

				if(type.isMeasure()) {
					baseX = 4;
					if(type == ToBricksIconType.MeasureHeight) {
						drawVerticalMeasure(g2, 1, 0, m.brickWidth*2);
					}
					else {
						drawHorizontalMeasure(g2, 4, size-1, size-3);						
					}
				}
				else if(type == ToBricksIconType.Disabled) {
					g2.setColor(DISABLED_COLOR);					
				}
				
				// Full:
				g2.fillRect(baseX, 0, 2*m.brickWidth, 2*m.brickWidth);
				// Sideways plates:
				int topPlatesStopAt = 2*m.brickWidth-2*m.plateHeight;
				if(type != ToBricksIconType.Disabled) {
					g2.setColor(Color.BLUE);
					g2.fillRect(baseX + 1*m.plateHeight, topPlatesStopAt-m.brickWidth, m.plateHeight, m.brickWidth);					
					g2.fillRect(baseX + 3*m.plateHeight, 0, m.plateHeight, topPlatesStopAt-m.brickWidth);					
					g2.fillRect(baseX + m.brickWidth, topPlatesStopAt, m.brickWidth, m.plateHeight);					
				}

				g2.setColor(Color.BLACK);
				// Border:
				g2.drawRect(baseX, 0, 2*m.brickWidth, 2*m.brickWidth);
				// Additional Lines:
				// Top plate lines:
				for(int i = 1; i <= 4; ++i) {
					g2.drawLine(baseX + i*m.plateHeight, 0, baseX + i*m.plateHeight, topPlatesStopAt);					
				}
				g2.drawLine(baseX, topPlatesStopAt-m.brickWidth, baseX+2*m.brickWidth, topPlatesStopAt-m.brickWidth);
				g2.drawLine(baseX, topPlatesStopAt, baseX+2*m.brickWidth, topPlatesStopAt);
				g2.drawLine(baseX, topPlatesStopAt+m.plateHeight, baseX+2*m.brickWidth, topPlatesStopAt+m.plateHeight);
				g2.drawLine(baseX + m.brickWidth, topPlatesStopAt, baseX + m.brickWidth, 2*m.brickWidth);
			}
		};
	}

	public static Icon crop(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Cropper.drawCropHighlight(g2, new Rectangle(3, 4, size-8, size-9));
			}
		};
	}
	
	public static Icon colorLegend(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Font origFont = g2.getFont();
				int w = 8;
				for(int y = 0; w*y < Math.min(colorGroups.length*w, size); y++) {
					LEGOColor[] colors = colorGroups[y].getColors();
					LEGOColor color = colors.length > 0 ? colors[0] : LEGOColor.WHITE;
					g2.setColor(color.getRGB());
					g2.fillRect(0, w*y, w, w);
					g2.setColor(Color.BLACK);
					g2.drawRect(0, w*y, w, w);
					// draw "text":
					g2.setColor(Color.LIGHT_GRAY);
					g2.fillRect(w + 4, w*y + w/3 + 1, size-w-4, w/2);
				}
				g2.setFont(origFont);
			}
		};
	}
	
	public static BrickGraphicsIcon plus(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/3;
				g2.drawLine(mid-q, mid, mid+q, mid);
				g2.drawLine(mid, mid-q, mid, mid+q);
			}
		};
	}
	public static Icon minus(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/3;
				g2.drawLine(mid-q, mid, mid+q, mid);
			}
		};
	}

	public static Icon shorter(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/4;
				g2.drawLine(mid+q, 0, mid, q);
				g2.drawLine(mid-q, 0, mid, q);
				g2.drawLine(mid+q, size-1, mid, size-q);
				g2.drawLine(mid-q, size-1, mid, size-q);
			}
		};
	}
	public static Icon slimmer(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/4;
				g2.drawLine(0, mid-q, q, mid);
				g2.drawLine(0, mid+q, q, mid);
				g2.drawLine(size-1, mid+q, size-q, mid);
				g2.drawLine(size-1, mid-q, size-q, mid);
			}
		};
	}
	public static Icon taller(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/4;
				g2.drawLine(mid, 0, mid+q, q);
				g2.drawLine(mid, 0, mid-q, q);
				g2.drawLine(mid, size-1, mid+q, size-q);
				g2.drawLine(mid, size-1, mid-q, size-q);
			}
		};
	}
	public static Icon wider(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/4;
				g2.drawLine(0, mid, q, mid+q);
				g2.drawLine(0, mid, q, mid-q);
				g2.drawLine(size-1, mid, size-q, mid+q);
				g2.drawLine(size-1, mid, size-q, mid-q);
			}
		};
	}

	public static Icon treshold(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				// draw left side:
				Paint p = g2.getPaint();
				Paint gradient = new LinearGradientPaint(0, 0, 0, size-1, new float[]{0,1}, new Color[]{Color.RED, Color.BLUE});
				g2.setPaint(gradient);
				g2.fillArc(0, 0, size, size, 90, 180); 
				g2.setPaint(p);
				
				// draw transformed side:
				g2.setColor(Color.RED);
				g2.fillArc(0, 0, size, size, 0, 90); 
				g2.setColor(Color.BLUE);
				g2.fillArc(0, 0, size, size, 0, -90); 
				
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
				g2.drawLine(size/2, 0, size/2, size-1);
			}
		};
	}	
	
	public static Icon floydSteinberg(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				final Random rand = new Random(10937);
				// draw left side:
				Paint p = g2.getPaint();
				Paint gradient = new LinearGradientPaint(0, 0, 0, size-1, new float[]{0,1}, new Color[]{Color.RED, Color.BLUE});
				g2.setPaint(gradient);
				g2.fillArc(0, 0, size, size, 90, 180); 
				g2.setPaint(p);
				
				// draw transformed side:
				g2.setColor(Color.RED);
				g2.fillArc(0, 0, size, size, -90, 180); 

				g2.setColor(Color.BLUE);
				final int BLOCK_SIZE = 1;
				for(int y = 0; y < size; y+=BLOCK_SIZE) {
					float partBlue = y /(float)size;
					int d = size/2-y;
					int width = (int)Math.sqrt(size*size/4 - d*d);
					for(int x = size/2; x < size/2+width; x+=BLOCK_SIZE) {
						if(rand.nextFloat() < partBlue)
  						  g2.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
					}
				}
				
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
				g2.drawLine(size/2, 0, size/2, size-1);
			}
		};
	}	
	
	public static Icon saturation(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Paint p = g2.getPaint();
				Paint gradient = new LinearGradientPaint(0, 0, size-1, 0, new float[]{0,1}, new Color[]{Color.WHITE, Color.BLACK});
				g2.setPaint(gradient);
				g2.fillOval(0, 0, size, size);
				g2.setPaint(p);
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
			}
		};
	}
	public static Icon contrast(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillArc(0, 0, size, size, -90, -180);
				g2.setColor(Color.BLACK);
				g2.fillArc(0, 0, size, size, -90, 180);
				g2.drawOval(0, 0, size-1, size-1);
			}
		};
	}
	public static BrickGraphicsIcon gamma(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				Stroke stroke = new BasicStroke(2);
				g2.setStroke(stroke);
				int q = size/3;
				CubicCurve2D.Double curve = new CubicCurve2D.Double(mid-q, size-1, mid-q, mid, mid+q, mid, mid+q, 0);
				
				g2.draw(curve);
			}
		};
	}
	public static Icon brightness(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				int q = size/4;
				g2.translate(mid, mid);
				g2.setColor(Color.YELLOW);
				g2.fillOval(-q, -q, 2*q, 2*q);
				g2.setColor(Color.BLACK);
				g2.drawOval(-q, -q, 2*q, 2*q);
				for(int i = 0; i < 8; i++) {
					g2.drawLine(q + 2, 0, mid, 0);
					g2.rotate(2*Math.PI/8);
				}
			}
		};
	}
	public static BrickGraphicsIcon sharpness(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillOval(0, 0, size, size);
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
				g2.drawLine(mid, 0, mid, size-1);
			}
		};
	}
}