package colors.parsers;

import java.util.*;
import java.io.*;
import java.text.ParseException;
import colors.ColorIdNamePair;
import colors.LEGOColor;

/**
 * ID, Name, #rgb, |parts|, |sets|, from, to, LEGO names, LDraw IDs, Bricklink IDs, Peeron names
 * @author LD
 */
public class RebrickableColorSheetParser implements ColorSheetParserI {
	public static void main(String[] args) throws Exception {
		RebrickableColorSheetParser p = new RebrickableColorSheetParser();
		InputStreamReader reader = new InputStreamReader(new FileInputStream(new File("C:\\workspace\\BrickGraphics\\ColorsRebrickable.html"))); // I'm just testing some local stuff on my Windows box.
		List<String> lines = p.parse(reader);
		for(String line : lines)
			System.out.println(line);
	}
	
	@Override
	public List<String> parse(InputStreamReader isr) throws IOException, ParseException {		
		BufferedReader br = new BufferedReader(isr);
		List<String> out = new LinkedList<String>();
		
		ParserHelper.skipPastLine(br, "<tbody>", true);

		String line;
		while(null != (line = br.readLine())) {
			if(!line.equals("<tr>"))
				continue;
			// Read color:
			LEGOColor c = new LEGOColor();
			List<ColorIdNamePair> data = new ArrayList<ColorIdNamePair>();
			while(!"</tr>".equals(line = br.readLine())) {
				if(line == null)
					throw new ParseException("Expected </tr> before end of file", data.size());
				line = line.trim();
				if(line.startsWith("<td>") && line.endsWith("</td>")) {
					String a = line.substring(4, line.length()-5);
					// Trim the anchor tag:
					if(a.startsWith("<a href")) {
						a = a.substring(a.indexOf('>')+1); // Trim <a href=...
						a = a.substring(0, a.length()-4); // Trim ...</a>
					}
					
					c.loadRebrickableData(a);
				}
				else if(line.startsWith("<span") && !line.contains("display:none")) { // ID,name pairs line.
					line = line.substring(line.indexOf(">")+1);
					int id = Integer.parseInt(line.substring(0, line.indexOf(" "))); // int before ' '
					int indexOfSemiColon;
					while((indexOfSemiColon = line.indexOf(";")) != -1) {
						line = line.substring(indexOfSemiColon+1); // Go to next ';'. The next color name is right after.
						int indexOfAnd = line.indexOf("&"); // Color name ends at '&'
						if(indexOfAnd == -1)
							break;
						data.add(new ColorIdNamePair(id, line.substring(0, indexOfAnd)));
						int indexOfComma = line.indexOf(","); // Color name ends at '&'
						line = line.substring(indexOfComma+1);
					}					
				}
				else if(line.startsWith("</td>")) {
					ColorIdNamePair[] pairs = new ColorIdNamePair[data.size()];
					pairs = data.toArray(pairs);
					c.loadRebrickableData(pairs);
					data.clear();
				}
			}
			//System.out.println("Added color: " + c);
			out.add(c.toString());
		}				
		return out;
	}	
}
