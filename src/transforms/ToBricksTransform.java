package transforms;

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
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
						   brickTransform, 
						   plateTransform, 
						   sidePlateTransform, 
						   basicTransform,
						   rTransform;
	private FloydSteinbergTransform ditheringTransform;
	private ThresholdTransform thresholdTransform;
	private LEGOColorApproximator colorApproximator;
	private LEGOColor[][] normalColors, sidewaysColors;
	private boolean[][] normalColorsChoosen;
	
	public ToBricksTransform(LEGOColor[] colors, ToBricksType toBricksType, HalfToneType halfToneType, boolean useLDrawColors) {
		studTileTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		brickTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		plateTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		sidePlateTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);

		basicTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_BILINEAR);
		rTransform = new ScaleTransform(ScaleTransform.Type.dims, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		
		colorApproximator = new LEGOColorApproximator(colors, 7, useLDrawColors);
		ditheringTransform = new FloydSteinbergTransform(colorApproximator);
		thresholdTransform = new ThresholdTransform(colorApproximator);
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
		else 
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

	public void setToBricksType(ToBricksType toBricksType) {
		this.toBricksType = toBricksType;
	}

	public ToBricksType getToBricksType() {
		return toBricksType;
	}
	
	public void setBasicUnitSize(int width, int height) {
		//System.out.println("setBasicUnitSize " + width + "x" + height);
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

	public boolean setColors(LEGOColor[] colors, boolean useLDrawColors) {
		if(colorApproximator.setColors(colors, useLDrawColors)) {
			basicTransform.clearBuffer();
			ditheringTransform.clearBuffer();
			thresholdTransform.clearBuffer();
			return true;
		}
		return false;		
	}
	
	private void updateScaleTransforms() {
		studTileTransform.setWidth(width/Sizes.brick.width());
		studTileTransform.setHeight(height/Sizes.brick.width());

		brickTransform.setWidth(width/Sizes.brick.width());
		brickTransform.setHeight(height/Sizes.brick.height());

		plateTransform.setWidth(width/Sizes.brick.width());
		plateTransform.setHeight(height/Sizes.plate.height());

		sidePlateTransform.setWidth(width/Sizes.plate.height());
		sidePlateTransform.setHeight(height/Sizes.brick.width());

		basicTransform.setWidth(width);
		basicTransform.setHeight(height);
		
		rTransform.setWidth(width);
		rTransform.setHeight(height);
	}

	/**
	 * Writes to original.
	 */
	public BufferedImage bestMatch(BufferedImage normal, LEGOColor[][] normalColors, 
			                       BufferedImage sideways, LEGOColor[][] sidewaysColors, BufferedImage original) {
		if(original.getWidth() != width) {
			throw new IllegalArgumentException("Width " + original.getWidth() + "!=" + width);
		}
		if(original.getHeight() != height) {
			throw new IllegalArgumentException("Height " + original.getHeight() + "!=" + height);
		}
		if(normal.getWidth() != normalColors.length) {
			throw new IllegalArgumentException("Normal mismatch width: " + normal.getWidth() + "!=" + normalColors.length);			
		}
		if(normal.getHeight() != normalColors[0].length) {
			throw new IllegalArgumentException("Normal mismatch height: " + normal.getHeight() + "!=" + normalColors[0].length);			
		}
		if(sideways.getWidth() != sidewaysColors.length) {
			throw new IllegalArgumentException("sideways mismatch width: " + sideways.getWidth() + "!=" + sidewaysColors.length);
		}
		if(sideways.getHeight() != sidewaysColors[0].length) {
			throw new IllegalArgumentException("sideways mismatch height: " + sideways.getHeight() + "!=" + sidewaysColors[0].length);
		}

		this.normalColors = normalColors;
		this.sidewaysColors = sidewaysColors;

		int cw = width/Sizes.block.width();
		int ch = height/Sizes.block.height();
		normalColorsChoosen = new boolean[cw][ch];
		
		for(int x = 0; x < cw; x++) {
			for(int y = 0; y < ch; y++) {
				int[] n = normal.getRGB(x*2, y*5, 2, 5, null, 0, 2);
				int[] s = sideways.getRGB(x*5, y*2, 5, 2 , null, 0, 5);
				
				int ox = x*Sizes.block.width();
				int oy = y*Sizes.block.height();
				int[] o = original.getRGB(ox, oy, Sizes.block.width(), Sizes.block.height(), null, 0, Sizes.block.width());

				normalColorsChoosen[x][y] = arrayBestMatch(n, s, o);
				original.setRGB(ox, oy, Sizes.block.width(), Sizes.block.height(), o, 0, Sizes.block.width());
			}
		}
		return original;
	}
	
	/*
	 * Writes in original, return whether normal the best match
	 */
	private boolean arrayBestMatch(int[] normal, int[] sideways, int[] original) {
		int distNormal = 0;
		int distSideways = 0;
		int length = original.length;
		
		for(int i = 0; i < length; i++) {
			int o = original[i];
			int n = normal[i/20 + (i % Sizes.block.width()) / Sizes.brick.width()];
			distNormal += LEGOColorApproximator.dist(n, o);
			int s = sideways[i/2%Sizes.brick.width() + 5*(i/50)];
			distSideways += LEGOColorApproximator.dist(s, o);
		}
		
		if(distNormal <= distSideways) {
			for(int i = 0; i < length; i++) {
				original[i] = normal[2*(i/20) + (i % 10)/5];				
			}
			return true;
		}
		else {
			for(int i = 0; i < length; i++) {
				original[i] = sideways[(i/2)%5 + 5*(i/50)]; // xAdd + yAdd
			}
			return false;
		}
	}
	
	public BufferedImage transform(BufferedImage in) {
		return toBricksType.transform(in, this);
	}

	/*
	 * Only for SNOT
	 */
	public Set<LEGOColor> drawLastInstructions(Graphics2D g2, 
			Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize) {
		if(blockWidth != 10 || blockHeight != 10)
			throw new IllegalArgumentException("Block 10x10");
			
		return drawLastInstructions(g2, basicUnitRect, toSize, false);
	}

	/*
	 * Only for SNOT
	 */
	public Set<LEGOColor> drawLastColors(Graphics2D g2, 
			Rectangle basicUnitRect, int blockWidth, int blockHeight, Dimension toSize) {
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

		Font font = LEGOColor.makeFont(g2, (int)(scaleW/5), (int)(scaleH/5), lastUsedColors());
		g2.setFont(font);
		int fontHeight = g2.getFontMetrics(font).getHeight();
		
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
					g2.setColor(color.rgb);
					g2.fill(r);
					g2.setColor(Color.BLACK);
				}
				else {
					String id = color.getShortIdentifier(); // ix + "x" + iy;//
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

	private void addAll(Map<LEGOColor, Integer> m, LEGOColor[][] colors) {
		for(int x = 0; x < colors.length; x++) {
			for(int y = 0; y < colors[0].length; y++) {
				LEGOColor c = colors[x][y];
				if(m.containsKey(c)) {
					m.put(c, m.get(c)+1);					
				}
				else {
					m.put(c, 0);					
				}
			}
		}
	}
	
	public LEGOColor[] lastUsedColors() {
		Map<LEGOColor, Integer> m = new TreeMap<LEGOColor, Integer>();
		addAll(m, normalColors);
		addAll(m, sidewaysColors);
		List<Map.Entry<LEGOColor, Integer>> l = new ArrayList<Map.Entry<LEGOColor, Integer>>(m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<LEGOColor, Integer>>() {
			public int compare(Map.Entry<LEGOColor, Integer> o1, Map.Entry<LEGOColor, Integer> o2) {
				//return o2.getValue().compareTo(o1.getValue());
				return o1.getKey().id_LEGO - o2.getKey().id_LEGO;
			}
		});
		LEGOColor[] out = new LEGOColor[l.size()];
		for(int i = 0; i < l.size(); i++) {
			out[i] = l.get(i).getKey();
		}
		return out;
	}

	// ONLY FOR SNOT!
	public void buildLastInstructions(LDRBuilder printer, Rectangle bounds) {
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
