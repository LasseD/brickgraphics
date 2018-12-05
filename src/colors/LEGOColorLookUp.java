package colors;

/**
 * Mapping for quick lookup discards the lower 2 bits from each color component. 
 * Effectively reducing the size of the color space from 16.777.216 to 262.144
 * @author LD
 */
public class LEGOColorLookUp {
	public static final int COMPONENT_SIZE = 64;
	public static final int MAP_SIZE = COMPONENT_SIZE*COMPONENT_SIZE*COMPONENT_SIZE;
	private static final byte[] map = new byte[MAP_SIZE]; // from color to byte indexing color in legoColors
	
	private static LEGOColor[] legoColors = null;
	
	private static void clearMap() {
		for(int i = 0; i < MAP_SIZE; ++i) {
			map[i] = -1;
		}
	}
	
	public static int size() {
		return legoColors.length;
	}
	
	/**
	 * Set the colors.
	 * @param colors The colors to set
	 * @param useLDraw_RGB Uses color values from LDraw if true (LEGO if false)
	 * @return true if colors were set.
	 */
	public static boolean setColors(LEGOColor[] colors) {
		if(colors == null || colors.length < 2)
			throw new IllegalArgumentException("Not enough colors!");
		if(colors.length > 127)
			throw new IllegalArgumentException("too many colors: " + colors.length + " > 127");
		if(legoColors == colors)
			return false; // no update.
		
		clearMap();
		legoColors = colors;
		return true;
	}
	
	public static int getRed(int rgb) {
		return (rgb & (0xFF0000)) >> 16;
	}
	public static int getGreen(int rgb) {
		return (rgb & (0x00FF00)) >> 8;
	}
	public static int getBlue(int rgb) {
		return rgb & 0x0000FF;
	}
	
	public static LEGOColor lookUp(int rgb) {
		int r = getRed(rgb);
		int g = getGreen(rgb);
		int b = getBlue(rgb);
		return lookUp(r, g, b);
	}

	public static LEGOColor lookUp(float[] rgb) {
		return lookUp((int)rgb[0], (int)rgb[1], (int)rgb[2]);
	}
	
	private static int truncate(int a) {
		if(a < 0)
			return 0;
		if(a > 255)
			return 255;
		return a;
	}

    public static LEGOColor lookUp(int r, int g, int b) {
    	/*r = truncate(r);
    	g = truncate(g);
    	b = truncate(b);//*/ // Uncommented for now as it seems to bother the dithering algorithm
		int indexInMap = (r>>2)*64*64 + (g>>2)*64 + (b>>2);
		
		if(map[indexInMap] == -1) {
			int[] labInput = new int[3]; 
			CIELab.rgb2lab(r, g, b, labInput);
			
			double minDiff = Double.MAX_VALUE;
			byte minDiffIndex = -1;
			for(byte i = 0; i < legoColors.length; ++i) {
				LEGOColor c = legoColors[i];
				double diff = ColorDifference.diffCIE94(labInput, c.getLAB());
				//double diff = ColorDifference.diffRGB(r, g, b, legoColors[i].getRGB());
				diff /= c.getIntensity();
				if(diff < minDiff) {
					minDiff = diff;
					minDiffIndex = i;
				}
			}
			map[indexInMap] = minDiffIndex;
		}
		return legoColors[ map[indexInMap] ];
	}
}
