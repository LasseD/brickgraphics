package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.*;

import mosaic.rendering.ProgressCallback;

public class BrightnessTransform extends RGBTransform {
	public BrightnessTransform(float[] initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in,
			ProgressCallback progressCallback) {
		if(allAreOne())
			return in;

		RescaleOp op = new RescaleOp(get(), new float[3], null);
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
		op.filter(in, out);
		// TODO: Use progressCallback!
		return out;
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return in;
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.brightness(size).paintIcon(null, g, 0, 0);
	}
}
