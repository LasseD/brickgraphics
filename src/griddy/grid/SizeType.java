package griddy.grid;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.*;
import ui.Icons;

public enum SizeType {
	plate(2, 5) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.plate(iconSize);
		}
	}, brick(6, 5) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.brick(iconSize);
		}
	}, sidewaysPlate(5, 2) {
		@Override
		public Icon icon(int iconSize) {
			return new RotateIcon(Icons.plate(iconSize));
		}
	}, sidewaysBrick(5, 6) {
		@Override
		public Icon icon(int iconSize) {
			return new RotateIcon(Icons.brick(iconSize));
		}
	}, stud(5, 5) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.stud(iconSize);
		}
	};
	
	public int width() {
		return width;
	}
	public int height() {
		return height;
	}
	
	private int width, height;

	public abstract Icon icon(int iconSize);
	
	private SizeType(int h, int w) {
		width = w;
		height = h;
	}
	
	private class RotateIcon implements Icon {
		private Icon icon;
		
		public RotateIcon(Icon i) {
			icon = i;
		}
		
		@Override
		public int getIconHeight() {
			return icon.getIconHeight();
		}

		@Override
		public int getIconWidth() {
			return icon.getIconWidth();
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D)g;
			g2.rotate(Math.PI/2, x+getIconWidth()/2, y+getIconHeight()/2);
			icon.paintIcon(c, g2, x, y);
			g2.rotate(-Math.PI/2, x+getIconWidth()/2, y+getIconHeight()/2);					
		}		
	}
}
