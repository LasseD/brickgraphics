package mosaic.io;

import io.DataFile;
import io.ModelState;
import java.awt.*;
import java.awt.geom.Rectangle2D;

import transforms.ScaleTransform;

import bricks.ToBricksType;
import mosaic.controllers.*;

public enum BrickGraphicsState implements ModelState {
	MainWindowPlacement(new Rectangle(0, 0, 860, 430)),
	MainWindowDividerLocation(412),
	ImageFileName("mosaic_sample_input.jpg"),
	ImageFile(new DataFile()),

	// How to display colors:
	ColorsShownNumber(ColorController.ShownID.ID.ordinal()),
	ColorsShownText(ColorController.ShownName.NAME.ordinal()),
	ColorsLocalizedFileName("Danish"),
	ColorsFromYear(1900),
	ColorsToYear(2100), // Although it would be cool if this code was used so far into the future
	ColorsShowMetallic(true),
	ColorsShowOnlyLDD(false),
	ColorsShowTransparent(false),
	ColorsShowOtherColorsGroup(true),
	ColorsMinParts(1),
	ColorsMinSets(100),
	ColorsLoadRebrickableURL("https://rebrickable.com/colors/"),
	ColorsLoadRebrickableFile(""),
	ColorsIntensities(new java.util.TreeMap<Integer,Double>()),
	
	// Printing:
	PrintCoverPageShow(true),
	PrintCoverPageShowFileName(true),
	PrintCoverPageShowLegend(true),
	PrintCoverPageCoverPictureTypeIndex(0),
	PrintShowPositionIndex(0),
	PrintShowLegend(true),
	PrintShowPageNumber(true),
	PrintMagnifiersPerPage(new Dimension(3,3)),
	PrintFontSize(14f),
	PrintMagnifierSizePercentage(50),
	PrintDisplayTextRight("to the right      "),
	PrintDisplayTextDown("down"),
	
	// Magnifier:
	MagnifierShowLegend(false),
	MagnifierShowColors(true),
	MagnifierShowTotals(true),
	MagnifierShow(false),
	MagnifierSize(new Dimension(4, 4)),
		
	// Color Chooser:
	ColorDistributionChartShow(true),
	
	// Divider Location Button:
	DividerLocationButtonShow(false),
	
	// Prepare:
	PrepareSharpness(1.0f),
	PrepareGamma(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareBrightness(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareContrast(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareSaturation(1.0f),
	PrepareCropEnabled(true),
	PrepareCrop(new Rectangle2D.Double(0.217, 0.042, 0.5, 0.5)),
	PrepareFiltersEnabled(false),

	// Options dialog:
	PrepareAllowFilterReordering(false),
	PrepareScaleQuality(ScaleTransform.ScaleQuality.NearestNeighbor.ordinal()),
	PrepareScaleBeforePreparing(false),
	
	// ToBrick:
	ToBricksWidth(240),
	ToBricksHeight(240),
	ToBricksSizeRatioLocked(true),
	ToBricksPropagationPercentage(50),
	SelectedColors(new int[]{0, /*4,*/ 15, 19, 28, 70, 71, 72, 84, 308, 320, 484}),
	ToBricksFiltered(ToBricksType.getDefaultTypes()),
	ToBricksTypeIndex(0),
	
	// Export:
	ExportOptimize(true);
		
	private Object defaultValue;
	private Class<?> objectType;
	private BrickGraphicsState(Object defaultValue) {
		this.defaultValue = defaultValue;
		this.objectType = defaultValue.getClass();
	}
	@Override
	public Class<?> getType() {
		return objectType;
	}
	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}
	@Override
	public String getName() {
		return name();
	}
}
