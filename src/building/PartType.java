package building;

import java.awt.Point;

/**
 * Part type, like "2 x 4 brick", "1 x 1 plate", etc.
 * Consider a part in BrickShaper to be placed stud up with lower left corner stud at grid position 0,0.
 * - A plate WxH (like 1x2) in LDraw is non-rotated (see Part.java rotation 0) with center at LDraw 0,0, 
 *   so its center is translated x=(W-1)*10, y=(H-1)*10
 * - Turn the plate 90 degrees and its center flips.
 * - Tiles are handled like plates
 * - Bricks are placed with the top equal to that of a plate in LDraw, to z must be translated to reflect this.
 *   This is done on Part.java
 * - Corner plates also have a unoccupied/empty positions which need to be rotated as well.
 * @author LD
 */
public class PartType {
	public static final int LDRAW_STUD_WIDTH = 20;
	public static final int LDRAW_PLATE_HEIGHT = 8;
	
	private final int width, depth, lDrawOffsetX, lDrawOffsetY, id, timesTurned90Degrees;
	private final Point[] emptyPositions;
	private final boolean identityWhenTurned90, identityWhenTurned180;
	private final Category category;
	
	public static final PartType[] partTypes = PartTypesLoader.loadPartTypes();
	
	public PartType(String partDescription) {
		String[] parts = partDescription.split("[|]");
		for(int i = 0; i < parts.length; ++i) 
			parts[i] = parts[i].trim();
		if(parts.length != 2) {
			throw new IllegalArgumentException("Expected part description line to contain 2 parts delimited with | (pipe). Contained " + parts.length + ": " + partDescription);
		}
		String[] dimParts = parts[1].split("\\s+");
		if(dimParts.length != 5 && dimParts.length != 4) {
			throw new IllegalArgumentException("Expected part description to be '[Brick|Plate|Tile] W x H [Corner]. Contained " + dimParts.length + " parts: " + parts[1]);
		}

		width = Integer.parseInt(dimParts[3].trim());
		depth = Integer.parseInt(dimParts[1].trim());
		boolean isCorner = dimParts.length == 5;
		// See class description:
		lDrawOffsetX = isCorner ? (width-2)*LDRAW_STUD_WIDTH/4 : (width-1)*LDRAW_STUD_WIDTH/2;
		lDrawOffsetY = isCorner ? (depth-2)*LDRAW_STUD_WIDTH/4 : (depth-1)*LDRAW_STUD_WIDTH/2;
		
		category = Category.valueOf(dimParts[0]);
		id = Integer.parseInt(parts[0]);
		timesTurned90Degrees = 0;
		
		if(isCorner) {
			emptyPositions = new Point[width/2*depth/2];
			// Set empty positions in top right corner (max x and max y):
			int idx = 0;
			for(int y = depth/2; y < depth; ++y) {
				for(int x = width/2; x < width; ++x) {
					emptyPositions[idx++] = new Point(x, y);					
				}
			}
		}
		else {
			emptyPositions = null;
		}

		identityWhenTurned90 = width == depth && emptyPositions == null;
		identityWhenTurned180 = emptyPositions == null;
	}
	
	private PartType(int width, int depth, int lDrawCenterX, int lDrawCenterY, Category category, int id, Point[] emptyPositions, int timesTurned90Degrees) {
		this.width = width;
		this.depth = depth;
		this.category = category;
		this.id = id;
		this.emptyPositions = emptyPositions;
		this.lDrawOffsetX = lDrawCenterX;
		this.lDrawOffsetY = lDrawCenterY;
		this.timesTurned90Degrees = timesTurned90Degrees;
		
		identityWhenTurned90 = width == depth && emptyPositions == null; // OK Now we only handle corners
		identityWhenTurned180 = emptyPositions == null; // OK Now we only handle corners
	}
	
	private Point[] getEmptyPositionsWhenTurned90() {
		if(emptyPositions == null)
			return null;

		Point[] ret = new Point[emptyPositions.length];
		for(int i = 0; i < emptyPositions.length; ++i) {
			ret[i] = new Point(emptyPositions[i].y, width-1-emptyPositions[i].x);
		}
		return ret;
	}
	
	public PartType turn90() {
		int turnedLDX = lDrawOffsetY;
		int turnedLDY = lDrawOffsetX;
		if(emptyPositions != null) { // Is corner. Move offset differently
			switch(timesTurned90Degrees) {
			case 0:
				turnedLDX = (width-2)*LDRAW_STUD_WIDTH/4;
				turnedLDY = width*LDRAW_STUD_WIDTH/2 + (width-2)*LDRAW_STUD_WIDTH/4;
				break;
			case 1:
				turnedLDX = width*LDRAW_STUD_WIDTH/2 + (width-2)*LDRAW_STUD_WIDTH/4;
				turnedLDY = width*LDRAW_STUD_WIDTH/2 + (width-2)*LDRAW_STUD_WIDTH/4;
				break;
			case 2:
				turnedLDY = (width-2)*LDRAW_STUD_WIDTH/4;
				turnedLDX = width*LDRAW_STUD_WIDTH/2 + (width-2)*LDRAW_STUD_WIDTH/4;
				break;
			default:
				throw new IllegalStateException();
			}			
		}
		return new PartType(depth, width, turnedLDX, turnedLDY, category, id, getEmptyPositionsWhenTurned90(), timesTurned90Degrees+1);
	}
	
	public int getLDrawCenterX() {
		return lDrawOffsetX;
	}
	
	public int getLDrawCenterY() {
		return lDrawOffsetY;
	}
	
	public boolean getIdentityWhenTurned90() {
		return identityWhenTurned90;
	}
	
	public boolean getIdentityWhenTurned180() {
		return identityWhenTurned180;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public int getID() {
		return id;
	}
	
	public Point[] getEmptyPositions() {
		return emptyPositions;
	}
	
	public boolean isEmpty(int x, int y) {
		if(emptyPositions == null)
			return false;
		for(Point p : emptyPositions) {
			if(p.x == x && p.y == y)
				return true;
		}
		return false;
	}
	
	public int getTimesTurned90Degrees() {
		return timesTurned90Degrees;
	}
	
	public static enum Category {
		Brick, Plate, Tile;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(category);
		sb.append(" ");
		sb.append(width);
		sb.append(" X ");
		sb.append(depth);
		sb.append(" (");
		sb.append(lDrawOffsetX);
		sb.append(" X ");
		sb.append(lDrawOffsetY);
		sb.append(")");
		if(emptyPositions != null) {
			sb.append(" empty positions:");
			for(Point p : emptyPositions) {
				sb.append(" (");
				sb.append(p.x);
				sb.append(",");
				sb.append(p.y);
				sb.append(")");
			}
		}
		
		if(identityWhenTurned90)
			sb.append(" ID@90");
		if(identityWhenTurned180)
			sb.append(" ID@180");
		return sb.toString(); 
	}
}
