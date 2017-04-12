package transforms;

import java.awt.Dimension;
import java.awt.image.*;

public class GammaTransform extends RGBTransform {
	public GammaTransform(float[] initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(allAre())
			return in;

		int w = in.getWidth();
		int h = in.getHeight();
		
		short[][] gammaSpectrum = new short[3][256];
		for(int rgb = 0; rgb < 3; rgb++) {
			for(int i = 0; i < 256; i++) {
				long s = Math.round(256*Math.pow(i/256.0, 1/get(rgb)));
				gammaSpectrum[rgb][i] = (short)Math.min(s, 255);
			}			
		}

		LookupTable table = new ShortLookupTable(0,gammaSpectrum);
		LookupOp op = new LookupOp(table, null);
		BufferedImage tmp = new BufferedImage(w, h, in.getType());
		return op.filter(in, tmp);
	}

	@Override
	public Dimension getTransformedSize(BufferedImage in) {
		return new Dimension(in.getWidth(), in.getHeight());
	}
}
