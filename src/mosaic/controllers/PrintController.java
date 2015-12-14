package mosaic.controllers;

import io.*;

import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import bricks.ToBricksType;
import colors.LEGOColor;
import mosaic.controllers.MagnifierController;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.*;
import transforms.ToBricksTransform;
import icon.*;

/**
 * This class takes care of the printing mechanism
 * @author ld
 */
public class PrintController implements Printable, ModelHandler<BrickGraphicsState> {
	private MainController mc;
	private List<ChangeListener> listeners;
	private PrintDialog printDialog;
	private PageFormat pageFormat;
	private MagnifierController magnifierController;
	private UIController uiController;
	private ColorController colorController;
	// Model state:
	private boolean coverPageShow, coverPageShowFileName, coverPageShowLegend, showLegend, showPageNumber;
	private float fontSizeMM;
	private CoverPagePictureType coverPagePictureType;
	private ShowPosition showPosition;
	private Dimension magnifiersPerPage;
	private PrinterJob printerJob;
	private MainWindow mw;
	
	public PrintController(Model<BrickGraphicsState> model, MainController mc, MainWindow mw) {
		this.mc = mc;
		this.mw = mw;
		magnifierController = mc.getMagnifierController();
		colorController = mc.getColorController();
		uiController = mc.getUIController();
		listeners = new ArrayList<ChangeListener>();		
		printerJob = PrinterJob.getPrinterJob();
		pageFormat = printerJob.defaultPage();
		model.addModelHandler(this);
		handleModelChange(model);

		printDialog = new PrintDialog(mw, this);
	}
	
	public void print() {
        printerJob.setPrintable(PrintController.this, pageFormat);				
		if(printerJob.printDialog()) {
		    try {
		    	printerJob.print();
		    } 
		    catch (PrinterException e2) {
				String message = "An error ocurred while printing: " + e2.getMessage();
				JOptionPane.showMessageDialog(mw, message, "Error when printing", JOptionPane.ERROR_MESSAGE);
				Log.log(e2);
		    }
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
	}
	
	public void addListener(ChangeListener l) {
		listeners.add(l);
	}
	
	private void notifyListeners(ChangeEvent e) {
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
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
	public boolean getCoverPageShow() {
		return coverPageShow;
	}
	public boolean getCoverPageShowFileName() {
		return coverPageShowFileName;
	}
	public boolean getCoverPageShowLegend() {
		return coverPageShowLegend;
	}
	public boolean getShowLegend() {
		return showLegend;
	}
	public boolean getShowPageNumber() {
		return showPageNumber;
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
	
	public Action createPrintAction() {
		Action printAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				printDialog.pack();
				printDialog.setVisible(true);
			}
		};

		printAction.putValue(Action.SHORT_DESCRIPTION, "Print the mosaic.");
		printAction.putValue(Action.SMALL_ICON, Icons.get(16, "printer"));
		printAction.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "printer"));
		printAction.putValue(Action.NAME, "Print");
		printAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		printAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Print".indexOf('P'));
		printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));

		return printAction;
	}

	private double writeFileName(FontMetrics fm, int xMin, int xMax, int yMin, Graphics2D g2, int fontSizeIn1_72inches) {
		if(!coverPageShowFileName) 
			return 0;
						
		String s = mc.getFile().getName();
		Rectangle2D bounds = fm.getStringBounds(s, g2);
		float x = xMin + (float)((xMax-xMin)-bounds.getWidth())/2;
		float y = yMin + fontSizeIn1_72inches*8/10;
		g2.drawString(s, x, y);
		return fontSizeIn1_72inches*1.2; // 2.2 to make a little bit of space above the letters
	}
	
	private int drawbom(Graphics2D g2, int xMin, int xMax, int yMin, int yMax, int fontSizeIn1_72inches) {
		if(!coverPageShowLegend)
			return yMin;
		if(coverPagePictureType != CoverPagePictureType.None) {
			yMax = yMin + (yMax-yMin)/2;
		}
		LEGOColor.CountingLEGOColor[] bom = mw.getBrickedView().getLegendColors();
		
		int rowHeight = fontSizeIn1_72inches*6/5;
		int rows = Math.max(1, (yMax-yMin)/rowHeight);		
		int columns = (bom.length+rows-1)/rows;
		int columnWidth = (xMax-xMin)/columns;
		int i = 0;
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
			int maxWidth = columnWidth - rowHeight;
			
			g2.drawString(cut(c.cnt + "x " + colorController.getNormalIdentifier(c.c), g2, maxWidth), xMin + x*columnWidth + rowHeight, yMin + y*rowHeight + fontSizeIn1_72inches*9/10);

			++i;
		}
		return yMax;
	}
	
	private static int lastLength = Short.MAX_VALUE;
	private static String cut(String s, Graphics2D g2, int maxWidth) {
		Font font = g2.getFont();
		FontRenderContext context = g2.getFontRenderContext();
		if(lastLength+1 < s.length()) {
			s = s.substring(0, lastLength+1);
		}
		while(font.getStringBounds(s, context).getWidth() > maxWidth) {
			s = s.substring(0,  s.length()-1);
		}
		lastLength = s.length(); 
		return s;
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
	
	private void drawCoverPicture(Graphics2D g2, int xMin, int xMax, int yMin, int yMax) {
		if(coverPagePictureType == CoverPagePictureType.None)
			return;
		if(coverPagePictureType == CoverPagePictureType.Both) {
			BufferedImage left = mw.getImagePreparingView().getFullyPreparredImage();
			drawImage(g2, xMin, xMin + (xMax-xMin)*9/20, yMin, yMax, left);
			
			BufferedImage right = mw.getFinalImage();
			drawImage(g2, xMin + (xMax-xMin)*11/20, xMax, yMin, yMax, right);
			return;
		}
		
		BufferedImage image;
		if(coverPagePictureType == CoverPagePictureType.Original) {
			image = mw.getImagePreparingView().getFullyPreparredImage();
		}
		else {
			image = mw.getFinalImage();
		}
		drawImage(g2, xMin, xMax, yMin, yMax, image);
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
		drawCoverPicture(g2, xMin, xMax, yMin, yMax);
		
		//g2.setColor(Color.GREEN);
		//g2.drawRect(xMin,  yMin, xMax-xMin, yMax-yMin);
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
	
	private BufferedImage grayImage;
	private BufferedImage grayImageIn;
	private BufferedImage getGrayImage(BufferedImage orig, int width, int height) {
		if(grayImageIn == orig && grayImage != null && grayImage.getWidth() == width && grayImage.getHeight() == height) {
			return grayImage;
		}
		// Build and return gray Image:
		grayImageIn = orig;
		grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);				
		AffineTransform at = AffineTransform.getScaleInstance(width/(double)orig.getWidth(), height/(double)orig.getHeight());

		Graphics2D g2 = grayImage.createGraphics();
	    g2.drawImage(orig, at, null);
		
		return grayImage;
	}
	
	private double drawShowPosition(int page, int numPagesWidth, int numPagesHeight, FontMetrics fm, int xMin, int xMax, int yMin, int yMax, 
			Graphics2D g2, int unit, Dimension coreImageInCoreUnits, Dimension pageSizeInCoreUnits) {
		// Nothing:
		if(showPosition == ShowPosition.None)
			return 0;

		// Written:
		int fromLeft = (page % numPagesWidth)+1;
		int fromTop = (page / numPagesWidth)+1;
		if(showPosition == ShowPosition.Written) {
			//String pageNumberString = "" + fromLeft + " mod højre, " + fromTop + " ned.";
			String pageNumberString = fromLeft + ". from left, " + fromTop + ". from top.";
			Rectangle2D pageNumberStringBounds = fm.getStringBounds(pageNumberString, g2);
			float x = xMin + (float)((xMax-xMin)-pageNumberStringBounds.getWidth())/2;
			float y = yMin + unit;
			g2.drawString(pageNumberString, x, y);
			return unit*1.1; // 2.2 to make a little bit of space above the letters
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
			int outerHeight = (yMax-yMin)/5;
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
			BufferedImage finalImage = mw.getFinalImage();
			int grayWidth = (int)Math.round(outerBoxWidth * coreImageInCoreUnits.width/((double)pageSizeInCoreUnits.width*numPagesWidth));
			int grayHeight = (int)Math.round(outerBoxHeight * coreImageInCoreUnits.height/((double)pageSizeInCoreUnits.height*numPagesHeight));
			g2.drawImage(getGrayImage(finalImage, grayWidth, grayHeight), null, outerBoxX, yMin);			
			
			g2.drawRect(outerBoxX, yMin, outerBoxWidth, outerBoxHeight);
			int xLeft = outerBoxX + (int)((fromLeft-1)*innerBoxWidth);
			int xRight = outerBoxX + (int)(fromLeft*innerBoxWidth);
			int yTop = yMin + (int)((fromTop-1)*innerBoxHeight);
			int yBottom = yMin + (int)(fromTop*innerBoxHeight);
			g2.setColor(Color.RED);
			g2.fillRect(xLeft, yTop, (int)innerBoxWidth, (int)innerBoxHeight);
			g2.setColor(Color.BLACK);
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
	
	private int drawMagnifier(Graphics2D g2, int page, int numPagesWidth, int xMin, int xMax, int yMin, int yMax, int pageSizeInCoreUnitsW, int pageSizeInCoreUnitsH, PageFormat pf, Set<LEGOColor> used) {
		// Find out how big each page is (compared to full image):
		Dimension shownMagnifierSize = new Dimension((int)pf.getImageableWidth(), (int)((pf.getImageableWidth() * pageSizeInCoreUnitsH) / pageSizeInCoreUnitsW));
		int indentX = xMin;
		int indentY = yMax - shownMagnifierSize.height;
		int parts = (showLegend ? 2 : 1);
		if(shownMagnifierSize.height > (yMax-yMin)/parts) {
			shownMagnifierSize.height = (yMax-yMin)/parts;
			shownMagnifierSize.width = (shownMagnifierSize.height * pageSizeInCoreUnitsW) / pageSizeInCoreUnitsH;
			indentX = (xMax+xMin-shownMagnifierSize.width)/2;
			indentY = showLegend ? yMin + (yMax-yMin)/2 : yMin;
		}
		
		// draw magnified:
		g2.setColor(Color.BLACK);
		g2.translate(indentX, indentY);
		ToBricksTransform tbTransform = magnifierController.getTBTransform();
		int basicUnitWidth = tbTransform.getToBricksType().getUnitWidth();
		int basicUnitHeight = tbTransform.getToBricksType().getUnitHeight();
		Rectangle basicUnitRect = magnifierController.getCoreRect();
		basicUnitRect.width *= magnifiersPerPage.width;
		basicUnitRect.height *= magnifiersPerPage.height;
		basicUnitRect.x = (page % numPagesWidth)*basicUnitRect.width;
		basicUnitRect.y = (page / numPagesWidth)*basicUnitRect.height;
		
		if(tbTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
			if(uiController.showColors())
				used.addAll(tbTransform.drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, 0, 0));
			else
				used.addAll(tbTransform.drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize));
		}
		else {
			if(uiController.showColors()) {
				ToBricksType tbt = tbTransform.getToBricksType();
				used.addAll(tbTransform.getMainTransform().drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, tbt.getStudsShownWide(), tbt.getStudsShownTall()));
			}
			else
				used.addAll(tbTransform.getMainTransform().drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize));
		}
		
		g2.setColor(Color.RED);
		for(int x = 1; x < magnifiersPerPage.width; ++x) {
			int xIndent = x*shownMagnifierSize.width/magnifiersPerPage.width;
			g2.drawLine(xIndent, 0, xIndent, shownMagnifierSize.height);
		}
		for(int y = 1; y < magnifiersPerPage.height; ++y) {			
			int yIndent = y*shownMagnifierSize.height/magnifiersPerPage.height;
			g2.drawLine(0, yIndent, shownMagnifierSize.width, yIndent);
		}
		//g2.setColor(Color.BLACK);
		
		g2.translate(-indentX, -indentY);		
		return indentY;
	}
	
	private void drawLegend(Graphics2D g2, int xMin, int xMax, int yMin, int yMax, int fontSizeIn1_72inches, Set<LEGOColor> used) {
		if(!showLegend)
			return;
		int rowHeight = fontSizeIn1_72inches*6/5;
		int rows = Math.max(1, (yMax-yMin)/rowHeight);
		int columns = (used.size()+rows-1)/rows;
		int columnWidth = (xMax-xMin)/columns;
		int i = 0;
		for(LEGOColor c : used) {
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
			int maxWidth = columnWidth-rowHeight;
			g2.drawString(colorController.getNormalIdentifier(c), xMin + x*columnWidth + rowHeight, yMin + y*rowHeight + fontSizeIn1_72inches*9/10);
			++i;
		}
	}
	
	public int getNumberOfPages() {
		// Find out how big the magnifier is:
		final int coreImageInCoreUnitsW = magnifierController.getCoreImageInCoreUnits().getWidth();
		final int coreImageInCoreUnitsH = magnifierController.getCoreImageInCoreUnits().getHeight();
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
		
		// Special case: Cover page:
		if(page == 0 && coverPageShow) {
			printCoverPage(g2, pf);
			return PAGE_EXISTS;
		}
		if(coverPageShow)
			--page;
		
		// Find out how big the magnifier is:
		final int coreImageInCoreUnitsW = magnifierController.getCoreImageInCoreUnits().getWidth();
		final int coreImageInCoreUnitsH = magnifierController.getCoreImageInCoreUnits().getHeight();
		final Dimension coreImageInCoreUnits = new Dimension(coreImageInCoreUnitsW, coreImageInCoreUnitsH);
		final Dimension magnifierSizeInCoreUnits = magnifierController.getSizeInUnits();
		final int pageSizeInCoreUnitsW = magnifierSizeInCoreUnits.width * magnifiersPerPage.width;
		final int pageSizeInCoreUnitsH = magnifierSizeInCoreUnits.height * magnifiersPerPage.height;
		final Dimension pageSizeInCoreUnits = new Dimension(pageSizeInCoreUnitsW, pageSizeInCoreUnitsH);

		final int numPagesWidth = (coreImageInCoreUnitsW+pageSizeInCoreUnitsW-1) / pageSizeInCoreUnitsW;
		final int numPagesHeight = (coreImageInCoreUnitsH+pageSizeInCoreUnitsH-1) / pageSizeInCoreUnitsH;		
		final int numberOfPages = numPagesWidth*numPagesHeight; 
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
		Set<LEGOColor> used = new TreeSet<LEGOColor>();
		yMax = drawMagnifier(g2, page, numPagesWidth, xMin, xMax, yMin, yMax, pageSizeInCoreUnitsW, pageSizeInCoreUnitsH, pf, used);
		
		// Legend:
		g2.setFont(font);
		drawLegend(g2, xMin, xMax, yMin, yMax, fontSizeIn1_72inches, used);
				
		//g2.setColor(Color.GREEN);
		//g2.drawRect(xMin,  yMin, xMax-xMin, yMax-yMin);
		
	    return PAGE_EXISTS;
	}
	
	public static enum CoverPagePictureType {
		Original, Mosaic, Both, None;
	}
	public static enum ShowPosition {
		MiddleBox("Box with placement written inside"), SmartBox("Box with placement written outside"), Written("Text only"), None("None");
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
	}
}
