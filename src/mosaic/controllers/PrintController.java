package mosaic.controllers;

import io.*;

import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import colors.LEGOColor;
import mosaic.controllers.MagnifierController;
import mosaic.io.BrickGraphicsState;
import mosaic.rendering.GrayScaleGraphics2D;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineImageListener;
import mosaic.rendering.PipelineMosaicListener;
import mosaic.ui.*;
import mosaic.ui.dialogs.PrintDialog;
import transforms.ToBricksTransform;
import ui.ProgressDialog;
import icon.*;

/**
 * This class takes care of the printing mechanism
 * @author LD
 */
public class PrintController implements Printable, ModelHandler<BrickGraphicsState>, PipelineImageListener, PipelineMosaicListener {
	private MainController mc;
	private List<ChangeListener> listeners;
	private PageFormat pageFormat;
	private MagnifierController magnifierController;
	private UIController uiController;
	private ColorController colorController;
	private BufferedImage lastPreparedImage;
	private Dimension lastMosaicSize;
	// Model state:
	private boolean coverPageShow, coverPageShowFileName, coverPageShowLegend, showLegend, showPageNumber;
	private float fontSizeMM;
	private int magnifierSizePercentage;
	private String rightCountDisplayText, downCountDisplayText;
	private CoverPagePictureType coverPagePictureType;
	private ShowPosition showPosition;
	private Dimension magnifiersPerPage;
	private PrinterJob printerJob;
	private MainWindow mw;
	private ProgressDialog.ProgressWorker printWorker;
	
	public PrintController(Model<BrickGraphicsState> model, MainController mc, Pipeline pipeline) {
		this.mc = mc;
		magnifierController = mc.getMagnifierController();
		colorController = mc.getColorController();
		uiController = mc.getUIController();
		listeners = new ArrayList<ChangeListener>();		
		printerJob = PrinterJob.getPrinterJob();
		pageFormat = printerJob.defaultPage();
		model.addModelHandler(this);
		handleModelChange(model);
		magnifierController.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				notifyListeners(e);
			}
		});

		pipeline.addPreparedImageListener(this);
		pipeline.addMosaicListener(this);
	}
	
	public void setMainWindow(MainWindow mw) {
		this.mw = mw;
	}
	
	public void print() {
		if(mw == null)
			throw new IllegalStateException();
        printerJob.setPrintable(PrintController.this, pageFormat);				
		if(printerJob.printDialog()) {
			final ProgressDialog progressDialog = new ProgressDialog(mw, "Printing", new ProgressDialog.CancelAction() {
				@Override
				public void cancel() {
					printerJob.cancel();
				}
			});
	    	printWorker = progressDialog.createWorker(new Runnable() {
				@Override
				public void run() {
			    	try {
			    		Log.log("Initiating printing.");
				    	printerJob.print();				    	
			    	}
				    catch(PrinterAbortException e1) {
				    	Log.log("Printing aborted.");
				    }
				    catch (PrinterException e2) {
						String message = "An error ocurred while printing: " + e2.getMessage();
						JOptionPane.showMessageDialog(mw, message, "Error when printing", JOptionPane.ERROR_MESSAGE);
						Log.log(e2);
				    }
			    	finally {
			    		printWorker = null;
			    	}
				}
			});
			printWorker.execute();
		}
	}
	
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		coverPageShow = (Boolean)model.get(BrickGraphicsState.PrintCoverPageShow);
		coverPageShowFileName = (Boolean)model.get(BrickGraphicsState.PrintCoverPageShowFileName);
		coverPageShowLegend = (Boolean)model.get(BrickGraphicsState.PrintCoverPageShowLegend);
		coverPagePictureType = CoverPagePictureType.values()[(Integer)model.get(BrickGraphicsState.PrintCoverPageCoverPictureTypeIndex)];
		showPosition = ShowPosition.values()[(Integer)model.get(BrickGraphicsState.PrintShowPositionIndex)];
		showLegend = (Boolean)model.get(BrickGraphicsState.PrintShowLegend);
		showPageNumber = (Boolean)model.get(BrickGraphicsState.PrintShowPageNumber);
		magnifiersPerPage = (Dimension)model.get(BrickGraphicsState.PrintMagnifiersPerPage);
		fontSizeMM = (Float)model.get(BrickGraphicsState.PrintFontSize);
		magnifierSizePercentage = (Integer)model.get(BrickGraphicsState.PrintMagnifierSizePercentage);
		downCountDisplayText = (String)model.get(BrickGraphicsState.PrintDisplayTextDown);
		rightCountDisplayText = (String)model.get(BrickGraphicsState.PrintDisplayTextRight);
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
	private void notifyListeners(ChangeEvent e) {
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	public void setDownCountDisplayText(String s, Object caller) {
		downCountDisplayText = s;
		notifyListeners(new ChangeEvent(caller));		
	}
	public void setRightCountDisplayText(String s, Object caller) {
		rightCountDisplayText = s;
		notifyListeners(new ChangeEvent(caller));		
	}
	public void setMagnifierSizePercentage(int i, Object caller) {
		magnifierSizePercentage = i;
		notifyListeners(new ChangeEvent(caller));		
	}
	public void setFontSize(float f, Object caller) {
		fontSizeMM = f;
		notifyListeners(new ChangeEvent(caller));		
	}
	public void setCoverPageShow(boolean b, Object caller) {
		coverPageShow = b;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setCoverPageShowFileName(boolean b, Object caller) {
		coverPageShowFileName = b;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setCoverPageShowLegend(boolean b, Object caller) {
		coverPageShowLegend = b;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setShowColors(boolean b, Object caller) {
		uiController.setShowColors(b);
		notifyListeners(new ChangeEvent(caller));
	}
	public void setShowLegend(boolean b, Object caller) {
		showLegend = b;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setShowPageNumber(boolean b, Object caller) {
		showPageNumber = b;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setCoverPagePictureType(CoverPagePictureType c, Object caller) {
		coverPagePictureType = c;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setPageFormat(PageFormat p, Object caller) {
		pageFormat = p;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setShowPosition(ShowPosition s, Object caller) {
		showPosition = s;
		notifyListeners(new ChangeEvent(caller));
	}
	public void setMagnifiersPerPage(Dimension d, Object caller) {
		magnifiersPerPage = d;
		notifyListeners(new ChangeEvent(caller));		
	}	
	public float getFontSize() {
		return fontSizeMM;
	}
	public int getMagnifierSizePercentage() {
		return magnifierSizePercentage;
	}
	public boolean getCoverPageShow() {
		return coverPageShow;
	}
	public boolean getCoverPageShowFileName() {
		return coverPageShowFileName;
	}
	public boolean getCoverPageShowLegend() {
		return coverPageShowLegend;
	}
	public boolean getShowColors() {
		return uiController.showColors();
	}
	public boolean getShowLegend() {
		return showLegend;
	}
	public boolean getShowPageNumber() {
		return showPageNumber;
	}
	public String getDownCountDisplayText() {
		return downCountDisplayText;
	}
	public String getRightCountDisplayText() {
		return rightCountDisplayText;
	}
	public CoverPagePictureType getCoverPagePictureType() {
		return coverPagePictureType;
	}
	public PageFormat getPageFormat() {
		return pageFormat;
	}
	public ShowPosition getShowPosition() {
		return showPosition;
	}
	public Dimension getMagnifiersPerPage() {
		return magnifiersPerPage;		
	}
	public PrinterJob getPrinterJob() {
		return printerJob;
	}
	
	public static Action createPrintAction(final PrintDialog printDialog) {
		Action printAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printDialog.pack();
				printDialog.setVisible(true);
			}
		};

		printAction.putValue(Action.SHORT_DESCRIPTION, "Print the mosaic.");
		printAction.putValue(Action.SMALL_ICON, Icons.get(16, "printer", "PRINT"));
		printAction.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "printer", "PRINT"));
		printAction.putValue(Action.NAME, "Print");
		printAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		printAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Print".indexOf('P'));
		printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

		return printAction;
	}

	private double writeFileName(FontMetrics fm, int xMin, int xMax, int yMin, Graphics2D g2, int fontSizeIn1_72inches) {
		if(!coverPageShowFileName) 
			return 0;

		File f = mc.getFile();
		String s = f == null ? "-" : f.getName();
		Rectangle2D bounds = fm.getStringBounds(s, g2);
		float x = xMin + (float)((xMax-xMin)-bounds.getWidth())/2;
		float y = yMin + fontSizeIn1_72inches*8/10;
		g2.drawString(s, x, y);
		return fontSizeIn1_72inches*1.2; // 2.2 to make a little bit of space above the letters
	}
	
	private int drawbom(Graphics2D g2, int xMin, int xMax, int yMin, int yMax, int fontSizeIn1_72inches) {
		if(mw == null || !coverPageShowLegend)
			return 0;
		
		if(coverPagePictureType != CoverPagePictureType.None) {
			yMax = yMin + (yMax-yMin)/2;
		}
		LEGOColor.CountingLEGOColor[] bom = mw.getBrickedView().getLegendColors();
		
		int rowHeight = fontSizeIn1_72inches*6/5;
		int rows = Math.max(1, (yMax-yMin)/rowHeight);		
		int columns = Math.max(1, (bom.length+rows-1)/rows);
		int columnWidth = (xMax-xMin)/columns;
		int i = 0;
		int textHeight = fontSizeIn1_72inches;

		for(LEGOColor.CountingLEGOColor c : bom) {
			int x = i%columns;
			int y = i/columns;

			g2.setColor(Color.WHITE);
			int xIndent = xMin + x*columnWidth;
			int yIndent = yMin + y*rowHeight;
			g2.fillRect(xIndent, yIndent, columnWidth, rowHeight);
			g2.setColor(c.c.getRGB());
			g2.fillRect(xIndent, yIndent, fontSizeIn1_72inches, fontSizeIn1_72inches);
			g2.setColor(Color.BLACK);
			g2.drawRect(xIndent, yIndent, fontSizeIn1_72inches, fontSizeIn1_72inches);
			
			String identifier = colorController.getNormalIdentifier(c.c);
			if(identifier == null)
				identifier = "";
			else
				identifier = "x " + identifier;
			g2.drawString(c.cnt + identifier, 
					xMin + x*columnWidth + rowHeight, 
					yMin + y*rowHeight + fontSizeIn1_72inches*9/10 - (fontSizeIn1_72inches - textHeight)/2);

			++i;
		}
		return yMax;
	}
		
	private static Rectangle getSizeOfDrawnImage(int xMin, int xMax, int yMin, int yMax, Dimension imageSize) {
		double scale = (xMax-xMin)/imageSize.getWidth();
		if(scale*imageSize.getHeight() > (yMax-yMin)) {
			scale = (yMax-yMin)/imageSize.getHeight();			
		}
		int x = (int)(xMin + ((xMax-xMin)-imageSize.width*scale));
		return new Rectangle(x, yMin, (int)(scale*imageSize.width), (int)(scale*imageSize.height));
	}

	private static void drawImage(Graphics2D g2, int xMin, int xMax, int yMin, int yMax, BufferedImage image) {
		double scale = (xMax-xMin)/(double)image.getWidth();
		if(scale*image.getHeight() > (yMax-yMin)) {
			scale = (yMax-yMin)/(double)image.getHeight();			
		}
		AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
		int x = (int)(xMin + ((xMax-xMin)-image.getWidth()*scale));
		g2.translate(x, yMin);
		g2.drawImage(image, at, null);
		g2.translate(-x, -yMin);		
	}
	
	private void drawCoverPicture(Graphics2D g2, PageFormat pf, FontMetrics fm, int xMin, int xMax, int yMin, int yMax) {
		if(coverPagePictureType == CoverPagePictureType.Both) {
			BufferedImage left = lastPreparedImage;
			drawImage(g2, xMin, xMin + (xMax-xMin)*9/20, yMin, yMax, left);
			
			int startX = xMin + (xMax-xMin)*11/20;
			Rectangle drawRect = getSizeOfDrawnImage(startX, xMax, yMin, yMax, lastMosaicSize);
			g2.translate(drawRect.x, drawRect.y);
			mw.getBrickedView().getToBricksTransform().drawAll(g2, new Dimension(drawRect.width, drawRect.height));
			g2.translate(-drawRect.x, -drawRect.y);
		}
		else if(coverPagePictureType == CoverPagePictureType.Original) {
			drawImage(g2, xMin, xMax, yMin, yMax, lastPreparedImage);
		}
		else if(coverPagePictureType == CoverPagePictureType.Mosaic) {
			Rectangle drawRect = getSizeOfDrawnImage(xMin, xMax, yMin, yMax, lastMosaicSize);
			g2.translate(xMin, yMin);
			mw.getBrickedView().getToBricksTransform().drawAll(g2, new Dimension(drawRect.width, drawRect.height));
			g2.translate(-xMin, -yMin);
		}
		else if(coverPagePictureType == CoverPagePictureType.Overview) {
			//g2.translate(xMin, yMin);
			//g2.translate(-xMin, -yMin);
			
			Dimension coreImage = magnifierController.getCoreImageSizeInCoreUnits();
			Dimension magnifierSizeInCoreUnits = magnifierController.getSizeInUnits();

			Dimension oldMagnifiersPerPage = magnifiersPerPage;
			magnifiersPerPage = new Dimension(coreImage.width / magnifierSizeInCoreUnits.width, 
											  coreImage.height / magnifierSizeInCoreUnits.height);
			int pageSizeInCoreUnitsW = magnifierSizeInCoreUnits.width * magnifiersPerPage.width;
			int pageSizeInCoreUnitsH = magnifierSizeInCoreUnits.height * magnifiersPerPage.height;
			
			drawMagnifier(g2, 0, 1, 1, xMin, xMax, yMin, yMax, pageSizeInCoreUnitsW, pageSizeInCoreUnitsH, pf, fm, null);		
			magnifiersPerPage = oldMagnifiersPerPage;
		}
	}
	
	private void printCoverPage(Graphics2D g2, PageFormat pf) {
	    // Find bounds:
		final int xMin = (int)pf.getImageableX();
		final int xMax = (int)(pf.getImageableWidth() + pf.getImageableX());
		int yMin = (int)pf.getImageableY();
		int yMax = (int)(pf.getImageableHeight() + pf.getImageableY());
		final int fontSizeIn1_72inches = getFontSizeIn1_72inches(pf);
		
		// Compute font:
		Font font = new Font("SansSerif", Font.PLAIN, fontSizeIn1_72inches);
		g2.setFont(font);
		g2.setColor(Color.BLACK);
		FontMetrics fm = g2.getFontMetrics(font);

		// File name:
		yMin += writeFileName(fm, xMin, xMax, yMin, g2, fontSizeIn1_72inches);
		
		// BOM:
		yMin = drawbom(g2, xMin, xMax, yMin, yMax, fontSizeIn1_72inches);
		
		// Cover picture:
		drawCoverPicture(g2, pf, fm, xMin, xMax, yMin, yMax);
	}
	
	private int getFontSizeIn1_72inches(PageFormat pf) {
	    double heightInInches = pf.getImageableHeight()/72;
	    double heightInMM = heightInInches * 25.4;
		return (int)(pf.getImageableHeight() * fontSizeMM / heightInMM);		
	}
	
	/*
	 * Return how much to decrease xMax.
	 */
	private double writePageNumber(int page, int numberOfPages, FontMetrics fm, int xMin, int xMax, int yMax, Graphics2D g2, int fontSizeIn1_72inches) {
		if(!showPageNumber) 
			return 0;
						
		String pageNumberString = (page+1) + " / " + numberOfPages;
		Rectangle2D pageNumberStringBounds = fm.getStringBounds(pageNumberString, g2);
		float x = xMin + (float)((xMax-xMin)-pageNumberStringBounds.getWidth())/2;
		float y = yMax - fontSizeIn1_72inches/20;
		g2.drawString(pageNumberString, x, y);
		return fontSizeIn1_72inches*1.1; // 2.2 to make a little bit of space above the letters
	}
	
	private static void drawHorizontalArrow(Graphics2D g2, int leftX, int y, int rightX, int arrowSize) {
		g2.drawLine(leftX, y, rightX, y);
		g2.drawLine(leftX,  y, leftX+arrowSize, y+arrowSize);
		g2.drawLine(leftX,  y, leftX+arrowSize, y-arrowSize);
		g2.drawLine(rightX,  y, rightX-arrowSize, y+arrowSize);
		g2.drawLine(rightX,  y, rightX-arrowSize, y-arrowSize);
	}
	
	private static void drawVerticalArrow(Graphics2D g2, int x, int topY, int bottomY, int arrowSize) {
		g2.drawLine(x, topY, x, bottomY);
		g2.drawLine(x, topY, x+arrowSize, topY+arrowSize);
		g2.drawLine(x, topY, x-arrowSize, topY+arrowSize);
		g2.drawLine(x, bottomY, x+arrowSize, bottomY-arrowSize);
		g2.drawLine(x, bottomY, x-arrowSize, bottomY-arrowSize);
	}
	
	public double drawShownPositionForMagnifier(Graphics2D g2, int page, int xMax, int yMax) {
		int unit = 20;
		int yMin = 6;

		if(showPosition == ShowPosition.Written) {
			g2.setFont(new Font("Arial", Font.BOLD, 40));
			unit = 45;
			yMin = 10;
		}
		FontMetrics fm = g2.getFontMetrics();

		Dimension coreImageSizeInCoreUnits = magnifierController.getCoreImageSizeInCoreUnits();
		if(coreImageSizeInCoreUnits == null)
			return 0; // Not ready.
		final int coreImageInCoreUnitsW = coreImageSizeInCoreUnits.width;
		final int coreImageInCoreUnitsH = coreImageSizeInCoreUnits.height;
		final Dimension magnifierSizeInCoreUnits = magnifierController.getSizeInUnits();
		final int pageSizeInCoreUnitsW = magnifierSizeInCoreUnits.width;
		final int pageSizeInCoreUnitsH = magnifierSizeInCoreUnits.height;

		final int numPagesWidth = (coreImageInCoreUnitsW+pageSizeInCoreUnitsW-1) / pageSizeInCoreUnitsW;
		final int numPagesHeight = (coreImageInCoreUnitsH+pageSizeInCoreUnitsH-1) / pageSizeInCoreUnitsH;		
		
		return drawShowPosition(page, numPagesWidth, numPagesHeight, fm, 
				0, xMax, yMin, yMax, g2, unit, coreImageSizeInCoreUnits, magnifierSizeInCoreUnits);
	}
	
	private double drawShowPosition(int page, int numPagesWidth, int numPagesHeight, FontMetrics fm, 
			int xMin, int xMax, int yMin, int yMax, 
			Graphics2D g2, int unit, Dimension coreImageInCoreUnits, Dimension pageSizeInCoreUnits) {
		// Nothing:
		if(showPosition == ShowPosition.None)
			return 0;

		int fromLeft = (page % numPagesWidth)+1;
		int fromTop = (page / numPagesWidth)+1;

		// Written:
		if(showPosition == ShowPosition.Written || showPosition == ShowPosition.TextAndBox) {
			String pageNumberString = fromLeft + rightCountDisplayText + fromTop + downCountDisplayText;
			Rectangle2D pageNumberStringBounds = fm.getStringBounds(pageNumberString, g2);
			float x = xMin + (float)((xMax-xMin)-pageNumberStringBounds.getWidth())/2;
			float y = yMin + unit;
			g2.drawString(pageNumberString, x, y);
			if(showPosition == ShowPosition.Written)
				return unit*1.2; // 1.2 to make a little bit of space above the letters
			yMin += unit*2;
		}
		
		// Boxes:
		int xMid = (xMax+xMin)/2;
		int arrowSize = unit/5;			

		String leftText = ""+(fromLeft-1);
		Rectangle2D leftTextStringBounds = fm.getStringBounds(leftText, g2);
		String rightText = ""+(numPagesWidth-fromLeft);
		Rectangle2D rightTextStringBounds = fm.getStringBounds(rightText, g2);
		String topText = ""+(fromTop-1);
		String bottomText = ""+(numPagesHeight-fromTop);
		
		if(showPosition == ShowPosition.MiddleBox) { // Show box in middle: 3x1, 2 on top/bottom, 3 on left/right. Unit is fontSizeIn1_72inches
			int outerWidth = 9*unit;
			int outerHeight = 5*unit;
			int leftX = xMid-outerWidth/2;
			int rightX = leftX+outerWidth;
			g2.drawRect(leftX, yMin, outerWidth, outerHeight); // Outer box
			g2.drawRect(xMid-unit, yMin+2*unit, 2*unit, unit); // Inner box
			int horizontalArrowY = yMin+5*unit/2-arrowSize;
			drawHorizontalArrow(g2, leftX, horizontalArrowY, xMid-unit, arrowSize); // left arrow
			drawHorizontalArrow(g2, xMid+unit, horizontalArrowY, rightX, arrowSize); // right arrow
			drawVerticalArrow(g2, xMid, yMin, yMin+2*unit, arrowSize);
			drawVerticalArrow(g2, xMid, yMin+3*unit, yMin+outerHeight, arrowSize);
			g2.drawString(topText, xMid + arrowSize, yMin + 3*unit/2);
			g2.drawString(bottomText, xMid + arrowSize, yMin + 9*unit/2);
			g2.drawString(leftText, xMid - 4*unit + (int)(3*unit-leftTextStringBounds.getWidth())/2, horizontalArrowY + arrowSize + unit);
			g2.drawString(rightText, xMid + unit + (int)(3*unit-rightTextStringBounds.getWidth())/2, horizontalArrowY + arrowSize + unit);
			return outerHeight + unit/5;
		}
		else { // "Smart" box
			int outerHeight = Math.max(5*unit, (yMax-yMin)/5);
			int outerBoxHeight = outerHeight - unit - 2*arrowSize;
			int outerBoxWidth = (outerBoxHeight * coreImageInCoreUnits.width) / coreImageInCoreUnits.height;
			if(outerBoxWidth > 0.8 * (xMax-xMin)) {
				outerBoxWidth = (int)(0.8 * (xMax-xMin));
				outerBoxHeight = (outerBoxWidth * coreImageInCoreUnits.height) / coreImageInCoreUnits.width;
				outerHeight = outerBoxHeight + unit + 2*arrowSize;
			}
			double innerBoxHeight = outerBoxHeight/(double)numPagesHeight;
			double innerBoxWidth = outerBoxWidth/(double)numPagesWidth;
			// boxes and lines:
			int outerBoxX = xMid - outerBoxWidth/2;
			
			// Draw gray picture:
			int grayWidth = (int)Math.round(outerBoxWidth * coreImageInCoreUnits.width/((double)pageSizeInCoreUnits.width*numPagesWidth));
			int grayHeight = (int)Math.round(outerBoxHeight * coreImageInCoreUnits.height/((double)pageSizeInCoreUnits.height*numPagesHeight));
			g2.translate(outerBoxX, yMin);
			mw.getBrickedView().getToBricksTransform().drawAll(new GrayScaleGraphics2D(g2), new Dimension(grayWidth, grayHeight));
			g2.translate(-outerBoxX, -yMin);
			
			g2.drawRect(outerBoxX, yMin, outerBoxWidth, outerBoxHeight);
			int xLeft = outerBoxX + (int)((fromLeft-1)*innerBoxWidth);
			int xRight = outerBoxX + (int)(fromLeft*innerBoxWidth);
			int yTop = yMin + (int)((fromTop-1)*innerBoxHeight);
			int yBottom = yMin + (int)(fromTop*innerBoxHeight);
			g2.setColor(Color.RED);
			g2.fillRect(xLeft, yTop, (int)innerBoxWidth+1, (int)innerBoxHeight+1);
			g2.setColor(Color.BLACK);
			
			if(showPosition == ShowPosition.TextAndBox) {
				g2.drawRect(xLeft, yTop, (int)innerBoxWidth+1, (int)innerBoxHeight+1);
				return outerHeight + 2.2*unit;
			}
			g2.drawLine(outerBoxX, yTop, outerBoxX+outerBoxWidth, yTop);
			g2.drawLine(outerBoxX, yBottom, outerBoxX+outerBoxWidth, yBottom);
			g2.drawLine(xLeft, yMin, xLeft, yMin+outerBoxHeight);
			g2.drawLine(xRight, yMin, xRight, yMin+outerBoxHeight);
			// Arrows & text:
			if(fromLeft > 1) {
				drawHorizontalArrow(g2, outerBoxX, yMin+outerBoxHeight+arrowSize, xLeft, arrowSize);
				g2.drawString(leftText, outerBoxX + (int)(xLeft-outerBoxX-leftTextStringBounds.getWidth())/2, yMin+outerHeight-unit/10);				
			}
			if(numPagesWidth-fromLeft> 0) {
				drawHorizontalArrow(g2, xRight, yMin+outerBoxHeight+arrowSize, outerBoxX + outerBoxWidth, arrowSize);
				g2.drawString(rightText, xRight + (int)(outerBoxX+outerBoxWidth-xRight-rightTextStringBounds.getWidth())/2, yMin+outerHeight-unit/10);				
			}
			int rightArrowX = outerBoxX + outerBoxWidth + arrowSize;
			if(fromTop > 1) {
				drawVerticalArrow(g2, rightArrowX, yMin, yTop, arrowSize);
				g2.drawString(topText, rightArrowX + arrowSize, yMin + (yTop-yMin+unit)/2);				
			}
			if(numPagesHeight-fromTop > 0) {
				drawVerticalArrow(g2, rightArrowX, yBottom, yMin + outerBoxHeight, arrowSize);
				g2.drawString(bottomText, rightArrowX + arrowSize, yBottom + (yMin+outerBoxHeight-yBottom+unit)/2);				
			}
			
			return outerHeight + unit/5;
		}
	}
	
	private int drawMagnifier(Graphics2D g2, int page, int numPagesWidth, int numPagesHeight, int xMin, int xMax, int yMin, int yMax, int pageSizeInCoreUnitsW, int pageSizeInCoreUnitsH, PageFormat pf, FontMetrics fm, Set<LEGOColor.CountingLEGOColor> used) {
		// Find out how big each page is (compared to full image):
		Dimension shownMagnifierSize = new Dimension((int)pf.getImageableWidth(), (int)((pf.getImageableWidth() * pageSizeInCoreUnitsH) / pageSizeInCoreUnitsW));
		int indentX = xMin;
		int indentY = yMax - shownMagnifierSize.height;
		if(shownMagnifierSize.height > (yMax-yMin)*magnifierSizePercentage/100) {
			shownMagnifierSize.height = (yMax-yMin)*magnifierSizePercentage/100;
			shownMagnifierSize.width = (shownMagnifierSize.height * pageSizeInCoreUnitsW) / pageSizeInCoreUnitsH;
			indentX = (xMax+xMin-shownMagnifierSize.width)/2;
			indentY = yMax - shownMagnifierSize.height;
		}
		Dimension smallMagnifierSize;
		if(shownMagnifierSize.width == 1 && shownMagnifierSize.height == 1) {
			smallMagnifierSize = shownMagnifierSize;
		}
		else {
			smallMagnifierSize = new Dimension((int)(shownMagnifierSize.width / magnifiersPerPage.width*0.95), 
											   (int)(shownMagnifierSize.height / magnifiersPerPage.height*0.95));
		}

		// draw magnified:
		g2.translate(indentX, indentY);
		ToBricksTransform tbTransform = magnifierController.getTBTransform();

		Rectangle basicUnitRect = magnifierController.getCoreRect();
		//int smallPage = 1;
		for(int y = 0; y < magnifiersPerPage.height; ++y) {
			int yIndent = y*shownMagnifierSize.height/magnifiersPerPage.height;
			g2.translate(0, yIndent);

			for(int x = 0; x < magnifiersPerPage.width; ++x) {
				int xIndent = x*shownMagnifierSize.width/magnifiersPerPage.width;
				
				basicUnitRect.x = ((page % numPagesWidth)*magnifiersPerPage.width + x)*basicUnitRect.width;
				basicUnitRect.y = (/*numPagesHeight-1-*/ (page / numPagesWidth) * magnifiersPerPage.height + y)*basicUnitRect.height; // Add numPagesHeight-1- in first parenthesis to start from bottom.

				g2.translate(xIndent, 0);
				LEGOColor.CountingLEGOColor[] m = tbTransform.draw(g2, basicUnitRect, smallMagnifierSize, uiController.showColors(), used != null); // TODO set last parameter false for an overview on each magnifier
				
				
				
				/*
				
				String ss = "" + smallPage++;
				Rectangle2D stringBounds = fm.getStringBounds(ss, g2);
				Rectangle2D stringBounds3 = fm.getStringBounds("abc", g2);
				int boxWidth = (int)(1.3*stringBounds3.getWidth());
				int boxHeight = (int)(1.1*stringBounds.getHeight());
				//g2.drawString(leftText, xMid - 4*unit + (int)(3*unit-leftTextStringBounds.getWidth())/2, horizontalArrowY + arrowSize + unit);
				int ww = smallMagnifierSize.width/2;
				int hh = smallMagnifierSize.height/2;
				int xx = ww - boxWidth/2;
				int yy = hh - boxHeight/2;
				g2.setColor(Color.WHITE);
				g2.fillRect(xx, yy, boxWidth, boxHeight);
				g2.setColor(Color.BLACK);
				g2.drawRect(xx, yy, boxWidth, boxHeight);
				g2.drawString(""+ss, (int)(ww-stringBounds.getWidth()/2), (int)(hh+stringBounds.getHeight()*0.38));//*/
				
				
				
				
				
				
				
				g2.translate(-xIndent, 0);
				if(used != null) {
					for(int i = 0; i < m.length; ++i)
						used.add(m[i]);					
				}
			}
			g2.translate(0, -yIndent);
		}
		
		g2.translate(-indentX, -indentY);		
		return indentY;
	}
	
	private void drawLegend(Graphics2D g2, int xMin, int xMax, int yMin, int yMax, int fontSizeIn1_72inches, Set<LEGOColor.CountingLEGOColor> used) {
		if(!showLegend)
			return;
		int rowHeight = fontSizeIn1_72inches*6/5;
		int rows = Math.max(1, (yMax-yMin)/rowHeight);
		int columns = Math.max(1, (used.size()+rows-1)/rows);
		int columnWidth = Math.max(1, (xMax-xMin)/columns);
		int i = 0;
		
		int textHeight = fontSizeIn1_72inches;

		for(LEGOColor.CountingLEGOColor cc : used) {
			LEGOColor c = cc.c;
			int x = i%columns;
			int y = i/columns;

			g2.setColor(Color.WHITE);
			int xIndent = xMin + x*columnWidth;
			int yIndent = yMin + y*rowHeight;
			g2.fillRect(xIndent, yIndent, columnWidth, rowHeight);
			g2.setColor(c.getRGB());
			g2.fillRect(xIndent, yIndent, fontSizeIn1_72inches, fontSizeIn1_72inches);
			g2.setColor(Color.BLACK);
			g2.drawRect(xIndent, yIndent, fontSizeIn1_72inches, fontSizeIn1_72inches);
			String id = colorController.getNormalIdentifier(c);
			if(id != null)
				g2.drawString(id, 
					xMin + x*columnWidth + rowHeight, 
					yMin + y*rowHeight + fontSizeIn1_72inches*9/10 - (fontSizeIn1_72inches - textHeight)/2);
			++i;
		}
	}
	
	public int getNumberOfPages() {
		// Find out how big the magnifier is:
		Dimension coreImage = magnifierController.getCoreImageSizeInCoreUnits();
		if(coreImage == null)
			return 0; // Not ready.
		final int coreImageInCoreUnitsW = coreImage.width;
		final int coreImageInCoreUnitsH = coreImage.height;
		final Dimension magnifierSizeInCoreUnits = magnifierController.getSizeInUnits();
		final int pageSizeInCoreUnitsW = magnifierSizeInCoreUnits.width * magnifiersPerPage.width;
		final int pageSizeInCoreUnitsH = magnifierSizeInCoreUnits.height * magnifiersPerPage.height;

		final int numPagesWidth = (coreImageInCoreUnitsW+pageSizeInCoreUnitsW-1) / pageSizeInCoreUnitsW;
		final int numPagesHeight = (coreImageInCoreUnitsH+pageSizeInCoreUnitsH-1) / pageSizeInCoreUnitsH;		
		return numPagesWidth*numPagesHeight; 		
	}
	
	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
		Graphics2D g2 = (Graphics2D)g;
		
		// Find out how big the magnifier is:
		final int coreImageInCoreUnitsW = magnifierController.getCoreImageSizeInCoreUnits().width;
		final int coreImageInCoreUnitsH = magnifierController.getCoreImageSizeInCoreUnits().height;
		final Dimension coreImageInCoreUnits = new Dimension(coreImageInCoreUnitsW, coreImageInCoreUnitsH);
		final Dimension magnifierSizeInCoreUnits = magnifierController.getSizeInUnits();
		final int pageSizeInCoreUnitsW = magnifierSizeInCoreUnits.width * magnifiersPerPage.width;
		final int pageSizeInCoreUnitsH = magnifierSizeInCoreUnits.height * magnifiersPerPage.height;
		final Dimension pageSizeInCoreUnits = new Dimension(pageSizeInCoreUnitsW, pageSizeInCoreUnitsH);

		final int numPagesWidth = (coreImageInCoreUnitsW+pageSizeInCoreUnitsW-1) / pageSizeInCoreUnitsW;
		final int numPagesHeight = (coreImageInCoreUnitsH+pageSizeInCoreUnitsH-1) / pageSizeInCoreUnitsH;		
		final int numberOfPages = numPagesWidth*numPagesHeight; 
		
		if(printWorker != null) {
			int numPages = getNumberOfPages() + (getCoverPageShow() ? 1 : 0);
			String text = "Rendering page " + page + " of " + (numberOfPages + (coverPageShow ? 1 : 0)) + ".";
			if(numPages == page)
				text = "Saving file. Please wait...";
			printWorker.setProgressAndText(page*100/numPages, text);
		}
		
		// Special case: Cover page:
		if(page == 0 && coverPageShow) {
			printCoverPage(g2, pf);
			return PAGE_EXISTS;
		}
		if(coverPageShow)
			--page;
		
	    if (page >= numberOfPages) {
	    	return NO_SUCH_PAGE;
	    }
	    
	    // Find bounds:
		final int xMin = (int)pf.getImageableX();
		final int xMax = (int)(pf.getImageableWidth() + pf.getImageableX());
		int yMin = (int)pf.getImageableY();
		int yMax = (int)(pf.getImageableHeight() + pf.getImageableY());
		final int fontSizeIn1_72inches = getFontSizeIn1_72inches(pf);
		
		// Compute font:
		Font font = new Font("SansSerif", Font.PLAIN, fontSizeIn1_72inches);
		g2.setFont(font);
		g2.setColor(Color.BLACK);
		FontMetrics fm = g2.getFontMetrics(font);

		// Page number:
		yMax -= writePageNumber(page, numberOfPages, fm, xMin, xMax, yMax, g2, fontSizeIn1_72inches);
		
		// Position:
		yMin += drawShowPosition(page, numPagesWidth, numPagesHeight, fm, xMin, xMax, yMin, yMax, g2, fontSizeIn1_72inches, coreImageInCoreUnits, pageSizeInCoreUnits);
		
		// magnifier:
		Set<LEGOColor.CountingLEGOColor> used = new TreeSet<LEGOColor.CountingLEGOColor>();
		yMax = drawMagnifier(g2, page, numPagesWidth, numPagesHeight, xMin, xMax, yMin, yMax, pageSizeInCoreUnitsW, pageSizeInCoreUnitsH, pf, fm, used);
		
		// Legend:
		g2.setFont(font);
		drawLegend(g2, xMin, xMax, yMin, yMax, fontSizeIn1_72inches, used);
				
	    return PAGE_EXISTS;
	}
	
	public static enum CoverPagePictureType {
		Original, Mosaic, Both, None, Overview;
	}
	public static enum ShowPosition {
		MiddleBox("Box with placement written inside"),
		SmartBox("Box with placement written outside"),
		Written("Text only"),
		TextAndBox("Text and box with location"),
		None("None");
		public String title;
		private ShowPosition(String title) {
			this.title = title;
		}		
	}

	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrintCoverPageShow, coverPageShow);
		model.set(BrickGraphicsState.PrintCoverPageShowFileName, coverPageShowFileName);
		model.set(BrickGraphicsState.PrintCoverPageShowLegend, coverPageShowLegend);
		model.set(BrickGraphicsState.PrintCoverPageCoverPictureTypeIndex, coverPagePictureType.ordinal());
		model.set(BrickGraphicsState.PrintShowPositionIndex, showPosition.ordinal());
		model.set(BrickGraphicsState.PrintShowLegend, showLegend);
		model.set(BrickGraphicsState.PrintShowPageNumber, showPageNumber);
		model.set(BrickGraphicsState.PrintMagnifiersPerPage, magnifiersPerPage);
		model.set(BrickGraphicsState.PrintFontSize, fontSizeMM);
		model.set(BrickGraphicsState.PrintMagnifierSizePercentage, magnifierSizePercentage);
		model.set(BrickGraphicsState.PrintDisplayTextDown, downCountDisplayText);
		model.set(BrickGraphicsState.PrintDisplayTextRight, rightCountDisplayText);		
	}

	@Override
	public void imageChanged(BufferedImage image) {
		lastPreparedImage = image;
	}

	@Override
	public void mosaicChanged(Dimension mosaicImageSize) {
		lastMosaicSize = mosaicImageSize;
	}
}
