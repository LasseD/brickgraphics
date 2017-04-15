package transforms;

import java.awt.Dimension;
import java.awt.image.*;

public class BrightnessTransform extends RGBTransform {
	public BrightnessTransform(float[] initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(allAre())
			return in;

		RescaleOp op = new RescaleOp(get(), new float[3], null);
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_RGB);
		op.filter(in, out);
		return out;
	}

	@Override
	public Dimension getTransformedSize(BufferedImage in) {
		return new Dimension(in.getWidth(), in.getHeight());
	}
}
