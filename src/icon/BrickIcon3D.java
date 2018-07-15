package icon;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Draws isometric LEGO pieces.
 * @author LD
 */
public class BrickIcon3D {
	public static final float TRIANGLE_HEIGHT_PER_SECTION = 0.4f;
	public static final float PLATE_HEIGHT_PER_SECTION_RATIO = 0.44f;
	public static final float STUD_WIDTH_MULT = 1.3f;
	
	// From constructor:
	private int iconWidth, studsWide, studsDeep; 
	
	// Computed:
	private int sections; // "Sections" of the drawing, each for a stud shown width-wise.
	private int plateHeightDY, studHeight;
	private float sectionWidth, sectionHeight, sectionDiagonal;
	
	public BrickIcon3D(int iconWidth, int studsWide, int studsDeep) {
		this.iconWidth = iconWidth;
		this.studsWide = studsWide;
		this.studsDeep = studsDeep;
		sections = studsWide+studsDeep;
		sectionWidth = iconWidth / (float)sections;
		sectionHeight = TRIANGLE_HEIGHT_PER_SECTION*sectionWidth;
		plateHeightDY = (int)(PLATE_HEIGHT_PER_SECTION_RATIO * sectionWidth);
		studHeight = (int)(plateHeightDY * 1.7 / 3.2);
		sectionDiagonal = (float)Math.sqrt(sectionWidth*sectionWidth + sectionHeight*sectionHeight);
	}
		
	private int drawStudFacingUp(Graphics g, Color color, Point base) {
		int ovalWidth = (int)(sectionDiagonal*STUD_WIDTH_MULT*4.8/7.8);
		int ovalHeight = (int)(ovalWidth*TRIANGLE_HEIGHT_PER_SECTION);
		int leftX = (int)(base.x-ovalWidth*0.5);
		int rightX = (int)(base.x+ovalWidth*0.5);
		int top = base.y-ovalHeight/2-studHeight;
		if(g == null)
			return top;
		
		// Draw lower oval:
		g.setColor(color);
		g.fillOval(leftX, base.y-ovalHeight/2, ovalWidth, ovalHeight);
		g.setColor(Color.BLACK);
		g.drawOval(leftX, base.y-ovalHeight/2, ovalWidth, ovalHeight);
		
		// Draw sides:
		g.setColor(color);
		g.fillRect(leftX, base.y-studHeight, ovalWidth, studHeight);
		g.setColor(Color.BLACK);
		g.drawLine(leftX, base.y, leftX, base.y-studHeight);
		g.drawLine(rightX, base.y, rightX, base.y-studHeight);

		// Draw upper oval:
		g.setColor(color);
		g.fillOval(leftX, top, ovalWidth, ovalHeight);
		g.setColor(Color.BLACK);
		g.drawOval(leftX, top, ovalWidth, ovalHeight);		
		return top;
	}
	
	public int drawElementStudsUp(Graphics2D g, Color color, int platesHigh, boolean drawStuds) {
		// Use "iconWidth" as a base for the drawing. 
		/*
		   / \
		  /  /|
		 |\ / | h (plates high)
		 \ | /
		  \|/
		  d  w
		 */
		final int blockHeight = platesHigh*plateHeightDY;
		final int t = (int)(TRIANGLE_HEIGHT_PER_SECTION * sectionWidth); // triangle y-increment per section.
		
		// Fill the block: Left side lower first, then follow around clockwise:
		int[] blockXs = new int[]{0, 0, (int)(sectionWidth*(sections-studsDeep)), 
								  iconWidth-1, iconWidth-1, (int)(sectionWidth*studsDeep)};
		int[] blockYs = new int[]{-t*studsDeep, -t*studsDeep-blockHeight, -t*sections-blockHeight, 
								  -t*studsWide-blockHeight, -t*studsWide, 0};
		if(g != null) {
			g.setColor(color);
			g.fillPolygon(blockXs, blockYs, 6);

			// Draw outline of block:
			g.setColor(Color.BLACK);
			g.drawPolygon(blockXs, blockYs, 6);		
			int xIndentLowerCorner = (int)(studsDeep*sectionWidth);
			g.drawLine(0, -t*studsDeep-blockHeight, xIndentLowerCorner, -blockHeight);
			g.drawLine(xIndentLowerCorner, 0, xIndentLowerCorner, -blockHeight);
			g.drawLine(iconWidth-1, -studsWide*t-blockHeight, xIndentLowerCorner, -blockHeight);			
		}
		
		int top = -t*sections-blockHeight;
		if(!drawStuds)
			return -top;
		
		// Draw studs:
		for(int y = 0; y < studsDeep; ++y) {
			float startX = sectionWidth*(y+1);
			float startY = -blockHeight - t*studsDeep + t*y;
			for(int x = 0; x < studsWide; ++x) {
				int px = (int)(startX + x * sectionWidth);
				int py = (int)(startY - x * t);
				int topStud = drawStudFacingUp(g, color, new Point(px, py));	
				if(topStud < top)
					top = topStud;
			}
		}
		return -top;
	}	
	
	public int drawSNOT(Graphics2D g, Color color) {
		assert(studsWide == 2 && studsDeep == 1);
		int t = (int)(TRIANGLE_HEIGHT_PER_SECTION * sectionWidth); // triangle y-increment per section.
		int plateWidthDY = (plateHeightDY*5)/2;
		
		// Fill the block: Left side lower first, then follow around clockwise:
		int[] blockXs = new int[]{0, 0, (int)(sectionWidth*2), iconWidth-1, iconWidth-1, (int)sectionWidth};
		int[] blockYs = new int[]{-t, -t-plateHeightDY-plateWidthDY, -3*t-plateHeightDY-plateWidthDY, 
								  -2*t-plateHeightDY-plateWidthDY, -2*t, 0};
		if(g != null) {
			g.setColor(color);
			g.fillPolygon(blockXs, blockYs, 6);

			// Draw outline of block:
			g.setColor(Color.BLACK);
			g.drawPolygon(blockXs, blockYs, 6);				
			// Draw outline of bricks:
			int splitX = (int)sectionWidth;
			g.drawLine(splitX, 0, splitX, -plateHeightDY-plateWidthDY); // Vertical "center" line.
			g.drawLine(0, -t-plateHeightDY, splitX, -plateHeightDY); // Left mid "horizontal" split line.
			g.drawLine(0, -t-plateHeightDY-plateWidthDY, splitX, -plateHeightDY-plateWidthDY); // Left top "horizontal" split line.
			g.drawLine(iconWidth-1, -2*t-plateHeightDY, splitX, -plateHeightDY); // Right mid "horizontal" split line.
			g.drawLine(iconWidth-1, -2*t-plateHeightDY-plateWidthDY, splitX, -plateHeightDY-plateWidthDY); // Right top "horizontal" split line.
			g.drawLine(2*splitX, -t, 2*splitX, -t-plateHeightDY);
			for(int i = 0; i < 4; ++i) {
				int x0 = (i+1)*2*splitX/5;
				int y1 = -plateHeightDY-(i+1)*2*t/5;
				g.drawLine(x0+splitX, y1, x0+splitX, y1-plateWidthDY);
				g.drawLine(x0, y1-t-plateWidthDY, x0+splitX, y1-plateWidthDY);
			}
			// Draw anti stud:
			int wallDX = (int)(sectionWidth*1.6/8.0);
			int wallDY = (int)(plateWidthDY*1.6/8.0);
			int[] wallXs = new int[]{wallDX, wallDX, splitX-wallDX, splitX-wallDX};
			int[] wallYs = new int[]{(int)(-t*((sectionWidth-wallDX)/sectionWidth)) - plateHeightDY-wallDY, 
									 (int)(-t*((sectionWidth-wallDX)/sectionWidth)) - plateHeightDY-plateWidthDY+wallDY, 
									 (int)(-t*(wallDX/sectionWidth)) - plateHeightDY-plateWidthDY+wallDY, 
									 (int)(-t*(wallDX/sectionWidth)) - plateHeightDY-wallDY};
			g.setColor(color.darker());
			g.fillPolygon(wallXs, wallYs, 4);
			g.setColor(Color.BLACK);
			g.drawPolygon(wallXs, wallYs, 4);
			int midX = 2*wallDX;
			int midY = -t-plateHeightDY-wallDY;
			int midTopY = (int)(-t*((sectionWidth-midX)/sectionWidth)) - plateHeightDY-wallDY-(plateWidthDY-2*wallDY);
			g.drawLine(midX, midY, midX, midTopY);
			g.drawLine(wallXs[0], wallYs[0], midX, midY);
			g.drawLine(wallXs[3], midY-(int)(t*(midX-wallXs[3])/sectionWidth), midX, midY);
		}
		return -blockYs[2];
	}	

	public int drawElementStudsOut(Graphics2D g, Color color, int platesHigh, boolean drawStuds) {
		// Use "iconWidth" as a base for the drawing. 
		/* 
		   / \
		  /  /|
		 |\ / | d
		 \ | /
		  \|/
		 h  w
		 wDX + hDX = iconWidth
		 wDX = hDX*w*5/2 => iconWidth = hDX + hDX*w*5/2 => hDX = iconWidth/(1+w*5/2)
		 */
		final int HDX = (int)(iconWidth/(1+studsWide*2.5/platesHigh));
		//final int hDX = HDX/platesHigh;
		//final int hDY = (int)(hDX*TRIANGLE_HEIGHT_PER_SECTION);
		final int HDY = (int)(HDX*TRIANGLE_HEIGHT_PER_SECTION);

		final int WDX = (iconWidth - HDX);		
		final int wDX = WDX/studsWide;
		final int WDY = (int)(WDX*TRIANGLE_HEIGHT_PER_SECTION);
		final int wDY = (int)(wDX*TRIANGLE_HEIGHT_PER_SECTION);

		final int DDY = (int)(PLATE_HEIGHT_PER_SECTION_RATIO * wDX * 2.5 * studsDeep);
		final int dDY = (int)(PLATE_HEIGHT_PER_SECTION_RATIO * wDX * 2.5);
		
		// Fill the block: Left side lower first, then follow around clockwise:
		int[] blockXs = new int[]{0, 0, WDX, iconWidth-1, iconWidth-1, HDX};
		int[] blockYs = new int[]{-HDY, -HDY-DDY, -HDY-DDY-WDY, -WDY-DDY, -WDY, 0};
		if(g == null)
			return -blockYs[2];
		
		g.setColor(color);
		g.fillPolygon(blockXs, blockYs, 6);

		// Draw outline of block:
		g.setColor(Color.BLACK);
		g.drawPolygon(blockXs, blockYs, 6);		
		g.drawLine(blockXs[1], blockYs[1], HDX, -DDY);
		g.drawLine(blockXs[3], blockYs[3], HDX, -DDY);
		g.drawLine(blockXs[5], blockYs[5], HDX, -DDY);
		
		if(!drawStuds)
			return -blockYs[2];		

		// Draw studs:
		AffineTransform initTransform = g.getTransform();
		AffineTransform sheared = new AffineTransform();
		sheared.translate(initTransform.getTranslateX() + HDX, initTransform.getTranslateY()-DDY);
		sheared.shear(0, -wDY/(double)wDX);
		g.setTransform(sheared);
				
		int studHeight = (int)(dDY * 4.8f / 7.8f);
		int studWidth = (int)(wDX * 4.8f / 7.8f);
		for(int y = 0; y < studsDeep; ++y) {
			float startX = wDX*0.5f;
			float startY = dDY*0.5f + y*dDY;
			for(int x = 0; x < studsWide; ++x) {
				int px = (int)(startX + x * wDX);
				int py = (int)(startY);
				g.setColor(Color.BLACK);
				g.drawOval(px - studWidth/2, py - studHeight/2, studWidth, studHeight);
/*
				int dx = hDX/2;
				int dy = hDY/2;

				// Lines between bottom and top:
				int[] studCornersX = new int[]{px-dx, px+dx, px+dx+dx, px+dx-dx};
				int[] studCornersY = new int[]{py+studHeight/2, py-studHeight/2, py+dy-studHeight/2, py+dy+studHeight/2};
				g.setColor(color);
				g.fillPolygon(studCornersX, studCornersY, 4);
				g.setColor(Color.BLACK);
				g.drawLine(studCornersX[0], studCornersY[0], studCornersX[3], studCornersY[3]);
				g.drawLine(studCornersX[1], studCornersY[1], studCornersX[2], studCornersY[2]);

				// Upper stud plane:
				g.setColor(color);
				g.fillOval(px+dx - studWidth/2, py+dy - studHeight/2, studWidth, studHeight);
				g.setColor(Color.BLACK);
				g.drawOval(px+dx - studWidth/2, py+dy - studHeight/2, studWidth, studHeight);*/
			}
		}
		g.setTransform(initTransform);
		return -blockYs[2];
	}	
	
	/**
	 * Test method
	 * @param args ignored
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				JFrame w = new JFrame("TEST 3D ICON");

				JPanel p = new JPanel() {
					@Override 
					public void paint(Graphics g) {
						Graphics2D g2 = (Graphics2D)g;
						g2.translate(0, getHeight());
						BrickIcon3D b = new BrickIcon3D(getWidth(), 2, 1);
						b.drawSNOT(g2, Color.GREEN);
					}
				};
				w.add(p);
				w.setSize(800, 800);
				w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				w.setVisible(true);
			}
		});
	}
}
