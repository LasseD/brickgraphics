package transforms;

import java.awt.image.*;
import colors.*;

/**
 * 2x5 = 10x10
 */
public class ThresholdTransform extends BufferedLEGOColorTransform {
	private LEGOColorApproximator colorApproximator;
	
	public ThresholdTransform(LEGOColorApproximator ca) {
		super(1);
		colorApproximator = ca;
	}
	
	@Override
	public LEGOColor[][] lcTransformUnbuffered(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();
		
		LEGOColor[][] pixels = new LEGOColor[w][h];
		int[] iPixels = new int[w*h];
		in.getRGB(0, 0, w, h, iPixels, 0, w);

		for(int y = 0, i = 0; y < h; y++) {
			for(int x = 0; x < w; x++, i++) {
				int index = colorApproximator.getNearestIndex(iPixels[i]);
				pixels[x][y] = colorApproximator.getLEGOColor(index);
			}
		}
	    return pixels;
	}
}
