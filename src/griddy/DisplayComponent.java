package griddy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public interface DisplayComponent extends Serializable {
	void draw(BufferedImage baseImage, Graphics2D g2);
}
