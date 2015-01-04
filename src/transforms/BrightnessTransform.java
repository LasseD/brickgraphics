package transforms;

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
		BufferedImage out = new BufferedImage(in.getWidth(), in.getHeight(), in.getType());
		op.filter(in, out);
		return out;
	}
}
