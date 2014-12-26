package colors.parsers;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

import colors.LEGOColor;

public class ColorSheetParser {
	public static final String COLORS_FILE = "colors.txt";
	private static ColorSheetParserI htmlParser = new RebrickableColorSheetParser();
	
	public static void saveFromWeb(String uri) throws MalformedURLException, IOException {
		InputStream is = null;
		try {
	    	URL url = new URL(uri.trim());
	    	is = url.openStream();

	    	List<String> lines = htmlParser.parse(new InputStreamReader(is));
	    	writeColorsFile(lines);
	    } finally {
	        if (is != null) 
	        	is.close();
	    }
	}
	
	public static void saveFromFile(String file) throws MalformedURLException, IOException {
		InputStream is = null;
		try {
	    	File f = new File(file.trim());
	    	is = new FileInputStream(f);

	    	List<String> lines = htmlParser.parse(new InputStreamReader(is));
	    	writeColorsFile(lines);
	    } finally {
	        if (is != null) 
	        	is.close();
	    }
	}
	
	private static void writeColorsFile(List<String> lines) throws IOException {
		OutputStream os = null;
		try {
	    	File f = new File(COLORS_FILE);
			os = new FileOutputStream(f, false);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			for(String line : lines) {
				bw.write(line);
				bw.newLine();
			}
			bw.flush();
			os.flush();
		}
		finally {
			if(os != null)
				os.close();
		}
	}
	
	public static List<LEGOColor> readColorsFile() throws IOException {
		InputStream is = null;
		List<LEGOColor> out = new LinkedList<LEGOColor>();
		try {
	    	File f = new File(COLORS_FILE);
	    	is = new FileInputStream(f);
	    	InputStreamReader isr = new InputStreamReader(is);
	    	BufferedReader br = new BufferedReader(isr);
	    	String line;
	    	while((line = br.readLine()) != null) {
	    		LEGOColor c = LEGOColor.parse(line);
	    		if(c != null)
	    			out.add(c);
	    	}
	    } finally {
	        if (is != null) 
	        	is.close();
	    }
		return out;
	}

}