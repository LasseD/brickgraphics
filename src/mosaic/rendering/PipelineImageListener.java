package mosaic.rendering;

import java.awt.image.BufferedImage;

public interface PipelineImageListener {
	void imageChanged(BufferedImage image);
}
