package colors.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import colors.LEGOColor;

import mosaic.controllers.ColorController;

public class LDDXMLParser implements ColorSheetParserI {
	@Override
	public List<String> parse(InputStreamReader isr, ColorController cc)
			throws IOException {
		// construct lookup for ldraw id -> LEGOColor:
		Map<Integer,LEGOColor> map = new TreeMap<Integer,LEGOColor>();
		for(LEGOColor color : cc.getColorsFromDisk()) {
			map.put(color.getFirstIDLDraw(), color);
		}
		
		BufferedReader br = new BufferedReader(isr);
		List<String> out = new LinkedList<String>();

		// Read all lines until "<Brick..." starts:
		String line;
		while((line = br.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("<Brick "))
				break;
			if(!line.startsWith("<Material "))
				continue;
			String[] parts = line.split("\"");
			if(parts.length < 4)
				throw new IOException("Line " + line + " is not a valid Material definition.");
			int ldrawID = Integer.parseInt(parts[1]);
			int legoID = Integer.parseInt(parts[3]);
			if(!map.containsKey(ldrawID)) {
				System.err.println("Unknown color in ldraw.xml: " + ldrawID);
				continue;				
			}
			LEGOColor color = map.get(ldrawID);
			map.remove(ldrawID);
			color.setIdLEGO(legoID);
			out.add(color.toDelimitedString());
		}
		// remaining colors:
		for(LEGOColor color : map.values()) {
			out.add(color.toDelimitedString());			
		}
		
		return out;
	}
}
