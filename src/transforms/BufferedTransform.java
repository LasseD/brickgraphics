package transforms;

//import io.Log;

import java.awt.image.*;

import mosaic.rendering.ProgressCallback;

public abstract class BufferedTransform implements Transform {
	private BufferedImage[][] imagePairs;
	private int pairIndex;
	private ProgressCallback progressCallback = ProgressCallback.NOP;
	
	public BufferedTransform(int bufferSize) {
		if(bufferSize == 0)
			throw new IllegalArgumentException();
		imagePairs = new BufferedImage[bufferSize][2];
	}

	public BufferedTransform() {
		this(1);
	}
	
	public void clearBuffer() {
		imagePairs = new BufferedImage[imagePairs.length][2];		
	}
	
	@Override
	public void setProgressCallback(ProgressCallback progressCallback) {
		this.progressCallback = progressCallback;
	}
	
	@Override
	public BufferedImage transform(BufferedImage in) {
		progressCallback.reportProgress(0);
		
		for(BufferedImage[] pair : imagePairs) {
			if(pair[0] == in) {
				if(progressCallback != null)
					progressCallback.reportProgress(1000);
				return pair[1];
			}
		}
		
		//long startTime = System.currentTimeMillis();
		BufferedImage newOut = transformUnbuffered(in, progressCallback);
		imagePairs[pairIndex] = new BufferedImage[]{in, newOut};
		
		pairIndex++;
		if(pairIndex == imagePairs.length)
			pairIndex = 0;
		//long endTime = System.currentTimeMillis();
		//Log.log(getClass().getName() + " transformation performed in " + (endTime-startTime) + "ms.");
		progressCallback.reportProgress(1000);
		return newOut;
	}
	
	public abstract BufferedImage transformUnbuffered(BufferedImage in, ProgressCallback progressCallback);
}
