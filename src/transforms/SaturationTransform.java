package transforms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.*;

public class SaturationTransform extends StateTransform<Float> {
	public SaturationTransform(Float initialState) {
		super(initialState);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(get().equals(1f))
			return in;

		int w = in.getWidth();
		int h = in.getHeight();
		
		int[] rgbs = in.getRGB(0, 0, w, h, null, 0, w);
		float[] hsb = new float[3];
		for(int i = 0; i < rgbs.length; i++) {
			int rgb = rgbs[i];
			Color c = new Color(rgb);
			Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
			hsb[1] *= get();
			if(hsb[1] > 1)
				hsb[1] = 1;
			// brightness:
/*			hsb[2] *= get();
			if(hsb[2] > 1)
				hsb[2] = 1;//*/
			rgbs[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		}
		BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		tmp.setRGB(0, 0, w, h, rgbs, 0, w);
		return tmp;
	}

	@Override
	public Dimension getTransformedSize(BufferedImage in) {
		return new Dimension(in.getWidth(), in.getHeight());
	}
}
