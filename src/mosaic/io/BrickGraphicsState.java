package mosaic.io;

import io.ModelState;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import mosaic.controllers.*;

public enum BrickGraphicsState implements ModelState {
	MainWindowPlacement(new Rectangle(0, 0, 860, 430)),
	MainWindowDividerLocation(412),
	Image(new File("mosaic_sample_input.jpg")),

	// How to display colors:
	ColorsShownNumber(ColorController.ShownID.ID.ordinal()),
	ColorsShownText(ColorController.ShownName.NAME.ordinal()),
	ColorsLocalizedFileName("Danish"),
	ColorsFromYear(1900),
	ColorsToYear(2100), // Although it would be cool if this code was used so far into the future
	ColorsShowMetallic(false),
	ColorsShowTransparent(false),
	ColorsMinParts(1),
	ColorsMinSets(100),
	ColorsLoadURL("http://rebrickable.com/colors"),
	ColorsLoadFile(""),
	
	// Magnifier:
	//MagnifierShow(false),
	MagnifierShowLegend(true),
	MagnifierShowColors(true),
	MagnifierSize(new Dimension(4, 4)),
	
	// Prepare:
	PrepareSharpness(1.0f),
	PrepareSharpnessRank(0),
	PrepareGamma(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareGammaRank(1),
	PrepareBrightness(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareBrightnessRank(2),
	PrepareContrast(new float[]{1.0f, 1.0f, 1.0f}),
	PrepareContrastRank(3),
	PrepareSaturation(1.0f),
	PrepareSaturationRank(4),
	PrepareCropEnabled(false),
	PrepareCrop(new Rectangle2D.Double(0.25, 0.25, 0.5, 0.5)),
	PrepareFiltersEnabled(false),

	// ToBrick:
	ToBricksWidth(480),
	ToBricksHeight(160),
	ToBricksPropagationPercentage(50),
	SelectedColors(new String[]{}),
	SelectedColorGroups(new String[]{"Basic", "Modern"}),
	ToBricksTypeIndex(0),
	ToBricksHalfToneTypeIndex(0),
	ToBricksSizeTypeWidthIndex(2),
	ToBricksSizeTypeHeightIndex(2);
		
	private Object defaultValue;
	private Class<?> objectType;
	BrickGraphicsState(Object defaultValue) {
		this.defaultValue = defaultValue;
		this.objectType = defaultValue.getClass();
	}
	public Class<?> getType() {
		return objectType;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public String getName() {
		return name();
	}
}
