package mosaic.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.*;
import java.util.Collections;
import java.util.List;

import bricks.*;
import building.*;
import mosaic.controllers.*;
import mosaic.ui.*;
import colors.*;
import transforms.*;

/**
 * Class responsible for outputting to LDR file. 
 * This is a very simple LDR "printer".
 * @author LD
 */
public class LDRPrinter {
	private ToBricksTransform tbt;
	private ToBricksType type;
	private Dimension blockSize, size;
	private boolean optimize;
	
	public LDRPrinter(MainController mc, BrickedView brickedView) {
		tbt = brickedView.getToBricksTransform();
		type = mc.getToBricksController().getToBricksType();
		MagnifierController magnifier = mc.getMagnifierController();
		blockSize = magnifier.getSizeInMosaicBlocks();
		size = brickedView.getBrickedSize();
		optimize = mc.getOptionsController().getOptimizeUseOfBricksBeforeExporting();
	}
	
	public void printTo(File file) throws IOException {
		FileOutputStream outStream = new FileOutputStream(file, false);
		PrintWriter out = new PrintWriter(outStream);
		String fileName = file.getName();
		String modelName = fileName;
		int index;
		if((index = modelName.indexOf('.')) > 0)
			modelName = modelName.substring(0, index);
		out.println("0 " + modelName); // 0 Untitled
		out.println("0 Name: " + fileName); // 0 Name: Germany.ldr
		out.println("0 Unofficial Model"); // 0 Unofficial Model
		out.println("0 ROTATION CENTER 0 0 0 1 \"Custom\""); // 0 ROTATION CENTER 0 0 0 1 "Custom"
		out.println("0 ROTATION CONFIG 0 0"); // 0 ROTATION CONFIG 0 0 
		out.println("0 ROTSTEP 0 30 0 REL");
		
		switch(type) {
		case STUD_FROM_TOP:
			buildWithPlatesFromTop(out, 1, 1, LXFPrinter.PLATE_1_X_1);
			break;
		case BRICK_FROM_TOP:
			buildWithBricksFromTop(out, 1, 1, LXFPrinter.BRICK_1_X_1);
			break;
		case ONE_BY_TWO_STUDS_FROM_TOP:
			buildWithPlatesFromTop(out, 2, 1, LXFPrinter.PLATE_1_X_2);
			break;
		case ONE_BY_THREE_STUDS_FROM_TOP:
			buildWithPlatesFromTop(out, 3, 1, LXFPrinter.PLATE_1_X_3);
			break;
		case ONE_BY_FOUR_STUDS_FROM_TOP:
			buildWithPlatesFromTop(out, 4, 1, LXFPrinter.PLATE_1_X_4);
			break;
		case TILE_FROM_TOP:
			buildWithTilesFromTop(out);			
			break;
		case PLATE_FROM_SIDE:
			buildWithPartsSeenFromSide(out, 1, 1, LXFPrinter.PLATE_1_X_1);
			break;
		case PLATE_2_1_FROM_SIDE:
			buildWithPartsSeenFromSide(out, 2, 1, LXFPrinter.PLATE_1_X_2);
			break;
		case PLATE_3_1_FROM_SIDE:
			buildWithPartsSeenFromSide(out, 3, 1, LXFPrinter.PLATE_1_X_3);
			break;
		case PLATE_4_1_FROM_SIDE:
			buildWithPartsSeenFromSide(out, 4, 1, LXFPrinter.PLATE_1_X_4);
			break;
		case BRICK_FROM_SIDE:
			buildWithPartsSeenFromSide(out, 1, 3, LXFPrinter.BRICK_1_X_1);
			break;
		case TWO_BY_TWO_PLATES_FROM_TOP:
			buildWithPlatesFromTop(out, 2, 2, LXFPrinter.PLATE_2_X_2);
			break;
		case TWO_BY_FOUR_BRICKS_FROM_TOP:
			buildWithBricksFromTop(out, 4, 2, LXFPrinter.BRICK_2_X_4);
			break;
		case SNOT_IN_2_BY_2:
			buildSnot(out);
			break;
		default: 
			throw new IllegalStateException("Enum broken: " + type);
		}
		
		out.println();
		outStream.flush();
		out.close();
		outStream.close();
	}
	
	private void buildSnot(final PrintWriter out) {
		InstructionsBuilderI builder = new InstructionsBuilderI() {
			@Override
			public void add(int id, int x, int y, LEGOColor color) {
				if(!color.isLDraw())
					return;
				
				String part = LXFPrinter.PLATE_1_X_1 + ".DAT";
				if(y%5==0)
					part = LXFPrinter.TILE_1_X_1 + ".DAT";
				out.printf("1 %d 0 %d %d 0 0 -1 0 1 0 1 0 0 %s\n", color.getLDraw()[0].getID(), 8*y, 20*x, part);
			}

			@Override
			public void addSideways(int id, int x, int y, LEGOColor color) {
				if(!color.isLDraw())
					return;

				String part = LXFPrinter.PLATE_1_X_1 + ".DAT";
				if(y%5==4)
					part = LXFPrinter.TILE_1_X_1 + ".DAT";
				out.printf("1 %d 0 %d %d -1 0 0 0 0 -1 0 -1 0 %s\n", color.getLDraw()[0].getID(), 10+20*y, -2+8*x, part);
			}
		};
		
		for(int y = 0; y < size.height/10; y+=blockSize.height) {
			for(int x = 0; x < size.width/10; x+=blockSize.width) {
				tbt.buildLastInstructions(builder, new Rectangle(x,y,blockSize.width, blockSize.height));
			}
			out.println("0 STEP");
		}
	}
	
	private void buildWithTilesFromTop(PrintWriter out) {
		// TODO: Add optimization also for this by copying for plates!
		buildUnoptimized(out, 20, 20, "0 -1 0 0 0 -1 1 0 0 " + LXFPrinter.TILE_1_X_1 + ".DAT"); 
	}

	private void buildWithBricksFromTop(PrintWriter out, int elementWidth, int elementDepth, String partNumber) {
		// TODO: Add optimization also for this by copying for plates!
		buildUnoptimized(out, 20*elementWidth, 20*elementDepth, "0 -1 0 0 0 -1 1 0 0 " + partNumber + ".DAT"); 
	}

	private void buildWithPlatesFromTop(PrintWriter out, int elementWidth, int elementDepth, String partNumber) {
		if(optimize) {
			LEGOColorGrid unoptimized = tbt.getMainTransform().lastInstructions();
			int unoptimizedWidth = unoptimized.getWidth(); 
			int width = unoptimizedWidth * elementWidth; 
			int unoptimizedDepth = unoptimized.getHeight();
			int depth = unoptimizedDepth * elementDepth;
			LEGOColor[][][] optimizationGrid = new LEGOColor[1][depth][width];
			for(int y = 0; y < unoptimizedDepth; ++y) {				
				LEGOColor[] row = unoptimized.getRow(unoptimizedDepth-y-1); // Flip!
				for(int x = 0; x < unoptimizedWidth; ++x) {
					for(int yy = 0; yy < elementDepth; ++yy) {
						for(int xx = 0; xx < elementWidth; ++xx) {
							optimizationGrid[0][y*elementDepth + yy][x*elementWidth + xx] = row[x];
						}
					}
				}
			}
			List<Part> optimized = new Optimizer(optimizationGrid).placedParts;
			buildOptimizedPicture(out, optimized, blockSize.width * elementWidth, blockSize.height * elementDepth, 1);
		}
		else {
			buildUnoptimized(out, elementWidth*20, elementDepth*20, "0 -1 0 0 0 -1 1 0 0 " + partNumber + ".DAT");
		}
	}
	
	private static void buildOptimizedPicture(PrintWriter out, List<Part> optimized, int stepWidth, int stepDepth, int stepHeight) {
		// Find dimensions to create blocking:
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;		
		int maxZ = Integer.MIN_VALUE;		
		for(Part p : optimized) {
			minX = Math.min(minX, p.x);
			minY = Math.min(minY, p.y);
			minZ = Math.min(minZ, p.z);
			maxX = Math.max(maxX, p.x);
			maxY = Math.max(maxY, p.y);
			maxZ = Math.max(maxZ, p.z);
		}
		
		int numberOfStepsWide = 1 + (maxX-minX) / stepWidth;
		int numberOfStepsDeep = 1 + (maxY-minY) / stepDepth;
		//int numberOfStepsTall = 1 + (maxZ-minZ) / stepHeight;
		
		// Set step:
		for(Part p : optimized) {
			int x = (p.x-minX)/stepWidth;
			int y = (p.y-minY)/stepDepth;
			int z = (p.z-minZ)/stepHeight;
			p.step = z * numberOfStepsWide*numberOfStepsDeep + y * numberOfStepsWide + x;
		}
		Collections.sort(optimized);
		
		// Print:
		int lastStep = 0;
		for(Part p : optimized) {
			p.printLDR(out, PartType.LDRAW_STUD_WIDTH, PartType.LDRAW_STUD_WIDTH, -PartType.LDRAW_PLATE_HEIGHT);
			if(p.step != lastStep) {
				out.println("0 STEP");
				lastStep = p.step;
			}
		}
	}

	private void buildWithPartsSeenFromSide(PrintWriter out, int elementWidth, int elementHeight, String partNumber) {
		if(optimize) {
			LEGOColorGrid unoptimized = tbt.getMainTransform().lastInstructions();
			int unoptimizedWidth = unoptimized.getWidth(); 
			int width = unoptimizedWidth * elementWidth; 
			int unoptimizedHeight = unoptimized.getHeight();
			int height = unoptimizedHeight * elementHeight;
			LEGOColor[][][] optimizationGrid = new LEGOColor[height][1][width];
			for(int y = 0; y < unoptimizedHeight; ++y) {
				LEGOColor[] row = unoptimized.getRow(unoptimizedHeight-1-y);
				for(int x = 0; x < unoptimizedWidth; ++x) {
					for(int yy = 0; yy < elementHeight; ++yy) {
						for(int xx = 0; xx < elementWidth; ++xx) {
							optimizationGrid[y*elementHeight + yy][0][x*elementWidth + xx] = row[x];
						}
					}
				}
			}
			List<Part> optimized = new Optimizer(optimizationGrid).placedParts;
			buildOptimizedPicture(out, optimized, blockSize.width * elementWidth, 1, blockSize.height * elementHeight);
		}
		else {
			buildUnoptimized(out, elementWidth*20, elementHeight*8, "0 -1 0 0 0 -1 1 0 0 " + partNumber + ".DAT");
		}
	}
	
	private void buildUnoptimized(PrintWriter out, int xMult, int yMult, String orientAndDat) {
		LEGOColorGrid instructions = tbt.getMainTransform().lastInstructions();
		int w = instructions.getWidth();
		int h = instructions.getHeight();
		for(int by = (h/blockSize.height)*blockSize.height; by >= 0; by -= blockSize.height) {
			for(int bx = 0; bx < w; bx += blockSize.width) {
				int iMax = Math.min(blockSize.width, w-bx);
				int jMax = Math.min(blockSize.height, h-by);
				for(int j = 0; j < jMax; j++) {
					int y = by+j;
					LEGOColor[] row = instructions.getRow(y);
					for(int i = 0; i < iMax; i++) {
						int x = bx+i;

						if(!row[x].isLDraw())
							continue;
						int color = row[x].getLDraw()[0].getID();
						out.printf("1 %d 0 %d %d %s", color, yMult*y, xMult*x, orientAndDat);
						out.println();
					}
				}
				out.println("0 STEP");
			}
		}	
	}	
}
