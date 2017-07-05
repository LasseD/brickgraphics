package transforms;

import colors.*;

import java.awt.image.*;

public interface LEGOColorTransform extends Transform {
	LEGOColorGrid lcTransform(BufferedImage in);
}
