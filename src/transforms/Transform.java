package transforms;

import java.awt.image.BufferedImage;

public interface Transform {
	BufferedImage transform(BufferedImage in);
}
