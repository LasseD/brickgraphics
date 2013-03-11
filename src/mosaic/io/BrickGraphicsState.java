package mosaic.io;

import io.ModelState;
import io.Text;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.TreeSet;

import colors.LEGOColor;

public enum BrickGraphicsState implements ModelState {
	MainWindowPlacement(new Rectangle(0, 0, 640, 480)),
	MainWindowDividerLocation(400),
	Image(new File("mosaic.jpg")),
	//ImageType("jpg"),
	ImageRestriction(new Dimension(800,800)),
	ImageRestrictionEnabled(false),
	AnnoyingQuestions(true), 
	Language(Text.getAvailableLocales()[0]),
	
	// Magnifier:
	MagnifierShow(false),
	MagnifierShowColors(false),
	MagnifierBlockSize(new Dimension(4, 4)),
	MagnifierSize(0.3),
	
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

	// ToBrick:
	ToBricksWidth(100),
	ToBricksHeight(100),
	ToBricksColors(new TreeSet<LEGOColor>()),
	ToBricksColorGroups(new TreeSet<String>(){
		private static final long serialVersionUID = -7790837972418502219L;

		{
			add("Basic");
		}
	}),
	ToBricksTypeIndex(0),
	ToBricksHalfToneTypeIndex(0),
	ToBricksSizeTypeWidthIndex(2),
	ToBricksSizeTypeHeightIndex(2),
	ToBricksDirectionIsWidth(true);
	
	// TODO: all other stats (menu enable stuff)
	
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
