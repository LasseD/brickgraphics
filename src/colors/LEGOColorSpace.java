package colors;

import java.awt.*;
import java.awt.color.*;
import java.util.*;

public class LEGOColorSpace extends ColorSpace {
	private static final long serialVersionUID = -6012469644443808252L;
	private Map<Color, Color> map;
	private Color[] colors;
	private static final ColorSpace RGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	public LEGOColorSpace(Color[] colors) {
		super(ColorSpace.TYPE_RGB, 3);
		map = new HashMap<Color, Color>();
		this.colors = colors;
	}

	public float[] fromCIEXYZ(float[] colorvalue) {
		return fromRGB(RGB.fromCIEXYZ(colorvalue));
	}

	@Override
	public float[] fromRGB(float[] rgbvalue) {
		float fr = rgbvalue[0];
		float fg = rgbvalue[1];
		float fb = rgbvalue[2];
		Color from = new Color(fr, fg, fb);

		Color to = map.get(from);
		if(to != null) 
			return to.getRGBColorComponents(null);
		for(Color c : colors) {
			if(to == null || dist(fr, fg, fb, c) < dist(fr, fg, fb, to)) {
				to = c;
			}
		}
		map.put(from, to);
		return to.getRGBColorComponents(null);
	}

	private float dist(float ar, float ag, float ab, Color b) {
		float[] ba = new float[]{0, 0, 0, ar, ag, ab};
		b.getRGBColorComponents(ba);
		return dist(ba);
	}

	private float dist(float[] ab) {
		float dr = ab[0]-ab[3];
		float dg = ab[1]-ab[4];
		float db = ab[2]-ab[5];

		return dr*dr+dg*dg+db*db;
	}

	@Override
	public float[] toCIEXYZ(float[] colorvalue) {
		return RGB.toCIEXYZ(toRGB(colorvalue));
	}

	@Override
	public float[] toRGB(float[] colorvalue) {
		return colorvalue;
	}
	
	public @Override String toString() {
		return this.getClass().getName() + "[Map size:" + map.size() + "]";
	}
}
