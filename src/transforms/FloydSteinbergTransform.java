package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.*;

import mosaic.controllers.ColorController;
import mosaic.rendering.ProgressCallback;
import colors.*;

public class FloydSteinbergTransform extends BufferedLEGOColorTransform {
	private int propagationPercentage;
	private ProgressCallback progressCallback = ProgressCallback.NOP;
	
	public FloydSteinbergTransform(int pp, ColorController cc) {
		super(1, cc);
		propagationPercentage = pp;
	}
	
	public FloydSteinbergTransform(int bufferSize, int pp, ColorController cc) {
		super(bufferSize, cc);
		propagationPercentage = pp;
	}
	
	public boolean setPropagationPercentage(int pp) {
		if(propagationPercentage == pp)
			return false;
		propagationPercentage = pp;
		clearBuffer();
		return true;
	}
	public int getPropagationPercentage() {
		return propagationPercentage;
	}
	
	private static int boundFF(int a) {
		if(a < 0) {
			return 0;
		}
		if(a > 255) {
			return 255;
		}
		return a;
	}
	private static void splitColor(int c, int[] components) {
		components[0] = LEGOColorLookUp.getRed(c);
		components[1] = LEGOColorLookUp.getGreen(c);
		components[2] = LEGOColorLookUp.getBlue(c);
	}
	private static int mergeColor(int[] components) {
		return (components[0] << 16) + (components[1] << 8) + components[2];
	}	
	private static void diff(int before, int after, int[] out) {
		out[0] = LEGOColorLookUp.getRed(before) - LEGOColorLookUp.getRed(after);
		out[1] = LEGOColorLookUp.getGreen(before) - LEGOColorLookUp.getGreen(after);
		out[2] = LEGOColorLookUp.getBlue(before) - LEGOColorLookUp.getBlue(after);
	}	
	private static void processPixel(final int pixel, LEGOColor[][] out, final int x, final int y, int[] diff) {
		LEGOColor nearest = LEGOColorLookUp.lookUp(pixel);
		
		out[y][x] = nearest;
		diff(pixel, nearest.getRGB().getRGB(), diff);
	}	
	private void sub(int[] pixels, int pixelIndex, int weight, int[] diff) {
		if(pixelIndex < 0 || pixelIndex >= pixels.length)
			return; // Error silently.
		int[] components = new int[3];
		splitColor(pixels[pixelIndex], components);
		for(int i = 0; i < 3; ++i) {
			components[i] += (weight*diff[i]*propagationPercentage)/1600;
			components[i] = boundFF(components[i]);
		}
		pixels[pixelIndex] = mergeColor(components);
	}
	
	@Override
	public LEGOColorGrid lcTransformUnbuffered(BufferedImage in) {		
		int w = in.getWidth();
		int h = in.getHeight();
		if(w == 0 || h == 0)
			throw new IllegalArgumentException("In-image has null dimension!");
		//io.Log.log("Floyd-Steinberg " + w + "x" + h + " with " + LEGOColorLookUp.size() + " colors.");		
		
		int[] pixels = new int[w*h];
		in.getRGB(0, 0, w, h, pixels, 0, w);
		LEGOColor[][] out = new LEGOColor[h][w];

		int[] diff = new int[3];
		int dir = 1, start = 0;
		for(int y = 0; y < h-1; y++, dir = -dir, start = (w-1)-start) {
			progressCallback.reportProgress(1000*y/h);
			//handle first pixel in each row specially:
			processPixel(pixels[y*w+start], out, start, y, diff);
			sub(pixels, y*w+start+dir, 8, diff);
			sub(pixels, (y+1)*w+start, 6, diff);
			sub(pixels, (y+1)*w+start+dir, 2, diff);
			
			// handle most pixels:
			for(int i = 1; i < w-1; i++) {
				int x = start+dir*i;
				processPixel(pixels[y*w+x], out, x, y, diff);

				sub(pixels,x+dir+w*y, 7, diff);
				sub(pixels,x-dir+w*(y+1), 3, diff);
				sub(pixels,x    +w*(y+1), 5, diff);
				sub(pixels,x+dir+w*(y+1), 1, diff);
			}
			//handle last pixel in each row specially:
			processPixel(pixels[y*w+w-1-start], out, w-1-start, y, diff);

			sub(pixels,w-1-start+w*(y+1), 9, diff);
			sub(pixels,w-1-start-dir+w*(y+1), 7, diff);			
		}
		//handle last row specially:
		for(int i = 0; i < w-1; i++) {
			int x = start+dir*i;
			processPixel(pixels[(h-1)*w+x], out, x, h-1, diff);
			sub(pixels, x+dir+(h-1)*w, 16, diff);
		}
		//handle last pixel in last row specially:
		processPixel(pixels[w*(h-1)+w-1-start], out, w-1-start, h-1, diff);

	    return new LEGOColorGrid(out);
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		return in;
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.floydSteinberg(size).paintIcon(null, g, 0, 0);
	}

	@Override
	public void setProgressCallback(ProgressCallback p) {
		this.progressCallback = p;
	}
}
