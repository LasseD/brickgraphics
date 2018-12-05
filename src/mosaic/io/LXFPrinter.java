package mosaic.io;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import bricks.*;
import building.Optimizer;
import building.Part;
import mosaic.controllers.*;
import mosaic.ui.*;
import colors.*;
import transforms.*;

/**
 * Class responsible for outputting to LXF file. 
 * This is a very simple LXF "printer".
 * @author LD
 */
public class LXFPrinter {
	public static final String PLATE_1_X_1 = "3024";
	public static final String PLATE_1_X_2 = "3023";
	public static final String PLATE_1_X_3 = "3623";
	public static final String PLATE_1_X_4 = "3710";
	public static final String BRICK_1_X_1 = "3005";
	public static final String TILE_1_X_1 = "3070";
	public static final String PLATE_2_X_2 = "3022";
	public static final String BRICK_2_X_4 = "3001";

	public static final double PLATE_HALF_WIDTH = 0.4;//0000000596046448;
	public static final double PLATE_HEIGHT = 0.32;//1999999284744263;
	//public static final String UP_ORIENTATION = "1,0,0,0,1,0,0,0,1";
	//public static final String SIDEWAYS_ORIENTATION = "0,0.99999994039535522,0,0.99999994039535522,0,0,0,0,-0.99999982118606567";
	public static final String SIDEWAYS_ORIENTATION = "0,1,0,1,0,0,0,0,-1";
	
	private ToBricksTransform tbt;
	private ToBricksType type;
	private Dimension size;
	private boolean optimize;
	
	private LXFPrinter(MainController mc, BrickedView brickedView) {
		tbt = brickedView.getToBricksTransform();
		type = mc.getToBricksController().getToBricksType();
		size = brickedView.getBrickedSize();
		optimize = mc.getOptionsController().getOptimizeUseOfBricksBeforeExporting();
	}
	
	public static void printTo(MainController mc, MainWindow mw, File file) throws IOException {
		BrickedView brickedView = mw.getBrickedView();
		LXFPrinter printer = new LXFPrinter(mc, brickedView);
		
		// File handling:
		String fileName = file.getName();
		String modelName = fileName;
		int index;
		if((index = modelName.indexOf('.')) > 0)
			modelName = modelName.substring(0, index);
		
		// Zip handling:
		FileOutputStream outStream = new FileOutputStream(file, false);
		ZipOutputStream zos = new ZipOutputStream(outStream);

		// XML file:
		{
			PrintWriter pw = new PrintWriter(zos);
			ZipEntry ze = new ZipEntry("IMAGE100.LXFML");
			zos.putNextEntry(ze);
			writeHeaderBlock(pw,  modelName);
			printer.writeDynamicSection(pw, true);		
			writeCenterBlock(pw);
			printer.writeDynamicSection(pw, false);		
			writeFooterBlock(pw);		
			pw.flush();
			zos.flush();
		}
		
		// PNG file: (128x128)
		{
			ZipEntry ze = new ZipEntry("IMAGE100.PNG");
			zos.putNextEntry(ze);
			BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2 = (Graphics2D)image.getGraphics();
			mw.getBrickedView().getToBricksTransform().drawAll(g2, new Dimension(128, 128));
			ImageIO.write(image, "png", zos);
			zos.flush();
		}
		
		// Close down:
		zos.closeEntry();
		zos.close();
	}
	
	private void writeDynamicSection(PrintWriter out, boolean brickSection) {
		switch(type) {
		case STUD_FROM_TOP:
			buildWith1xXPlatesFromTop(out, brickSection, 1, PLATE_1_X_1);
			break;
		case ONE_BY_TWO_STUDS_FROM_TOP:
			buildWith1xXPlatesFromTop(out, brickSection, 2, PLATE_1_X_2);
			break;
		case ONE_BY_THREE_STUDS_FROM_TOP:
			buildWith1xXPlatesFromTop(out, brickSection, 3, PLATE_1_X_3);
			break;
		case ONE_BY_FOUR_STUDS_FROM_TOP:
			buildWith1xXPlatesFromTop(out, brickSection, 4, PLATE_1_X_4);
			break;
		case TILE_FROM_TOP:
			buildWith1x1TilesFromTop(out, brickSection);
			break;
		case PLATE_FROM_SIDE:
			buildWith1xXPlatesFromSide(out, brickSection, 1, PLATE_1_X_1);
			break;
		case PLATE_2_1_FROM_SIDE:
			buildWith1xXPlatesFromSide(out, brickSection, 2, PLATE_1_X_2);
			break;
		case PLATE_3_1_FROM_SIDE:
			buildWith1xXPlatesFromSide(out, brickSection, 3, PLATE_1_X_3);
			break;
		case PLATE_4_1_FROM_SIDE:
			buildWith1xXPlatesFromSide(out, brickSection, 4, PLATE_1_X_4);
			break;
		case BRICK_FROM_SIDE:
			buildWith1x1BricksFromSide(out, brickSection);
			break;
		case BRICK_FROM_TOP:
			buildWith1x1BricksFromTop(out, brickSection);
			break;
		case TWO_BY_TWO_PLATES_FROM_TOP:
			buildWith2xXFromTop(2, out, brickSection, PLATE_2_X_2);
			break;
		case SNOT_IN_2_BY_2:
			buildSnot(out, brickSection);
			break;
		case TWO_BY_FOUR_BRICKS_FROM_TOP:
			buildWith2xXFromTop(4, out, brickSection, BRICK_2_X_4);
			break;
		default: 
			throw new IllegalStateException("Enum broken: " + type);
		}
	}
	
	private void buildSnot(final PrintWriter out, final boolean brickSection) {
		final double HALF_WIDTH = PLATE_HALF_WIDTH*size.width/5;
		
		InstructionsBuilderI builder = new InstructionsBuilderI() {
			@Override
			public void add(int id, int x, int y, LEGOColor color) {
				String part = y%5==0 ? TILE_1_X_1 : PLATE_1_X_1;
				printElement(out, id, part, Part.STUDS_UP_TURN_LDRAW_0, color.getIDLEGO(), PLATE_HALF_WIDTH+2*PLATE_HALF_WIDTH*x-HALF_WIDTH, PLATE_HALF_WIDTH, -PLATE_HEIGHT*y + PLATE_HALF_WIDTH, y%5==0);
			}

			@Override
			public void addSideways(int id, int x, int y, LEGOColor color) {
				String part = x%5==4 ? TILE_1_X_1 : PLATE_1_X_1;
				printElement(out, id, part, SIDEWAYS_ORIENTATION, color.getIDLEGO(), PLATE_HEIGHT*x-HALF_WIDTH, PLATE_HALF_WIDTH, -2*PLATE_HALF_WIDTH*y + PLATE_HEIGHT, x%5==4);
			}
		};
		
		tbt.buildLastInstructions(builder, new Rectangle(0, 0, size.width, size.height));
	}
	
	private void buildWith1xXPlatesFromSide(PrintWriter out, boolean isElementSection, int width, String partNumber) {
		buildFromSide(out, width*PLATE_HALF_WIDTH, width*2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, PLATE_HEIGHT, partNumber, isElementSection); 
	}

	private void buildWith1x1BricksFromSide(PrintWriter out, boolean isElementSection) {
		buildFromSide(out, PLATE_HALF_WIDTH, 2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, 3*PLATE_HEIGHT, BRICK_1_X_1, isElementSection); 
	}

	private void buildWith1xXPlatesFromTop(PrintWriter out, boolean isElementSection, int width, String partNumber) {
		buildFromTop(out, width, 1, partNumber, isElementSection, false); 
	}

	private void buildWith2xXFromTop(int x, PrintWriter out, boolean isElementSection, String partNumber) {
		buildFromTop(out, x, 2, partNumber, isElementSection, false); 
	}

	private void buildWith1x1TilesFromTop(PrintWriter out, boolean isElementSection) {
		buildFromTop(out, 1, 1, TILE_1_X_1, isElementSection, true); 
	}

	private void buildWith1x1BricksFromTop(PrintWriter out, boolean isElementSection) {
		buildFromTop(out, 1, 1, BRICK_1_X_1, isElementSection, true); 
	}

	private void buildFromTop(PrintWriter out, int elementWidth, int elementDepth, String element, 
			boolean isElementSection, boolean decorationSection) {
		if(!optimize) {
			int elementIndex = 0;
			LEGOColorGrid instructions = tbt.getMainTransform().lastInstructions();
			int w = instructions.getWidth();
			int h = instructions.getHeight();
			double multX = elementWidth*2*PLATE_HALF_WIDTH;
			double multY = elementDepth*2*PLATE_HALF_WIDTH;			

			for(int y = 0; y < h; y++) {
				LEGOColor[] row = instructions.getRow(y);
				double yy = PLATE_HALF_WIDTH - h/2*multY + y*multY;
				for(int x = 0; x < w; x++) {
					double xx = PLATE_HALF_WIDTH - w/2*multX + x*multX;
					int color = row[x].getIDLEGO();
					if(isElementSection)
						printElement(out, elementIndex++, element, Part.STUDS_UP_TURN_LDRAW_0, color, xx, yy, 0, decorationSection);
					else
						printRigidSystem(out, elementIndex++, Part.STUDS_UP_TURN_LDRAW_0, xx, yy, 0);
				}
			}			
		}
		else {
			LEGOColorGrid unoptimized = tbt.getMainTransform().lastInstructions();
			int unoptimizedWidth = unoptimized.getWidth(); 
			int width = unoptimizedWidth * elementWidth; 
			int unoptimizedDepth = unoptimized.getHeight();
			int depth = unoptimizedDepth * elementDepth;
			LEGOColor[][][] optimizationGrid = new LEGOColor[1][depth][width];
			for(int y = 0; y < unoptimizedDepth; ++y) {				
				LEGOColor[] row = unoptimized.getRow(y);
				for(int x = 0; x < unoptimizedWidth; ++x) {
					for(int yy = 0; yy < elementDepth; ++yy) {
						for(int xx = 0; xx < elementWidth; ++xx) {
							optimizationGrid[0][y*elementDepth + yy][x*elementWidth + xx] = row[x];
						}
					}
				}
			}
			printOptimized(out, elementWidth, elementDepth, optimizationGrid, isElementSection, decorationSection);
		}
	}

	private static void printOptimized(PrintWriter out, int elementWidth, int elementDepth, LEGOColor[][][] optimizationGrid, boolean isElementSection, boolean decorationSection) {
		int elementIndex = 0;
		double multX = elementWidth*2*PLATE_HALF_WIDTH;
		double multY = elementDepth*2*PLATE_HALF_WIDTH;	
		int depth = optimizationGrid[0].length;
		int width = optimizationGrid[0][0].length;

		List<Part> optimized = new Optimizer(optimizationGrid).placedParts;
		for(Part p : optimized) {
			// ldraw 20 = LDD 0.4:
			double yy = PLATE_HALF_WIDTH- depth/2.0*multY + p.y*multY;// + p.type.getLDrawCenterY()/20.0*0.4;
			double xx = PLATE_HALF_WIDTH- width/2.0*multX + p.x*multX;// + p.type.getLDrawCenterX()/20.0*0.4;
			int color = p.color.getIDLEGO();
			String orient = Part.LDD_STUDS_UP_TURNS[p.type.getTimesTurned90Degrees()];
			if(isElementSection)
				printElement(out, elementIndex++, p.type.getID()+"", orient, color, xx, yy, 0, decorationSection);
			else
				printRigidSystem(out, elementIndex++, orient, xx, yy, 0);				
		}		
	}
	
	private void buildFromSide(PrintWriter out, double startX, double multX, double startY, double multZ, 
			String element, boolean isElementSection) {
		LEGOColorGrid instructions = tbt.getMainTransform().lastInstructions();
		int w = instructions.getWidth();
		int h = instructions.getHeight();
		int i = 0;

		for(int z = 0; z < h; z++) {
			LEGOColor[] row = instructions.getRow(z);
			double zz = -z*multZ;
			for(int x = 0; x < w; x++) {
			double xx = startX - (w/2*multX) + x*multX;
				int color = row[x].getIDLEGO();
				if(isElementSection)
					printElement(out, i++, element, Part.STUDS_UP_TURN_LDRAW_0, color, xx, startY, zz, false);
				else
					printRigidSystem(out, i++, Part.STUDS_UP_TURN_LDRAW_0, xx, startY, zz);
			}
		}
		// TODO: Handle optimized version.
	}	
	
	private static void printElement(PrintWriter out, int id, String element, String orient, int color, double x, double y, double z, boolean decorationSection) {
		out.println("<Brick refID=\"" + id + "\" designID=\"" + element + "\" itemNos=\"\">");
		String c = decorationSection ? color + ",0" : color + "";
		String a = decorationSection ? " decoration=\"0\"" : "";
		out.println("  <Part refID=\"" + id + "\" designID=\"" + element + "\" materials=\"" + c + "\"" + a + ">");
		out.println("    <Bone refID=\"" + id + "\" transformation=\"" + orient + "," + x + "," + z + "," + y + "\">");
		out.println("    </Bone>");
		out.println("  </Part>");
		out.println("</Brick>");		
	}
	
	private static void printRigidSystem(PrintWriter out, int id, String orient, double x, double y, double z) {
		out.println("<RigidSystem>");
		out.println("  <Rigid refID=\"" + id + "\" transformation=\"" + orient + "," + x + "," + z + "," + y + "\" boneRefs=\"" + id + "\"/>");
		out.println("</RigidSystem>");
	}
	
	private static void writeHeaderBlock(PrintWriter pw, String fileNameNoExt) {
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
		pw.println("<LXFML versionMajor=\"5\" versionMinor=\"0\" name=\"" + fileNameNoExt + "\">");
		pw.println("  <Meta>");
		pw.println("    <Application name=\"" + MainController.APP_NAME + "\" versionMajor=\"" + MainController.VERSION_MAJOR + "\" versionMinor=\"" + MainController.VERSION_MINOR + "\"/>");
		pw.println("    <Brand name=\"" + MainController.APP_NAME_SHORT + "\"/>");
		pw.println("    <BrickSet version=\"1564.2\"/>");
		pw.println("  </Meta>");
		pw.println("  <Cameras>");
		pw.println("    <Camera refID=\"0\" fieldOfView=\"80\" distance=\"69.282035827636719\" transformation=\"0.70710688829421997,0,-0.70710688829421997,-0.40824830532073975,0.81649661064147949,-0.40824830532073975,0.57735037803649902,0.57735013961791992,0.57735037803649902,39.999996185302734,39.999992370605469,39.999996185302734\"/>");
		pw.println("  </Cameras>");
		pw.println("  <Bricks cameraRef=\"0\">");
	}
	private static void writeCenterBlock(PrintWriter pw) {
		pw.println("</Bricks>");
		pw.println("<RigidSystems>");
	}
	private static void writeFooterBlock(PrintWriter pw) {
		pw.println("  </RigidSystems>");
		pw.println("  <GroupSystems>");
		pw.println("    <GroupSystem>");
		pw.println("    </GroupSystem>");
		pw.println("  </GroupSystems>");
		pw.println("  <BuildingInstructions>");
		pw.println("  </BuildingInstructions>");
		pw.println("</LXFML>");
	}
}
