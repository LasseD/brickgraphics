package transforms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.*;
import java.util.*;
import java.util.Map.Entry;

import colors.*;

public abstract class BufferedLEGOColorTransform implements LEGOColorTransform, Transform, InstructionsTransform {
	private BufferedImage[] ins;
	private LEGOColor[][][] outColors;
	private BufferedImage[] outImages;
	private int pairIndex, lastIndex;
	
	public BufferedLEGOColorTransform(int bufferSize) {
		ins = new BufferedImage[bufferSize];
		outImages = new BufferedImage[bufferSize];
		outColors = new LEGOColor[bufferSize][][];
	}

	public BufferedLEGOColorTransform() {
		this(1);
	}
	
	public void clearBuffer() {
		int size = ins.length;
		ins = new BufferedImage[size];
		outImages = new BufferedImage[size];
		outColors = new LEGOColor[size][][];
	}
	
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
	
	private BufferedImage build(LEGOColor[][] lcs) {
		int w = lcs.length;
		int h = lcs[0].length;
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] pixels = new int[w*h];
		for(int y = 0, i = 0; y < h; y++) {
			for(int x = 0; x < w; x++, i++) {
				LEGOColor c = lcs[x][y];
				int pixel = c.rgb.getRGB();
				pixels[i] = pixel;
			}
		}
		out.setRGB(0, 0, w, h, pixels, 0, w);
		return out;
	}
	
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
		Font font = LEGOColor.makeFont(g2, cellW-4, cellH-4, lastUsedColors());
		g2.setFont(font);
		int fontHeight = g2.getFontMetrics(font).getHeight();
		
		g2.setColor(Color.BLACK);
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int xIndent = (int)Math.round(scaleW*x);
				int yIndent = (int)Math.round(scaleH*y);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);
				g2.draw(r);

				int ix = unitBounds.x/blockWidth+x;
				int iy = unitBounds.y/blockHeight+y;
				if(ix < transformedColors.length && iy < transformedColors[ix].length) {
					
					LEGOColor color = transformedColors[ix][iy];
					used.add(color);
					
					String id = color.getShortIdentifier();
					int originX = (int)(r.getCenterX() - g2.getFontMetrics(font).stringWidth(id)/2);
					int originY = (int)(r.getCenterY() + fontHeight/2);
					g2.drawString(id, originX, originY);					
				}
			}
		}
		return used;
	}

	public Set<LEGOColor> drawLastColors(Graphics2D g2, 
			Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize) {
		int w = unitBounds.width/blockWidth;
		int h = unitBounds.height/blockHeight;
		double scaleW = toSize.width / (double)w;
		double scaleH = toSize.height / (double)h;
		int cellW = (int)Math.round(scaleW);
		int cellH = (int)Math.round(scaleH);
		
		LEGOColor[][] transformedColors = outColors[lastIndex];
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for(int x = 0; x < w; x++) {
			for(int y = 0; y < h; y++) {
				int xIndent = (int)Math.round(scaleW*x);
				int yIndent = (int)Math.round(scaleH*y);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x/blockWidth+x;
				int iy = unitBounds.y/blockHeight+y;
				if(ix < transformedColors.length && iy < transformedColors[ix].length) {
					LEGOColor color = transformedColors[ix][iy];
					used.add(color);
					g2.setColor(color.rgb);
					g2.fill(r);
				}
				
				g2.setColor(Color.BLACK);
				g2.draw(r);
			}
		}
		return used;
	}
	
	public LEGOColor[] lastUsedColors() {
		LEGOColor[][] transformedColors = outColors[lastIndex];
		Map<LEGOColor, Integer> m = new TreeMap<LEGOColor, Integer>();
		for(int x = 0; x < transformedColors.length; x++) {
			for(int y = 0; y < transformedColors[0].length; y++) {
				LEGOColor c = transformedColors[x][y];
				if(m.containsKey(c)) {
					m.put(c, m.get(c)+1);					
				}
				else {
					m.put(c, 0);					
				}
			}
		}
		List<Map.Entry<LEGOColor, Integer>> l = new ArrayList<Map.Entry<LEGOColor, Integer>>(m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<LEGOColor, Integer>>() {
			public int compare(Entry<LEGOColor, Integer> o1, Entry<LEGOColor, Integer> o2) {
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
	
	public LEGOColor[][] lastInstructions() {
		return outColors[lastIndex];
	}
	
	public abstract LEGOColor[][] lcTransformUnbuffered(BufferedImage in);
}
