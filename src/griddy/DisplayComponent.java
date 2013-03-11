package griddy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

public interface DisplayComponent extends Serializable {
	void drawQuick(Graphics2D g2);
	void drawSlow(BufferedImage baseImage, Graphics2D g2);
	boolean isSlowValid();
}
