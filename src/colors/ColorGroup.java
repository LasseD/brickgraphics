package colors;

import java.awt.Color;
import java.io.*;
import java.util.*;

public class ColorGroup implements Comparable<ColorGroup> {
	public static final String GROUPS_FILE = "ColorGrouping.txt";
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
			sb.append(c.name_Peeron);
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
		List<LEGOColor> colors = new LinkedList<LEGOColor>();
		colors.add(generateLEGOColor(Color.BLACK));
		colors.add(generateLEGOColor(Color.DARK_GRAY));
		colors.add(generateLEGOColor(Color.GRAY));
		colors.add(generateLEGOColor(Color.LIGHT_GRAY));
		colors.add(generateLEGOColor(Color.WHITE));
		colors.add(generateLEGOColor(Color.RED));
		colors.add(generateLEGOColor(Color.BLUE));
		colors.add(generateLEGOColor(Color.GREEN));
		colors.add(generateLEGOColor(Color.YELLOW));
		
		ColorGroup group = new ColorGroup(new StringBuffer("Java colors"), colors);
		
		return new ColorGroup[]{group};
	}
	
	public static LEGOColor generateLEGOColor(Color c) {
		LEGOColor legoColor = new LEGOColor();
		legoColor.name_LEGO = c.toString();
		legoColor.rgb = c;
		return legoColor;
	}

	public static ColorGroup[] generateColorGroups() throws FileNotFoundException, IOException{
		List<ColorGroup> out = new LinkedList<ColorGroup>();
		
		Map<String, LEGOColor> sheet = ColorSheetParser.parse();
		Set<String> keys = sheet.keySet();
		Set<String> unused = new TreeSet<String>(keys);
		
		Scanner scanner = new Scanner(new File(GROUPS_FILE));
		boolean first = true;
		while(scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split(" ");
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
			List<LEGOColor> colors = new LinkedList<LEGOColor>();
			if(first) {
				colors.add(LEGOColor.BLACK);
			}
			
			i++;
			while(i < line.length) {
				token = line[i];
				if(token.startsWith("<") && token.endsWith(">")) {
					token = token.substring(1, token.length()-1);
					for(String key : keys) {
						if(key.startsWith(token)) {
							LEGOColor color = sheet.get(key);
							if(color.rgb != null) {
								colors.add(sheet.get(key));							
								unused.remove(key);
							}
						}
					}
				}
				else {
					LEGOColor color = sheet.get(token);
					if(color != null && color.rgb != null) {
						colors.add(sheet.get(token));
						unused.remove(token);
					}
				}
				i++;
			}

			if(first) {
				colors.add(LEGOColor.WHITE);
				first = false;
			}
			
			out.add(new ColorGroup(groupName, colors));
		}
		if(!unused.isEmpty()) {
			List<LEGOColor> colors = new LinkedList<LEGOColor>();
			
			for(String key : unused) {
				LEGOColor color = sheet.get(key);
				if(color != null && color.rgb != null)
					colors.add(color);
			}
			
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
