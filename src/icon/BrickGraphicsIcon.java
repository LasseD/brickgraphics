package icon;

import java.awt.*;
import javax.swing.*;

public abstract class BrickGraphicsIcon implements Icon {
	public final BrickIconMeasure measure;
	private int size;
	public final int mid;

	public BrickGraphicsIcon(int size) {
		this.size = size;
		mid = size/2;
		measure = new BrickIconMeasure(size);
	}		

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public int getIconWidth() {
		return size;
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
