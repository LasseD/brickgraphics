package mosaic.rendering;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
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
	
	private List<Transform> transforms;
	private int currentSection, currentSectionProgressInPromilles;
	
	public RenderingProgressBar() {
		super();
		transforms = new LinkedList<Transform>();
		this.setPreferredSize(new Dimension(100, HEIGHT));
	}
	
	public void resetProgress() {
		currentSection = currentSectionProgressInPromilles = 0;
		repaint();
	}
	
	public void registerTransform(Transform t) {
		final int idx = transforms.size();
		transforms.add(t);
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
		if(currentSection == 0 && currentSectionProgressInPromilles == 0)
			return; // Clear!
		//System.out.print("(" + currentSection + "/" + currentSectionProgressInPromilles + ")");
		Graphics2D g2 = (Graphics2D)g;
		g2.setStroke(new BasicStroke(2));
		int width = getWidth();
		int height = getHeight()-4;
		int sectionWidth = width / transforms.size();
		// Paint icons:
		g2.translate(-sectionWidth/2 - height/2, 2);
		for(Transform t : transforms) {
			g2.translate(sectionWidth, 0);
			t.paintIcon(g2, height);
		}
		g2.translate(sectionWidth/2 + height/2 - transforms.size()*sectionWidth, -2);
		// Paint progress:
		g2.setColor(PROGRESS_BAR_COLOR);
		g2.fillRect(0, 1, currentSection*sectionWidth + sectionWidth*currentSectionProgressInPromilles/1000, height+2);
	}
}
