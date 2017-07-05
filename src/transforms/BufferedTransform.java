package transforms;

//import io.Log;

import java.awt.image.*;

public abstract class BufferedTransform implements Transform {
	private BufferedImage[][] imagePairs;
	private int pairIndex;
	
	public BufferedTransform(int bufferSize) {
		imagePairs = new BufferedImage[bufferSize][2];
	}

	public BufferedTransform() {
		this(1);
	}
	
	public void clearBuffer() {
		imagePairs = new BufferedImage[imagePairs.length][2];		
	}
	
	@Override
	public BufferedImage transform(BufferedImage in) {
		for(BufferedImage[] pair : imagePairs) {
			if(pair[0] == in) {
				//Log.log(getClass().getName() + " transformation buffer skip.");
				return pair[1];
			}
		}
		
		//long startTime = System.currentTimeMillis();
		BufferedImage newOut = transformUnbuffered(in);
		if(imagePairs.length == 0)
			return newOut;
		imagePairs[pairIndex] = new BufferedImage[]{in, newOut};
		
		pairIndex++;
		if(pairIndex == imagePairs.length)
			pairIndex = 0;
		//long endTime = System.currentTimeMillis();
		//Log.log(getClass().getName() + " transformation performed in " + (endTime-startTime) + "ms.");
		return newOut;
	}
	
	public abstract BufferedImage transformUnbuffered(BufferedImage in);
}
