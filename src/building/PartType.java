package building;

/**
 * Part type, like "2 x 4 brick", "1 x 1 plate", etc.
 * @author ld
 */
public class PartType {
	private final int width, depth, timesTurned90Degrees;
	private final long emptyPositions;
	private final String name;
	private final int id; // Only use integer ID'en parts (for now) to increase performance.
	private boolean isBrick;
	
	public static final PartType[] partTypes = PartTypesLoader.loadPartTypes();
	
	public PartType(String partDescription) {
		String[] parts = partDescription.split("[|]");
		for(int i = 0; i < parts.length; ++i) 
			parts[i] = parts[i].trim();
		if(parts.length != 3 && parts.length != 4) {
			throw new IllegalArgumentException("Expected part description line to contain 3 or 4 parts. Contained " + parts.length + ": " + partDescription);
		}
		String[] dimParts = parts[2].split("[x]");
		if(dimParts.length != 3) {
			throw new IllegalArgumentException("Expected dimension part of description line to contain 3. Contained " + dimParts.length + ": " + partDescription);
		}

		width = Integer.parseInt(dimParts[0].trim());
		depth = Integer.parseInt(dimParts[1].trim());
		isBrick = 3 == Integer.parseInt(dimParts[2].trim());		
		name = parts[1];
		id = Integer.parseInt(parts[0]);
		timesTurned90Degrees = 0;
		
		long buildEmptyPositions = 0;		
		if(parts.length == 4) {
			// Set empty positions:
			int positions = width * depth;
			for(int i = 0; i < positions; ++i) {
				buildEmptyPositions = (buildEmptyPositions << 1) | (parts[3].charAt(i) == 'X' ? 1 : 0);
			}
		}
		emptyPositions = buildEmptyPositions;
	}
	
	
	private PartType(int width, int depth, boolean isBrick, String name, int id, long emptyPositions, int timesTurned90Degrees) {
		this.width = width;
		this.depth = depth;
		this.isBrick = isBrick;
		this.id = id;
		this.name = name;
		this.emptyPositions = emptyPositions;
		this.timesTurned90Degrees = timesTurned90Degrees;
	}

	public boolean canTurn90() {
		// TODO: Improve!
		return emptyPositions != 0 || width != depth;
	}
	public boolean canTurn180() {
		// TODO: Improve!
		return emptyPositions != 0;
	}
	
	public PartType turn90() {
		long copyEmptyPositions = emptyPositions;
		long turnedEmptyPositions = 0;
		boolean[][] occupied = new boolean[width][depth];
		for(int y = 0; y < width; ++y) {
			for(int x = 0; x < depth; ++x) {
				occupied[y][x] = 1 == (copyEmptyPositions & 1);
				copyEmptyPositions >>= 1;
			}
		}
		for(int x = 0; x < depth; ++x) {
			for(int y = 0; y < width; ++y) {
				turnedEmptyPositions = (turnedEmptyPositions << 1) | (occupied[y][x] ? 1 : 0);
			}
		}
		return new PartType(depth, width, isBrick, name, id, turnedEmptyPositions, timesTurned90Degrees+1);
	}
	
	public String getName() {
		return name;
	}
	public int getWidth() {
		return width;
	}
	public int getDepth() {
		return depth;
	}
	public boolean isBrick() {
		return isBrick;
	}
	public int getID() {
		return id;
	}
	public int getTimesTurned90Degrees() {
		return timesTurned90Degrees;
	}
}
