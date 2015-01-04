package transforms;

import java.awt.image.*;

import mosaic.controllers.ColorController;

import colors.*;

public class ThresholdTransform extends BufferedLEGOColorTransform {
	public ThresholdTransform(ColorController cc) {
		super(1, cc);
	}
	
	public ThresholdTransform(int bufferSize, ColorController cc) {
		super(bufferSize, cc);
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
				pixels[x][y] = LEGOColorLookUp.lookUp(iPixels[i]);
			}
		}
	    return pixels;
	}
}
