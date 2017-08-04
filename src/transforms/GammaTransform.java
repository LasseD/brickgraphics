package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.*;

import mosaic.rendering.ProgressCallback;

public class GammaTransform extends RGBTransform {
	public GammaTransform(float[] initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in, ProgressCallback progressCallback) {
		if(allAreOne())
			return in;

		int w = in.getWidth();
		int h = in.getHeight();
		
		progressCallback.reportProgress(100);
		short[][] gammaSpectrum = new short[3][256];
		for(int rgb = 0; rgb < 3; rgb++) {
			for(int i = 0; i < 256; i++) {
				long s = Math.round(256*Math.pow(i/256.0, 1/get(rgb)));
				gammaSpectrum[rgb][i] = (short)Math.min(s, 255);
			}			
		}

		progressCallback.reportProgress(300);
		LookupTable table = new ShortLookupTable(0,gammaSpectrum);
		progressCallback.reportProgress(500);
		LookupOp op = new LookupOp(table, null);
		BufferedImage tmp = new BufferedImage(w, h, in.getType());
		progressCallback.reportProgress(600);
		BufferedImage out = op.filter(in, tmp);
		progressCallback.reportProgress(1000);
		return out;
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return in;
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.gamma(size).paintIcon(null, g, 0, 0);
	}
}
