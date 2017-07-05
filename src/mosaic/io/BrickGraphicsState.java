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
	ColorsLoadRebrickableURL("http://rebrickable.com/colors"),
	ColorsLoadRebrickableFile(""),
	ColorsLoadLDDXMLFile(""),
	
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
	
	// Magnifier:
	MagnifierShowLegend(true),
	MagnifierShowColors(true),
	MagnifierShowTotals(true),
	MagnifierShow(false),
	MagnifierSize(new Dimension(4, 4)),
		
	// Color Chooser:
	ColorDistributionChartShow(true),
	
	// Prepare:
	PrepareSharpness(1.0f),
	PrepareGamma(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareBrightness(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareContrast(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareSaturation(1.0f),
	PrepareCropEnabled(false),
	PrepareCrop(new Rectangle2D.Double(0.25, 0.25, 0.5, 0.5)),
	PrepareFiltersEnabled(false),

	// Options dialog:
	PrepareAllowFilterReordering(false),
	PrepareScaleQuality(ScaleTransform.ScaleQuality.NearestNeighbor.ordinal()),
	PrepareScaleBeforePreparing(false),
	
	// ToBrick:
	ToBricksWidth(480),
	ToBricksHeight(160),
	ToBricksPropagationPercentage(50),
	SelectedColors(new int[]{0, 1, 2, 14, 15, 19, 70, 71, 72, 320}),
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
