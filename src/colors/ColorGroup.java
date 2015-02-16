package colors;

import java.io.*;
import java.util.*;

import mosaic.controllers.ColorController;

/**
 * The color grouping is shown in the color choose.
 * Color grouping is arranged using the ColorGrpuping.txt file. 
 * @author ld
 */
public class ColorGroup {
	public static final String GROUPS_FILE = "color_groups.txt";
	private List<String> prefixes, suffixes, infixes;
	private Set<String> colorNames;
	private String name;
	private boolean isOtherColorsGroup;
	
	private ColorGroup(Object groupName) {
		name = groupName.toString();
		colorNames = new TreeSet<String>();
		prefixes = new LinkedList<String>();
		suffixes = new LinkedList<String>();
		infixes = new LinkedList<String>();
		isOtherColorsGroup = false;
	}
	
	public boolean containsColor(LEGOColor c) {
		String n = c.getName();
		if(colorNames.contains(n))
			return true;
		for(String s : prefixes)
			if(n.startsWith(s))
				return true;
		for(String s : suffixes)
			if(n.endsWith(s))
				return true;
		for(String s : infixes)
			if(n.contains(s))
				return true;
		return false;
	}
	
	public boolean isOtherColorsGroup() {
		return isOtherColorsGroup;
	}

	public String getName() {
		return name;
	}
	
	public static ColorGroup generateOtherColorsGroup() {	
		ColorGroup ret = new ColorGroup("Other"){
			@Override
			public boolean containsColor(LEGOColor c) {
				return true;
			}
		};		
		ret.isOtherColorsGroup = true;
		return ret;
	}
	
	public static ColorGroup[] generateBackupColorGroups() {
		return new ColorGroup[]{generateOtherColorsGroup()};
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
			ColorGroup group = new ColorGroup(groupName);
			
			i++;
			while(i < line.length) {
				token = line[i];
				boolean startStar = token.startsWith("*");
				boolean endStar = token.endsWith("*");
				if(startStar) {
					if(endStar)
						group.infixes.add(token.substring(1, token.length()-1));
					else 
						group.suffixes.add(token.substring(1, token.length()));
				}
				else if(endStar) {
					group.prefixes.add(token.substring(0, token.length()-1));					
				}
				else {
					group.colorNames.add(token);					
				}
				i++;
			}
			out.add(group);
		}
		out.add(generateOtherColorsGroup());	
				
		return out.toArray(new ColorGroup[]{});
	}
}
