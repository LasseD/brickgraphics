package building;

import java.util.*;

import colors.LEGOColor;

public class Optimizer {
	private static final int EMPTY = -1;
	private static final int CLEARED_ONE_BY_ONE_PLATE = 999;
	private static final int FIRST_PLACED_INDEX = 1000;
	private static final int GOOD_SCORE = 8;
	private static final int BAD_SCORE = -1;
	
	private int height, width, depth;
	public List<Part> placedParts;
	private LEGOColor[] rebrickableIdToLEGOColor;
	private int[][][] partPositions;
	
	public Optimizer(LEGOColor[][][] oneByOnePlatePositions) {
		placedParts = new LinkedList<Part>();
		rebrickableIdToLEGOColor = new LEGOColor[1000];
		
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
						partPositions[z][y][x] = EMPTY;
					else
						partPositions[z][y][x] = c.getIDRebrickable();
				}
			}
		}		
	}

	private void addBestOccupationsForBricks(int z, List<PartType> bricks) {
		while(true) {
			Occupation bestOccupation = null;
			
			for(PartType pt : bricks) {
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
			
			if(bestOccupation == null || bestOccupation.score <= 0)
				return;
			
			// Update placed parts:
			Part newPart = new Part(bestOccupation.x, bestOccupation.y, z+2, // +2 because we know it is a brick. 
					rebrickableIdToLEGOColor[bestOccupation.color], bestOccupation.pt);
			placedParts.add(newPart);
			int partIdx = FIRST_PLACED_INDEX + placedParts.size();
			for(int zz = 0; zz < 3; ++zz) {
				for(int yy = 0; yy < bestOccupation.pt.getDepth(); ++yy) {
					for(int xx = 0; xx < bestOccupation.pt.getWidth(); ++xx) {
						partPositions[zz+z][yy+bestOccupation.y][xx+bestOccupation.x] = partIdx; 
					}
				}
			}
			
		} // while true		
	}
	
	/**
	 * @param oneByOnePlatePositions true where a 1 x 1 plate resides [z][y][x]
	 * @return
	 */
	private void optimize() {
		List<PartType> bricksThenPlates = new LinkedList<PartType>();
		List<PartType> platesThenBricks = new LinkedList<PartType>();		
		for(PartType pt : PartType.partTypes) {
			if(pt.isBrick())
				bricksThenPlates.add(pt);					
			else
				platesThenBricks.add(pt);
		}
		for(PartType pt : PartType.partTypes) {
			if(!pt.isBrick())
				bricksThenPlates.add(pt);					
			else
				platesThenBricks.add(pt);
		}
		
		placedParts = new LinkedList<Part>();
		
		// Do the same with plates:
		for(int z = 0; z < height; ++z) {
			while(true) {
				Occupation bestOccupation = null;
				
				for(PartType pt : z % 3 == 0 ? bricksThenPlates : platesThenBricks) {
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
				
				if(bestOccupation == null || bestOccupation.score <= 0)
					break;
				
				// Update placed parts:
				Part newPart = new Part(bestOccupation.x, bestOccupation.y, z, 
						rebrickableIdToLEGOColor[bestOccupation.color], bestOccupation.pt);
				placedParts.add(newPart);
				int partIdx = FIRST_PLACED_INDEX + placedParts.size();
				for(int zz = 0; zz < bestOccupation.getHeight(); ++zz) {
					for(int yy = 0; yy < bestOccupation.pt.getDepth(); ++yy) {
						for(int xx = 0; xx < bestOccupation.pt.getWidth(); ++xx) {
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
				
		int height = pt.isBrick() ? 3 : 1;
		if(startZ + height - 1 >= partPositions.length)
			return null;
			
		for(int zz = 0; zz < height; ++zz) {
			for(int yy = 0; yy < pt.getDepth(); ++yy) {
				for(int xx = 0; xx < pt.getWidth(); ++xx) {
					int cur = partPositions[startZ + zz][startY + yy][startX + xx];
					if(cur == EMPTY || cur >= FIRST_PLACED_INDEX)
						return null;
					else if(cur == CLEARED_ONE_BY_ONE_PLATE) {
						occupation.score += BAD_SCORE;
					}
					else if(occupation.color != -1 && occupation.color != cur) {
						return null;
					}
					else {
						occupation.color = cur;
						occupation.score += GOOD_SCORE;
					}
				}
			}
		}

		occupation.x = startX;
		occupation.y = startY;
		occupation.pt = pt;

		return occupation;
	}
	
	private static class Occupation {
		int x, y, score, color;
		PartType pt;
		int getHeight() {
			return pt.isBrick() ? 3 : 1;
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
			if(neighbours[i] == EMPTY)
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
					if(c == EMPTY)
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
					if(c == EMPTY || c >= CLEARED_ONE_BY_ONE_PLATE)
						continue;
					placedParts.add(new Part(x, y, z, rebrickableIdToLEGOColor[c], plate1by1Type));
				}
			}
		}
	}
}
