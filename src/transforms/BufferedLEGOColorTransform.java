package transforms;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import mosaic.controllers.ColorController;
import colors.*;

public abstract class BufferedLEGOColorTransform implements LEGOColorTransform, InstructionsTransform {
	private TransformationSet[] sets;
	private int pairIndex, lastIndex;
	private ColorController cc;

	public BufferedLEGOColorTransform(int bufferSize, ColorController cc) {
		this.cc = cc;
		sets = new TransformationSet[bufferSize];
		lastIndex = -1;
		pairIndex = 0;
	}

	public BufferedLEGOColorTransform(ColorController cc) {
		this(1, cc);
	}

	public void clearBuffer() {
		int size = sets.length;
		sets = new TransformationSet[size];
	}

	@Override
	public LEGOColorGrid lcTransform(BufferedImage in) {
		return transformSet(in).colors;
	}

	@Override
	public BufferedImage transform(BufferedImage in) {
		transformSet(in);
		return null;
		//return transformSet(in).out;
	}

	private TransformationSet transformSet(BufferedImage in) {
		for (int i = 0; i < sets.length; i++) {
			if (sets[i] != null && sets[i].in == in) {
				lastIndex = i;
				return sets[i];
			}
		}

		TransformationSet s = new TransformationSet();
		s.in = in;
		s.colors = lcTransformUnbuffered(in);
		//s.out = toBufferedImage(s.colors);

		if (sets.length == 0)
			return s;

		sets[pairIndex] = s;
		lastIndex = pairIndex;

		pairIndex++;
		pairIndex %= sets.length;
		return s;
	}

	private static class TransformationSet {
		public BufferedImage in;//, out;
		public LEGOColorGrid colors;
	}

	/*private static BufferedImage toBufferedImage(LEGOColorGrid lcs) {
		int h = lcs.getHeight();
		int w = lcs.getWidth();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		int[] pixels = new int[w * h];
		for (int y = 0, i = 0; y < h; y++) {
			LEGOColor row[] = lcs.getRow(y);
			for (int x = 0; x < w; x++, i++) {
				LEGOColor c = row[x];
				int pixel = c.getRGB().getRGB();
				pixels[i] = pixel;
			}
		}
		out.setRGB(0, 0, w, h, pixels, 0, w);
		return out;
	}*/

	@Override
	public LEGOColor.CountingLEGOColor[] drawLastInstructions(Graphics2D g2,
			Rectangle unitBounds, int blockWidth, int blockHeight,
			Dimension toSize) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, toSize.width, toSize.height);

		int w = unitBounds.width / blockWidth;
		int h = unitBounds.height / blockHeight;
		double scaleW = toSize.width / (double) w;
		double scaleH = toSize.height / (double) h;
		int cellW = (int) Math.round(scaleW);
		int cellH = (int) Math.round(scaleH);

		if(lastIndex == -1 || sets[lastIndex] == null)
			return new LEGOColor.CountingLEGOColor[]{};
		
		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		Font font = LEGOColor.makeFont(g2, cellW - 4, cellH - 4, cc,
				lastUsedColorCounts());
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);
		int fontHeight = (fm.getDescent() + fm.getAscent()) / 2;

		LEGOColor.CountingLEGOColor[] m = new LEGOColor.CountingLEGOColor[LEGOColor.getMaxRebrickableId()+1];
		int cnt = 0;

		g2.setColor(Color.BLACK);
		for (int y = 0; y < h; y++) {
			int yIndent = (int) Math.round(scaleH * y);
			int iy = unitBounds.y / blockHeight + y;
			if (iy >= transformedColors.getHeight())
				continue;
			LEGOColor[] row = transformedColors.getRow(iy);

			for (int x = 0; x < w; x++) {
				int xIndent = (int) Math.round(scaleW * x);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);
				g2.draw(r);

				int ix = unitBounds.x / blockWidth + x;
				if (ix < transformedColors.getWidth()) {
					LEGOColor color = row[ix];
					int idx = color.getIDRebrickable();
					if(m[idx] == null) {
						m[idx] = new LEGOColor.CountingLEGOColor(color, 1);
						++cnt;
					}
					else
						m[idx].cnt++;

					String id = cc.getShortIdentifier(color);
					int originX = (int) (r.getCenterX() - g2.getFontMetrics(
							font).stringWidth(id) / 2);
					int originY = (int) (r.getCenterY() + fontHeight / 2);
					g2.drawString(id, originX, originY);
				}
			}
		}
		return trim(m, cnt);
	}

	@Override
	public LEGOColor.CountingLEGOColor[] drawLastColors(Graphics2D g2, Rectangle unitBounds,
			int blockWidth, int blockHeight, Dimension toSize,
			int numStudsWide, int numStudsTall, boolean drawOutlines) {
		// Find scaling parameters:
		int w = unitBounds.width / blockWidth;
		int h = unitBounds.height / blockHeight;
		double scaleW = toSize.width / (double)w;
		double scaleH = toSize.height / (double)h;
		int cellW = (int)Math.ceil(scaleW);
		int cellH = (int)Math.ceil(scaleH);

		// draw colors and studs:
		if(lastIndex == -1 || sets[lastIndex] == null)
			return new LEGOColor.CountingLEGOColor[]{};
		LEGOColor.CountingLEGOColor[] m = new LEGOColor.CountingLEGOColor[LEGOColor.getMaxRebrickableId()+1];
		int cnt = 0;

		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		for (int y = 0; y < h; y++) {
			int yIndent = (int)Math.round(scaleH * y);
			int iy = unitBounds.y / blockHeight + y;
			if (iy >= transformedColors.getHeight())
				break;
			LEGOColor[] row = transformedColors.getRow(iy);

			for (int x = 0; x < w; x++) {
				int xIndent = (int)Math.round(scaleW * x);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x / blockWidth + x;
				if (ix >= transformedColors.getWidth())
					break;
					
				LEGOColor color = row[ix];
				int idx = color.getIDRebrickable();
				if(m[idx] == null) {
					m[idx] = new LEGOColor.CountingLEGOColor(color, 1);
					++cnt;
				}
				else {
					m[idx].cnt++;
				}
				g2.setColor(color.getRGB());
				g2.fill(r);

				if (numStudsWide > 0 && drawOutlines) {
					g2.setColor(color.getRGB().equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
					// Draw studs:
					final int cell = (int)Math.round(scaleW / numStudsWide);
					final int stud = (int)Math.round(scaleW * 2 / 3 / numStudsWide);
					final int gap = (int)Math.round(scaleW / 6 / numStudsWide);
					for (int xx = 0; xx < numStudsWide; ++xx) {
						for (int yy = 0; yy < numStudsTall; ++yy) {
							g2.drawOval(xIndent + cell * xx + gap, yIndent+ cell * yy + gap, stud, stud);
						}
					}
				}
			}
		}
		
		if(!drawOutlines)
			return trim(m, cnt);

		// Outlines:
		for (int y = 0; y < h; y++) {
			double yIndent = scaleH * y;
			int iy = unitBounds.y / blockHeight + y;
			if (iy >= transformedColors.getHeight())
				break;
			LEGOColor[] row = transformedColors.getRow(iy);

			for (int x = 0; x < w; x++) {
				double xIndent = scaleW * x;
				int ix = unitBounds.x / blockWidth + x;
				if (ix >= transformedColors.getWidth())
					break;
					
				LEGOColor color = row[ix];
				g2.setColor(color.getRGB().equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
				Rectangle2D.Double r = new Rectangle2D.Double(xIndent, yIndent, scaleW, scaleH);
				g2.draw(r);
			}
		}
		return trim(m, cnt);
	}
	
	@Override
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts() {
		if (lastIndex == -1 || sets[lastIndex] == null)
			return new LEGOColor.CountingLEGOColor[] {};
		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		if (transformedColors == null)
			return new LEGOColor.CountingLEGOColor[] {};

		LEGOColor.CountingLEGOColor[] m = new LEGOColor.CountingLEGOColor[LEGOColor.getMaxRebrickableId()+1];
		int entries = 0;
		for (int y = 0; y < transformedColors.getHeight(); y++) {
			LEGOColor[] row = transformedColors.getRow(y);
			for (int x = 0; x < row.length; x++) {
				LEGOColor c = row[x];
				LEGOColor.CountingLEGOColor mc = m[c.getIDRebrickable()];
				if (m[c.getIDRebrickable()] != null) {
					mc.cnt++;
				} else {
					m[c.getIDRebrickable()] = new LEGOColor.CountingLEGOColor(c, 1);
					++entries;
				}
			}
		}
		return trim(m, entries);
	}

	private static LEGOColor.CountingLEGOColor[] trim(LEGOColor.CountingLEGOColor[] m, int size) {
		LEGOColor.CountingLEGOColor[] out = new LEGOColor.CountingLEGOColor[size];
		for (int i = 0, idx = 0; i <= LEGOColor.getMaxRebrickableId(); i++) {
			if(m[i] != null)
				out[idx++] = m[i];
		}
		return out;		
	}
	
	public LEGOColorGrid lastInstructions() {
		return sets[lastIndex].colors;
	}

	public abstract LEGOColorGrid lcTransformUnbuffered(BufferedImage in);
}
