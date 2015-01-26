package colors.parsers;

import java.util.*;
import java.io.*;
import colors.LEGOColor;
import mosaic.controllers.ColorController;

/**
 * ID, Name, #rgb, |parts|, |sets|, from, to, LEGO names, LDraw IDs, Bricklink IDs, Peeron names
 * @author ld
 */
public class RebrickableColorSheetParser implements ColorSheetParserI {
	@Override
	public List<String> parse(InputStreamReader isr, ColorController cc) throws IOException {
		// construct lookup for rebrickable id -> ldd id:
		Map<Integer,Integer> map = new TreeMap<Integer,Integer>();
		for(LEGOColor color : cc.getColorsFromDisk()) {
			map.put(color.getIDRebrickable(), color.getIDLEGO());
		}
		
		BufferedReader br = new BufferedReader(isr);
		List<String> out = new LinkedList<String>();
		
		ParserHelper.skipPastLineStartingWith(br, "<table class=", true);
		ParserHelper.skipPastLine(br, "</tr>", true);
		// Now we are at the line with non-transparent colors:
		String tr = br.readLine();
		readTRs(out, tr, map);
		
		ParserHelper.skipPastLine(br, "</tr>", true);
		// Now we are at the line with transparent colors:
		tr = br.readLine();
		readTRs(out, tr, map);		
		
		return out;
	}
	
	private static void readTRs(List<String> out, String trs, Map<Integer,Integer> map) {
		if(trs == null)
			return;
		char[] cs = trs.toCharArray();
		int csi = 0;
		while(csi < cs.length) {
			// Spool past <tr><td><img /></td><td>:
			csi = eatTags(cs, csi, 5);
			if(csi >= cs.length)
				return;
			// retrieve 11 pieces of data:
			int id = 0;
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 11; ++i) {
				if(i != 0)
					sb.append('|');
				while(csi < cs.length) {
					if(cs[csi] == '<') {
						if(csi < cs.length-1 && cs[csi+1] == 'b') {
							sb.append(' ');
							csi++;
							while(csi < cs.length && cs[csi] != '>')
								csi++;
							++csi;							
						}
						else 
							break;
					}	
					else if(cs[csi] == '&') {
						while(csi < cs.length && cs[csi] != ';')
							csi++;
						++csi;
					}
					else {
						if(i == 0)
							id = 10*id + cs[csi];
						sb.append(cs[csi++]);						
					}
				}
				csi = eatTags(cs, csi, 2);
			}
			sb.append('|');
			if(map.containsKey(id))
				sb.append(map.get(id));
			else
				sb.append("-1");
			out.add(sb.toString());
		}
	}
	
	private static int eatTags(char[] cs, int csi, int numTags) {
		for(int ignore = 0; ignore < numTags; ++ignore) {
			while(csi < cs.length && cs[csi] != '<')
				++csi;
			while(csi < cs.length && cs[csi] != '>')
				++csi;
			++csi;
		}
		return csi;
	}
}
