package colors;

import java.awt.*;
import java.io.*;
import java.util.Calendar;

import mosaic.controllers.ColorController;

public class LEGOColor implements Comparable<LEGOColor>, Serializable {
	private Color rgb;
	private int[] lab;

	private int parts, sets, from, to;
	private double intensity;
	private ColorIdNamePair rebrickable;
	private ColorIdNamePair[] lego, ldraw, bricklink, brickowl;
	
	private static int maxRebrickableId = 0;
	
	public LEGOColor(Color color, int idx, String name) {
		rebrickable = new ColorIdNamePair(idx, name);
		sets = Integer.MAX_VALUE;
		parts = Integer.MAX_VALUE;
		from = 0;
		to = Integer.MAX_VALUE;
		intensity = 1;
		setRGB(color);

		lego = new ColorIdNamePair[]{new ColorIdNamePair(idx, name)};
		ldraw = new ColorIdNamePair[]{new ColorIdNamePair(idx, name)};
		bricklink = new ColorIdNamePair[]{new ColorIdNamePair(idx, name)};
		brickowl = new ColorIdNamePair[]{new ColorIdNamePair(idx, name)};
		if(maxRebrickableId < rebrickable.getID())
			maxRebrickableId = rebrickable.getID();
	}
	
	public LEGOColor() {
		parts = sets = from = to = -1; // Rest null.
		intensity = 1;
	}
	
	public static int getMaxRebrickableId() {
		return maxRebrickableId;
	}
		
	public static LEGOColor parse(String s) {
		if(s == null)
			return null;
		String[] parts = s.split("[|]", -1);
		if(parts.length != 11)
			throw new IllegalArgumentException("Expected color line to contain 11 sections. Contained " + parts.length + ": " + s);
		for(int i = 0; i < 11; ++i) 
			parts[i] = parts[i].trim();
		
		LEGOColor c = new LEGOColor();
		
		// Rebrickable:
		int idR = parseInt(parts[0]);
		if(maxRebrickableId < idR)
			maxRebrickableId = idR;
		c.rebrickable = new ColorIdNamePair(idR, parts[1]);

		// RGB:
		c.setRGB(new Color(parseInt(parts[2])));

		// various numbers:
		c.parts = parseInt(parts[3]);
		c.sets = parseInt(parts[4]);
		c.from = parseInt(parts[5]);
		c.to = parseInt(parts[6]);
		
		// ID's and names for various sites:
		c.lego = ColorIdNamePair.parse(parts[7]);
		c.ldraw = ColorIdNamePair.parse(parts[8]);
		c.bricklink = ColorIdNamePair.parse(parts[9]);
		c.brickowl = ColorIdNamePair.parse(parts[10]);
		return c;
	}
	
	@Override
	public String toString() {
		return rebrickable.getID() + 
				"|" + rebrickable.getName() + 
				"|#" + Integer.toHexString(rgb.getRGB() & 0xFFFFFF) + 
				"|" + parts + 
				"|" + sets + 
				"|" + from + 
				"|" + to + 
				"|" + ColorIdNamePair.toString(lego) + 
				"|" + ColorIdNamePair.toString(ldraw) + 
				"|" + ColorIdNamePair.toString(bricklink) + 
				"|" + ColorIdNamePair.toString(brickowl);
	}
	
	private static int parseInt(String s) {
		s = s.trim();
		if(s.length() == 0)
			return 0;
		if(s.charAt(0) == '#') {
			return Integer.parseInt(s.substring(1), 16);
		}
		return Integer.parseInt(s);
	}
	
	public int getIDRebrickable() {
		return rebrickable.getID();
	}
	public int getIDLEGO() {
		return lego[0].getID();
	}
	public String getName() {
		return rebrickable.getName();
	}
	public Color getRGB() {
		return rgb;
	}
	public int[] getLAB() {
		return lab;
	}
	public int getParts() {
		return parts;
	}
	public int getSets() {
		return sets;
	}
	public int getFrom() {
		return from;
	}
	public int getTo() {
		return to;
	}
	public ColorIdNamePair[] getLEGO() {
		return lego;
	}
	public ColorIdNamePair[] getLDraw() {
		return ldraw;
	}
	public ColorIdNamePair[] getBrickLink() {
		return bricklink;
	}
	public ColorIdNamePair[] getBrickOwl() {
		return brickowl;
	}
	public double getIntensity() {
		return intensity;
	}
	
	public boolean isLDD() {
		return lego.length != 0;
	}
	public boolean isLDraw() {
		return ldraw.length != 0;
	}
	public boolean isBrickLink() {
		return bricklink.length != 0;
	}
	public boolean isBrickOwl() {
		return brickowl.length != 0;
	}
	public boolean isTransparent() {
		return getName().toLowerCase().contains("trans");
	}
	public boolean isMetallic() {
		String s = getName().toLowerCase();
		return s.contains("copper") || s.contains("silver") || s.contains("gold") || s.contains("chrome") || s.contains("metallic");
	}
	
	public void setRGB(Color color) {
		this.rgb = color;
		updateLAB();
	}

	public void setIntensity(double i) {
		intensity = i;
	}
	
	private void updateLAB() {
		int rgb = this.rgb.getRGB();
		int r = LEGOColorLookUp.getRed(rgb);
		int g = LEGOColorLookUp.getGreen(rgb);
		int b = LEGOColorLookUp.getBlue(rgb);
		lab = new int[3];
		CIELab.rgb2lab(r,  g, b, lab);		
	}
	
	public static Font makeFont(Graphics2D g2, int limitWidth, int limitHeight, ColorController cc, LEGOColor.CountingLEGOColor... colors) {
		if(limitHeight <= 1 || limitWidth <= 1) {
			return new Font("Monospaced", Font.PLAIN, 1);
		}
		
		int fontSize = limitHeight - 1;
		Font font = new Font("Monospaced", Font.PLAIN, fontSize);

		String maxString = "";
		int maxLength = 0;
		for(LEGOColor.CountingLEGOColor color : colors) {
			String id = cc.getShortIdentifier(color.c);
			int length = g2.getFontMetrics(font).stringWidth(id);

			if(maxLength < length) {
				maxString = id;
				maxLength = length;
			}
		}
		
		int maxWidth, height;
		do {
			fontSize--;
			font = new Font("Monospaced", Font.PLAIN, fontSize);
			FontMetrics fm = g2.getFontMetrics(font);
			maxWidth = fm.stringWidth(maxString);
			height = (fm.getDescent()+fm.getAscent())/2;
		}
		while(maxWidth > limitWidth || height > limitHeight);
		return font;
	}
	
	/* 
	 * For loading from 2020 version of Rebrickable colors file:
	 */
	public void loadRebrickableData(String s) {
		if(rebrickable == null) {
			rebrickable = new ColorIdNamePair(Integer.parseInt(s), null);
		}
		else if(rebrickable.getName() == null) {
			rebrickable = new ColorIdNamePair(rebrickable.getID(), s);
		}
		else if(rgb == null) {
			if(rebrickable.getID() == 0)
				setRGB(new Color(parseInt("#000000")));
			else if(rebrickable.getID() == 0)
				setRGB(new Color(parseInt("#FFFFFF")));
			else
				setRGB(new Color(parseInt("#" + s)));
		}
		else if(parts == -1) {
			parts = Integer.parseInt(s);
		}
		else if(sets == -1) {
			sets = Integer.parseInt(s);
		}
		else if(from == -1) {
			if(s.isEmpty()) {
				from = 1949;
			}
			else {
				from = Integer.parseInt(s);				
			}
		}
		else if(to == -1) {
			if(s.isEmpty()) {
				to = Calendar.getInstance().get(Calendar.YEAR);
			}
			else {
				to = Integer.parseInt(s);				
			}
		}
	}
	public void loadRebrickableData(ColorIdNamePair[] pairs) {
		if(to == -1)
			return; // Not ready!
		
		if(lego == null)
			lego = pairs;
		else if(ldraw == null)
			ldraw = pairs;
		else if(bricklink == null)
			bricklink = pairs;
		else
			brickowl = pairs;
	}
	
	public static final LEGOColor BLACK = new LEGOColor(Color.BLACK, 0, "Black");
	public static final LEGOColor WHITE = new LEGOColor(Color.WHITE, 15, "White");
	public static final LEGOColor[] BW = {BLACK, WHITE};

	@Override
	public int hashCode() {
		return rebrickable.getID();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof LEGOColor && ((LEGOColor)other).rebrickable.getID() == rebrickable.getID();
	}
	
	@Override
	public int compareTo(LEGOColor other) {
		return rebrickable.getID() < other.rebrickable.getID() ? -1 : (rebrickable.getID() == other.rebrickable.getID() ? 0 : 1);
	}
	
	public static class CountingLEGOColor implements Comparable<CountingLEGOColor> {
		public LEGOColor c;
		public int cnt;
		
		public CountingLEGOColor(LEGOColor c, int cnt) {
			this.c = c;
			this.cnt = cnt;
		}
		
		@Override
		public int compareTo(CountingLEGOColor other) {
			return c.compareTo(other.c);
		}
		
		@Override
		public String toString() {
			return c.toString() + ", cnt: " + cnt;
		}
	}
}
