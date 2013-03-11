package transforms;

import javax.swing.*;
import ui.*;

public enum HalfToneType {
	FloydSteinberg(Icons.get(32, "floyd_steinberg")), Threshold(Icons.get(32, "solid_region"));
	
	private Icon icon;
	private HalfToneType(Icon icon) {
		this.icon = icon;
	}
	public Icon getIcon() {
		return icon;
	}
}
