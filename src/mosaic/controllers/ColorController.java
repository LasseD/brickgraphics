package mosaic.controllers;

import java.awt.Color;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import icon.*;
import javax.swing.*;
import javax.swing.event.*;
import colors.*;
import colors.parsers.*;
import mosaic.io.*;
import io.*;

public class ColorController implements ModelHandler<BrickGraphicsState> {
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
	private boolean showMetallic, showTransparent, showOnlyLDD, showOtherColorsGroup;
	private String loadRebrickableURL, loadRebrickableFile;
	// Color Chooser state:
	private LEGOColor[] colorChooserSelectedColors;
	private boolean usesBackupColors;
	
	public ColorController(Model<BrickGraphicsState> model) {
		listeners = new LinkedList<ChangeListener>();
		incrementalIDs = new TreeMap<LEGOColor, Integer>();
		allLocalizedColors = new TreeMap<String, Map<LEGOColor, String> >(); // other two localization attributes null is ok.
		filteredColors = new ArrayList<LEGOColor>(150);
		model.addModelHandler(this);
		
		handleModelChange(model);
		Icons.colorControllerLoaded(this);
		
		try {
			reloadColorTranslations(null, false);
		} catch (IOException e) {
			Log.log(e);
			updateColorListsAndFilters(null, false); // Continue without translations.
		}
		
		try {
			ColorSheetParser.ensureBackupColorsFile();
		} catch (IOException e) {
			Log.log(e);
		}
	}
	
	public LEGOColor[] getColorChooserSelectedColors() {
		if(colorChooserSelectedColors == null || colorChooserSelectedColors.length < 2) {
			return LEGOColor.BW;
		}
		return colorChooserSelectedColors;
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
			String[] line = scanner.nextLine().split("[\\=]");
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
		allLocalizedColors.clear();
		localizedColors = null;

		File folder = new File(COLOR_TRANSLATION_FOLDER_NAME);
		if(!folder.isDirectory())
			throw new IOException("The folder " + COLOR_TRANSLATION_FOLDER_NAME + " does not exist.");
		
		File[] files = folder.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File f, String name) {
				return name.endsWith(".txt");
			}
		});
		
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
		os.close();
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
	public ColorGroup[] getColorGroupsFromDisk() {
		return groupsFromDisk;
	}
	public void setColorChooserSelectedColors(Set<LEGOColor> selectedColors, ChangeEvent e) {
		this.colorChooserSelectedColors = selectedColors.toArray(new LEGOColor[selectedColors.size()]);
		
		updateIncrementalIDs();				
		notifyListeners(e);
	}
	
	public static boolean copyColorsFileToBackup(Object source) {
		try {
			ColorSheetParser.copyColorsFileToBackup();
		} catch (IOException e) {
			Log.log(e);
			return false;
		}
		return true;
	}
	public boolean reloadBackupColorsFile(Object source) {
		try {
			ColorSheetParser.copyBackupColorsFile();
		} catch (IOException e) {
			Log.log(e);
			return false;
		}
		return reloadColorsFile(source, true);
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
		for(LEGOColor c : colorChooserSelectedColors) {
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
	/*public boolean loadColorsFromURL(String url, JDialog toModalizeOnError) {
		try {
			ColorSheetParser.saveFromWeb(url, this);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(toModalizeOnError, "Error loading colors from web: " + e.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e);
			return false;
		} catch (ParseException e2) {
			JOptionPane.showMessageDialog(toModalizeOnError, "Error parsing colors file from web: " + e2.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e2);
			return false;
		}
		loadRebrickableURL = url;
		reloadColorsFile(toModalizeOnError);
		reloadColorGroups(true); // Current implementation requires the groups to be reloaded.
		return true;
	}*/
	public String getLoadRebrickableFile() {
		return loadRebrickableFile;
	}
	public boolean loadColorsFromFile(String file, JDialog toModalizeOnError) {
		try {
			ColorSheetParser.saveFromRebrickableFile(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(toModalizeOnError, "Error loading colors from file: " + e.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e);
			return false;
		} catch (ParseException e2) {
			JOptionPane.showMessageDialog(toModalizeOnError, "Error parsing colors file from file: " + e2.getMessage(), "Error loading colors", JOptionPane.ERROR_MESSAGE);
			Log.log(e2);
			return false;
		}
		loadRebrickableFile = file;
		reloadColorsFile(toModalizeOnError);
		reloadColorGroups(true); // Current implementation requires the groups to be reloaded.
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
				return;
			this.fromYear = from;
			this.toYear = to;
			updateColorListsAndFilters(toMoalizeOnError, true);
		}
		catch(NumberFormatException e) { // also exceptions from parseInt!
			// NOP!
		}
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
				return;
			this.minSets = s;
			this.minParts = p;
			updateColorListsAndFilters(toMoalizeOnError, true);
		}
		catch(Exception e) { // also exceptions from parseInt!
			// NOP!
		}
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
	public boolean getShowOtherColorsGroup() {
		return showOtherColorsGroup;
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
	public void setShowOtherColorsGroup(boolean so, Object source) {
		showOtherColorsGroup = so;
		updateColorListsAndFilters(source, true);
	}
	
	public String getShortIdentifier(LEGOColor c) {
		switch(shownID) {
		case NONE:
			return "-";
		case ID:
			return ""+c.getIDRebrickable();
		case LDRAW:
			if(!c.isLDraw())
				return "";
			return ""+c.getLDraw()[0].getID();
		case BRICKLINK:
			if(!c.isBrickLink())
				return "";
			return ""+c.getBrickLink()[0].getID();
		case BRICKOWL:
			if(!c.isBrickOwl())
				return "";
			return ""+c.getBrickOwl()[0].getID();
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
			return t;// + " = " + c.getLEGO()[0].getName();
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
			return ColorIdNamePair.getIDs(c.getLDraw());
		case BRICKLINK:
			return ColorIdNamePair.getIDs(c.getBrickLink());
		case BRICKOWL:
			return ColorIdNamePair.getIDs(c.getBrickOwl());
		case INCREMENTAL:
			return incrementalIDs.get(c)+"";
		case LEGO:
			return ColorIdNamePair.getIDs(c.getLEGO());
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
			notifyListeners(null); // TODO: Why only when not localized?... I have forgotten.
	}
	public String getShownName(LEGOColor c) {
		switch(shownName) {
		case NONE:
			return null;
		case NAME:
			return c.getName();
		case RGB:
			return "#" + Integer.toHexString(c.getRGB().getRGB());
		case LAB:
			return "("+c.getLAB()[0]+","+c.getLAB()[1]+","+c.getLAB()[2]+")";
		case LEGO:
			return ColorIdNamePair.getNames(c.getLEGO());
		case LDRAW:
			return ColorIdNamePair.getNames(c.getLDraw());
		case BRICKLINK:
			return ColorIdNamePair.getNames(c.getBrickLink());
		case BRICKOWL:
			return ColorIdNamePair.getNames(c.getBrickOwl());
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
		sb.append(ColorIdNamePair.getIdsAndNames(c.getLEGO()));
		sb.append(", LDraw Color(s):");		
		sb.append(ColorIdNamePair.getIdsAndNames(c.getLDraw()));
		sb.append(", BrickLink Color(s):");		
		sb.append(ColorIdNamePair.getIdsAndNames(c.getBrickLink()));
		sb.append(", BrickOwl Color(s):");		
		sb.append(ColorIdNamePair.getIdsAndNames(c.getBrickOwl()));
		return sb.toString();
	}
	
	public static List<LEGOColor> generateBackupColors() {
		List<LEGOColor> colors = new LinkedList<LEGOColor>();
		colors.add(LEGOColor.BLACK);
		colors.add(new LEGOColor(Color.DARK_GRAY, 8, "Dark Gray"));
		colors.add(new LEGOColor(Color.GRAY, 7, "Gray"));
		colors.add(LEGOColor.WHITE);
		colors.add(new LEGOColor(Color.RED, 4, "Red"));
		colors.add(new LEGOColor(Color.BLUE, 1, "Blue"));
		colors.add(new LEGOColor(Color.GREEN, 10, "Green"));
		colors.add(new LEGOColor(Color.YELLOW, 14, "Yellow"));	
		return colors;
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
	public void handleModelChange(Model<BrickGraphicsState> model) {
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
		showOtherColorsGroup = (Boolean)model.get(BrickGraphicsState.ColorsShowOtherColorsGroup);
		minParts = (Integer)model.get(BrickGraphicsState.ColorsMinParts);
		minSets = (Integer)model.get(BrickGraphicsState.ColorsMinSets);
		loadRebrickableURL = (String)model.get(BrickGraphicsState.ColorsLoadRebrickableURL);
		loadRebrickableFile = (String)model.get(BrickGraphicsState.ColorsLoadRebrickableFile);
		updateColorListsAndFilters(this, false);

		// Stuff for color chooser:
		reloadColorGroups(false);
		{
			int[] selectedColors = (int[])model.get(BrickGraphicsState.SelectedColors);
			List<LEGOColor> selectedColorList = new LinkedList<LEGOColor>();
			Set<Integer> selectedColorsFromModel = new TreeSet<Integer>();
			for(int i : selectedColors) {
				selectedColorsFromModel.add(i);
			}
			for(LEGOColor color : colorsFromDisk) {
				if(selectedColorsFromModel.contains(color.getIDRebrickable()))
					selectedColorList.add(color);
			}
			this.colorChooserSelectedColors = selectedColorList.toArray(new LEGOColor[selectedColorList.size()]);
		}
		
		// Intensities:
		Map<Integer,Double> m = (Map<Integer,Double>)model.get(BrickGraphicsState.ColorsIntensities);
		for(LEGOColor color : colorsFromDisk) {
			int id = color.getIDRebrickable();
			if(m.containsKey(id))
				color.setIntensity(m.get(color.getIDRebrickable()));
			else
				color.setIntensity(1);
		}

		notifyListeners(null);		
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
		model.set(BrickGraphicsState.ColorsShowOtherColorsGroup, showOtherColorsGroup);
		model.set(BrickGraphicsState.ColorsMinParts, minParts);
		model.set(BrickGraphicsState.ColorsMinSets, minSets);
		model.set(BrickGraphicsState.ColorsLoadRebrickableURL, loadRebrickableURL);
		model.set(BrickGraphicsState.ColorsLoadRebrickableFile, loadRebrickableFile);
		// Color chooser:		
		
		int[] sc = new int[colorChooserSelectedColors.length];
		int i = 0;
		for(LEGOColor color: colorChooserSelectedColors) {
			sc[i++] = color.getIDRebrickable();
		}
		model.set(BrickGraphicsState.SelectedColors, sc);
		
		// Intensities:
		Map<Integer,Double> m = new TreeMap<Integer,Double>();
		for(LEGOColor color : colorsFromDisk) {
			m.put(color.getIDRebrickable(), color.getIntensity());
		}
		model.set(BrickGraphicsState.ColorsIntensities, m);
	}

	public static enum ShownID {
		NONE("None"), ID("Rebrickable ID"), LEGO("LEGO/LDD IDs"), LDRAW("LDraw IDs"), BRICKLINK("BrickLink IDs"), BRICKOWL("BrickOwl IDs"), INCREMENTAL("Incremental number");

		private final String displayName;
		private ShownID(String displayName) {
			this.displayName = displayName;
		}
		@Override
		public String toString() {
			return displayName;
		}
	}
	public static enum ShownName {
		NONE("None"), NAME("Rebrickable name"), RGB("#RGB-value"), LAB("CIE Lab components"), LEGO("LEGO/LDD Names"), LDRAW("LDraw Names"), BRICKLINK("BrickLink names"), BRICKOWL("BrickOwl names"), LOCALIZED(null);

		private final String displayName;
		private ShownName(String displayName) {
			this.displayName = displayName;
		}
		@Override
		public String toString() {
			return displayName;
		}
	}
}
