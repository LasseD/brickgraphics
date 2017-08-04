package transforms;

import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Graphics2D;

import mosaic.rendering.ProgressCallback;

public interface Transform {
	BufferedImage transform(BufferedImage in);
	Dimension getTransformedSize(Dimension in);
	void paintIcon(Graphics2D g, int size);
	void setProgressCallback(ProgressCallback p);
}
