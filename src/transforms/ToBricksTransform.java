package transforms;

import java.awt.*;
import java.awt.image.*;
import transforms.ScaleTransform.ScaleQuality;
import mosaic.controllers.ColorController;
import mosaic.io.InstructionsBuilderI;
import mosaic.rendering.ProgressCallback;
import colors.*;
import bricks.*;

/**
 * Transforms an image to bricks in the given size of basic LEGO units.
 * @author LD
 */
public class ToBricksTransform implements InstructionsTransform {	
	private int width, height;
	private ToBricksType toBricksType;
	private ScaleTransform brickFromTopTransform, 
						   brickFromSideTransform, 
						   plateFromSideTransform, 
						   verticalPlateFromSideTransform, 
						   snotOutputTransform,
						   rTransform;
	private FloydSteinbergTransform ditheringTransform;
	private ThresholdTransform thresholdTransform;
	private LEGOColorGrid normalColors, sidewaysColors;
	private boolean[][] normalColorsChoosen;
	private ColorController cc;
	
	public ToBricksTransform(LEGOColor[] colors, ToBricksType toBricksType, int propagationPercentage, int width, int height, ColorController cc) {
		this.cc = cc;
		brickFromTopTransform = new ScaleTransform("Construct from top",  false, ScaleQuality.RetainColors);
		brickFromSideTransform = new ScaleTransform("Construct bricks from side", false, ScaleQuality.RetainColors);
		plateFromSideTransform = new ScaleTransform("Construct plates from side", false, ScaleQuality.RetainColors);
		verticalPlateFromSideTransform = new ScaleTransform("Construct vertical plates from side", false, ScaleQuality.RetainColors);

		snotOutputTransform = new ScaleTransform("SNOT output", false, ScaleQuality.RetainColors);
		rTransform = new ScaleTransform("To correct construction scale", false, ScaleQuality.RetainColors);
		
		LEGOColorLookUp.setColors(colors);

		ditheringTransform = new FloydSteinbergTransform(2, propagationPercentage, cc);
		thresholdTransform = new ThresholdTransform(2, cc);
		
		this.toBricksType = toBricksType;
		setBasicUnitSize(width, height);
	}
	
	public Transform getSnotOutputTransform() {
		return snotOutputTransform;
	}

	public Transform getBrickFromSideTransform(int studsLength) {
		brickFromSideTransform.setWidth(width/(SizeInfo.BRICK_WIDTH*studsLength));
		brickFromSideTransform.setHeight(height/SizeInfo.BRICK_HEIGHT);

		return brickFromSideTransform;
	}

	public BufferedLEGOColorTransform getMainTransform() {
		return ditheringTransform.getPropagationPercentage() == 0 ? thresholdTransform : ditheringTransform;
	}

	public Transform getPlateFromSideTransform(int studsLength) {
		plateFromSideTransform.setWidth(width/(SizeInfo.BRICK_WIDTH*studsLength));
		plateFromSideTransform.setHeight(height/SizeInfo.PLATE_HEIGHT);

		return plateFromSideTransform;
	}

	public Transform getRTransform() {
		rTransform.setWidth(width);
		rTransform.setHeight(height);

		return rTransform;
	}

	public Transform getVerticalPlateFromSideTransform() {
		verticalPlateFromSideTransform.setWidth(width/SizeInfo.PLATE_HEIGHT);
		verticalPlateFromSideTransform.setHeight(height/SizeInfo.BRICK_WIDTH);

		return verticalPlateFromSideTransform;
	}

	public Transform getBrickFromTopTransform(int studsWidth, int studsHeight) {
		brickFromTopTransform.setWidth(width/(SizeInfo.BRICK_WIDTH*studsWidth));
		brickFromTopTransform.setHeight(height/(SizeInfo.BRICK_WIDTH*studsHeight));

		return brickFromTopTransform;
	}

	public void setToBricksType(ToBricksType toBricksType) {
		this.toBricksType = toBricksType;
	}
	
	public void setPropagationPercentage(int pp) {
		if(ditheringTransform.setPropagationPercentage(pp))
			snotOutputTransform.clearBuffer(); // pipe line breakage => clear the basic transform buffer to enforce update in view.
	}

	public ToBricksType getToBricksType() {
		return toBricksType;
	}
	
	public void setBasicUnitSize(int width, int height) {
		this.width = width;
		this.height = height;
		updateBasicTransform();
	}
	
	public Dimension getBasicUnitSize() {
		if(getToBricksType() == ToBricksType.SNOT_IN_2_BY_2)
			return new Dimension(width, height);
		else
			return new Dimension(width/toBricksType.getUnitWidth(), height/toBricksType.getUnitHeight());
	}
	
	public boolean setColors(LEGOColor[] colors) {
		if(LEGOColorLookUp.setColors(colors)) {
			snotOutputTransform.clearBuffer();
			ditheringTransform.clearBuffer();
			thresholdTransform.clearBuffer();
			return true;
		}
		return false;		
	}
	
	private void updateBasicTransform() {
		snotOutputTransform.setWidth(width);
		snotOutputTransform.setHeight(height);		
	}

	/**
	 * Writes to original.
	 */
	public BufferedImage bestMatch(LEGOColorGrid normalColors, 
			                       LEGOColorGrid sidewaysColors, BufferedImage original) {
		if(this.normalColors == normalColors && this.sidewaysColors == sidewaysColors) {
			return original; // buffered!
		}
		
		if(height == 0 || width == 0)
			return original;
		if(original.getWidth() != width) {
			throw new IllegalArgumentException("Width " + original.getWidth() + "!=" + width);
		}
		if(original.getHeight() != height) {
			throw new IllegalArgumentException("Height " + original.getHeight() + "!=" + height);
		}

		this.normalColors = normalColors;
		this.sidewaysColors = sidewaysColors;

		int cw = width/SizeInfo.SNOT_BLOCK_WIDTH;
		int ch = height/SizeInfo.SNOT_BLOCK_WIDTH;
		normalColorsChoosen = new boolean[cw][ch];
		
		int[] outputPixels = new int[width*height];
		outputPixels = original.getRGB(0,  0, width, height, outputPixels, 0, width);
		
		for(int x = 0; x < cw; x++) {
			for(int y = 0; y < ch; y++) {				
				normalColorsChoosen[x][y] = arrayBestMatch(x, y, outputPixels);
			}
		}
		
		original.setRGB(0, 0, width, height, outputPixels, 0, width);
		return original;
	}
	
	/*
	 * Writes in original, return whether normal the best match
	 */
	private boolean arrayBestMatch(int blockX, int blockY, int[] original) {
		final int w = width;
		int originalIBlock = w*10*blockY + blockX*10;
		int n2 = 2;
		int n5 = 5;

		int distNormal = 0;
		for(int y = 0; y < n5; ++y) {
			int originalIY = originalIBlock + n2*w*y;
			LEGOColor[] row = normalColors.getRow(blockY*n5+y);
			for(int x = 0; x < n2; x++) {
				int originalIXY = originalIY + n5*x;
				Color normalColor = row[blockX*n2+x].getRGB();
				for(int x2 = 0; x2 < n5; x2++) {
					for(int y2 = 0; y2 < n2; ++y2) {
						int originalColor = original[originalIXY + x2 + w*y2];
						distNormal += ColorDifference.diffCIE94(normalColor, originalColor);
					}
				}
			}
		}
		
		n2 = 5;
		n5 = 2;
		int distSideways = 0;
		for(int y = 0; y < n5; ++y) {
			int originalIY = originalIBlock + n2*w*y;
			LEGOColor[] row = sidewaysColors.getRow(blockY*n5+y);
			for(int x = 0; x < n2; x++) {
				int originalIXY = originalIY + n5*x;
				Color sidewaysColor = row[blockX*n2+x].getRGB();
				for(int x2 = 0; x2 < n5; x2++) {
					for(int y2 = 0; y2 < n2; ++y2) {
						int originalColor = original[originalIXY + x2 + w*y2];
						distSideways += ColorDifference.diffCIE94(sidewaysColor, originalColor);
					}
				}
			}
		}
						
		boolean res = false;
		if(distNormal <= distSideways) {
			res = true;
			n2 = 2;
			n5 = 5;
		}
		
		for(int y = 0; y < n5; ++y) {
			int originalIY = originalIBlock + n2*w*y;
			LEGOColor[] row = (res ? normalColors : sidewaysColors).getRow(blockY*n5+y);
			for(int x = 0; x < n2; x++) {
				int originalIXY = originalIY + n5*x;
				int c = row[blockX*n2+x].getRGB().getRGB();				
				for(int x2 = 0; x2 < n5; x2++) {
					for(int y2 = 0; y2 < n2; ++y2) {
						original[originalIXY + x2 + w*y2] = c;
					}
				}
			}
		}
		return res;
	}
	
	@Override
	public BufferedImage transform(BufferedImage in) {
		return toBricksType.transform(in, this);
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return toBricksType.getTransformedSize(in, this);
	}

	/*
	 * Only for SNOT
	 */
	@Override
	public LEGOColor.CountingLEGOColor[] drawLastInstructions(Graphics2D g2, 
			Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize) {
		if(blockWidth != 10 || blockHeight != 10)
			throw new IllegalArgumentException("Block 10x10");
			
		return drawSnot(g2, basicUnitRect, toSize, false, true);
	}
	
	public LEGOColor.CountingLEGOColor[] drawAll(Graphics2D g2, Dimension toSize) {
		Rectangle basicUnitRect = new Rectangle(0, 0, width, height);
		return draw(g2, basicUnitRect, toSize, true, false);
	}
	
	public void drawAllSpaced(Graphics2D g2, Dimension toSize) {
		Rectangle basicUnitRect = new Rectangle(0, 0, width, height);
		draw(g2, basicUnitRect, toSize, true, false);
	}
	
	public LEGOColor.CountingLEGOColor[] draw(Graphics2D g2, Rectangle basicUnitRect, Dimension toSize, boolean showColors, boolean showOutlines) {
		int basicUnitWidth = getToBricksType().getUnitWidth();
		int basicUnitHeight = getToBricksType().getUnitHeight();
		if(getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
			if(showColors)
				return drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, toSize, 0, 0, showOutlines);
			else
				return drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, toSize);
		}
		else {
			if(showColors) {
				ToBricksType tbt = getToBricksType();
				return getMainTransform().drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, toSize, tbt.getStudsShownWide(), tbt.getStudsShownTall(), showOutlines);
			}
			else
				return getMainTransform().drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, toSize);
		}
	}

	/*
	 * Only for SNOT
	 */
	@Override
	public LEGOColor.CountingLEGOColor[] drawLastColors(Graphics2D g2, Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize, int numStudsWide, int numStudsTall, boolean showOutlines) {
		if(blockWidth != 10 || blockHeight != 10)
			throw new IllegalArgumentException("Block 10x10");
			
		return drawSnot(g2, basicUnitRect, toSize, true, showOutlines);
	}
	
	private LEGOColor.CountingLEGOColor[] drawSnot(Graphics2D g2, Rectangle basicUnitRect, 
			Dimension toSize, boolean drawColors, boolean showOutlines) {
		if(normalColorsChoosen == null)
			return new LEGOColor.CountingLEGOColor[]{};
		if(!drawColors) {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, toSize.width, toSize.height);			
		}
		
		int w = basicUnitRect.width/10;
		int h = basicUnitRect.height/10;

		double scaleW = (double)toSize.width / w;
		double scaleH = (double)toSize.height / h;

		int fontHeight = 0;
		if(!drawColors) {
			Font font = LEGOColor.makeFont(g2, (int)(scaleW/5), (int)(scaleH/5), cc, lastUsedColorCounts());
			g2.setFont(font);
			FontMetrics fm = g2.getFontMetrics(font);
			fontHeight = (fm.getDescent()+fm.getAscent())/2;			
		}
		
		if(showOutlines) {
			g2.setColor(Color.BLACK);
			g2.drawRect(0, 0, toSize.width, toSize.height);			
		}
		LEGOColor.CountingLEGOColor[] m = new LEGOColor.CountingLEGOColor[LEGOColor.getMaxRebrickableId()+1];
		int cnt = 0;
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int ix = basicUnitRect.x/10+x;
				int iy = basicUnitRect.y/10+y;
				
				if(normalColorsChoosen.length > ix && normalColorsChoosen[ix].length > iy) {
					if(normalColorsChoosen[ix][iy])
						cnt += snot(m, g2, basicUnitRect, true, drawColors, scaleW, scaleH, fontHeight, x, y, showOutlines);
					else
						cnt += snot(m, g2, basicUnitRect, false, drawColors, scaleW, scaleH, fontHeight, x, y, showOutlines);
				}
			}
		}
		
		LEGOColor.CountingLEGOColor[] ret = new LEGOColor.CountingLEGOColor[cnt];
		for(int i = 0, idx = 0; i < m.length; ++i) {
			if(m[i] != null)
				ret[idx++] = m[i];			
		}
		return ret;
	}
	
	/*
	 * For Instructions
	 */
	private int snot(LEGOColor.CountingLEGOColor[] m, Graphics2D g2, Rectangle basicUnitRect, 
								 boolean normal, boolean drawColors, 
			          			 double scaleW, double scaleH, int fontSize, int x, int y, boolean showOutlines) {
		int n2 = 2;
		int n5 = 5;
		if(!normal) {
			n2 = 5;
			n5 = 2;
		}
		
		int ret = 0;
		for(int j = 0; j < n5; j++) { // =
			int iy = basicUnitRect.y/n2+y*n5+j;
			for(int i = 0; i < n2; i++) { // |
				int ix = basicUnitRect.x/n5+x*n2+i;
				LEGOColor color = (normal ? normalColors : sidewaysColors).getRow(iy)[ix];
				int idx = color.getIDRebrickable();
				
				if(m[idx] == null) {
					m[idx] = new LEGOColor.CountingLEGOColor(color, 1);
					++ret;
				}
				else
					m[idx].cnt++;
				
				int xIndent = (int)Math.round(scaleW*x+scaleW/n2*i);
				int yIndent = (int)Math.round(scaleH*y+scaleH/n5*j);
				int w = (int)(1+scaleW/n2);
				int h = (int)(1+scaleH/n5);
				Rectangle r = new Rectangle(xIndent, yIndent, w, h);

				if(drawColors) {
					g2.setColor(color.getRGB());
					g2.fill(r);
					if(showOutlines) {
						g2.setColor(color.getRGB() == Color.BLACK ? Color.WHITE : Color.BLACK);
						g2.draw(r);						
					}
				}
				else {
					String id = cc.getShortIdentifier(color); // ix + "x" + iy;//
					int width = g2.getFontMetrics().stringWidth(id);
					int originX = (int)(r.getCenterX() - width/2);
					int originY = (int)(r.getCenterY() + fontSize/2);
					g2.drawString(id, originX, originY);											
					g2.draw(r);					
				}
			}			
		}
		return ret;
	}

	private static boolean addOne(LEGOColor.CountingLEGOColor[] m, LEGOColor c) {
		int idx = c.getIDRebrickable();
		if(m[idx] != null) {
			m[idx].cnt++;
			return false;
		}
		else {
			m[idx] = new LEGOColor.CountingLEGOColor(c, 1);					
			return true;
		}
	}
	
	private int addAll(LEGOColor.CountingLEGOColor[] m) {
		if(normalColorsChoosen == null)
			return 0; // nop
		int size = 0;
		for(int x = 0; x < normalColorsChoosen.length; x++) {
			for(int y = 0; y < normalColorsChoosen[0].length; y++) {
				boolean normalColorChosen = normalColorsChoosen[x][y];
				
				int n2 = 2;
				int n5 = 5;
				if(!normalColorChosen) {
					n2 = 5;
					n5 = 2;
				}				
				
				for(int h = 0; h < n5; ++h) {
					LEGOColor[] row = (normalColorChosen ? normalColors : sidewaysColors).getRow(y*n5+h);
					for(int w = 0; w < n2; ++w) {
						if(addOne(m, row[x*n2+w]))
							++size;
					}
				}				
			}
		}
		return size;
	}
	
	@Override
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts() {
		LEGOColor.CountingLEGOColor[] m = new LEGOColor.CountingLEGOColor[LEGOColor.getMaxRebrickableId()+1];
		int size = addAll(m);
		LEGOColor.CountingLEGOColor[] ret = new LEGOColor.CountingLEGOColor[size];		
		for(int i = 0, idx = 0; i < m.length; i++) {
			if(m[i] != null)
				ret[idx++] = m[i];
		}
		return ret;
	}

	// ONLY FOR SNOT!
	public void buildLastInstructions(InstructionsBuilderI printer, Rectangle bounds) {
		int id = 0;
		int maxX = Math.min(normalColorsChoosen.length, bounds.x+bounds.width);
		for(int x = bounds.x; x < maxX; x++) {
			int maxY = Math.min(normalColorsChoosen[0].length, bounds.y+bounds.height);
			for(int y = bounds.y; y < maxY;y++) {
				boolean normal = normalColorsChoosen[x][y]; 
				int n2 = 2;
				int n5 = 5;
				if(!normal) {
					n2 = 5;
					n5 = 2;
				}
				
				for(int j = 0; j < n5; j++) { // =
					int iy = y*n5+j;
					LEGOColor[] row = (normal ? normalColors : sidewaysColors).getRow(iy);
					for(int i = 0; i < n2; i++) { // |
						int ix = x*n2+i;
						LEGOColor color = row[ix];
						if(normal) {
							printer.add(id++, 2*x+i, 5*y+j, color);
						}
						else {
							printer.addSideways(id++, 5*x+i, 2*y+j, color);
						}
					}			
				}
			}
		}
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		getMainTransform().paintIcon(g, size);
	}

	@Override
	public void setProgressCallback(ProgressCallback p) {
		thresholdTransform.setProgressCallback(p);
		ditheringTransform.setProgressCallback(p);
	}
}
