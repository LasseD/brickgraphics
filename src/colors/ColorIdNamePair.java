package colors;

import java.util.*;

public class ColorIdNamePair {
	private int id;
	private String name;
	
	public ColorIdNamePair(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public ColorIdNamePair(String encoded) {
		String[] parts = encoded.split("[\\$]");
		if(parts.length == 1) {
			id = Integer.parseInt(parts[0]);
			name = "";
		}
		else if(parts.length == 2) {
			id = Integer.parseInt(parts[0]);
			name = parts[1].trim();
		}
		else
			throw new IllegalArgumentException("ID, name pair for color should be separated with '$'. Found '" + encoded + "'. Parts: " + parts.length);
	}
	
	public int getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return id + "$" + name;
	}
	
	public static String toString(ColorIdNamePair[] pairs) {
		if(pairs.length == 0)
			return "";
		StringBuilder sb = new StringBuilder(pairs[0].toString());
		for(int i = 1; i < pairs.length; ++i) {
			sb.append('#');
			sb.append(pairs[i].toString());
		}
		return sb.toString();
	}
	
	public static ColorIdNamePair[] parse(String encoded) {
		if(encoded.isEmpty())
			return new ColorIdNamePair[]{};
		String[] parts = encoded.split("[\\#]");
		ColorIdNamePair[] ret = new ColorIdNamePair[parts.length];
		for(int i = 0; i < parts.length; ++i)
			ret[i] = new ColorIdNamePair(parts[i]);
		return ret;
	}
	public static ColorIdNamePair[] parseOld(String encodedIds, String rebrickableName) {
		if(encodedIds.isEmpty())
			return new ColorIdNamePair[]{};
		String[] parts = encodedIds.split("[\\,]");
		ColorIdNamePair[] ret = new ColorIdNamePair[parts.length];
		for(int i = 0; i < parts.length; ++i)
			ret[i] = new ColorIdNamePair(Integer.parseInt(parts[i].trim()), rebrickableName);
		return ret;
	}
	public static ColorIdNamePair[] parseOld(int id, String encodedNames, String backupName) {
		if(encodedNames.isEmpty())
			return new ColorIdNamePair[]{new ColorIdNamePair(id, backupName)};
		String[] parts = encodedNames.split("[\\,]");
		ColorIdNamePair[] ret = new ColorIdNamePair[parts.length];
		for(int i = 0; i < parts.length; ++i)
			ret[i] = new ColorIdNamePair(id, parts[i].trim());
		return ret;
	}
	
	public static String getIDs(ColorIdNamePair[] pairs) {
		Set<Integer> m = new TreeSet<Integer>();
		for(ColorIdNamePair p : pairs) {
			if(!m.contains(p.getID()))
				m.add(p.getID());			
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Integer i : m) {
			if(!first)
				sb.append('/');
			sb.append(i);
			first = false;
		}
		return sb.toString();
	}
	public static String getNames(ColorIdNamePair[] pairs) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(ColorIdNamePair p : pairs) {
			if(!first)
				sb.append('/');
			sb.append(p.getName());
			first = false;
		}
		return sb.toString();
	}
	public static String getIdsAndNames(ColorIdNamePair[] pairs) {
		if(pairs == null)
			throw new IllegalArgumentException("Null argument provided!");
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(ColorIdNamePair p : pairs) {
			if(!first)
				sb.append('/');
			sb.append(p.getName());
			sb.append('(');
			sb.append(p.getID());
			sb.append(')');
			first = false;
		}
		return sb.toString();
	}
}
