package ui;

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
	private static ColorGroup[] colorGroups = ColorGroup.generateBackupColorGroups();;

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

	public static Icon colors(int size) {
		return new NormalIcon(size) {
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
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				// Left triangle:
				int[] xs = new int[]{0, size/2, size/2};
				int[] ys = new int[]{size/4, 0, size/2};
				g2.fillPolygon(xs,  ys, 3);
				
				// Right triangle:
				xs = new int[]{0, 0, size/2};
				ys = new int[]{size, size/2, size*3/4};
				g2.fillPolygon(xs,  ys, 3);
			}
		};				
	}
	
	public static Icon leftTriangle(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, size, size};
				int[] ys = new int[]{size/2, 0, size};
				g2.fillPolygon(xs,  ys, 3);
			}
		};		
	}
	
	public static Icon rightTriangle(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int[] xs = new int[]{0, 0, size};
				int[] ys = new int[]{0, size, size/2};
				g2.fillPolygon(xs,  ys, 3);
			}
		};		
	}
	
	public static Icon exit(int size) {
		return new NormalIcon(size) {
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
	
	public static Icon prepareFiltersEnable(int size) {
		return new NormalIcon(size) {
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
	
	public static Icon showColors(int size) {
		return new NormalIcon(size) {
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
	
	public static Icon height(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				// main line:
				g2.drawLine(mid, 0, mid, size-1);
				// arrow:
				g2.drawLine(mid, 0, mid+little, little);
				g2.drawLine(mid, 0, mid-little, little);
				// arrow:
				g2.drawLine(mid, size-1, mid+little, size-1-little);
				g2.drawLine(mid, size-1, mid-little, size-1-little);
			}
		};
	}

	public static Icon width(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				// main line:
				g2.drawLine(0, mid, size-1, mid);
				// arrow:
				g2.drawLine(0, mid, little, mid+little);
				g2.drawLine(0, mid, little, mid-little);
				// arrow:
				g2.drawLine(size-1, mid, size-1-little, mid+little);
				g2.drawLine(size-1, mid, size-1-little, mid-little);
			}
		};
	}

	public static Icon brickFromSide(int size, final boolean enabled) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				if(enabled)
					g2.setColor(Color.RED);
				else
					g2.setColor(DISABLED_COLOR);
				// brick:
				g2.fillRect(mid-brick_width/2, stud_height, brick_width, size-stud_height);
				// stud:
				g2.fillRect(mid-stud_width/2, 0, stud_width, stud_height);

				g2.setColor(Color.BLACK);
				// brick:
				g2.drawRect(mid-brick_width/2, stud_height, brick_width, size-stud_height);
				// stud:
				g2.drawRect(mid-stud_width/2, 0, stud_width, stud_height);
			}
		};
	}

	public static Icon plateFromSide(int size, final boolean enabled) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				if(enabled)
					g2.setColor(Color.RED);
				else
					g2.setColor(DISABLED_COLOR);
				// plate:
				g2.fillRect(mid-brick_width/2, size-plate_height, brick_width, plate_height);
				// stud:
				g2.fillRect(mid-stud_width/2, size-plate_height-stud_height, stud_width, stud_height);

				g2.setColor(Color.BLACK);
				// plate:
				g2.drawRect(mid-brick_width/2, size-plate_height, brick_width, plate_height);
				// stud:
				g2.drawRect(mid-stud_width/2, size-plate_height-stud_height, stud_width, stud_height);
			}
		};
	}

	public static Icon tileFromTop(int size, final boolean enabled) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				if(enabled)
					g2.setColor(Color.RED);
				else
					g2.setColor(DISABLED_COLOR);
				g2.fillRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				g2.setColor(Color.BLACK);
				g2.drawRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
			}
		};
	}

	public static Icon studFromTop(int size, final int bricksWide, final boolean enabled) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				if(enabled)
					g2.setColor(Color.RED);
				else
					g2.setColor(DISABLED_COLOR);
				g2.fillRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				g2.setColor(Color.BLACK);
				g2.drawRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				
				int realStudWidth = stud_width/bricksWide;
				int smallBrickWidth = brick_width/bricksWide;
				for(int x = 0; x < bricksWide; ++x) {
					int indentX = mid-brick_width/2 + x*smallBrickWidth + (smallBrickWidth-realStudWidth)/2;
					for(int y = 0; y < bricksWide; ++y) {
						int indentY = mid-brick_width/2 + y*smallBrickWidth + (smallBrickWidth-realStudWidth)/2;
						g2.drawOval(indentX, indentY, realStudWidth, realStudWidth);
					}
				}
			}
		};
	}

	public static Icon snot(int size, final boolean enabled) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				if(enabled)
					g2.setColor(Color.RED);
				else
					g2.setColor(DISABLED_COLOR);
				// bottom plate
				g2.fillRect(0, size-plate_height, size, plate_height);
				// sideways plates:
				for(int i = 0; i < 3; i++) {
					g2.setColor(enabled ? (i % 2 == 0 ? Color.RED : Color.BLUE) : DISABLED_COLOR);
					g2.fillRect(i*size/3, 0, size/3, size-plate_height);				
					g2.setColor(Color.BLACK);
					g2.drawRect(i*size/3, 0, size/3, size-plate_height);
				}
				g2.drawRect(0, size-plate_height, size, plate_height);
			}
		};
	}

	public static Icon plateHeight(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLUE);
				// plate:
				g2.fillRect(0, size-plate_height, brick_width, plate_height);
				// stud:
				g2.fillRect((brick_width-stud_width)/2, size-plate_height-stud_height, stud_width, stud_height);

				g2.setColor(Color.BLACK);
				// plate:
				g2.drawRect(0, size-plate_height, brick_width, plate_height);
				// stud:
				g2.drawRect((brick_width-stud_width)/2, size-plate_height-stud_height, stud_width, stud_height);

				// main line:
				g2.drawLine(size-little, size-plate_height, size-little, size-1);
				// arrow:
				g2.drawLine(size-little, size-plate_height, size-1, size-plate_height+little);
				g2.drawLine(size-little, size-plate_height, size-2*little, size-plate_height+little);
				// arrow:
				g2.drawLine(size-little, size-1, size-1, size-1-little);
				g2.drawLine(size-little, size-1, size-2*little, size-1-little);
			}
		};
	}

	public static Icon brickHeight(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLUE);
				// plate:
				g2.fillRect(0, stud_height, brick_width, brick_height);
				// stud:
				g2.fillRect((brick_width-stud_width)/2, 0, stud_width, stud_height);

				g2.setColor(Color.BLACK);
				// plate:
				g2.drawRect(0, stud_height, brick_width, brick_height);
				// stud:
				g2.drawRect((brick_width-stud_width)/2, 0, stud_width, stud_height);

				// main line:
				g2.drawLine(size-little, stud_height, size-little, size-1);
				// arrow:
				g2.drawLine(size-little, stud_height, size-1, stud_height+little);
				g2.drawLine(size-little, stud_height, size-2*little, stud_height+little);
				// arrow:
				g2.drawLine(size-little, size-1, size-1, size-1-little);
				g2.drawLine(size-little, size-1, size-2*little, size-1-little);
			}
		};
	}

	public static Icon brickWidth(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.RED);
				g2.fillRect(mid-brick_width/2, size-brick_width, brick_width, brick_width);
				g2.fillOval(mid-stud_width/2, size-brick_width+(brick_width-stud_width)/2, stud_width, stud_width);
				g2.setColor(Color.BLACK);
				g2.drawRect(mid-brick_width/2, size-brick_width, brick_width, brick_width);
				g2.drawOval(mid-stud_width/2, size-brick_width+(brick_width-stud_width)/2, stud_width, stud_width);

				// main line:
				g2.drawLine(mid-brick_width/2, size-brick_width-little, mid+brick_width/2, size-brick_width-little);
				// arrow:
				g2.drawLine(mid-brick_width/2, size-brick_width-little, mid-brick_width/2+little, size-brick_width-little+little);
				g2.drawLine(mid-brick_width/2, size-brick_width-little, mid-brick_width/2+little, size-brick_width-little-little);
				// arrow:
				g2.drawLine(mid+brick_width/2, size-brick_width-little, mid+brick_width/2-little, size-brick_width-little+little);
				g2.drawLine(mid+brick_width/2, size-brick_width-little, mid+brick_width/2-little, size-brick_width-little-little);
			}
		};
	}
	
	public static Icon crop(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				Cropper.drawCropHighlight(g2, new Rectangle(3, 4, size-8, size-9));
			}
		};
	}
	
	public static Icon colorLegend(int size) {
		return new NormalIcon(size) {
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
	
	public static NormalIcon plus(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/3;
				g2.drawLine(mid-q, mid, mid+q, mid);
				g2.drawLine(mid, mid-q, mid, mid+q);
			}
		};
	}
	public static Icon minus(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.BLACK);
				int q = size/3;
				g2.drawLine(mid-q, mid, mid+q, mid);
			}
		};
	}

	public static Icon shorter(int size) {
		return new NormalIcon(size) {
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
	public static Icon slimmer(int size) {
		return new NormalIcon(size) {
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
	public static Icon taller(int size) {
		return new NormalIcon(size) {
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
	public static Icon wider(int size) {
		return new NormalIcon(size) {
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

	public static Icon treshold(int size) {
		return new NormalIcon(size) {
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
	
	public static Icon floydSteinberg(int size) {
		return new NormalIcon(size) {
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
	
	public static Icon saturation(int size) {
		return new NormalIcon(size) {
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
	public static Icon contrast(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillArc(0, 0, size, size, -90, -180);
				g2.setColor(Color.BLACK);
				g2.fillArc(0, 0, size, size, -90, 180);
				g2.drawOval(0, 0, size-1, size-1);
			}
		};
	}
	public static NormalIcon gamma(int size) {
		return new NormalIcon(size) {
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
	public static Icon brightness(int size) {
		return new NormalIcon(size) {
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
	public static NormalIcon sharpness(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(Color.WHITE);
				g2.fillOval(0, 0, size, size);
				g2.setColor(Color.BLACK);
				g2.drawOval(0, 0, size-1, size-1);
				g2.drawLine(mid, 0, mid, size-1);
			}
		};
	}
	
	private abstract static class NormalIcon implements Icon {
		int size, mid, little, 
		brick_height, plate_height, stud_height, 
		brick_width, stud_width;

		NormalIcon(int size) {
			this.size = size;

			mid = size/2;
			little = size/10;

			brick_height = size*6/7;
			plate_height = size*2/7;
			stud_height = size*1/7;
			brick_width = size*5/7;
			stud_width = brick_width*2/3;
		}		

		public int getIconHeight() {
			return size;
		}

		public int getIconWidth() {
			return size;
		}		

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D)g;
			g2.translate(x, y);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			paint(g2);
			g2.translate(-x, -y);			
		}

		abstract void paint(Graphics2D g2);
	}
	private abstract static class HalfWidthIcon implements Icon {
		int size;
		
		HalfWidthIcon(int size) {
			this.size = size;
		}		

		public int getIconHeight() {
			return size;
		}

		public int getIconWidth() {
			return size/2;
		}		

		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D)g;
			g2.translate(x, y);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			paint(g2);
			g2.translate(-x, -y);			
		}

		abstract void paint(Graphics2D g2);
	}}
