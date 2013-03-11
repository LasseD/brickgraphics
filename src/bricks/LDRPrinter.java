package bricks;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.*;

import mosaic.ui.bricked.*;
import colors.*;
import transforms.*;

public class LDRPrinter {
	private ToBricksTransform tbt;
	private ToBricksType type;
	private Dimension blockSize, size;
	
	public LDRPrinter(BrickedView brickedView) {
		tbt = brickedView.getToBricksTransform();
		ToBricksToolBar toolBar = brickedView.getToolBar();
		type = toolBar.getToBricksType();
		Magnifier magnifier = brickedView.getMagnifier();
		blockSize = magnifier.getBlockSize();
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
		case stud:
			buildStud(out);
			break;
		case plate:
			buildPlate(out);
			break;
		case snot:
			buildSnot(out);
			break;
		case brick:
			buildBrick(out);
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
		LDRBuilder builder = new LDRBuilder() {
			public void add(int x, int y, LEGOColor color) {
				String part = "3024.DAT";
				if(y%5==0)
					part = "3070B.DAT";
				out.printf("1 %d 0 %d %d 0 0 -1 0 1 0 1 0 0 %s\n", color.id_LDraw, 8*y, 20*x, part);
			}

			public void addSideways(int x, int y, LEGOColor color) {
				String part = "3024.DAT";
				if(x%5==4)
					part = "3070B.DAT";
				out.printf("1 %d 0 %d %d -1 0 0 0 0 -1 0 -1 0 %s\n", color.id_LDraw, 10+20*y, -2+8*x, part);
			}
		};
		
		for(int y = 0; y < size.height/10; y+=blockSize.height) {
			for(int x = 0; x < size.width/10; x+=blockSize.width) {
				tbt.buildLastInstructions(builder, new Rectangle(x,y,blockSize.width, blockSize.height));
			}
			out.println("0 STEP");
		}
	}
	
	private void buildStud(PrintWriter out) {
		build(out, 20, 0, 20, 0, "0 -1 0 0 0 -1 1 0 0 3024.DAT"); 
	}

	private void buildPlate(PrintWriter out) {
		build(out, 20, 0, 8, 0, "0 0 -1 0 1 0 1 0 0 3024.DAT");
	}
	
	private void buildBrick(PrintWriter out) {
		build(out, 20, 0, 8*3, 0, "0 0 -1 0 1 0 1 0 0 3005.DAT");
	}
	
	private void build(PrintWriter out, 
			int xMult, int xOffset, 
			int yMult, int yOffset, 
			String orientAndDat) {
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
						int color = instructions[x][y].id_LDraw;
						out.printf("1 %d 0 %d %d %s\n", color, yMult*y+yOffset, xMult*x+xOffset, orientAndDat);
					}
				}
				out.println("0 STEP");
			}
		}	
	}	
}
