package icon;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class ToBricksIcon {
	public BrickGraphicsIcon get(final ToBricksIconType type, final int size) {
		return new BrickGraphicsIcon(size) {
			@Override
			public void paint(Graphics2D g2) {
				ToBricksIcon.this.paint(g2, type, size);
			}
		};
	}
	
	public abstract void paint(Graphics2D g2, ToBricksIconType type, int size);

	public static void drawVerticalMeasure(Graphics2D g2, int x, int y1, int y2) {
		Color tempColor = g2.getColor();
		g2.setColor(Color.BLACK);
		g2.drawLine(x, y1, x, y2);
		g2.drawLine(x-1, y1, x+1, y1);
		g2.drawLine(x-1, y2, x+1, y2);
		g2.setColor(tempColor);
	}
	public static void drawHorizontalMeasure(Graphics2D g2, int x1, int x2, int y) {
		Color tempColor = g2.getColor();
		g2.setColor(Color.BLACK);
		g2.drawLine(x1, y, x2, y);
		g2.drawLine(x1, y-1, x1, y+1);
		g2.drawLine(x2, y-1, x2, y+1);
		g2.setColor(tempColor);
	}
	
	public static enum ToBricksIconType {
		Disabled(false), Enabled(false), MeasureWidth(true), MeasureHeight(true);
		
		private ToBricksIconType(boolean isMeasure) {
			this.isMeasure = isMeasure;
		}
		private boolean isMeasure;
		public boolean isMeasure() {
			return isMeasure;
		}
	}
}
