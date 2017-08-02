package mosaic.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import colors.LEGOColor;

import mosaic.controllers.MainController;
import mosaic.controllers.UIController;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineListener;

public class ColorDistributionChart extends JPanel implements PipelineListener {
	public static final int PREFERRED_SIZE = 128;
	
	private BrickedView bw;
	private LEGOColor.CountingLEGOColor[] colors;
	private UIController uiController;	
	
	public ColorDistributionChart(MainController mc, MainWindow mw, Pipeline pipeline) {
		uiController = mc.getUIController();
		bw = mw.getBrickedView();
		
		pipeline.addMosaicImageListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		if(colors == null)
			return;
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;

		// Find size:
		Dimension size = getSize();
		int diam = Math.min(size.width, size.height);
		int xIndent = size.width > diam ? (size.width-diam)/2 : 0;
		int yIndent = size.height > diam ? (size.height-diam)/2 : 0;

		// Draw colors:
		int sum = 0;
		for(LEGOColor.CountingLEGOColor c : colors) {
			sum += c.cnt;
		}
		int angle = 0;
		for(LEGOColor.CountingLEGOColor c : colors) {
			g2.setColor(c.c.getRGB());
			int angleAdd = (int)Math.round(360.0 * c.cnt / sum);
			g2.fillArc(xIndent, yIndent, diam, diam, angle, angleAdd);
			angle += angleAdd;
		}

		g2.setColor(Color.BLACK);
		g2.drawOval(xIndent, yIndent, diam, diam);
	}

	@Override
	public void imageChanged(BufferedImage image) {
		boolean visible = uiController.showColorDistributionChart();
		setVisible(visible);
		setPreferredSize(visible ? new Dimension(PREFERRED_SIZE, PREFERRED_SIZE) : new Dimension(0, 0));		
		if(!visible)
			return;
		colors = bw.getLegendColors();
		repaint();
	}
}
