package griddy.grid;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.*;
import icon.*;
import icon.ToBricksIcon.ToBricksIconType;

public enum GridCellType {
	plate(5, 2) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.plateFromSide().get(ToBricksIconType.Enabled, iconSize);
		}
	}, brick(5, 6) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.brickFromSide().get(ToBricksIconType.Enabled, iconSize);
		}
	}, sidewaysPlate(5, 2) {
		@Override
		public Icon icon(int iconSize) {
			return new RotateIcon(Icons.plateFromSide().get(ToBricksIconType.Enabled, iconSize));
		}
	}, sidewaysBrick(5, 6) {
		@Override
		public Icon icon(int iconSize) {
			return new RotateIcon(Icons.brickFromSide().get(ToBricksIconType.Enabled, iconSize));
		}
	}, stud(5, 5) {
		@Override
		public Icon icon(int iconSize) {
			return Icons.studFromTop(1).get(ToBricksIconType.Enabled, iconSize);
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
	
	private GridCellType(int w, int h) {
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
