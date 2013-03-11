package ui;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.io.*;
import colors.*;
import mosaic.ui.prepare.*;

public class Icons {
	public static final int SIZE_LARGE = 32;
	public static final int SIZE_SMALL = 16;

	private static ColorGroup[] colorGroups;
	static {
		try {
			colorGroups = ColorGroup.generateColorGroups();
		}
		catch(IOException e) {
			colorGroups = ColorGroup.generateBackupColorGroups();
		}
	}
	
	public static ImageIcon get(int size, String image) {
		String fileName = "icons" + File.separator + size + "x" + size + File.separator + image + ".png";
		return new ImageIcon(fileName);
	}

	private static Color randomColor(Object... seeds) {
		int hash = 0;
		for(Object o : seeds)
			hash += o.hashCode();
		ColorGroup group = colorGroups[hash % colorGroups.length];
		LEGOColor[] colors = group.getColors();
		return colors[hash % colors.length].rgb;
	}

	public static Icon colors(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				int w = 8;
				for(int y = 0; w*y < Math.min(colorGroups.length*w, size); y++) {
					LEGOColor[] colors = colorGroups[y].getColors();
					for(int x = 0; w*x < Math.min(colors.length*w, size); x++) {
						g2.setColor(colors[x].rgb);
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
	
	public static Icon showColors(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				int w = size/4;
				for(int y = 0; w*y < size; y++) {
					for(int x = 0; w*x < size; x++) {
						g2.setColor(randomColor(getClass(), y, 2*x));
						g2.fillRect(w*x, w*y, w, w);
						g2.setColor(Color.BLACK);
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

	public static Icon brick(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(randomColor(getClass()));
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

	public static Icon plate(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(randomColor(getClass()));
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

	public static Icon tile(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(randomColor(getClass()));
				g2.fillRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				g2.setColor(Color.BLACK);
				g2.drawRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
			}
		};
	}

	public static Icon stud(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(randomColor(getClass()));
				g2.fillRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				g2.fillOval(mid-stud_width/2, mid-stud_width/2, stud_width, stud_width);
				g2.setColor(Color.BLACK);
				g2.drawRect(mid-brick_width/2, mid-brick_width/2, brick_width, brick_width);
				g2.drawOval(mid-stud_width/2, mid-stud_width/2, stud_width, stud_width);
			}
		};
	}

	public static Icon snot(int size) {
		return new NormalIcon(size) {
			public void paint(Graphics2D g2) {
				g2.setColor(randomColor(getClass()));
				// bottom plate
				g2.fillRect(0, size-plate_height, size, plate_height);
				// sideways plates:
				for(int i = 0; i < 3; i++) {
					g2.setColor(randomColor(i*getClass().hashCode()));
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
				g2.setColor(randomColor(getClass()));
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
				g2.setColor(randomColor(getClass()));
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
				g2.setColor(randomColor(getClass()));
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
				g2.setColor(randomColor(getClass()));
				g2.fillRect(0, 0, size-1, size-1);
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
					LEGOColor color = colors[colors.length/2];
					g2.setColor(color.rgb);
					g2.fillRect(0, w*y, w, w);
					g2.setColor(Color.BLACK);
					g2.drawRect(0, w*y, w, w);
					g2.setFont(LEGOColor.makeFont(g2, size - w - 2, w, color));
					g2.drawString(color.getShortIdentifier(), w + 2, y*w+w);
				}
				g2.setFont(origFont);
			}
		};
	}
	
	public static Icon plus(int size) {
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
	public static Icon gamma(int size) {
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
	public static Icon sharpness(int size) {
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
}
