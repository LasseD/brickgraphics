package mosaic.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

import transforms.Transform;

/**
 * Progress shown by painting icons in the middle, then swiping over them.
 * @author LD
 */
public class RenderingProgressBar extends JPanel {
	public static final int OPACITY = 127; // 127 for half transparent.
	public static final int HEIGHT = 24;
	public static final Color PROGRESS_BAR_COLOR = new Color(0, 0, 255, OPACITY);
	public static final int NUM_TRANSFORMS = 9; // For easy concurrency.
	public static final int WAIT_MS_UNTIL_SHOW = 300;
	
	private Transform[] transforms;
	private int currentSection, currentSectionProgressInPromilles, numTranforms;
	private long currentStartTime;
	
	public RenderingProgressBar() {
		numTranforms = 0;
		transforms = new Transform[9];
		this.setPreferredSize(new Dimension(100, HEIGHT));
	}
	
	public void resetProgress() {
		currentSection = currentSectionProgressInPromilles = 0;
		repaint();
	}
	
	public synchronized void registerTransform(Transform t) {
		final int idx = numTranforms;
		transforms[idx] = t;
		numTranforms++;
		t.setProgressCallback(new ProgressCallback() {
			@Override
			public void reportProgress(int progressInPromilles) {
				if(progressInPromilles < 0 || progressInPromilles > 1000)
					throw new IllegalArgumentException("Promille should be in [0;1000]: " + progressInPromilles);
				if(currentSection == idx && currentSectionProgressInPromilles == progressInPromilles)
					return; // no change. No reason for callback.
				currentSection = idx;
				currentSectionProgressInPromilles = progressInPromilles;
				repaint();
			}
		});
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(currentSection == 0 && currentSectionProgressInPromilles == 0) {
			currentStartTime = -1;
			return; // Clear!
		}			
		if(currentStartTime == -1)
			currentStartTime = System.currentTimeMillis();
		if(System.currentTimeMillis() - currentStartTime < WAIT_MS_UNTIL_SHOW)
			return;
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(2));
		int width = getWidth();
		int height = getHeight()-4;
		int sectionWidth = width / numTranforms;
		// Paint icons:
		g2.translate(-sectionWidth/2 - height/2, 2);
		for(Transform t : transforms) {
			g2.translate(sectionWidth, 0);
			t.paintIcon(g2, height);
		}
		g2.translate(sectionWidth/2 + height/2 - numTranforms*sectionWidth, -2);
		// Paint progress:
		g2.setColor(PROGRESS_BAR_COLOR);
		g2.fillRect(0, 1, currentSection*sectionWidth + sectionWidth*currentSectionProgressInPromilles/1000, height+2);
	}
}
