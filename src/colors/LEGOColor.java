package colors;

import java.awt.*;
import java.io.*;
import mosaic.controllers.ColorController;

public class LEGOColor implements Comparable<LEGOColor>, Serializable {
	private static final long serialVersionUID = 6713668277298989578L;
	private int idRebrickable, idLEGO;
	private String name;
	private Color rgb;
	private int[] lab;

	private int parts, sets, from, to;
	private String namesLEGO, idsLDraw, idsBrickLink, namesPeeron;
	
	public LEGOColor(int rgb) {
		idRebrickable = rgb;
		name = "Pure RGB #" + rgb;
		sets = Integer.MAX_VALUE;
		parts = Integer.MAX_VALUE;
		from = 0;
		to = Integer.MAX_VALUE;
		idLEGO = -1;
		setRGB(new Color(rgb));
	}
	
	private LEGOColor() {}
		
	public static LEGOColor parse(String s) {
		if(s == null)
			return null;
		String[] parts = s.split("[|]", -1);
		if(parts.length != 12) {
			System.err.println("Expected color line to contain 11 parts. Contained " + parts.length + ": " + s);
			return null;
		}
		for(int i = 0; i < 12; ++i) 
			parts[i] = parts[i].trim();
		
		LEGOColor c = new LEGOColor();
		c.idRebrickable = parseInt(parts[0]);
		c.name = parts[1];
		if(c.idRebrickable == BLACK.idRebrickable)
			c.setRGB(BLACK.rgb);
		else if(c.idRebrickable == WHITE.idRebrickable)			
			c.setRGB(WHITE.rgb);
		else
			c.setRGB(new Color(parseInt(parts[2])));
		c.parts = parseInt(parts[3]);
		c.sets = parseInt(parts[4]);
		c.from = parseInt(parts[5]);
		c.to = parseInt(parts[6]);
		c.namesLEGO = parts[7];
		c.idsLDraw = parts[8];
		c.idsBrickLink = parts[9];
		c.namesPeeron = parts[10];
		c.idLEGO = parseInt(parts[11]);
		return c;
	}
	
	public String toDelimitedString() {
		return idRebrickable + 
				"|" + name + 
				"|#" + Integer.toHexString(rgb.getRGB() & 0xFFFFFF) + 
				"|" + parts + 
				"|" + sets + 
				"|" + from + 
				"|" + to + 
				"|" + namesLEGO + 
				"|" + idsLDraw + 
				"|" + idsBrickLink + 
				"|" + namesPeeron + 
				"|" + idLEGO;
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
		return idRebrickable;
	}
	public int getIDLEGO() {
		return idLEGO;
	}
	public String getName() {
		return name;
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
	public String getNamesLEGO() {
		return namesLEGO;
	}
	public String getIDsLDraw() {
		return idsLDraw;
	}
	public int getFirstIDLDraw() {
		if(idsLDraw == null || idsLDraw.length() == 0)
			return -1;
		int commaIndex = idsLDraw.indexOf(',');
		if(commaIndex >= 0)
			return Integer.parseInt(idsLDraw.substring(0, commaIndex));
		return Integer.parseInt(idsLDraw);
	}
	public String getIDsBL() {
		return idsBrickLink;
	}
	public int getFirstIDBL() {
		if(idsBrickLink == null || idsBrickLink.length() == 0)
			return -1;
		int commaIndex = idsBrickLink.indexOf(',');
		if(commaIndex >= 0)
			return Integer.parseInt(idsBrickLink.substring(0, commaIndex));
		return Integer.parseInt(idsBrickLink);
	}
	public String getNamesPeeron() {
		return namesPeeron;
	}
	
	public boolean isLDD() {
		return idLEGO != -1;
	}
	public boolean isTransparent() {
		return name.toLowerCase().contains("trans");
	}
	public boolean isMetallic() {
		String s = name.toLowerCase();
		return s.contains("copper") || s.contains("silver") || s.contains("gold") || s.contains("chrome") || s.contains("metallic");
	}
	
	public void setRGB(Color color) {
		this.rgb = color;
		updateLAB();
	}
	public void setIdLEGO(int idLEGO) {
		this.idLEGO = idLEGO;
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
	
	public static final LEGOColor BLACK = new LEGOColor(0x000000);
	public static final LEGOColor WHITE = new LEGOColor(0xFFFFFF);
	static {
		BLACK.name = "Black";
		BLACK.rgb = Color.BLACK;
		BLACK.idRebrickable = 0;

		WHITE.name = "White";
		WHITE.rgb = Color.WHITE;
		WHITE.idRebrickable = 15;
	}
	public static final LEGOColor[] BW = {BLACK, WHITE};

	@Override
	public int hashCode() {
		return idRebrickable;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof LEGOColor && ((LEGOColor)other).idRebrickable == idRebrickable;
	}
	
	@Override
	public int compareTo(LEGOColor other) {
		return idRebrickable < other.idRebrickable ? -1 : (idRebrickable == other.idRebrickable ? 0 : 1);
	}
	
	public static class CountingLEGOColor {
		public LEGOColor c;
		public int cnt;
	}
}
