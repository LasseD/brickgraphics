package transforms;

import java.awt.Dimension;
import java.awt.image.*;

public class SharpnessTransform extends StateTransform<Float> {
	public SharpnessTransform (float initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(get().equals(1f))
			return in;
		
		int w = in.getWidth();
		int h = in.getHeight();
		
		float s = get() - 1;
		float[] sharp = {0f,   -s, 0f, 
				-s,  1+4*s, -s, 
				0.0f, -s,  0.0f}; 
		BufferedImage tmp = new BufferedImage(w, h, in.getType());
		Kernel kernel = new Kernel(3,3,sharp);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		op.filter(in, tmp);
		return tmp;
	}

	@Override
	public Dimension getTransformedSize(BufferedImage in) {
		return new Dimension(in.getWidth(), in.getHeight());
	}
}
