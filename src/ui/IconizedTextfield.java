package ui;

import java.awt.*;
import javax.swing.*;

public class IconizedTextfield extends JTextField {
	public static final int PADDING = 3;
	private Icon icon;
	
	public IconizedTextfield(int columns, Icon icon) {
		super(columns);
		this.icon = icon;
	}
	
	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		int iconWidth = icon.getIconWidth();
		int iconHeight = icon.getIconHeight();
		int width = getWidth();
		int height = getHeight();
		icon.paintIcon(this, g2, width - iconWidth - PADDING - 1, (height-iconHeight)/2 + PADDING - 1);
	}
}
