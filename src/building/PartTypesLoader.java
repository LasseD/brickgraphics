package building;

import java.io.*;
import java.util.*;

public class PartTypesLoader {
	public static final String PART_TYPES_FILE = "part_types.txt";
	
	private PartTypesLoader() {
		// No initialization, please. KTHXBAI :)
	}
	
	public static PartType[] loadPartTypes() {
		List<PartType> partTypesList;

		try {
			partTypesList = loadPartTypes(new File(PART_TYPES_FILE));
		}
		catch(IOException e) {
			partTypesList = new LinkedList<PartType>();
			partTypesList.add(new PartType("3007|Brick 2 x 8|8x2x3"));
			partTypesList.add(new PartType("3001|Brick 2 x 4|4x2x3"));
			partTypesList.add(new PartType("3004|Brick 1 x 2|2x1x3"));
			partTypesList.add(new PartType("3005|Brick 1 x 1|1x1x3"));

			partTypesList.add(new PartType("3710|Plate 1 x 4|4x1x1"));
			partTypesList.add(new PartType("3623|Plate 1 x 3|3x1x1"));
			partTypesList.add(new PartType("3023|Plate 1 x 2|2x1x1"));
			partTypesList.add(new PartType("3024|Plate 1 x 1|1x1x1"));
		}
		
		PartType[] ret = new PartType[partTypesList.size()];
		int idx = 0;
		for(PartType pt : partTypesList) {
			ret[idx++] = pt;
		}
		
		return ret;
	}
	
	public static List<PartType> loadPartTypes(File partTypesFile) throws IOException {
		List<PartType> ret = new LinkedList<PartType>();
		
		InputStream is = null;
		try {
	    	is = new FileInputStream(partTypesFile);
	    	InputStreamReader isr = new InputStreamReader(is);
	    	BufferedReader br = new BufferedReader(isr);
	    	String line;
	    	while((line = br.readLine()) != null) {
	    		line = line.trim();
	    		if(line.isEmpty() || line.startsWith("#"))
	    			continue;
	    		PartType p = new PartType(line);
    			ret.add(p);
    			boolean canTurn90 = p.canTurn90();    			
    			boolean canTurn180 = p.canTurn180();    			

    			if(canTurn90) {
	    			p = p.turn90();
	    			boolean turnedCanTurn180 = p.canTurn180();
	    			ret.add(p);
	    			p = p.turn90();
	    			if(canTurn180)
	    				ret.add(p);
	    			if(turnedCanTurn180) {
		    			p = p.turn90();
		    			ret.add(p);	    				
	    			}
	    		}
	    	}
	    } finally {
	        if (is != null) 
	        	is.close();
	    }
		
		return ret;
	}
}
