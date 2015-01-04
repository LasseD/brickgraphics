package transforms;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;

import mosaic.controllers.ColorController;

import colors.*;
import bricks.*;

/**
 * Transforms an image to bricks in the given size of basic LEGO units.
 * @author LD
 */
public class ToBricksTransform implements InstructionsTransform {	
	private int width, height;
	private ToBricksType toBricksType;
	private HalfToneType halfToneType;
	private ScaleTransform studTileTransform, 
						   twoByTwoTransform,
						   brickTransform, 
						   plateTransform, 
						   sidePlateTransform, 
						   basicTransform,
						   rTransform;
	private FloydSteinbergTransform ditheringTransform;
	private ThresholdTransform thresholdTransform;
	private LEGOColor[][] normalColors, sidewaysColors;
	private boolean[][] normalColorsChoosen;
	private ColorController cc;
	
	public ToBricksTransform(LEGOColor[] colors, ToBricksType toBricksType, HalfToneType halfToneType, int propagationPercentage, ColorController cc) {
		this.cc = cc;
		studTileTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		twoByTwoTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		brickTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		plateTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		sidePlateTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);

		basicTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		rTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
		LEGOColorLookUp.setColors(colors);

		ditheringTransform = new FloydSteinbergTransform(2, propagationPercentage, cc);
		thresholdTransform = new ThresholdTransform(2, cc);
		
		this.toBricksType = toBricksType;
		this.halfToneType = halfToneType;
		updateScaleTransforms();
	}
	
	public Transform getBasicTransform() {
		return basicTransform;
	}

	public Transform getBrickTransform() {
		return brickTransform;
	}

	public BufferedLEGOColorTransform getMainTransform() {
		if(halfToneType == HalfToneType.FloydSteinberg)
			return ditheringTransform;
		return thresholdTransform;
	}

	public Transform getPlateTransform() {
		return plateTransform;
	}

	public Transform getRTransform() {
		return rTransform;
	}

	public Transform getSidePlateTransform() {
		return sidePlateTransform;
	}

	public Transform getStudTileTransform() {
		return studTileTransform;
	}

	public Transform getTwoByTwoTransform() {
		return twoByTwoTransform;
	}

	public void setToBricksType(ToBricksType toBricksType) {
		this.toBricksType = toBricksType;
	}
	
	public void setPropagationPercentage(int pp) {
		if(ditheringTransform.setPropagationPercentage(pp))
			basicTransform.clearBuffer(); // pipe line breakage => clear the basic transform buffer to enforce update in view.
	}

	public ToBricksType getToBricksType() {
		return toBricksType;
	}
	
	public void setBasicUnitSize(int width, int height) {
		this.width = width;
		this.height = height;
		updateScaleTransforms();
	}
	
	public void setHalfToneType(HalfToneType halfToneType) {
		if(this.halfToneType != halfToneType) {
			this.halfToneType = halfToneType;
			basicTransform.clearBuffer();
		}
	}

	public boolean setColors(LEGOColor[] colors) {
		if(LEGOColorLookUp.setColors(colors)) {
			basicTransform.clearBuffer();
			ditheringTransform.clearBuffer();
			thresholdTransform.clearBuffer();
			return true;
		}
		return false;		
	}
	
	private void updateScaleTransforms() {
		studTileTransform.setWidth(width/SizeInfo.BRICK_WIDTH);
		studTileTransform.setHeight(height/SizeInfo.BRICK_WIDTH);

		twoByTwoTransform.setWidth(width/SizeInfo.SNOT_BLOCK_WIDTH);
		twoByTwoTransform.setHeight(height/SizeInfo.SNOT_BLOCK_WIDTH);

		brickTransform.setWidth(width/SizeInfo.BRICK_WIDTH);
		brickTransform.setHeight(height/SizeInfo.BRICK_HEIGHT);

		plateTransform.setWidth(width/SizeInfo.BRICK_WIDTH);
		plateTransform.setHeight(height/SizeInfo.PLATE_HEIGHT);

		sidePlateTransform.setWidth(width/SizeInfo.PLATE_HEIGHT);
		sidePlateTransform.setHeight(height/SizeInfo.BRICK_WIDTH);

		basicTransform.setWidth(width);
		basicTransform.setHeight(height);
		
		rTransform.setWidth(width);
		rTransform.setHeight(height);
	}

	/**
	 * Writes to original.
	 */
	public BufferedImage bestMatch(LEGOColor[][] normalColors, 
			                       LEGOColor[][] sidewaysColors, BufferedImage original) {
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
		
		int[] o = new int[width*height];
		o = original.getRGB(0,  0, width, height, o, 0, width);
		
		for(int x = 0; x < cw; x++) {
			for(int y = 0; y < ch; y++) {				
				normalColorsChoosen[x][y] = arrayBestMatch(x, y, o);
			}
		}
		
		original.setRGB(0, 0, width, height, o, 0, width);
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
		for(int x = 0; x < n2; x++) {
			int originalIX = originalIBlock + n5*x;
			for(int y = 0; y < n5; ++y) {
				int originalIXY = originalIX + n2*w*y;
				Color normalColor = normalColors[blockX*n2+x][blockY*n5+y].getRGB();
				for(int x2 = 0; x2 < n5; x2++) {
					for(int y2 = 0; y2 < n2; ++y2) {
						int originalColor =  original[originalIXY + x2 + w*y2];
						distNormal += ColorDifference.diffCIE94(normalColor, originalColor);
					}
				}
			}
		}
		
		n2 = 5;
		n5 = 2;
		int distSideways = 0;
		for(int x = 0; x < n2; x++) {
			int originalIX = originalIBlock + n5*x;
			for(int y = 0; y < n5; ++y) {
				int originalIXY = originalIX + n2*w*y;
				Color sidewaysColor = sidewaysColors[blockX*n2+x][blockY*n5+y].getRGB();
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
		
		for(int x = 0; x < n2; x++) {
			int originalIX = originalIBlock + n5*x;
			for(int y = 0; y < n5; ++y) {
				int originalIXY = originalIX + n2*w*y;
				int c = (res ? normalColors[blockX*n2+x][blockY*n5+y] : sidewaysColors[blockX*n2+x][blockY*n5+y]).getRGB().getRGB();				
				for(int x2 = 0; x2 < n5; x2++) {
					for(int y2 = 0; y2 < n2; ++y2) {
						//original[originalIXY + x2 + w*y2] = (distNormal == distSideways ? Color.WHITE : (res ? Color.red : Color.BLUE)).getRGB();
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

	/*
	 * Only for SNOT
	 */
	@Override
	public Set<LEGOColor> drawLastInstructions(Graphics2D g2, 
			Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize) {
		if(blockWidth != 10 || blockHeight != 10)
			throw new IllegalArgumentException("Block 10x10");
			
		return drawLastInstructions(g2, basicUnitRect, toSize, false);
	}

	/*
	 * Only for SNOT
	 */
	@Override
	public Set<LEGOColor> drawLastColors(Graphics2D g2, 
			Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize, int ignore) {
		if(blockWidth != 10 || blockHeight != 10)
			throw new IllegalArgumentException("Block 10x10");
			
		return drawLastInstructions(g2, basicUnitRect, toSize, true);
	}
	
	private Set<LEGOColor> drawLastInstructions(Graphics2D g2, Rectangle basicUnitRect, Dimension toSize, boolean drawColors) {
		if(!drawColors) {
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, toSize.width, toSize.height);			
		}
		
		int w = basicUnitRect.width/10;
		int h = basicUnitRect.height/10;

		double scaleW = (double)toSize.width / w;
		double scaleH = (double)toSize.height / h;

		Font font = LEGOColor.makeFont(g2, (int)(scaleW/5), (int)(scaleH/5), cc, lastUsedColorCounts());
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);
		int fontHeight = (fm.getDescent()+fm.getAscent())/2;
		
		g2.setColor(Color.BLACK);
		g2.drawRect(0, 0, toSize.width, toSize.height);
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int ix = basicUnitRect.x/10+x;
				int iy = basicUnitRect.y/10+y;
				
				if(normalColorsChoosen.length > ix && normalColorsChoosen[ix].length > iy) {
					if(normalColorsChoosen[ix][iy]) {
						used.addAll(snot(g2, basicUnitRect, true, drawColors, scaleW, scaleH, fontHeight, x, y));
					}
					else {
						used.addAll(snot(g2, basicUnitRect, false, drawColors, scaleW, scaleH, fontHeight, x, y));
					}		
				}
			}
		}
		return used;
	}
	
	/*
	 * For Instructions
	 */
	private List<LEGOColor> snot(Graphics2D g2, Rectangle basicUnitRect, boolean normal, boolean drawColors, 
			          			 double scaleW, double scaleH, int fontSize, int x, int y) {
		int n2 = 2;
		int n5 = 5;
		if(!normal) {
			n2 = 5;
			n5 = 2;
		}
		
		List<LEGOColor> used = new LinkedList<LEGOColor>();
		for(int i = 0; i < n2; i++) { // |
			for(int j = 0; j < n5; j++) { // =
				LEGOColor color;
				int ix = basicUnitRect.x/n5+x*n2+i;
				int iy = basicUnitRect.y/n2+y*n5+j;
				if(normal)
					color = normalColors[ix][iy];
				else
					color = sidewaysColors[ix][iy];
				used.add(color);
				
				int xIndent = (int)Math.round(scaleW*x+scaleW/n2*i);
				int yIndent = (int)Math.round(scaleH*y+scaleH/n5*j);
				int w = (int)Math.round(scaleW/n2);
				int h = (int)Math.round(scaleH/n5);
				Rectangle r = new Rectangle(xIndent, yIndent, w, h);

				if(drawColors) {
					g2.setColor(color.getRGB());
					g2.fill(r);					
					g2.setColor(color.getRGB() == Color.BLACK ? Color.WHITE : Color.BLACK);
				}
				else {
					String id = cc.getShortIdentifier(color); // ix + "x" + iy;//
					int width = g2.getFontMetrics().stringWidth(id);
					int originX = (int)(r.getCenterX() - width/2);
					int originY = (int)(r.getCenterY() + fontSize/2);
					g2.drawString(id, originX, originY);											
				}
				g2.draw(r);					
			}			
		}
		return used;
	}

	private static void addOne(Map<LEGOColor, Integer> m, LEGOColor c) {
		if(m.containsKey(c)) {
			m.put(c, m.get(c)+1);					
		}
		else {
			m.put(c, 1);					
		}
	}
	
	private void addAll(Map<LEGOColor, Integer> m) {
		for(int x = 0; x < normalColorsChoosen.length; x++) {
			for(int y = 0; y < normalColorsChoosen[0].length; y++) {
				boolean normalColorChosen = normalColorsChoosen[x][y];
				
				int n2 = 2;
				int n5 = 5;
				if(!normalColorChosen) {
					n2 = 5;
					n5 = 2;
				}				
				
				for(int w = 0; w < n2; ++w) {
					for(int h = 0; h < n5; ++h) {
						if(normalColorChosen) {
							addOne(m, normalColors[x*n2+w][y*n5+h]);
						}
						else {
							addOne(m, sidewaysColors[x*n2+w][y*n5+h]);							
						}						
					}
				}				
			}
		}
	}
	
	@Override
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts() {
		Map<LEGOColor, Integer> colorCounts = new TreeMap<LEGOColor, Integer>();

		addAll(colorCounts);
		
		List<Map.Entry<LEGOColor, Integer>> l = new ArrayList<Map.Entry<LEGOColor, Integer>>(colorCounts.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<LEGOColor, Integer>>() {
			@Override
			public int compare(Map.Entry<LEGOColor, Integer> o1, Map.Entry<LEGOColor, Integer> o2) {
				//return o2.getValue().compareTo(o1.getValue());
				return o1.getKey().getID() - o2.getKey().getID();
			}
		});
		LEGOColor.CountingLEGOColor[] out = new LEGOColor.CountingLEGOColor[l.size()];
		for(int i = 0; i < l.size(); i++) {
			out[i] = new LEGOColor.CountingLEGOColor(); 
			out[i].c = l.get(i).getKey();
			out[i].cnt = l.get(i).getValue();
		}
		return out;
	}

	// ONLY FOR SNOT!
	public void buildLastInstructions(LDRPrinter.LDRBuilder printer, Rectangle bounds) {
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
				
				for(int i = 0; i < n2; i++) { // |
					for(int j = 0; j < n5; j++) { // =
						LEGOColor color;
						int ix = x*n2+i;
						int iy = y*n5+j;
						if(normal) {
							color = normalColors[ix][iy];
							printer.add(2*x+i, 5*y+j, color);
						}
						else {
							color = sidewaysColors[ix][iy];
							printer.addSideways(5*x+i, 2*y+j, color);
						}
					}			
				}
			}
		}
	}
}
