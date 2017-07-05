package mosaic.rendering;

import java.awt.image.BufferedImage;

public interface PipelineListener {
	void imageChanged(BufferedImage image);
}
