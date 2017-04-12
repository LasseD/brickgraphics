package transforms;

import java.awt.image.BufferedImage;
import java.awt.Dimension;

public interface Transform {
	BufferedImage transform(BufferedImage in);
	Dimension getTransformedSize(BufferedImage in);
}
