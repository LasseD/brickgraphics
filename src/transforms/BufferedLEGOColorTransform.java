package transforms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.*;
import java.util.*;
import java.util.Map.Entry;

import mosaic.controllers.ColorController;

import colors.*;

public abstract class BufferedLEGOColorTransform implements LEGOColorTransform, InstructionsTransform {
	private BufferedImage[] ins;
	private LEGOColor[][][] outColors;
	private BufferedImage[] outImages;
	private int pairIndex, lastIndex;
	private ColorController cc;
	
	public BufferedLEGOColorTransform(int bufferSize, ColorController cc) {
		this.cc = cc;
		ins = new BufferedImage[bufferSize];
		outImages = new BufferedImage[bufferSize];
		outColors = new LEGOColor[bufferSize][][];
	}

	public BufferedLEGOColorTransform(ColorController cc) {
		this(1, cc);
	}
	
	public void clearBuffer() {
		int size = ins.length;
		ins = new BufferedImage[size];
		outImages = new BufferedImage[size];
		outColors = new LEGOColor[size][][];
	}
	
	@Override
	public LEGOColor[][] lcTransform(BufferedImage in) {
		for(int i = 0; i < ins.length; i++)
			if(ins[i] == in) {
				lastIndex = i;
				return outColors[i];
			}
		
		LEGOColor[][] newOut = lcTransformUnbuffered(in);
		if(ins.length == 0)
			return newOut;

		ins[pairIndex] = in;
		outColors[pairIndex] = newOut;
		outImages[pairIndex] = null;
		
		pairIndex++;
		pairIndex %= ins.length;
		return newOut;
	}
	
	@Override
	public BufferedImage transform(BufferedImage in) {
		for(int i = 0; i < ins.length; i++) {
			if(ins[i] == in) {
				if(outImages[i] != null) {
					lastIndex = i;
					return outImages[i];					
				}
				BufferedImage built = build(outColors[i]);
				outImages[i] = built;
				return built;
			}
		}

		if(ins.length == 0)
			return build(lcTransform(in));

		lcTransform(in);
		return transform(in);
	}
	
	private static BufferedImage build(LEGOColor[][] lcs) {
		int w = lcs.length;
		int h = lcs[0].length;
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[w*h];
		for(int y = 0, i = 0; y < h; y++) {
			for(int x = 0; x < w; x++, i++) {
				LEGOColor c = lcs[x][y];
				int pixel = c.getRGB().getRGB();
				pixels[i] = pixel;
			}
		}
		out.setRGB(0, 0, w, h, pixels, 0, w);
		return out;
	}
	
	@Override
	public Set<LEGOColor> drawLastInstructions(Graphics2D g2, 
			Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, toSize.width, toSize.height);

		int w = unitBounds.width/blockWidth;
		int h = unitBounds.height/blockHeight;
		double scaleW = toSize.width / (double)w;
		double scaleH = toSize.height / (double)h;
		int cellW = (int)Math.round(scaleW);
		int cellH = (int)Math.round(scaleH);

		LEGOColor[][] transformedColors = outColors[lastIndex];
		Font font = LEGOColor.makeFont(g2, cellW-4, cellH-4, cc, lastUsedColorCounts());
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);
		int fontHeight = (fm.getDescent()+fm.getAscent())/2;
		
		g2.setColor(Color.BLACK);
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for(int x = 0; x < w; x++) {
			int xIndent = (int)Math.round(scaleW*x);
			for(int y = 0; y < h; y++) {
				int yIndent = (int)Math.round(scaleH*y);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);
				g2.draw(r);

				int ix = unitBounds.x/blockWidth+x;
				int iy = unitBounds.y/blockHeight+y;
				if(ix < transformedColors.length && iy < transformedColors[ix].length) {
					
					LEGOColor color = transformedColors[ix][iy];
					used.add(color);
					
					String id = cc.getShortIdentifier(color);
					int originX = (int)(r.getCenterX() - g2.getFontMetrics(font).stringWidth(id)/2);
					int originY = (int)(r.getCenterY() + fontHeight/2);
					g2.drawString(id, originX, originY);					
				}
			}
		}
		return used;
	}

	@Override
	public Set<LEGOColor> drawLastColors(Graphics2D g2, 
			Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize, int numStuds) {		
		// Find scaling parameters:
		int w = unitBounds.width/blockWidth;
		int h = unitBounds.height/blockHeight;
		double scaleW = toSize.width / (double)w;
		double scaleH = toSize.height / (double)h;
		int cellW = (int)Math.ceil(scaleW);
		int cellH = (int)Math.ceil(scaleH);
		
		// draw colors and studs:
		LEGOColor[][] transformedColors = outColors[lastIndex];
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for(int x = 0; x < w; x++) {
			int xIndent = (int)Math.round(scaleW*x);
			for(int y = 0; y < h; y++) {
				int yIndent = (int)Math.round(scaleH*y);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x/blockWidth+x;
				int iy = unitBounds.y/blockHeight+y;
				if(ix < transformedColors.length && iy < transformedColors[ix].length) {
					LEGOColor color = transformedColors[ix][iy];
					used.add(color);
					g2.setColor(color.getRGB());
					g2.fill(r);
					
					if(numStuds > 0) {				      
					  g2.setColor(color.getRGB() == Color.BLACK ? Color.WHITE : Color.BLACK);
					  if(numStuds == 1)
						  g2.drawOval(xIndent+cellW/6, yIndent+cellH/6, cellW*2/3, cellH*2/3);
					  else {
						  final int cell = cellW/numStuds;
						  final int stud = cellW*2/3/numStuds;
						  final int gap = cellW/6/numStuds;
						  for(int xx = 0; xx < numStuds; ++xx) {
							  for(int yy = 0; yy < numStuds; ++yy) {
								  	g2.drawOval(xIndent+cell*xx+gap, yIndent+cell*yy+gap, stud, stud);
							  }
						  }
					  }
					}
				}				
			}
		}
		
		// Draw lines:
		// Draw upper and left lines as the others will be drawn brick by brick:
		g2.setColor(Color.BLACK);
		for(int x = 0; x <= w; x++) {
			int xIndent = (int)Math.round(scaleW*x);
			g2.drawLine(xIndent, 0, xIndent, toSize.height);
		}
		for(int y = 0; y <= h; y++) {
			int yIndent = (int)Math.round(scaleH*y);
			g2.drawLine(0, yIndent, toSize.width, yIndent);
		}

		// Draw lines around black bricks:
		g2.setColor(Color.WHITE);
		for(int x = 0; x < w; x++) {
			int xIndent = (int)Math.round(scaleW*x);
			for(int y = 0; y < h; y++) {
				int yIndent = (int)Math.round(scaleH*y);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x/blockWidth+x;
				int iy = unitBounds.y/blockHeight+y;
				if(ix < transformedColors.length && iy < transformedColors[ix].length) {
					LEGOColor color = transformedColors[ix][iy];
					if(color.getRGB() == Color.BLACK)
						g2.draw(r);
				}				
			}
		}

		return used;
	}
	
	@Override
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts() {
		LEGOColor[][] transformedColors = outColors[lastIndex];
		Map<LEGOColor, Integer> m = new TreeMap<LEGOColor, Integer>();
		for(int x = 0; x < transformedColors.length; x++) {
			for(int y = 0; y < transformedColors[0].length; y++) {
				LEGOColor c = transformedColors[x][y];
				if(m.containsKey(c)) {
					m.put(c, m.get(c)+1);					
				}
				else {
					m.put(c, 1);					
				}
			}
		}
		List<Map.Entry<LEGOColor, Integer>> l = new ArrayList<Map.Entry<LEGOColor, Integer>>(m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<LEGOColor, Integer>>() {
			@Override
			public int compare(Entry<LEGOColor, Integer> o1, Entry<LEGOColor, Integer> o2) {
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
	
	public LEGOColor[][] lastInstructions() {
		return outColors[lastIndex];
	}
	
	public abstract LEGOColor[][] lcTransformUnbuffered(BufferedImage in);
}
