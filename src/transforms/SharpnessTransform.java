package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.*;

import mosaic.rendering.ProgressCallback;

public class SharpnessTransform extends StateTransform<Float> {
	public SharpnessTransform (float initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in, ProgressCallback progressCallback) {
		if(get().equals(1f))
			return in;
		
		int w = in.getWidth();
		int h = in.getHeight();
		
		float s = get() - 1;
		float[] sharp = {0f,   -s, 0f, 
				-s,  1+4*s, -s, 
				0.0f, -s,  0.0f}; 
		progressCallback.reportProgress(100);
		BufferedImage tmp = new BufferedImage(w, h, in.getType());
		progressCallback.reportProgress(300);
		Kernel kernel = new Kernel(3,3,sharp);
		progressCallback.reportProgress(500);
		ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		progressCallback.reportProgress(700);
		op.filter(in, tmp);
		progressCallback.reportProgress(1000);
		return tmp;
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return in;
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.sharpness(size).paintIcon(null, g, 0, 0);
	}
}
