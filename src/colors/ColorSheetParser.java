package colors;

import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.regex.*;

public class ColorSheetParser {
	public static final String COLORS_FILE = "colors.htm";
	private static Pattern tag = Pattern.compile("<[^>]+>");
	
	public static Map<String, LEGOColor> parse() throws FileNotFoundException, IOException {
		Scanner scanner = new Scanner(new File(COLORS_FILE));

		Map<String, LEGOColor> out = new TreeMap<String, LEGOColor>();

		while(scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if(line.equals("<tr bgcolor=\"#dddddd\">") || 
			   line.equals("<tr bgcolor=\"#eeeeee\">")) {
				// handle next line as if it contains info for a new color:
				if(!scanner.hasNextLine())
					break;
				String s = scanner.nextLine();
				LEGOColor newColor = parseColorLine(s);
				if(newColor != null) {
					out.put(newColor.name_Peeron, newColor);					
				}
				else {
					// should throw an exception...
					System.err.println("Failed to parse line: " + s);
				}
			}
		}
		scanner.close();
		return out;
	}

	private static LEGOColor parseColorLine(String line) {
		try {
			Scanner scanner = new Scanner(line);
			scanner.useDelimiter(tag);

			LEGOColor c = new LEGOColor();
			String s;
			
			// <td><a href="httpart">Black
			if(!scanner.hasNext()) return null; scanner.next(); // a
			if(!scanner.hasNext()) return null; 
			c.name_Peeron = scanner.next(); // name Peeron
			
			// </a></td><td>192751 =parts
			if(!scanner.hasNext()) return null; scanner.next(); // /td
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNextInt()) return null; 
			c.parts = scanner.nextInt(); // parts

			// </td><td>Black =name BL
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; 
			c.name_BL = scanner.next(); // name BL
			
			// </td><td>11  =id BL
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; s = scanner.next(); // id BL
			if(s.length() != 0)
				c.id_BL = Integer.parseInt(s);

			// </td><td>0 =id LDraw
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; s = scanner.next(); // id LDraw
			if(s.length() != 0)
				c.id_LDraw = Integer.parseInt(s);

			// </td><td><div id="ldBlack">212121 =rgb LDraw
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; scanner.next(); // div
			if(!scanner.hasNext()) return null; s = scanner.next(); // rgb LDraw
			if(s.length() != 0) {
				c.rgb_LDraw = new Color(Integer.parseInt(s, 16));
				
				// </div></td><td>26 =id LEGO
				if(!scanner.hasNext()) return null; scanner.next(); // /td
				if(!scanner.hasNext()) return null; scanner.next(); // td
			} // else no divs! => no scan x 2
			if(!scanner.hasNext()) return null; s = scanner.next(); // id LEGO
			if(s.length() != 0)
				c.id_LEGO = Integer.parseInt(s);
			
			// </td><td>Black =name LEGO
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; 
			c.name_LEGO = scanner.next(); // name LEGO
			
			// </td><td><div id="Black">1B2A34 =rgb
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; scanner.next(); // div
			if(!scanner.hasNext()) return null; s = scanner.next(); // rgb
			if(s.length() != 0) {
				c.rgb = new Color(Integer.parseInt(s, 16));
			
				// </div></td><td>60,0,0,100 =CMYK
				if(!scanner.hasNext()) return null; scanner.next(); // /td
				if(!scanner.hasNext()) return null; scanner.next(); // td
			} // else no divs! => no scan x 2
			if(!scanner.hasNext()) return null; s = scanner.next(); // CMYK
			// Don't process CMYK for now.
			
			// </td><td>Process Black C =Pantome
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; s = scanner.next(); // Pantome
			// Don't process pantome for now.

			// </td><td>COMMENT =comment
			if(!scanner.hasNext()) return null; scanner.next(); // td
			if(!scanner.hasNext()) return null; s = scanner.next(); // Comment
			c.comment = s;
			
			return c;
		}
		catch(NumberFormatException e) {
			return null;
		}
	}
}
