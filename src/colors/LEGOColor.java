package colors;

import java.awt.*;
import java.io.*;

public class LEGOColor implements Comparable<LEGOColor>, Serializable {
	private static final long serialVersionUID = 6713668277298989578L;
	public String name_Peeron;
	public int parts;
	public String name_BL;
	public int id_BL;
	public int id_LDraw;
	public Color rgb_LDraw;
	public int id_LEGO;
	public String name_LEGO;
	public Color rgb;
	public String comment;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name_Peeron);
		sb.append(", ");
		sb.append(parts);
		sb.append(", ");
		sb.append(name_BL);
		sb.append(", ");
		sb.append(id_BL);
		sb.append(", ");
		sb.append(id_LDraw);
		sb.append(", ");
		sb.append(rgb_LDraw);
		sb.append(", ");
		sb.append(id_LEGO);
		sb.append(", ");
		sb.append(name_LEGO);
		sb.append(", ");
		sb.append(rgb);
		sb.append(", ");
		sb.append(comment);
		return sb.toString();
	}
	
	public String getToolTipText() {
		StringBuilder toolTip = new StringBuilder();
		// name:
		toolTip.append(name_Peeron);
		// additional names:
		boolean appendNameBL = name_BL != null && !name_BL.equals(name_Peeron);
		boolean appendNameLEGO = name_LEGO != null && !name_Peeron.equals(name_LEGO) && 
								 !(appendNameBL && name_BL.equals(name_LEGO));
		if(appendNameBL) {
			if(appendNameLEGO) {
				toolTip.append(" (" + name_BL + ", " + name_LEGO + ")");				
			}
			else {
				toolTip.append(" (" + name_BL + ")");				
			}
		}
		else if(appendNameLEGO) {
			toolTip.append(" (" + name_LEGO + ")");			
		}
		// color code:
		toolTip.append(" #" + Integer.toHexString(rgb.getRGB()));
		// comment:
		if(comment != null) {
			toolTip.append(" " + comment);
		}
		
		return toolTip.toString();
	}
	
	public String getShortIdentifier() {
		return "" + id_LEGO;
	}
	
	public String getName() {
		if(name_LEGO != null)
			return name_LEGO;
		if(name_BL != null)
			return name_BL;
		if(name_Peeron != null)
			return name_Peeron;
		if(comment != null)
			return comment;
		return toString();
	}
	
	public static Font makeFont(Graphics2D g2, int limitWidth, int limitHeight, LEGOColor... colors) {
		if(limitHeight <= 1 || limitWidth <= 1) {
			return new Font("Monospaced", Font.PLAIN, 1);
		}
		
		int fontSize = limitHeight - 1;
		Font font = new Font("Monospaced", Font.PLAIN, fontSize);

		String maxString = "";
		int maxLength = 0;
		for(LEGOColor color : colors) {
			String id = color.getShortIdentifier();
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
			maxWidth = g2.getFontMetrics(font).stringWidth(maxString);
			height = g2.getFontMetrics(font).getHeight();
		}
		while(maxWidth > limitWidth || height > limitHeight);
		return font;
	}
	
	public static final LEGOColor BLACK = new LEGOColor();
	public static final LEGOColor WHITE = new LEGOColor();
	static {
		BLACK.name_Peeron = "Completely Black";
		BLACK.rgb = Color.BLACK;
		BLACK.comment = "This tone is darker than the official LEGO color for black.";
		BLACK.id_LDraw = 0;
		BLACK.id_LEGO = 26;
		WHITE.name_Peeron = "Completely White";
		WHITE.rgb = Color.WHITE;
		WHITE.id_LDraw = 15;
		WHITE.id_LEGO = 1;
		WHITE.comment = "This tone is lighter than the official LEGO color for white.";
	}
	public static final LEGOColor[] BW = {BLACK, WHITE};

	@Override
	public int hashCode() {
		return name_Peeron.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof LEGOColor && ((LEGOColor)other).name_Peeron.equals(name_Peeron);
	}
	
	public int compareTo(LEGOColor other) {
		return name_Peeron.compareTo(other.name_Peeron);
	}
}
