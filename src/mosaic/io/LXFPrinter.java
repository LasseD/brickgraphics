package mosaic.io;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import bricks.*;
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
	public static final double PLATE_HALF_WIDTH = 0.40000000596046448;
	public static final double PLATE_HEIGHT = 0.31999999284744263;
	public static final String UP_ORIENTATION = "1,0,0,0,1,0,0,0,1";
	public static final String SIDEWAYS_ORIENTATION = "0,0.99999994039535522,0,0.99999994039535522,0,0,0,0,-0.99999982118606567";
	
	private ToBricksTransform tbt;
	private ToBricksType type;
	private Dimension size;
	
	private LXFPrinter(MainController mc, BrickedView brickedView) {
		tbt = brickedView.getToBricksTransform();
		type = mc.getToBricksController().getToBricksType();
		size = brickedView.getBrickedSize();
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
			printer.writeDynanmicSection(pw, true);		
			writeCenterBlock(pw);
			printer.writeDynanmicSection(pw, false);		
			writeFooterBlock(pw);		
			pw.flush();
			zos.flush();
		}
		
		// PNG file: (128x128)
		{
			ZipEntry ze = new ZipEntry("IMAGE100.PNG");
			zos.putNextEntry(ze);
			BufferedImage image = mw.getFinalImage();
			ScaleTransform t = new ScaleTransform(ScaleTransform.Type.bounded, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, 0);
			t.setHeight(128);
			t.setWidth(128);
			ImageIO.write(t.transformUnbuffered(image), "png", zos);
			zos.flush();			
		}
		
		// Close down:
		zos.closeEntry();
		zos.close();
	}
	
	private void writeDynanmicSection(PrintWriter out, boolean brickSection) {
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
		case TWO_BY_TWO_PLATES_FROM_TOP:
			buildWith2x2PlatesFromTop(out, brickSection);
			break;
		case SNOT_IN_2_BY_2:
			buildSnot(out, brickSection);
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
				printElement(out, id, part, UP_ORIENTATION, color.getIDLEGO(), PLATE_HALF_WIDTH+2*PLATE_HALF_WIDTH*x-HALF_WIDTH, PLATE_HALF_WIDTH, -PLATE_HEIGHT*y + PLATE_HALF_WIDTH, y%5==0);
			}

			@Override
			public void addSideways(int id, int x, int y, LEGOColor color) {
				String part = x%5==4 ? TILE_1_X_1 : PLATE_1_X_1;
				printElement(out, id, part, SIDEWAYS_ORIENTATION, color.getIDLEGO(), PLATE_HEIGHT*x-HALF_WIDTH, PLATE_HALF_WIDTH, -2*PLATE_HALF_WIDTH*y + PLATE_HEIGHT, x%5==4);
			}
		};
		
		tbt.buildLastInstructions(builder, new Rectangle(0, 0, size.width, size.height));
	}
	
	private void buildWith1xXPlatesFromTop(PrintWriter out, boolean isElementSection, int width, String partNumber) {
		buildFromTop(out, width*PLATE_HALF_WIDTH, width*2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, 2*PLATE_HALF_WIDTH, UP_ORIENTATION, partNumber, isElementSection, false); 
	}

	private void buildWith1xXPlatesFromSide(PrintWriter out, boolean isElementSection, int width, String partNumber) {
		buildFromSide(out, width*PLATE_HALF_WIDTH, width*2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, PLATE_HEIGHT, UP_ORIENTATION, partNumber, isElementSection); 
	}

	private void buildWith1x1BricksFromSide(PrintWriter out, boolean isElementSection) {
		buildFromSide(out, PLATE_HALF_WIDTH, 2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, 3*PLATE_HEIGHT, UP_ORIENTATION, BRICK_1_X_1, isElementSection); 
	}

	private void buildWith2x2PlatesFromTop(PrintWriter out, boolean isElementSection) {
		buildFromTop(out, PLATE_HALF_WIDTH, 4*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, 4*PLATE_HALF_WIDTH, UP_ORIENTATION, PLATE_2_X_2, isElementSection, false); 
	}

	private void buildWith1x1TilesFromTop(PrintWriter out, boolean isElementSection) {
		buildFromTop(out, PLATE_HALF_WIDTH, 2*PLATE_HALF_WIDTH, PLATE_HALF_WIDTH, 2*PLATE_HALF_WIDTH, UP_ORIENTATION, TILE_1_X_1, isElementSection, true); 
	}

	private void buildFromTop(PrintWriter out, double startX, double multX, double startY, double multY, String orient, String element, 
			boolean isElementSection, boolean decorationSection) {
		LEGOColor[][] instructions = tbt.getMainTransform().lastInstructions();
		int w = instructions.length;
		int h = instructions[0].length;
		int i = 0;

		for(int x = 0; x < w; x++) {
			double xx = startX - (w/2*multX) + x*multX;
			for(int y = 0; y < h; y++) {
				int color = instructions[x][y].getIDLEGO();
				double yy = startY - (h/2)*multY + y*multY;
				if(isElementSection)
					printElement(out, i++, element, orient, color, xx, yy, 0, decorationSection);
				else
					printRigidSystem(out, i++, orient, xx, yy, 0);
			}
		}
	}	
	
	private void buildFromSide(PrintWriter out, double startX, double multX, double startY, double multZ, 
			String orient, String element, boolean isElementSection) {
		LEGOColor[][] instructions = tbt.getMainTransform().lastInstructions();
		int w = instructions.length;
		int h = instructions[0].length;
		int i = 0;

		for(int x = 0; x < w; x++) {
			double xx = startX - (w/2*multX) + x*multX;
			for(int z = 0; z < h; z++) {
				int color = instructions[x][z].getIDLEGO();
				double zz = -z*multZ;
				if(isElementSection)
					printElement(out, i++, element, orient, color, xx, startY, zz, false);
				else
					printRigidSystem(out, i++, orient, xx, startY, zz);
			}
		}
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
