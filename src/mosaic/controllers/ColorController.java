package mosaic.controllers;

import java.awt.Color;
import java.io.*;
import java.util.*;
import icon.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.*;
import colors.parsers.*;
import mosaic.io.*;
import io.*;

public class ColorController implements ModelSaver<BrickGraphicsState> {
	public static final String COLOR_TRANSLATION_FOLDER_NAME = "color_translations";
	
	private List<ChangeListener> listeners;	
	private Map<LEGOColor, Integer> incrementalIDs;
	private Map<String, Map<LEGOColor, String> > allLocalizedColors;
	private Map<LEGOColor, String> localizedColors;
	private List<LEGOColor> colorsFromDisk, filteredColors;
	private ColorGroup[] groupsFromDisk;
	// Model state:
	private ShownID shownID;
	private ShownName shownName;
	private String localizedFileNameNoTXT;
	private int fromYear, toYear, minParts, minSets;
	private boolean showMetallic, showTransparent, showOnlyLDD;
	private String loadRebrickableURL, loadRebrickableFile, loadLDDXMLFile;
	// Color Chooser state:
	private Set<LEGOColor> colorChooserSelectedColors;
	private Set<ColorGroup> colorChooserSelectedGroups;
	private LEGOColor[] selectedColorsMerged;
	private boolean usesBackupColors;
	
	private static volatile ColorController instance = null;

	public static ColorController instance(Model<BrickGraphicsState> model) {
		if(instance != null)
			return instance;
		instance = new ColorController(model);
		return instance;
	}
	
	private ColorController(Model<BrickGraphicsState> model) {
		listeners = new LinkedList<ChangeListener>();
		incrementalIDs = new TreeMap<LEGOColor, Integer>();
		allLocalizedColors = new TreeMap<String, Map<LEGOColor, String> >(); // other two localization attributes null is ok.
		colorChooserSelectedColors = new TreeSet<LEGOColor>();
		colorChooserSelectedGroups = new TreeSet<ColorGroup>();
		filteredColors = new ArrayList<LEGOColor>(150);
		model.addModelSaver(this);
		
		reloadModel(model);
		Icons.colorControllerLoaded(this);
		try {
			reloadColorTranslations(null, false);
		} catch (IOException e) {
			//e.printStackTrace(); // OK - now we just don't have any translations...
		}
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		// Stuff from file:
		usesBackupColors = !reloadColorsFile(this, false);
		
		// Stuff for color settings:
		shownID = ShownID.values()[(Integer)model.get(BrickGraphicsState.ColorsShownNumber)];
		shownName = ShownName.values()[(Integer)model.get(BrickGraphicsState.ColorsShownText)];
		localizedFileNameNoTXT = (String)model.get(BrickGraphicsState.ColorsLocalizedFileName);
		fromYear = (Integer)model.get(BrickGraphicsState.ColorsFromYear);
		toYear = (Integer)model.get(BrickGraphicsState.ColorsToYear);
		showOnlyLDD = (Boolean)model.get(BrickGraphicsState.ColorsShowOnlyLDD);
		showMetallic = (Boolean)model.get(BrickGraphicsState.ColorsShowMetallic);
		showTransparent = (Boolean)model.get(BrickGraphicsState.ColorsShowTransparent);
		minParts = (Integer)model.get(BrickGraphicsState.ColorsMinParts);
		minSets = (Integer)model.get(BrickGraphicsState.ColorsMinSets);
		loadRebrickableURL = (String)model.get(BrickGraphicsState.ColorsLoadRebrickableURL);
		loadRebrickableFile = (String)model.get(BrickGraphicsState.ColorsLoadRebrickableFile);
		loadLDDXMLFile = (String)model.get(BrickGraphicsState.ColorsLoadLDDXMLFile);
		updateColorListsAndFilters(this, false);

		// Stuff for color chooser:
		reloadColorGroups(false);
		colorChooserSelectedGroups.clear();
		colorChooserSelectedColors.clear();
		Set<String> selectedGroupsFromModel = new TreeSet<String>(Arrays.asList((String[])model.get(BrickGraphicsState.SelectedColorGroups)));
		for(ColorGroup group : groupsFromDisk) {
			if(selectedGroupsFromModel.contains(group.getName()))
				colorChooserSelectedGroups.add(group);
		}
		Set<String> selectedColorsFromModel = new TreeSet<String>(Arrays.asList((String[])model.get(BrickGraphicsState.SelectedColors)));
		for(LEGOColor color : colorsFromDisk) {
			if(selectedColorsFromModel.contains(color.getName()))
				colorChooserSelectedColors.add(color);
		}
		
		notifyListeners(null);		
	}
	
	public LEGOColor[] getColors() {
		if(selectedColorsMerged == null || selectedColorsMerged.length < 2)
			return LEGOColor.BW;
		return selectedColorsMerged;
	}
	
	public boolean usesBackupColors() {
		return usesBackupColors;
	}
	public String getLocalizedFileNameNoTXT() {
		return localizedFileNameNoTXT;
	}
	public String[] getLocalizedFileNamesNoTXT() {
		return allLocalizedColors.keySet().toArray(new String[allLocalizedColors.size()]);
	}
	public void setLocalizedFileNameNoTXT(String s, Object source) {
		localizedFileNameNoTXT = s;
		updateColorListsAndFilters(source, true);
	}
	private void readColorTranslationFile(File f, Map<String, LEGOColor> nameToColor) throws IOException {
		String fileNameNoTXT = f.getName();
		fileNameNoTXT = fileNameNoTXT.substring(0, fileNameNoTXT.length()-4);
		Map<LEGOColor,String> translation = new TreeMap<LEGOColor,String>();
		
		Scanner scanner = new Scanner(f);
		while(scanner.hasNextLine()) {
			String[] line = scanner.nextLine().split("=");
			if(line.length <= 1)
				continue;
			String a = line[0].trim();
			String b = line[1].trim();
			if(b.length() > 0 && nameToColor.containsKey(a)) {
				LEGOColor c = nameToColor.get(a);
				translation.put(c, b);
			}
		}
		scanner.close();
		if(!translation.isEmpty())
			allLocalizedColors.put(fileNameNoTXT, translation);
	}
	public int reloadColorTranslations(Object source, boolean propagate) throws IOException {
		File folder = new File(COLOR_TRANSLATION_FOLDER_NAME);
		if(!folder.isDirectory())
			throw new IOException("The folder " + COLOR_TRANSLATION_FOLDER_NAME + " does not exist.");
		
		File[] files = folder.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File f, String name) {
				return name.endsWith(".txt");
			}
		});
		allLocalizedColors.clear();
		localizedColors = null;
		Map<String, LEGOColor> helperMap = new TreeMap<String, LEGOColor>();
		for(LEGOColor c : colorsFromDisk)
			helperMap.put(c.getName(), c);
		for(File file : files) {
			readColorTranslationFile(file, helperMap);
		}	
		updateColorListsAndFilters(source, propagate);
		return files.length;
	}
	public void createColorTranslationFile(String name) throws IOException {
		if(name.length() <= 4)
			throw new IllegalArgumentException("Translation file name must be set.");
		File folder = new File(COLOR_TRANSLATION_FOLDER_NAME);
		if(!folder.isDirectory())
			throw new IOException("The folder " + COLOR_TRANSLATION_FOLDER_NAME + " does not exist.");
		
		File file = new File(folder, name);
		if(file.exists())
			throw new IllegalArgumentException("Translation file " + name + " already exists!");
		if(!file.createNewFile())
			throw new IOException("Unable to create the file " + name + ".");
		
		FileOutputStream os = new FileOutputStream(file, false);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
		for(LEGOColor c : colorsFromDisk) {
			bw.write(c.getName() + " = ");
			bw.newLine();
		}
		bw.flush();
		os.flush();
	}
	
	public boolean reloadColorGroups() {
		return reloadColorGroups(true);
	}
	private boolean reloadColorGroups(boolean propagateChanges) {
		boolean ok = true;
		try {
			groupsFromDisk = ColorGroup.generateColorGroups(this);
		} catch (IOException e) {
			groupsFromDisk = ColorGroup.generateBackupColorGroups();
			ok = false;
		}		
		if(propagateChanges)
			notifyListeners(null);
		return ok;
	}
	public Set<ColorGroup> getColorChooserSelectedColorGroups() {
		return colorChooserSelectedGroups;
	}
	public ColorGroup[] getColorGroupsFromDisk() {
		return groupsFromDisk;
	}
	public Set<LEGOColor> getColorChooserSelectedColors() {
		return colorChooserSelectedColors;
	}
	public void setColorChooserSelectedColorsAndGroups(Set<LEGOColor> selectedColors, Set<ColorGroup> selectedGroups, ChangeEvent e) {
		this.colorChooserSelectedColors = selectedColors;
		this.colorChooserSelectedGroups = selectedGroups;
		
		Set<LEGOColor> selectedColorsMerged = new TreeSet<LEGOColor>();
		for(ColorGroup group : selectedGroups) {
			for(LEGOColor c : group.getColors())
				selectedColorsMerged.add(c);
		}
		for(LEGOColor c : selectedColors)
			selectedColorsMerged.add(c);
		this.selectedColorsMerged =  selectedColorsMerged.toArray(new LEGOColor[selectedColorsMerged.size()]);
		
		updateIncrementalIDs();
				
		notifyListeners(e);
	}
	
	public boolean reloadColorsFile(Object source) {
		return reloadColorsFile(source, true);
	}
	private boolean reloadColorsFile(Object source, boolean propagateChanges) {
		// Update colors:
		boolean ok = true;
		try {
			colorsFromDisk = ColorSheetParser.readColorsFile();
		} catch (IOException e) {
			colorsFromDisk = generateBackupColors();
			Log.log(e);
			ok = false;
		}
		if(propagateChanges)
			updateColorListsAndFilters(source, true);
		return ok;
	}
	private void updateIncrementalIDs() {
		incrementalIDs.clear();
		int i = 1;
		for(LEGOColor c : selectedColorsMerged) {
			incrementalIDs.put(c, i++);
		}		
	}
	private void updateColorListsAndFilters(Object source, boolean propagateEvent) {
		// First remove filtered colors:
		filteredColors.clear();
		for(LEGOColor c : colorsFromDisk) {
			if(!(c.getTo() < fromYear || 
			     c.getFrom() > toYear || 
			     c.getSets() < minSets || 
			     c.getParts() < minParts || 
			     (showOnlyLDD && !c.isLDD()) ||
			     (!showMetallic && c.isMetallic()) ||
			     (!showTransparent && c.isTransparent())))
				filteredColors.add(c);
		}
		
		// Translations:
		if(localizedFileNameNoTXT != null && allLocalizedColors.containsKey(localizedFileNameNoTXT)) {
			localizedColors = allLocalizedColors.get(localizedFileNameNoTXT);
		}
		else
			localizedColors = null;
		
		if(propagateEvent)
			notifyListeners(new ChangeEvent(source));		
	}
	
	public List<LEGOColor> getFilteredColors() {
		return filteredColors;
	}
	public int getUnfilteredSize() {
		if(colorsFromDisk == null)
			return 0;
		return colorsFromDisk.size();
	}
	public List<LEGOColor> getColorsFromDisk() {
		return colorsFromDisk;
	}
	public String getLoadRebrickableURL() {
		return loadRebrickableURL;
	}
	public boolean loadColorsFromURL(String url, JDialog toMoalizeOnError) {
		try {
			ColorSheetParser.saveFromWeb(url, this);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(toMoalizeOnError, "Error loading colors from web: " + e.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e);
			return false;
		}
		loadRebrickableURL = url;
		reloadColorsFile(toMoalizeOnError);
		reloadColorGroups(true); // Current implementation requires the groups to be reloaded.
		return true;
	}
	public String getLoadRebrickableFile() {
		return loadRebrickableFile;
	}
	public String getLoadLDDXMLFile() {
		return loadLDDXMLFile;
	}
	public boolean loadColorsFromFile(String file, JDialog toMoalizeOnError) {
		try {
			ColorSheetParser.saveFromRebrickableFile(file, this);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(toMoalizeOnError, "Error loading colors from file: " + e.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e);
			return false;
		}
		loadRebrickableFile = file;
		reloadColorsFile(toMoalizeOnError);
		reloadColorGroups(true); // Current implementation requires the groups to be reloaded.
		return true;
	}
	public boolean loadLDDXMLFile(String file, JDialog toMoalizeOnError) {
		try {
			ColorSheetParser.saveFromLDDXMLFile(file, this);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(toMoalizeOnError, "Error loading ldraw.xml file: " + e.getMessage(), "Error loading file", JOptionPane.ERROR_MESSAGE);
			Log.log(e);
			return false;
		}
		loadLDDXMLFile = file;
		reloadColorsFile(toMoalizeOnError);
		return true;
	}
	public int getFromYear() {
		return fromYear;
	}
	public int getToYear() {
		return toYear;
	}
	public void setYearRange(String fromYear, String toYear, JDialog toMoalizeOnError) {
		try {
			int from = Integer.parseInt(fromYear);
			int to = Integer.parseInt(toYear);
			if(from > to)
				throw new IllegalArgumentException("The first number may not be larger than the second.");
			this.fromYear = from;
			this.toYear = to;
		}
		catch(Exception e) { // also exceptions from parseInt!
			JOptionPane.showMessageDialog(toMoalizeOnError, "Error setting range of years: " + e.getMessage(), "Error setting range of years", JOptionPane.ERROR_MESSAGE);			
		}
		updateColorListsAndFilters(toMoalizeOnError, true);
	}
	public int getMinSets() {
		return minSets;
	}
	public int getMinParts() {
		return minParts;
	}
	public void setMinQuantities(String minSets, String minParts, JDialog toMoalizeOnError) {
		try {
			int s = Integer.parseInt(minSets);
			int p = Integer.parseInt(minParts);
			if(s < 0 || p < 0)
				throw new IllegalArgumentException("Please use 0 or a positive quantity.");
			this.minSets = s;
			this.minParts = p;
		}
		catch(Exception e) { // also exceptions from parseInt!
			JOptionPane.showMessageDialog(toMoalizeOnError, "Error setting quantities: " + e.getMessage(), "Error setting quantities", JOptionPane.ERROR_MESSAGE);			
		}
		updateColorListsAndFilters(toMoalizeOnError, true);
	}
	public boolean getShowOnlyLDD() {
		return showOnlyLDD;
	}
	public boolean getShowMetallic() {
		return showMetallic;
	}
	public boolean getShowTransparent() {
		return showTransparent;
	}
	public void setShowOnlyLDD(boolean s, Object source) {
		showOnlyLDD = s;
		updateColorListsAndFilters(source, true);
	}
	public void setShowTransparent(boolean st, Object source) {
		showTransparent = st;
		updateColorListsAndFilters(source, true);
	}
	public void setShowMetallic(boolean sm, Object source) {
		showMetallic = sm;
		updateColorListsAndFilters(source, true);
	}
	
	public String getShortIdentifier(LEGOColor c) {
		switch(shownID) {
		case NONE:
			return "-";
		case ID:
			return ""+c.getIDRebrickable();
		case LDRAW:
			return ""+c.getFirstIDLDraw();
		case BRICKLINK:
			return ""+c.getFirstIDBL();
		case INCREMENTAL:
			return incrementalIDs.get(c)+"";
		case LEGO:
			return ""+c.getIDLEGO();
		default:
			throw new IllegalStateException("Enum broken: " + shownID);
		}
	}
	
	public String getNormalIdentifier(LEGOColor c) {
		String s = getShownID(c);
		String t = getShownName(c);
		if(s == null && t == null)
			return null;
		if(s == null)
			return t;
		if(t == null)
			return s;
		return s + ", " + t;
	}
	
	public ShownID getShownID() {
		return shownID;
	}
	public void setShownID(ShownID shownID) {
		this.shownID = shownID;
		notifyListeners(null);
	}
	public String getShownID(LEGOColor c) {
		switch(shownID) {
		case NONE:
			return null;
		case ID:
			return ""+c.getIDRebrickable();
		case LDRAW:
			return c.getIDsLDraw();
		case BRICKLINK:
			return c.getIDsBL();
		case INCREMENTAL:
			return incrementalIDs.get(c)+"";
		case LEGO:
			return ""+c.getIDLEGO();
		default:
			throw new IllegalStateException("Enum broken: " + shownID);
		}
	}
	
	public ShownName getShownName() {
		return shownName;
	}
	public void setShownName(ShownName shownName) {
		this.shownName = shownName;
		if(shownName != ShownName.LOCALIZED)
			notifyListeners(null);
	}
	public String getShownName(LEGOColor c) {
		switch(shownName) {
		case NONE:
			return null;
		case NAME:
			return ""+c.getName();
		case RGB:
			return "#" + Integer.toHexString(c.getRGB().getRGB());
		case LAB:
			return "("+c.getLAB()[0]+","+c.getLAB()[1]+","+c.getLAB()[2]+")";
		case LEGO:
			return c.getNamesLEGO();
		case PEERON:
			return c.getNamesPeeron();
		case LOCALIZED:
			return localizedColors == null ? "-" : localizedColors.get(c);
		default:
			throw new IllegalStateException("Enum broken: " + shownName);
		}
	}
	
	public static String getLongIdentifier(LEGOColor c) {
		StringBuilder sb = new StringBuilder();
		sb.append(c.getIDRebrickable());
		sb.append(", ");
		sb.append(c.getName());
		sb.append(", #");		
		sb.append(Integer.toHexString(c.getRGB().getRGB()));
		sb.append(", LAB(");		
		sb.append(c.getLAB()[0]);
		sb.append(",");		
		sb.append(c.getLAB()[1]);
		sb.append(",");		
		sb.append(c.getLAB()[2]);
		sb.append("), Num Parts: ");		
		sb.append(c.getParts());
		sb.append(", Num Sets: ");		
		sb.append(c.getSets());
		sb.append(", Years ");		
		sb.append(c.getFrom());
		sb.append("-");				
		sb.append(c.getTo());
		sb.append(", LEGO Color(s):");		
		sb.append(c.getNamesLEGO());
		sb.append(", LDraw Color(s):");		
		sb.append(c.getIDsLDraw());
		sb.append(", BrickLink Color(s):");		
		sb.append(c.getIDsBL());
		sb.append(", Peeron Color(s):");		
		sb.append(c.getNamesPeeron());
		return sb.toString();
	}
	
	public static List<LEGOColor> generateBackupColors() {
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
		return colors;
	}
	private static LEGOColor generateLEGOColor(Color c) {
		return new LEGOColor(c.getRGB());
	}
		
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners(ChangeEvent e) {
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}

	@Override
	public void save(Model<BrickGraphicsState> model) {
		// Color settings dialog:
		model.set(BrickGraphicsState.ColorsShownNumber, shownID.ordinal());
		model.set(BrickGraphicsState.ColorsShownText, shownName.ordinal());
		model.set(BrickGraphicsState.ColorsLocalizedFileName, localizedFileNameNoTXT);
		model.set(BrickGraphicsState.ColorsFromYear, fromYear);
		model.set(BrickGraphicsState.ColorsToYear, toYear);
		model.set(BrickGraphicsState.ColorsShowOnlyLDD, showOnlyLDD);
		model.set(BrickGraphicsState.ColorsShowMetallic, showMetallic);
		model.set(BrickGraphicsState.ColorsShowTransparent, showTransparent);
		model.set(BrickGraphicsState.ColorsMinParts, minParts);
		model.set(BrickGraphicsState.ColorsMinSets, minSets);
		model.set(BrickGraphicsState.ColorsLoadRebrickableURL, loadRebrickableURL);
		model.set(BrickGraphicsState.ColorsLoadRebrickableFile, loadRebrickableFile);
		model.set(BrickGraphicsState.ColorsLoadLDDXMLFile, loadLDDXMLFile);
		// Color chooser:
		String[] sg = new String[colorChooserSelectedGroups.size()];
		int i = 0;
		for(ColorGroup group : colorChooserSelectedGroups)
			sg[i++] = group.getName();
		model.set(BrickGraphicsState.SelectedColorGroups, sg);
		
		String[] sc = new String[colorChooserSelectedColors.size()];
		i = 0;
		for(LEGOColor color: colorChooserSelectedColors)
			sc[i++] = color.getName();
		model.set(BrickGraphicsState.SelectedColors, sc);
	}

	public static enum ShownID {
		NONE("None"), ID("Rebrickable ID"), LDRAW("LDraw IDs"), BRICKLINK("Bricklink IDs"), INCREMENTAL("Incremental number"), LEGO("LEGO/LDD ID");

		public final String displayName;
		private ShownID(String displayName) {
			this.displayName = displayName;
		}
	}
	public static enum ShownName {
		NONE("None"), NAME("Rebrickable name"), RGB("#RGB-value"), LAB("CIE Lab components"), LEGO("Official LEGO Names"), PEERON("Peeron names"), LOCALIZED(null);

		public final String displayName;
		private ShownName(String displayName) {
			this.displayName = displayName;
		}
	}
}
