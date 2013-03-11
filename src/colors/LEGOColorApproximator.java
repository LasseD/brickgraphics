package colors;

import java.awt.*;

public class LEGOColorApproximator {
	private byte[][][] map;
	private int[] colors;
	private LEGOColor[] legoColors;
	private int precision_bits;
	private int precision;	
	private MapState mapState;
	private int[] minVals, maxVals;

	public LEGOColorApproximator(LEGOColor[] colors) {
		this(colors, 7, true);
	}

	public LEGOColorApproximator(LEGOColor[] colors, int precision_bits, boolean useLDraw_RGB) {
		setColors(colors, useLDraw_RGB);
		this.precision_bits = precision_bits;
		this.precision = 1 << precision_bits;
		map = new byte[precision][precision][precision];

		createMapBuilder().start();
	}
	
	private synchronized void setMapState(MapState mapState) {
		this.mapState = mapState;
	}
	public synchronized MapState getMapState() {
		return mapState;
	}
	
	/**
	 * Set the color palette of this approximator.
	 * @param colors The colors to set
	 * @param useLDraw_RGB Uses color values from LDraw if true (LEGO if false)
	 * @return true if colors were set.
	 */
	public boolean setColors(LEGOColor[] colors, boolean useLDraw_RGB) {
		if(colors == null || colors.length == 0)
			throw new IllegalArgumentException("Not enough colors!");
		if(colors.length > 1<<Byte.SIZE)
			throw new IllegalArgumentException("too many colors: " + colors.length + ">" + (1<<Byte.SIZE));
		if(legoColors == colors)
			return false;

		legoColors = colors;
		this.colors = new int[colors.length];
		minVals = new int[3];
		maxVals = new int[3];
		for(int i = 0; i < colors.length; i++) {
			LEGOColor color = colors[i];
			if(useLDraw_RGB && color.rgb_LDraw != null) {
				this.colors[i] = color.rgb_LDraw.getRGB();
			}
			else {
				this.colors[i] = color.rgb.getRGB();				
			}
			updateExtremeVals(this.colors[i]);
		}
		setMapState(MapState.invalid);
		synchronized(this) {
			notifyAll();			
		}
		return true;
	}
	
	private void updateExtremeVals(int color) {
		int[] rgb = new int[]{getRed(color), getGreen(color), getBlue(color)};
		for(int i = 0; i < minVals.length; i++) {
			minVals[i] = Math.min(minVals[i], rgb[i]);
			maxVals[i] = Math.max(maxVals[i], rgb[i]);			
		}
	}
		
	public float[] toPropagate(float[] propagated, int nearest) {
		int[] nrgb = new int[]{getRed(nearest), getGreen(nearest), getBlue(nearest)};
		float[] toPropagate = new float[3];
		for(int i = 0; i < 3; i++) {
//			if(nrgb[i] > minVals[i] && nrgb[i] < maxVals[i])
				toPropagate[i] = nrgb[i] - propagated[i];
		}
		return toPropagate;
	}

	/*
	  Subtract the diff from tmpRow, weighted by mult.
	 */
	public void sub(float[] tmpRow, int mult, float[] diff) {
		for(int i = 0; i < 3; i++) {
			tmpRow[i] -= diff[i]*(mult/16f);
			if(tmpRow[i] < 0)
				tmpRow[i] = 0;
			else if(tmpRow[i] > 255)
				tmpRow[i] = 255;//*/
		}
	}
	
	private void buildMap() {
		long startTime = System.currentTimeMillis();
		setMapState(MapState.building);
		for(int r = 0; r < precision; r++) {
			for(int g = 0; g < precision; g++) {
				for(int b = 0; b < precision; b++) {
					map[r][g][b] = (byte)minDistIndex(colorComponentVal(r), 
							                          colorComponentVal(g), 
							                          colorComponentVal(b));
				}
			}
			if(getMapState() == MapState.invalid)
				return;
		}
		setMapState(MapState.valid);
		System.out.println("Map built in " + (System.currentTimeMillis() - startTime) + "ms.");
	}
	
	private Thread createMapBuilder() {
		return new Thread() {
			public void run() {
				try {
					while(true) {
						synchronized(LEGOColorApproximator.this) {
							while(mapState == MapState.valid)
								LEGOColorApproximator.this.wait();
						}
						buildMap();
					}					
				}
				catch(InterruptedException e) {
					e.printStackTrace();
					return; // not much to do ;-(
				}
			}
		};
	}
	
	/*private void validateMap() {
		for(int color : colors) {
			int r = getRed(color);
			int g = getGreen(color);
			int b = getBlue(color);
			int mapColor = colors[map[getIndex(r)][getIndex(g)][getIndex(b)]];
			if(color != mapColor) {
				System.out.println("Bad map: " + new Color(color) + "!=" + new Color(mapColor));
			}
		}		
	}//*/

	private int colorComponentVal(int index) {
		return index << (8-precision_bits);
	}

	private int minDistIndex(int r, int g, int b) {
		int minDistIndex = 0;
		for(int i = 1; i < colors.length; i++) {
			if(dist(colors[     i      ], r, g, b) < 
			   dist(colors[minDistIndex], r, g, b))
				minDistIndex = i;
		}
		return minDistIndex;
	}

	private int dist(int c, int r, int g, int b) {
		int dr = getRed(c)-r;
		int dg = getGreen(c)-g;
		int db = getBlue(c)-b;
		return dr*dr+dg*dg+db*db;
	}
	
	public static int dist(int a, int b) {
		int red = getRed(a) - getRed(b);
		int green = getGreen(a) - getGreen(b);
		int blue = getBlue(a) - getBlue(b);		
		return red*red+green*green+blue*blue;
	}
	public static int getRed(int color) {
		return (color & (0xFF0000)) >> 16;
	}
	public static int getGreen(int color) {
		return (color & (0x00FF00)) >> 8;
	}
	public static int getBlue(int color) {
		return color & 0x0000FF;
	}
	private int getIndex(float component) {
		int res = ((int)component) >> (8-precision_bits);
	    if(res < 0 || res >= precision) {
	    	//System.out.println("res: " + res);
	    	return -1;
	    }
	    return res;
	}
	private int getIndex(int component) {
		return component >> (8-precision_bits);
	}

	public int getNearestIndex(int rgb) {
		int r = getRed(rgb);
		int g = getGreen(rgb);
		int b = getBlue(rgb);
		int index;
		if(getMapState() != MapState.valid)
			index = minDistIndex(r, g, b);
		else {
			index = 0;
			int rIndex = getIndex(r);
			int gIndex = getIndex(g);
			int bIndex = getIndex(b);
			index = map[rIndex][gIndex][bIndex];
		}
		return index;
	}
	
	public int getNearestIndex(float[] rgb) {
		float r = rgb[0];
		float g = rgb[1];
		float b = rgb[2];
		int rIndex = getIndex(r);
		int gIndex = getIndex(g);
		int bIndex = getIndex(b);
		if(getMapState() != MapState.valid || rIndex == -1 || gIndex == -1 || bIndex == -1)
			return minDistIndex((int)r, (int)g, (int)b);
		return map[rIndex][gIndex][bIndex];
	}

	public int getColor(int index) {
		return colors[index];
	}

	@Override
	public String toString() {
		return getClass().getName() + "[precision:" + precision + ",bits:" + precision_bits + 
		"," + mapInfo() + "]" + mapToString();
	}

	private String mapToString() {
		if(precision_bits > 3)
			return ""; // don't print that much

		StringBuilder out = new StringBuilder();
		for(int r = 0; r < precision; r++) {
			for(int g = 0; g < precision; g++) {
				for(int b = 0; b < precision; b++) {
					Color from = new Color(colorComponentVal(r), 
							colorComponentVal(g), 
							colorComponentVal(b));
					Color to = new Color(colors[map[r][g][b]]);
					out.append("\n");
					out.append(from);
					out.append(" -> ");
					out.append(to);
				}
			}
		}
		return out.toString();
	}

	private String mapInfo() {
		int[] colorCounts = new int[colors.length];
		for(byte[][] bbb : map) {
			for(byte[] bb : bbb) {
				for(byte b : bb) {
					colorCounts[b]++;
				}				
			}
		}
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < colors.length; i++) {
			out.append("[");
			out.append(new Color(colors[i]));
			out.append(":");
			out.append(colorCounts[i]);
			out.append("]");
		}
		return out.toString();
	}
	
	public LEGOColor getLEGOColor(int index) {
		return legoColors[index];
	}
	
	public String getColorInfo(int index) {
		if(legoColors == null || legoColors.length <= index)
			return Integer.toHexString(colors[index]);
		return legoColors[index].name_Peeron;
	}
	
	public enum MapState {
		valid, building, invalid;
	}
	
	public int size() {
		return colors.length;
	}
}
