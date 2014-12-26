package colors;

import java.io.*;
import java.util.*;

import mosaic.controllers.ColorController;

/**
 * The color grouping is shown in the color choose.
 * Color grouping is arranged using the ColorGrpuping.txt file. 
 * @author ld
 */
public class ColorGroup implements Comparable<ColorGroup> {
	public static final String GROUPS_FILE = "color_groups.txt";
	private LEGOColor[] colors;
	private String name;
	
	private ColorGroup(StringBuffer groupName, List<LEGOColor> colors) {
		this.name = groupName.toString();
		this.colors = new LEGOColor[colors.size()];
		colors.toArray(this.colors);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getName() + "[name:" + name);
		for(LEGOColor c : colors) {
			sb.append(",");
			sb.append(c.getName());
		}
		return sb.toString() + "]";
	}
	
	public LEGOColor[] getColors() {
		return colors;
	}

	public String getName() {
		return name;
	}
	
	public static ColorGroup[] generateBackupColorGroups() {	
		ColorGroup group = new ColorGroup(new StringBuffer("Java colors"), ColorController.generateBackupColors());		
		return new ColorGroup[]{group};
	}
	
	private static String[] tokenize(String s) {
		List<String> tokens = new LinkedList<String>();
		
		StringBuilder sb = new StringBuilder();
		char[] cs = s.toCharArray();
		int csi = 0;
		boolean inQuotes = false;
		while(csi < cs.length) {
			char c = cs[csi++];
			if(c == '"') {
				inQuotes = !inQuotes;
			}
			else if(c == ' ' || c == '\r' || c == '\n') {
				if(inQuotes) {
					sb.append(' ');
				}
				else {
					if(sb.length() > 0) {
						tokens.add(sb.toString());
					}
					sb = new StringBuilder();
				}
			}
			else {
				sb.append(c);
			}
		}
		if(sb.length() > 0) {
			tokens.add(sb.toString());
		}
		
		return tokens.toArray(new String[tokens.size()]);
	}

	public static ColorGroup[] generateColorGroups(ColorController cc) throws FileNotFoundException, IOException{
		List<ColorGroup> out = new LinkedList<ColorGroup>();
		
		Map<String,LEGOColor> unused = new TreeMap<String,LEGOColor>();
		for(LEGOColor c : cc.getColorsFromDisk()) {
			unused.put(c.getName(), c);
		}
		
		Scanner scanner = new Scanner(new File(GROUPS_FILE));

		while(scanner.hasNextLine()) {
			String[] line = tokenize(scanner.nextLine());
			if(line.length <= 1)
				continue;
			StringBuffer groupName = new StringBuffer(line[0]);
			// Find group name:
			String token;
			int i = 1;
			while(!(token = line[i]).equals("=")) {
				groupName.append(" ");
				groupName.append(token);
				i++;
			}
			List<LEGOColor> colorsOfGroup = new LinkedList<LEGOColor>();
			
			i++;
			while(i < line.length) {
				token = line[i];
				boolean startStar = token.startsWith("*");
				boolean endStar = token.endsWith("*");
				if(startStar || endStar) {
					if(startStar)
						token = token.substring(1);
					if(endStar)
						token = token.substring(0, token.length()-1);

					List<String> found = new LinkedList<String>();
					for(String c : unused.keySet()) {
						if(startStar && endStar && c.contains(token) || endStar && c.startsWith(token) || startStar && c.endsWith(token)) {
							colorsOfGroup.add(unused.get(c));							
							found.add(c);
						}
					}
					for(String s : found) {
						unused.remove(s);						
					}
				}
				else if(unused.containsKey(token)) {
					colorsOfGroup.add(unused.get(token));
					unused.remove(token);
				}
				i++;
			}

			if(!colorsOfGroup.isEmpty())
				out.add(new ColorGroup(groupName, colorsOfGroup));
		}
		if(!unused.isEmpty()) {
			List<LEGOColor> colors = new ArrayList<LEGOColor>(unused.values());
			out.add(new ColorGroup(new StringBuffer("Other"), colors));	
		}
				
		return out.toArray(new ColorGroup[]{});
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof ColorGroup && ((ColorGroup)other).name.equals(name);
	}

	public int compareTo(ColorGroup other) {
		return name.compareTo(other.name);
	}
}
