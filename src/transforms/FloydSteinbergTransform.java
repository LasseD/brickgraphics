package transforms;

import static colors.LEGOColorLookUp.*;

import java.awt.image.*;

import mosaic.controllers.ColorController;

import colors.*;

public class FloydSteinbergTransform extends BufferedLEGOColorTransform {
	private int propagationPercentage;
	
	public FloydSteinbergTransform(int pp, ColorController cc) {
		super(1, cc);
		propagationPercentage = pp;
	}
	
	public boolean setPropagationPercentage(int pp) {
		if(propagationPercentage == pp)
			return false;
		propagationPercentage = pp;
		clearBuffer();
		return true;
	}
	
	private void propagate(float[] before, int after, float[] leftOver) {
		leftOver[0] = before[0] - LEGOColorLookUp.getRed(after);
		leftOver[1] = before[1] - LEGOColorLookUp.getGreen(after);
		leftOver[2] = before[2] - LEGOColorLookUp.getBlue(after);
	}	
	
	private void processPixel(float[][][] tmpPixels, LEGOColor[][] realPixels, int x, int y, float[] leftOver) {
		final float[] pixel = tmpPixels[x][y];
		LEGOColor nearest = LEGOColorLookUp.lookUp(pixel);
		
		realPixels[x][y] = nearest;
		propagate(pixel, nearest.getRGB().getRGB(), leftOver);
	}
	
	private void sub(float[] tmpPixel, int weight, float[] diff) {
		for(int i = 0; i < 3; ++i)
    		tmpPixel[i] += weight*diff[i]*propagationPercentage/1600;
	}
	
	public LEGOColor[][] lcTransformUnbuffered(BufferedImage in) {		
		int w = in.getWidth();
		int h = in.getHeight();
		io.Log.log("Floyd-Steinberg " + w + "x" + h + " with " + LEGOColorLookUp.size() + " colors: ");		
		
		int[] iPixels = new int[w*h];
		in.getRGB(0, 0, w, h, iPixels, 0, w);
		LEGOColor[][] pixels = new LEGOColor[w][h];

		float[][][] tmpPixels = floatPixels(iPixels, w);

		// TODO: Use only two rows:
		//float[][] rowCurr = new float[w][3];
		//float[][] rowNext = new float[w][3];
		
		float[] diff = new float[3];
		int dir = 1, start = 0;
		for(int y = 0; y < h-1; y++, dir = -dir, start = (w-1)-start) {
			//handle first pixel in each row specially:
			processPixel(tmpPixels, pixels, start, y, diff);
			sub(tmpPixels[start+dir][y], 8, diff);
			sub(tmpPixels[start][y+1], 6, diff);
			sub(tmpPixels[start+dir][y+1], 2, diff);
			
			// handle most pixels:
			for(int i = 1; i < w-1; i++) {
				int x = start+dir*i;
				processPixel(tmpPixels, pixels, x, y, diff);

				sub(tmpPixels[x+dir][y], 7, diff);
				sub(tmpPixels[x-dir][y+1], 3, diff);
				sub(tmpPixels[x    ][y+1], 5, diff);
				sub(tmpPixels[x+dir][y+1], 1, diff);
			}
			//handle last pixel in each row specially:
			processPixel(tmpPixels, pixels, w-1-start, y, diff);

			sub(tmpPixels[w-1-start][y+1], 9, diff);
			sub(tmpPixels[w-1-start-dir][y+1], 7, diff);			
		}
		//handle last row specially:
		for(int i = 0; i < w-1; i++) {
			int x = start+dir*i;
			processPixel(tmpPixels, pixels, x, h-1, diff);
			sub(tmpPixels[x+dir][h-1], 16, diff);
		}
		//handle last pixel in last row specially:
		processPixel(tmpPixels, pixels, w-1-start, h-1, diff);

	    return pixels;
	}
	
	private float[][][] floatPixels(int[] intPixels, int w) {
		int h = intPixels.length/w;
		float[][][] floatPixels = new float[w][h][];
		for(int y = 0, i = 0; y < h; y++) {
			for(int x = 0; x < w; x++, i++) {
				int c = intPixels[i];
				floatPixels[x][y] = new float[]{getRed(c), getGreen(c), getBlue(c)};
			}
		}
		return floatPixels;
	}
}
