package griddy.rulers;

import javax.swing.*;
import ui.Icons;

public enum LengthType {
	plateHeight(2, Icons.plateHeight(24)), 
	brickHeight(6, Icons.brickHeight(24)), 
	brickWidth(5, Icons.brickWidth(24)), 
	cm(6, "cm."), 
	inch(6*2.54, "inch");
	
	public double getUnitLength() {
		return length;
	}

	public static final int ICON_SIZE = 24; // Initialization order error.
	private double length;
	private Icon icon;
	private String text;

	public JLabel makeDisplayComponent() {
		if(text == null)
			return new JLabel(icon);
		return new JLabel(text);
	}
	
	public Icon getIcon() {
		return icon;
	}
	
	public String getText() {
		return text;
	}
	
	private LengthType(double l, Icon icon) {
		length = l;
		this.icon = icon;
	}
	private LengthType(double l, String text) {
		length = l;
		this.text = text;
	}
}
