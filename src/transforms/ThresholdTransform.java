package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.*;

import mosaic.controllers.ColorController;
import mosaic.rendering.ProgressCallback;

import colors.*;

public class ThresholdTransform extends BufferedLEGOColorTransform {
	private ProgressCallback progressCallback = ProgressCallback.NOP;

	public ThresholdTransform(ColorController cc) {
		super(1, cc);
	}
	
	public ThresholdTransform(int bufferSize, ColorController cc) {
		super(bufferSize, cc);
	}
	
	@Override
	public LEGOColorGrid lcTransformUnbuffered(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();
		
		LEGOColor[][] pixels = new LEGOColor[h][w];
		int[] iPixels = new int[w*h];
		in.getRGB(0, 0, w, h, iPixels, 0, w);

		for(int y = 0, i = 0; y < h; y++) {
			progressCallback.reportProgress(1000*y/h);
			for(int x = 0; x < w; x++, i++) {
				pixels[y][x] = LEGOColorLookUp.lookUp(iPixels[i]);
			}
		}
	    return new LEGOColorGrid(pixels);
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return in;
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.treshold(size).paintIcon(null, g, 0, 0);
	}

	@Override
	public void setProgressCallback(ProgressCallback p) {
		progressCallback = p;
	}
}
