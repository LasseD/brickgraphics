package mosaic.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.*;
import bricks.*;
import mosaic.controllers.*;
import mosaic.ui.BrickedView;
import colors.*;
import transforms.*;

/**
 * Class responsible for outputting to LDR file. 
 * This is a very simple LDR "printer".
 * @author ld
 */
public class LDRPrinter {
	private ToBricksTransform tbt;
	private ToBricksType type;
	private Dimension blockSize, size;
	
	public LDRPrinter(MainController mc, BrickedView brickedView) {
		tbt = brickedView.getToBricksTransform();
		type = mc.getToBricksController().getToBricksType();
		MagnifierController magnifier = mc.getMagnifierController();
		blockSize = magnifier.getSizeInMosaicBlocks();
		size = brickedView.getBrickedSize();
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
			buildWithStuds(out, 1, LXFPrinter.PLATE_1_X_1);
			break;
		case ONE_BY_TWO_STUDS_FROM_TOP:
			buildWithStuds(out, 2, LXFPrinter.PLATE_1_X_2);
			break;
		case ONE_BY_THREE_STUDS_FROM_TOP:
			buildWithStuds(out, 3, LXFPrinter.PLATE_1_X_3);
			break;
		case ONE_BY_FOUR_STUDS_FROM_TOP:
			buildWithStuds(out, 4, LXFPrinter.PLATE_1_X_4);
			break;
		case TILE_FROM_TOP:
			buildWithTiles(out);			
			break;
		case PLATE_FROM_SIDE:
			buildWithPlates(out, 1, LXFPrinter.PLATE_1_X_1);
			break;
		case PLATE_2_1_FROM_SIDE:
			buildWithPlates(out, 2, LXFPrinter.PLATE_1_X_2);
			break;
		case PLATE_3_1_FROM_SIDE:
			buildWithPlates(out, 3, LXFPrinter.PLATE_1_X_3);
			break;
		case PLATE_4_1_FROM_SIDE:
			buildWithPlates(out, 4, LXFPrinter.PLATE_1_X_4);
			break;
		case BRICK_FROM_SIDE:
			buildWidthBricks(out);
			break;
		case TWO_BY_TWO_PLATES_FROM_TOP:
			buildWithTwoByTwoPlates(out);
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
				String part = "3024.DAT";
				if(y%5==0)
					part = "3070B.DAT";
				out.printf("1 %d 0 %d %d 0 0 -1 0 1 0 1 0 0 %s\n", color.getFirstIDLDraw(), 8*y, 20*x, part);
			}

			@Override
			public void addSideways(int id, int x, int y, LEGOColor color) {
				String part = "3024.DAT";
				if(x%5==4)
					part = "3070B.DAT";
				out.printf("1 %d 0 %d %d -1 0 0 0 0 -1 0 -1 0 %s\n", color.getFirstIDLDraw(), 10+20*y, -2+8*x, part);
			}
		};
		
		for(int y = 0; y < size.height/10; y+=blockSize.height) {
			for(int x = 0; x < size.width/10; x+=blockSize.width) {
				tbt.buildLastInstructions(builder, new Rectangle(x,y,blockSize.width, blockSize.height));
			}
			out.println("0 STEP");
		}
	}
	
	private void buildWithStuds(PrintWriter out, int width, String partNumber) {
		build(out, width*20, 20, "0 -1 0 0 0 -1 1 0 0 " + partNumber + ".DAT"); 
	}

	private void buildWithTwoByTwoPlates(PrintWriter out) {
		build(out, 40, 40, "0 -1 0 0 0 -1 1 0 0 3022.DAT"); 
	}

	private void buildWithTiles(PrintWriter out) {
		build(out, 20, 20, "0 -1 0 0 0 -1 1 0 0 3070B.DAT"); 
	}

	private void buildWithPlates(PrintWriter out, int width, String partNumber) {
		build(out, width*20, 8, "0 0 -1 0 1 0 1 0 0 " + partNumber + ".DAT");
	}
	
	private void buildWidthBricks(PrintWriter out) {
		build(out, 20, 24, "0 0 -1 0 1 0 1 0 0 3005.DAT");
	}
	
	private void build(PrintWriter out, int xMult, int yMult, String orientAndDat) {
		LEGOColor[][] instructions = tbt.getMainTransform().lastInstructions();
		int w = instructions.length;
		int h = instructions[0].length;
		for(int by = (h/blockSize.height)*blockSize.height; by >= 0; by -= blockSize.height) {
			for(int bx = 0; bx < w; bx += blockSize.width) {
				int iMax = Math.min(blockSize.width, w-bx);
				int jMax = Math.min(blockSize.height, h-by);
				for(int i = 0; i < iMax; i++) {
					for(int j = 0; j < jMax; j++) {
						int x = bx+i;
						int y = by+j;
						int color = instructions[x][y].getFirstIDLDraw();
						out.printf("1 %d 0 %d %d %s\n", color, yMult*y, xMult*x, orientAndDat);
					}
				}
				out.println("0 STEP");
			}
		}	
	}	
}
