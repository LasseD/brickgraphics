package icon;

import icon.ToBricksIcon.ToBricksIconType;
import io.Log;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import colors.*;
import mosaic.controllers.ColorController;
import mosaic.ui.*;

public class Icons {
	public static final int SIZE_LARGE = 32;
	public static final int SIZE_SMALL = 16;
	public static final Color DISABLED_COLOR = Color.LIGHT_GRAY;
	private static LEGOColor[][] colorGroups = null;
	private static LEGOColor[] colors = ColorController.generateBackupColors().toArray(new LEGOColor[]{});

	public static void colorControllerLoaded(ColorController cc) {
		List<LEGOColor> colorList = cc.getFilteredColors();
		colors = colorList.toArray(new LEGOColor[colorList.size()]);
		ColorGroup[] groups = cc.getColorGroupsFromDisk();
		colorGroups = new LEGOColor[groups.length][];
		Set<LEGOColor> unused = new TreeSet<LEGOColor>(colorList);
		int i = 0;
		for(ColorGroup group : groups) {
			List<LEGOColor> gList = new LinkedList<LEGOColor>();
			for(LEGOColor c : unused) {
				if(group.containsColor(c)) {
					gList.add(c);
				}
			}
			unused.removeAll(gList);
			colorGroups[i++] = gList.toArray(new LEGOColor[gList.size()]);
		}
	}
	
	public static ImageIcon get(int size, String image, String backupName) {
		String fileName = "icons/" + size + "x" + size + "/" + image + ".png";		
		ImageIcon icon = new ImageIcon(fileName);
		if(icon.getIconWidth() <= 0) { // Draw backup image:
			Log.log("Could not open image file '" + fileName + "'. Creating backup image.");
			BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.getGraphics();
			g.setFont(new Font("SansSerif", Font.PLAIN, 8));
			g.setColor(Color.WHITE);
			g.fillRect(0,  0, size-1, size-1);
			
			g.setColor(Color.RED);
			g.drawLine(0, 1, size-1, 1);
			g.drawLine(0, size-2, size-1, size-2);
			
			g.setColor(Color.BLACK);
			g.drawRect(0,  0, size-1, size-1);
			g.drawString(backupName, 2, size/2+4);
			icon = new ImageIcon(bufferedImage);
		}
		return icon;
	}

	private static Color randomColor(Object... seeds) {
		int hash = 0;
		for(Object o : seeds)
			hash += o.hashCode();
		return colors[Math.abs(hash) % colors.length].getRGB();
	}

	public static Icon colorsChooserDialog(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				if(colorGroups == null)
					return;
				int w = 8;
				for(int y = 0; w*y < Math.min(colorGroups.length*w, size); y++) {
					LEGOColor[] colors = colorGroups[y];
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
	
	public static Icon moveDown(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, size/2, size, size, size/2, 0};
				int[] ys = new int[]{0, size/2,    0, size/2, size, size/2};
				g2.fillPolygon(xs,  ys, 6);
			}
		};		
	}
	public static Icon moveLeft(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0     , size/2, size, size/2, size, size/2};
				int[] ys = new int[]{size/2,      0,    0, size/2, size, size};
				g2.fillPolygon(xs,  ys, 6);
			}
		};		
	}
	public static Icon moveUp(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0     , size/2,   size, size, size/2, 0};
				int[] ys = new int[]{size/2,      0, size/2, size, size/2, size};
				g2.fillPolygon(xs,  ys, 6);
			}
		};		
	}
	public static Icon moveRight(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, size/2,   size, size/2, 0, size/2};
				int[] ys = new int[]{0,      0, size/2, size, size, size/2};
				g2.fillPolygon(xs,  ys, 6);
			}
		};		
	}
	
	public static Icon dimensionLockOpen(int size) {
		return new HalfWidthIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.setColor(Color.DARK_GRAY);
				
				// Upper and lower rectangle:
				int linkHeight = getIconHeight()/3;
				int linkWidth = 5*getIconWidth()/9;
				int startX = (getIconWidth()-linkWidth)/2;
				g2.drawRect(startX, 3, linkWidth, linkHeight);
				g2.drawRect(startX, getIconHeight()-3-linkHeight, linkWidth, linkHeight);
								
				g2.setStroke(oldStroke);
			}
		};				
	}
	public static Icon dimensionLockClosed(int size) {
		return new HalfWidthIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Stroke oldStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.setColor(Color.DARK_GRAY);
				
				// Upper and lower rectangle:
				int linkHeight = getIconHeight()/3;
				int linkWidth = 5*getIconWidth()/9;
				int startX = (getIconWidth()-linkWidth)/2;
				g2.drawRect(startX, getIconHeight()/2 - linkHeight - 2, linkWidth, linkHeight);
				g2.drawRect(startX, getIconHeight()/2 + 2, linkWidth, linkHeight);

				// Middle link:
				g2.drawLine(getIconWidth()/2, getIconHeight()/2-linkHeight/2, 
						getIconWidth()/2, getIconHeight()/2+linkHeight/2);
								
				g2.setStroke(oldStroke);
			}
		};				
	}	
	
	public static Icon totalsSymbol(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Stroke tmpStroke = g2.getStroke();
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLACK);
				g2.drawLine(1,  1, size/2-1, 1); // Top line
				g2.drawLine(1,  1, size*2/5, size/2);
				g2.drawLine(1, size-1, size*2/5, size/2);
				g2.drawLine(1,  size-1, size/2, size-1); // Bottom line
				// Equals sign:
				int yDiffEqualsSign = size/7;
				g2.drawLine(size/2, size/2-yDiffEqualsSign, size-1, size/2-yDiffEqualsSign);
				g2.drawLine(size/2, size/2+yDiffEqualsSign, size-1, size/2+yDiffEqualsSign);
				g2.setStroke(tmpStroke);
			}
		};		
	}
	
	public static Icon exit(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				//g2.setColor(Color.RED);
				//g2.fillRect(0,  0, size, size);
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
				brightness(size/2).paint(g2);
				g2.translate(size/2, 0);
				sharpness(size/2).paint(g2);
				g2.translate(0, size/2);
				contrast(size/2).paint(g2);
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

	public static ToBricksIcon plateFromSide(final int STUDS) {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				int studs = STUDS; // Local copy.
				g2.setColor(Color.RED);
				BrickIconMeasure m = new BrickIconMeasure(size);
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					// size*M=brickWidth, x*M=size-4 => brickWidth/size = (size-4)/x => x = ...
					m = new BrickIconMeasure((size-5));
					studs = 1;
					baseX = 4;
					baseY = size/2 - (m.studHeight+m.plateHeight)/2 + m.studHeight;
					drawVerticalMeasure(g2, 1, baseY, baseY + 2*m.plateHeight);
					break;
				case MeasureWidth:
					//m = new BrickIconMeasure(size*size/m.brickWidth);
					baseX = 0;
					baseY = size-5 - 2*m.plateHeight;
					drawHorizontalMeasure(g2, 0, size-1, size-2);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = 0;//m.mid-m.brickWidth/2;
					baseY = m.mid-(m.plateHeight+m.studHeight)/2+m.studHeight;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}				
				
				// brick base and studs:
				int plateHeight = 2*m.plateHeight/studs;
				int studHeight = 2*m.studHeight/studs;
				int brickWidth = 2*m.brickWidth/studs;
				int studWidth = 2*m.studWidth/studs;
				
				g2.fillRect(baseX, baseY, 2*m.brickWidth, plateHeight);
				for(int i = 0; i < studs; ++i)
					g2.fillRect(baseX + brickWidth*i + (brickWidth-studWidth)/2, baseY-studHeight, studWidth, studHeight);

				g2.setColor(Color.BLACK);
				// brick base and stud:
				g2.drawRect(baseX, baseY, 2*m.brickWidth, plateHeight);
				for(int i = 0; i < studs; ++i)
					g2.drawRect(baseX + brickWidth*i + (brickWidth-studWidth)/2, baseY-studHeight, studWidth, studHeight);
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

	public static ToBricksIcon studsFromTop(final int BRICKS_WIDE, final int BRICKS_TALL) {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				int bricksWide = BRICKS_WIDE;
				int bricksTall = BRICKS_TALL;
				g2.setColor(Color.RED);
				
				BrickIconMeasure m = new BrickIconMeasure(size);
				
				int baseX, baseY;
				
				switch(type) {
				case MeasureHeight:
					if(bricksTall != 2)
						bricksWide = 1;
					m = new BrickIconMeasure(size-5);
					baseX = 4;
					baseY = 0;
					drawVerticalMeasure(g2, 1, 0, 2*m.brickWidth);
					break;
				case MeasureWidth:
					m = new BrickIconMeasure(size-5);
					baseX = 4;
					baseY = 0;
					drawHorizontalMeasure(g2, 4, 4+2*m.brickWidth, size-3);
					break;
				case Disabled:
					g2.setColor(DISABLED_COLOR);
					// Fall through intended:
				case Enabled:
					baseX = 0;//m.mid-m.brickWidth/2*bricksWide;
					baseY = m.mid-m.brickWidth*bricksTall/bricksWide;
					break;
				default:
					throw new IllegalStateException("Enum broken: " + type);
				}
								
				g2.fillRect(baseX, baseY, m.brickWidth*2, m.brickWidth*2*bricksTall/bricksWide);
				g2.setColor(Color.BLACK);
				g2.drawRect(baseX, baseY, m.brickWidth*2, m.brickWidth*2*bricksTall/bricksWide);
				//g2.drawRect(baseX, baseY, m.brickWidth*bricksWide, m.brickWidth*bricksTall);
				// studs:
				int drawnStudWidth = m.studWidth*2/bricksWide;
				int drawnBrickWidth = m.brickWidth*2/bricksWide;
				for(int x = 0; x < bricksWide; ++x) {
					int indentX = baseX + x*drawnBrickWidth + (drawnBrickWidth-drawnStudWidth)/2;
					for(int y = 0; y < bricksTall; ++y) {
						int indentY = baseY + y*drawnBrickWidth + (drawnBrickWidth-drawnStudWidth)/2;
						g2.drawOval(indentX, indentY, drawnStudWidth, drawnStudWidth);
					}
				}
			}
		};
	}

	/**
	 * Currently unused since 3D icon is stepping in for pretty display and 2x2 plate for measures.
	 */
	/*
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
	*/

	public static ToBricksIcon elementStudsUp3D(final int studsWide, final int studsDeep, 
			final int platesHigh, final boolean drawStuds) {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				if(type.isMeasure())
					throw new IllegalArgumentException("type can not be measure for 3D icons!");
				Color color = type == ToBricksIconType.Enabled ? Color.red : DISABLED_COLOR;
				
				BrickIcon3D b = new BrickIcon3D(size, studsWide, studsDeep);
				int height = b.drawElementStudsUp(null, color, platesHigh, drawStuds);
				AffineTransform originalTransform = g2.getTransform();
				if(height <= size) {
					g2.translate(0, size - (size-height)/2);
					b.drawElementStudsUp(g2, color, platesHigh, drawStuds);
				}
				else {
					double scale = size / (double)height;
					b = new BrickIcon3D((int)(size*scale), studsWide, studsDeep);
					g2.translate((int)((size-scale*size)/2), size);
					b.drawElementStudsUp(g2, color, platesHigh, drawStuds);
				}				
				g2.setTransform(originalTransform);
			}			
		};
	}
	public static ToBricksIcon elementStudsOut3D(final int studsWide, final int studsDeep, 
			final int platesHigh, final boolean drawStuds) {		
		return new ToBricksIcon() {

			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				if(type.isMeasure())
					throw new IllegalArgumentException("type can not be measure for 3D icons!");
				Color color = type == ToBricksIconType.Enabled ? Color.red : DISABLED_COLOR;
				
				BrickIcon3D b = new BrickIcon3D(size, studsWide, studsDeep);
				int height = b.drawElementStudsOut(null, color, platesHigh, drawStuds);
				AffineTransform originalTransform = g2.getTransform();
				if(height <= size) {
					g2.translate(0, size - (size-height)/2);
					b.drawElementStudsOut(g2, color, platesHigh, drawStuds);
				}
				else {
					double scale = size / (double)height;
					b = new BrickIcon3D((int)(size*scale), studsWide, studsDeep);
					g2.translate((int)((size-scale*size)/2), size);
					b.drawElementStudsOut(g2, color, platesHigh, drawStuds);
				}				
				g2.setTransform(originalTransform);
			}			
		};
	}
	public static ToBricksIcon snot() {
		return new ToBricksIcon() {
			@Override
			public void paint(Graphics2D g2, ToBricksIconType type, int size) {
				if(type.isMeasure())
					throw new IllegalArgumentException("type can not be measure for 3D icons!");
				Color color = type == ToBricksIconType.Enabled ? Color.red : DISABLED_COLOR;
				
				BrickIcon3D b = new BrickIcon3D(size, 2, 1);
				int height = b.drawSNOT(null, color);
				AffineTransform originalTransform = g2.getTransform();
				g2.translate(0, size - (size-height)/2);
				b.drawSNOT(g2, color);
				g2.setTransform(originalTransform);
			}			
		};
	}
	
	public static Icon filterToBrickTypes(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				AffineTransform originalTransform = g2.getTransform();
				ToBricksIcon i1 = studsFromTop(1, 1);
				ToBricksIcon i2 = plateFromSide(1);
				i1.paint(g2, ToBricksIconType.Enabled, size/2);
				g2.translate(0, size/2);
				i2.paint(g2, ToBricksIconType.Enabled, size/2);
				
				g2.translate(size/2, 0);
				drawChecked(g2);
				g2.translate(0, -size/2);
				drawChecked(g2);
				g2.setTransform(originalTransform);
			}
			
			private void drawChecked(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillRect(1, 1, size/2-2, size/2-2);
				g2.setColor(Color.BLACK);
				g2.drawRect(1, 1, size/2-2, size/2-2);
				// Check mark:
				g2.drawLine(4, size/4, size/4, size/2-4);
				g2.drawLine(size/4, size/2-4, size/2-4, 4);				
			}
		};
	}
	
	public static Icon crop(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				Color origColor = g2.getColor();
				g2.setColor(Color.BLACK);
				final int headDiam = 4*size/9;
				g2.fillOval((size-headDiam)/2, size/6, headDiam, headDiam);
				int[] xs = new int[]{size/8, size/2, 7*size/8};
				int[] ys = new int[]{size, size/2, size};
				g2.fillPolygon(xs,  ys, 3);
				Cropper.drawCropHighlight(g2, new Rectangle(3, 4, size-6, 3*size/5));
				g2.setColor(origColor);
			}
		};
	}
	
	public static Icon colorLegend(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				if(colorGroups == null)
					return;
				Color prevColor = g2.getColor();
				int w = 8;
				for(int y = 0; w*y < size; y++) {
					LEGOColor color = y >= colorGroups.length || colorGroups[y].length == 0 ? LEGOColor.WHITE : colorGroups[y][0];
					g2.setColor(color.getRGB());
					g2.fillRect(0, w*y, w, w);
					g2.setColor(Color.BLACK);
					g2.drawRect(0, w*y, w, w);
					// draw "text":
					g2.setColor(Color.LIGHT_GRAY);
					g2.fillRect(w + 4, w*y + w/3 + 1, size-w-4, w/2);
				}
				g2.setColor(prevColor);
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
				int q = size/8;
				g2.drawRect(mid-q, mid-q, 2*q, 2*q);
				g2.drawLine(mid, 0, mid, mid-q);
				g2.drawLine(mid, mid+q, mid, size-1);
				
				g2.drawLine(mid, mid-q, mid-q, mid-2*q);
				g2.drawLine(mid, mid-q, mid+q, mid-2*q);

				g2.drawLine(mid, mid+q, mid-q, mid+2*q);
				g2.drawLine(mid, mid+q, mid+q, mid+2*q);
			}
		};
	}
	public static Icon slimmer(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/8;
				g2.drawRect(mid-q, mid-q, 2*q, 2*q);
				g2.drawLine(0, mid, mid-q, mid);
				g2.drawLine(mid+q, mid, size-1, mid);

				g2.drawLine(mid-q, mid, mid-2*q, mid+q);
				g2.drawLine(mid-q, mid, mid-2*q, mid-q);

				g2.drawLine(mid+q, mid, mid+2*q, mid+q);
				g2.drawLine(mid+q, mid, mid+2*q, mid-q);
			}
		};
	}
	public static Icon taller(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/8;
				g2.drawRect(mid-q, mid-q, 2*q, 2*q);
				g2.drawLine(mid, 0, mid, mid-q);
				g2.drawLine(mid, mid+q, mid, size-1);

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
				int q = size/8;
				g2.drawRect(mid-q, mid-q, 2*q, 2*q);
				g2.drawLine(0, mid, mid-q, mid);
				g2.drawLine(mid+q, mid, size-1, mid);

				g2.drawLine(0, mid, q, mid+q);
				g2.drawLine(0, mid, q, mid-q);
				g2.drawLine(size-1, mid, size-q, mid+q);
				g2.drawLine(size-1, mid, size-q, mid-q);
			}
		};
	}
	
	public static Icon pack(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/8;
				g2.drawRect(0,  0, size-1, size-1);
				
				g2.drawLine(mid, 0, mid, size-1);
				g2.drawLine(0, mid, size-1, mid);
				g2.drawLine(mid, 0, mid+q, q);
				g2.drawLine(mid, 0, mid-q, q);
				g2.drawLine(mid, size-1, mid+q, size-q);
				g2.drawLine(mid, size-1, mid-q, size-q);
				g2.drawLine(0, mid, q, mid+q);
				g2.drawLine(0, mid, q, mid-q);
				g2.drawLine(size-1, mid, size-q, mid+q);
				g2.drawLine(size-1, mid, size-q, mid-q);
			}
		};
	}
	public static Icon colorDistributionChart(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				int angle = 0;
				int angleAdd = 100;
				for(LEGOColor color : colors) {
					g2.setColor(color.getRGB());
					g2.fillArc(0, 0, size, size, angle, angleAdd); 
					angle += angleAdd;
					angleAdd = Math.max(angleAdd-15, angleAdd*2/3);
					if(angleAdd == 0)
						break;
				}
				
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
			}
		};
	}

	public static Icon treshold(final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.setColor(Color.RED);
				g2.fillArc(0, 0, size, size, -90, 180); 
				g2.setColor(Color.BLUE);
				g2.fillArc(0, 0, size, size, 90, 180); 
				
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
				// draw background:
				g2.setColor(Color.RED);
				g2.fillArc(0, 0, size, size, -90, 180); 
				g2.setColor(Color.BLUE);
				g2.fillArc(0, 0, size, size, 90, 180); 

				// draw dithering:
				final int BLOCK_SIZE = 2;

				// Left side: Color red on top of blue:
				g2.setColor(Color.RED);
				for(int x = size/2-BLOCK_SIZE, skip = 2; x >= size/4; x-=BLOCK_SIZE, ++skip) {
					int dx = size/2-x;
					int height = (int)Math.sqrt(size*size/4 - dx*dx);
					for(int y = size/2-height + (7*skip%3)*BLOCK_SIZE; y < size/2+height; y+=BLOCK_SIZE*skip) {
					  g2.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
					}					
				}
				
				// Right side:
				g2.setColor(Color.BLUE);
				for(int x = size/2+1, skip = 2; x < 3*size/4; x+=BLOCK_SIZE, ++skip) {
					int dx = x-size/2;
					int height = (int)Math.sqrt(size*size/4 - dx*dx);
					for(int y = size/2-height + (5*skip%2)*BLOCK_SIZE; y < size/2+height; y+=BLOCK_SIZE*skip) {
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
	public static BrickGraphicsIcon contrast(final int size) {
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
				Stroke formerStroke = g2.getStroke();

				g2.setColor(Color.BLACK);
				Stroke stroke = new BasicStroke(2);
				g2.setStroke(stroke);
				int q = size/3;
				CubicCurve2D.Double curve = new CubicCurve2D.Double(mid-q, size-1, mid-q, mid, mid+q, mid, mid+q, 0);				
				g2.draw(curve);

				g2.setStroke(formerStroke);
			}
		};
	}
	public static BrickGraphicsIcon brightness(final int size) {
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
					g2.rotate(Math.PI/4);
				}
				g2.rotate(-4*Math.PI); // Rotate back.
				g2.translate(-mid, -mid);
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

	public static BrickGraphicsIcon checkbox(final int size, final boolean selected) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				g2.translate(-2, 0);
				JCheckBox cb = new JCheckBox();
				cb.setSelected(selected);
				cb.setSize(size, size);
				cb.print(g2);
			}
		};
	}
}
