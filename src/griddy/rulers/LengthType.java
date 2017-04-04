package griddy.rulers;

import javax.swing.*;
import icon.*;
import icon.ToBricksIcon.ToBricksIconType;

public enum LengthType {
	plateHeight(2, Icons.plateFromSide(1).get(ToBricksIconType.MeasureHeight, 24)), 
	brickHeight(6, Icons.brickFromSide().get(ToBricksIconType.MeasureHeight, 24)), 
	brickWidth(5, Icons.brickFromSide().get(ToBricksIconType.MeasureWidth, 24)), 
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
