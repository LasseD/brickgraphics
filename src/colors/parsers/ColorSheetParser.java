package colors.parsers;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

//import mosaic.controllers.ColorController;

import colors.LEGOColor;

public class ColorSheetParser {
	public static final String COLORS_FILE = "colors.txt";
	public static final String BACKUP_COLORS_FILE = "backup_colors.txt";
	private static ColorSheetParserI htmlParser = new RebrickableColorSheetParser();
	
	/*public static void saveFromWeb(String uri, ColorController cc) throws MalformedURLException, IOException, ParseException {
		InputStream is = null;
		try {
	    	URL url = new URL(uri.trim());
	    	is = url.openStream();

	    	List<String> colorsTxtFileLines = htmlParser.parse(new InputStreamReader(is));
	    	writeColorsFile(colorsTxtFileLines);
	    } finally {
	        if (is != null)
	        	is.close();
	    }
	}*/
	
	public static void saveFromRebrickableFile(String file) throws MalformedURLException, IOException, ParseException {
		InputStream is = null;
		try {
	    	File f = new File(file.trim());
	    	is = new FileInputStream(f);

	    	List<String> colorsTxtFileLines = htmlParser.parse(new InputStreamReader(is));
	    	writeColorsFile(colorsTxtFileLines);
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
			os.close();
		}
		finally {
			if(os != null)
				os.close();
		}
	}
	
	public static void ensureBackupColorsFile() throws IOException {
		File file = new File(BACKUP_COLORS_FILE);
		if(file.exists())
			return; // OK.
		Files.copy(Paths.get(COLORS_FILE), Paths.get(BACKUP_COLORS_FILE), StandardCopyOption.COPY_ATTRIBUTES);
	}
	
	public static void copyBackupColorsFile() throws IOException {
		Files.copy(Paths.get(BACKUP_COLORS_FILE), Paths.get(COLORS_FILE), StandardCopyOption.REPLACE_EXISTING);
	}		
	public static void copyColorsFileToBackup() throws IOException {
		Files.copy(Paths.get(COLORS_FILE), Paths.get(BACKUP_COLORS_FILE), StandardCopyOption.REPLACE_EXISTING);
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
