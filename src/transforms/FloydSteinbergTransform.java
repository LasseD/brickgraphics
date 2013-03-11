package transforms;

import static colors.LEGOColorApproximator.*;
import java.awt.image.*;
import colors.*;

/**
 * 2x5 = 10x10
 */
public class FloydSteinbergTransform extends BufferedLEGOColorTransform {
	private LEGOColorApproximator colorApproximator;
	private boolean writeTransformInfo;
	private int[] colorCounts;
	
	public FloydSteinbergTransform(LEGOColorApproximator ca) {
		super(1);
		colorApproximator = ca;
		writeTransformInfo = false;
	}
	
	private float[] processPixel(float[][][] tmpPixels, LEGOColor[][] realPixels, int x, int y) {
		float[] pixel = tmpPixels[x][y];
		int nearestIndex = colorApproximator.getNearestIndex(pixel);
		LEGOColor nearest = colorApproximator.getLEGOColor(nearestIndex);
		
		if(colorCounts != null)
			colorCounts[nearestIndex]++;
		
		realPixels[x][y] = nearest;
		return colorApproximator.toPropagate(pixel, nearest.rgb.getRGB());
	}
	
	public LEGOColor[][] lcTransformUnbuffered(BufferedImage in) {
		if(writeTransformInfo)
			colorCounts = new int[colorApproximator.size()];
		
		int w = in.getWidth();
		int h = in.getHeight();
		io.Log.log("Floyd-Steinberg " + w + "x" + h + " with " + colorApproximator.size() + " colors.");		
		
		int[] iPixels = new int[w*h];
		in.getRGB(0, 0, w, h, iPixels, 0, w);
		LEGOColor[][] pixels = new LEGOColor[w][h];

		float[][][] tmpPixels = floatPixels(iPixels, w);
		
		float[] diff;
		int dir = 1, start = 0;
		for(int y = 0; y < h-1; y++, dir = -dir, start = (w-1)-start) {
			//handle first pixel in each row specially:
			diff = processPixel(tmpPixels, pixels, start, y);
			colorApproximator.sub(tmpPixels[start+dir][y], 8, diff);
			colorApproximator.sub(tmpPixels[start][y+1], 6, diff);
			colorApproximator.sub(tmpPixels[start+dir][y+1], 2, diff);
			
			// handle most pixels:
			for(int i = 1; i < w-1; i++) {
				int x = start+dir*i;
				diff = processPixel(tmpPixels, pixels, x, y);

				colorApproximator.sub(tmpPixels[x+dir][y], 7, diff);
				colorApproximator.sub(tmpPixels[x-dir][y+1], 3, diff);
				colorApproximator.sub(tmpPixels[x    ][y+1], 5, diff);
				colorApproximator.sub(tmpPixels[x+dir][y+1], 1, diff);
			}
			//handle last pixel in each row specially:
			diff = processPixel(tmpPixels, pixels, w-1-start, y);

			colorApproximator.sub(tmpPixels[w-1-start][y+1], 9, diff);
			colorApproximator.sub(tmpPixels[w-1-start-dir][y+1], 7, diff);			
		}
		//handle last row specially:
		for(int i = 0; i < w-1; i++) {
			int x = start+dir*i;
			diff = processPixel(tmpPixels, pixels, x, h-1);
			colorApproximator.sub(tmpPixels[x+dir][h-1], 16, diff);
		}
		//handle last pixel in last row specially:
		processPixel(tmpPixels, pixels, w-1-start, h-1);

		if(writeTransformInfo)
			writeTransformInfo();
		
	    return pixels;
	}
	
	private void writeTransformInfo() {
		System.out.println("¤¤¤");
		int sum = 0;
		for(int i = 0; i < colorCounts.length; i++) {
			sum += colorCounts[i];
		}

		for(int i = 0; i < colorCounts.length; i++) {
			System.out.printf("%s: %.3f%%\n", 
					colorApproximator.getColorInfo(i), 
					colorCounts[i]*100f/sum);
		}
		System.out.println("¤¤¤");
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
