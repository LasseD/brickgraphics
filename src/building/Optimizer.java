package building;

import java.awt.Point;
import java.util.*;

import colors.LEGOColor;

public class Optimizer {
	// Input codes:
	private static final int EMPTY_PLATE = -1;
	//private static final int EMPTY_TILE = -2; TODO: Not how to indicate tiles!
	private static final int CLEARED_ONE_BY_ONE_PLATE = 999;	
	private static final int FIRST_PLACED_INDEX = 1000;
	
	// Scores:
	private static final int SCORE_REPLACE_1X1 = 3;
	private static final int SCORE_REPLACE_CLEARED_1X1 = -2;
	private static final int SCORE_FIRST_CLICK_ON_ANOTHER_PART = 15;
	private static final int SCORE_ADDITIONAL_CLICK_ON_ANOTHER_PART = 1;
	private static final int SCORE_ABOVE_EMPTY_PLATE = 20;
	
	private int height, width, depth;
	public List<Part> placedParts;
	private LEGOColor[] rebrickableIdToLEGOColor;
	private int[][][] partPositions;
	
	public Optimizer(LEGOColor[][][] oneByOnePlatePositions) {
		placedParts = new LinkedList<Part>();
		rebrickableIdToLEGOColor = new LEGOColor[2000];
		
		checkInputAndSetDimensions(oneByOnePlatePositions);
		setLdrawIdToLEGOColor(oneByOnePlatePositions);
		initiatePartPositions(oneByOnePlatePositions);
		hollow();
		optimize();
		placeRemaining1by1Plates();
		Collections.sort(placedParts);
	}
	
	private void checkInputAndSetDimensions(LEGOColor[][][] oneByOnePlatePositions) {
		height = oneByOnePlatePositions.length;
		if(height == 0)
			throw new IllegalArgumentException("No height in input!");
		depth = oneByOnePlatePositions[0].length;
		if(depth== 0)
			throw new IllegalArgumentException("No depth in input!");
		width = oneByOnePlatePositions[0][0].length;
		if(width == 0)
			throw new IllegalArgumentException("No width in input!");
	}
	
	private void setLdrawIdToLEGOColor(LEGOColor[][][] oneByOnePlatePositions) {		
		for(int z = 0; z < height; z++) {
			for(int y = 0; y < depth; ++y) {
				for(int x = 0; x < width; ++x) {					
					LEGOColor c = oneByOnePlatePositions[z][y][x];
					if(c != null)
						rebrickableIdToLEGOColor[c.getIDRebrickable()] = c;
				}
			}
		}
	}
	
	private void initiatePartPositions(LEGOColor[][][] oneByOnePlatePositions) {
		partPositions = new int[height][depth][width];
		for(int z = 0; z < height; z++) {			
			for(int y = 0; y < depth; ++y) {
				for(int x = 0; x < width; ++x) {					
					LEGOColor c = oneByOnePlatePositions[z][y][x];
					if(c == null)
						partPositions[z][y][x] = EMPTY_PLATE;
					else
						partPositions[z][y][x] = c.getIDRebrickable();
				}
			}
		}		
	}
	
	/**
	 * @param oneByOnePlatePositions true where a 1 x 1 plate resides [z][y][x]
	 * @return
	 */
	private void optimize() {
		List<PartType> bricksThenPlates = new LinkedList<PartType>();
		//List<PartType> platesThenBricks = new LinkedList<PartType>();		
		List<PartType> plates = new LinkedList<PartType>();		

		for(PartType pt : PartType.partTypes) {
			if(pt.getCategory() == PartType.Category.Brick)
				bricksThenPlates.add(pt);					
			else {
				//platesThenBricks.add(pt);
				plates.add(pt);				
			}
		}
		for(PartType pt : PartType.partTypes) {
			if(pt.getCategory() != PartType.Category.Brick)
				bricksThenPlates.add(pt);
			//else
				//platesThenBricks.add(pt);
		}
		
		placedParts = new LinkedList<Part>();
		
		// Do the same with plates:
		for(int z = 0; z < height; ++z) {
			while(true) {
				Occupation bestOccupation = null;
				
				for(PartType pt : z % 3 == 0 ? bricksThenPlates : plates) {
					for(int y = 0; y + pt.getDepth() <= depth; ++y) {
						for(int x = 0; x + pt.getWidth() <= width; ++x) {		
							Occupation runner = getOccupation(x, y, z, pt);
							if(runner == null)
								continue;
							if(bestOccupation == null || runner.score > bestOccupation.score)
								bestOccupation = runner;
						} // x
					} // y
				} // bricks
				
				if(bestOccupation == null)
					break; // Can't place any more.
				
				// Update placed parts:
				Part newPart = new Part(bestOccupation.x, bestOccupation.y, z, 
						rebrickableIdToLEGOColor[bestOccupation.color], bestOccupation.pt);
				placedParts.add(newPart);
				int partIdx = FIRST_PLACED_INDEX + placedParts.size();
				for(int zz = 0; zz < bestOccupation.getHeight(); ++zz) {
					for(int yy = 0; yy < bestOccupation.pt.getDepth(); ++yy) {
						for(int xx = 0; xx < bestOccupation.pt.getWidth(); ++xx) {
							if(!newPart.type.isEmpty(xx, yy))
								partPositions[z+zz][yy+bestOccupation.y][xx+bestOccupation.x] = partIdx; 
						}
					}					
				}
			} // while true				
		} // z		
	}
	
	private Occupation getOccupation(int startX, int startY, int startZ, PartType pt) {
		Occupation occupation = new Occupation();
		occupation.color = -1;
				
		int height = pt.getCategory() == PartType.Category.Brick ? 3 : 1;
		if(startZ + height - 1 >= partPositions.length)
			return null;
			
		for(int zz = 0; zz < height; ++zz) {
			for(int yy = 0; yy < pt.getDepth(); ++yy) {
				for(int xx = 0; xx < pt.getWidth(); ++xx) {
					if(pt.isEmpty(xx, yy))
						continue; // Doesn't occupy.
					
					int cur = partPositions[startZ + zz][startY + yy][startX + xx];
					if(cur == EMPTY_PLATE || cur >= FIRST_PLACED_INDEX)
						return null; // Empty or already placed.
					else if(cur == CLEARED_ONE_BY_ONE_PLATE) {
						occupation.score += SCORE_REPLACE_CLEARED_1X1; // In cleared space.
					}
					else if(occupation.color != -1 && occupation.color != cur) {
						return null; // Single brick can't cover two colors.
					}
					else {						
						occupation.color = cur;
						occupation.score += SCORE_REPLACE_1X1;
					}
				}
			}
		}
		if(occupation.color == -1 || occupation.score <= 0)
			return null;
		
		// Ensure bricks don't cause plates to fly:
		if(height == 3) {
			for(int yy = 0; yy < pt.getDepth(); ++yy) {
				int y = yy + startY;
				for(int xx = -1; xx <= pt.getWidth()+1; xx += pt.getWidth()+1) {
					int x = startX+xx; if(x <= 0 || x >= width) continue;
					if(causesFloatingPlate(startZ, y, x))
						return null;
				}
			}
			for(int xx = 0; xx < pt.getWidth(); ++xx) {
				// Same for x!
				int x = xx + startX;
				for(int yy = -1; yy <= pt.getDepth()+1; yy += pt.getDepth()+1) {
					int y = startY+yy; if(y <= 0 || y >= depth) continue;
					if(causesFloatingPlate(startZ, y, x))
						return null;
				}
			}
			if(pt.getEmptyPositions() != null) {
				for(Point p : pt.getEmptyPositions()) {
					if(causesFloatingPlate(startZ, startY+p.y, startX+p.x))
						return null;					
				}
			}
		}
		
		// Add score for parts connecting below (z - 1): 
		if(startZ > 0) {
			Set<Integer> hitBelow = new TreeSet<Integer>();
			
			for(int yy = 0; yy < pt.getDepth(); ++yy) {
				for(int xx = 0; xx < pt.getWidth(); ++xx) {
					if(pt.isEmpty(xx, yy))
						continue; // Doesn't occupy.
					
					int below = partPositions[startZ-1][startY + yy][startX + xx];
					if(below == EMPTY_PLATE) {
						occupation.score += SCORE_ABOVE_EMPTY_PLATE;
					}
					else if(below != CLEARED_ONE_BY_ONE_PLATE && below >= FIRST_PLACED_INDEX) {
						if(hitBelow.contains(below)) {
							occupation.score += SCORE_ADDITIONAL_CLICK_ON_ANOTHER_PART;
						}
						else {
							occupation.score += SCORE_FIRST_CLICK_ON_ANOTHER_PART;
							hitBelow.add(below);
						}
					}
				} // for xx
			} // for yy
		} // if(startZ > 0)
		
		occupation.x = startX;
		occupation.y = startY;
		occupation.pt = pt;

		return occupation;
	}
	
	private boolean causesFloatingPlate(int startZ, int y, int x) {
		int low = partPositions[startZ][y][x];
		int mid = partPositions[startZ+1][y][x];
		int high = partPositions[startZ+2][y][x];
		if(low != EMPTY_PLATE && mid != EMPTY_PLATE)
			return false; // Nothing to float: No problem.
		return mid != EMPTY_PLATE || high != EMPTY_PLATE;	
	}
	
	private static class Occupation {
		int x, y, score, color;
		PartType pt;
		int getHeight() {
			return pt.getCategory() == PartType.Category.Brick ? 3 : 1;
		}
		@Override
		public String toString() {
			return pt + " at " + x + "," + y + ", score: " + score;
		}
	}
	
	private boolean isSurrounded(int x, int y, int z) {
		int[] neighbours = {
				partPositions[z-1][y][x], partPositions[z+1][y][x],
				partPositions[z][y-1][x], partPositions[z][y+1][x],
				partPositions[z][y][x-1], partPositions[z][y][x+1],
				partPositions[z][y-1][x-1], partPositions[z][y+1][x-1],
				partPositions[z][y-1][x+1], partPositions[z][y+1][x+1]};
		for(int i = 0; i < neighbours.length; ++i) {
			if(neighbours[i] == EMPTY_PLATE)
				return false;
		}
		return true;
	}
	
	private void hollow() {
		int sumAll = 0, sumHollow = 0;
		
		for(int z = 1; z < height-1; z++) {
			for(int y = 1; y < depth-1; ++y) {
				for(int x = 1; x < width-1; ++x) {					
					int c = partPositions[z][y][x];
					if(c == EMPTY_PLATE)
						continue;
					sumAll++;
					if(isSurrounded(x, y, z)) {
						partPositions[z][y][x] = CLEARED_ONE_BY_ONE_PLATE;
						sumHollow++;
					}
				}
			}
		}		
		System.out.printf("Hollowing removed %d / %d bricks: %.2f %%", sumHollow, sumAll, (100*sumHollow/(double)sumAll));
	}

	private void placeRemaining1by1Plates() {
		PartType plate1by1Type = null;
		for(PartType pt : PartType.partTypes) {
			if(pt.getID() == 3024) {
				plate1by1Type = pt;
				break;
			}
		}
		
		for(int z = 0; z < height; z++) {
			for(int y = 0; y < depth; ++y) {
				for(int x = 0; x < width; ++x) {
					int c = partPositions[z][y][x];
					if(c == EMPTY_PLATE || c >= CLEARED_ONE_BY_ONE_PLATE)
						continue;
					placedParts.add(new Part(x, y, z, rebrickableIdToLEGOColor[c], plate1by1Type));
				}
			}
		}
	}
}
