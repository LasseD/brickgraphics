package transforms;

import java.awt.Color;
import java.awt.image.*;

public class ContrastTransform extends RGBTransform {
	public ContrastTransform(float[] initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(allAre())
			return in;

		int w = in.getWidth();
		int h = in.getHeight();
		
		int sumR = 0;
		int sumG = 0;
		int sumB = 0;
		int[] rgbs = in.getRGB(0, 0, w, h, null, 0, w);
		for(int rgb : rgbs) {
			Color color = new Color(rgb);
			sumR += color.getRed();
			sumG += color.getGreen();
			sumB += color.getBlue();
		}
		float meanR = sumR / rgbs.length;
		float meanG = sumG / rgbs.length;
		float meanB = sumB / rgbs.length;

		short[][] contrastSpectrum = new short[3][256];
		for(int i = 0; i < 256; i++) {
			short r = (short)Math.round(meanR + (i-meanR)*get(0));
			short g = (short)Math.round(meanG + (i-meanG)*get(1));
			short b = (short)Math.round(meanB + (i-meanB)*get(2));
			contrastSpectrum[0][i] = cut(r);
			contrastSpectrum[1][i] = cut(g);
			contrastSpectrum[2][i] = cut(b);
		}

		LookupTable table = new ShortLookupTable(0,contrastSpectrum);
		BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		return new LookupOp(table, null).filter(in, tmp);
	}

	private static short cut(short s) {
		if(s < 0)
			return 0;
		if(s > 255)
			return 255;
		return s;
	}
}
