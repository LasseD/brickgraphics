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

public abstract class BufferedLEGOColorTransform implements LEGOColorTransform,
		InstructionsTransform {
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
		return transformSet(in).out;
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
		s.out = toBufferedImage(s.colors);

		if (sets.length == 0)
			return s;

		sets[pairIndex] = s;
		lastIndex = pairIndex;

		pairIndex++;
		pairIndex %= sets.length;
		return s;
	}

	private static class TransformationSet {
		public BufferedImage in, out;
		public LEGOColorGrid colors;
	}

	private static BufferedImage toBufferedImage(LEGOColorGrid lcs) {
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
	}

	@Override
	public Set<LEGOColor> drawLastInstructions(Graphics2D g2,
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

		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		Font font = LEGOColor.makeFont(g2, cellW - 4, cellH - 4, cc,
				lastUsedColorCounts());
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics(font);
		int fontHeight = (fm.getDescent() + fm.getAscent()) / 2;

		g2.setColor(Color.BLACK);
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
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
					used.add(color);

					String id = cc.getShortIdentifier(color);
					int originX = (int) (r.getCenterX() - g2.getFontMetrics(
							font).stringWidth(id) / 2);
					int originY = (int) (r.getCenterY() + fontHeight / 2);
					g2.drawString(id, originX, originY);
				}
			}
		}
		return used;
	}

	@Override
	public Set<LEGOColor> drawLastColors(Graphics2D g2, Rectangle unitBounds,
			int blockWidth, int blockHeight, Dimension toSize,
			int numStudsWide, int numStudsTall) {
		// Find scaling parameters:
		int w = unitBounds.width / blockWidth;
		int h = unitBounds.height / blockHeight;
		double scaleW = toSize.width / (double) w;
		double scaleH = toSize.height / (double) h;
		int cellW = (int) Math.ceil(scaleW);
		int cellH = (int) Math.ceil(scaleH);

		// draw colors and studs:
		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		for (int y = 0; y < h; y++) {
			int yIndent = (int) Math.round(scaleH * y);
			int iy = unitBounds.y / blockHeight + y;
			if (iy >= transformedColors.getHeight())
				continue;
			LEGOColor[] row = transformedColors.getRow(iy);

			for (int x = 0; x < w; x++) {
				int xIndent = (int) Math.round(scaleW * x);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x / blockWidth + x;
				if (ix < transformedColors.getWidth()) {
					LEGOColor color = row[ix];
					used.add(color);
					g2.setColor(color.getRGB());
					g2.fill(r);

					if (numStudsWide > 0) {
						g2.setColor(color.getRGB() == Color.BLACK ? Color.WHITE
								: Color.BLACK);
						// Draw studs:s
						final int cell = cellW / numStudsWide;
						final int stud = cellW * 2 / 3 / numStudsWide;
						final int gap = cellW / 6 / numStudsWide;
						for (int xx = 0; xx < numStudsWide; ++xx) {
							for (int yy = 0; yy < numStudsTall; ++yy) {
								g2.drawOval(xIndent + cell * xx + gap, yIndent
										+ cell * yy + gap, stud, stud);
							}
						}
					}
				}
			}
		}

		// Draw lines:
		// Draw upper and left lines as the others will be drawn brick by brick:
		g2.setColor(Color.BLACK);
		for (int x = 0; x <= w; x++) {
			int xIndent = (int) Math.round(scaleW * x);
			g2.drawLine(xIndent, 0, xIndent, toSize.height);
		}
		for (int y = 0; y <= h; y++) {
			int yIndent = (int) Math.round(scaleH * y);
			g2.drawLine(0, yIndent, toSize.width, yIndent);
		}

		// Draw lines around black bricks:
		g2.setColor(Color.WHITE);
		for (int y = 0; y < h; y++) {
			int yIndent = (int) Math.round(scaleH * y);
			int iy = unitBounds.y / blockHeight + y;
			if (iy >= transformedColors.getHeight())
				continue;
			LEGOColor[] row = transformedColors.getRow(iy);

			for (int x = 0; x < w; x++) {
				int xIndent = (int) Math.round(scaleW * x);
				Rectangle r = new Rectangle(xIndent, yIndent, cellW, cellH);

				int ix = unitBounds.x / blockWidth + x;
				if (ix < transformedColors.getWidth()) {
					LEGOColor color = row[ix];
					if (color.getRGB() == Color.BLACK)
						g2.draw(r);
				}
			}
		}

		return used;
	}

	@Override
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts() {
		if (lastIndex == -1)
			return new LEGOColor.CountingLEGOColor[] {};
		LEGOColorGrid transformedColors = sets[lastIndex].colors;
		if (transformedColors == null)
			return new LEGOColor.CountingLEGOColor[] {};

		Map<LEGOColor, Integer> m = new TreeMap<LEGOColor, Integer>();
		for (int y = 0; y < transformedColors.getHeight(); y++) {
			LEGOColor[] row = transformedColors.getRow(y);
			for (int x = 0; x < row.length; x++) {
				LEGOColor c = row[x];
				if (m.containsKey(c)) {
					m.put(c, m.get(c) + 1);
				} else {
					m.put(c, 1);
				}
			}
		}
		List<Map.Entry<LEGOColor, Integer>> l = new ArrayList<Map.Entry<LEGOColor, Integer>>(
				m.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<LEGOColor, Integer>>() {
			@Override
			public int compare(Entry<LEGOColor, Integer> o1,
					Entry<LEGOColor, Integer> o2) {
				return o1.getKey().getIDRebrickable()
						- o2.getKey().getIDRebrickable();
			}
		});
		LEGOColor.CountingLEGOColor[] out = new LEGOColor.CountingLEGOColor[l
				.size()];
		for (int i = 0; i < l.size(); i++) {
			out[i] = new LEGOColor.CountingLEGOColor();
			out[i].c = l.get(i).getKey();
			out[i].cnt = l.get(i).getValue();
		}
		return out;
	}

	public LEGOColorGrid lastInstructions() {
		return sets[lastIndex].colors;
	}

	public abstract LEGOColorGrid lcTransformUnbuffered(BufferedImage in);
}
