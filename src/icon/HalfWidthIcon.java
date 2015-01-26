package icon;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

public abstract class HalfWidthIcon implements Icon {
	private int size;
	
	public HalfWidthIcon(int size) {
		this.size = size;
	}		

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public int getIconWidth() {
		return size/2;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2 = (Graphics2D)g;
		g2.translate(x, y);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		paint(g2);
		g2.translate(-x, -y);			
	}

	public abstract void paint(Graphics2D g2);
}