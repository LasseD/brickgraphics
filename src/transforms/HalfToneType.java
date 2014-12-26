package transforms;

import javax.swing.*;
import ui.*;

public enum HalfToneType {
	FloydSteinberg(Icons.floydSteinberg(Icons.SIZE_LARGE)), Threshold(Icons.treshold(Icons.SIZE_LARGE));
	
	private Icon icon;
	private HalfToneType(Icon icon) {
		this.icon = icon;
	}
	public Icon getIcon() {
		return icon;
	}
}
