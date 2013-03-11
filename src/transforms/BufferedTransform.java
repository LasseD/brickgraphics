package transforms;

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
	
	public BufferedImage transform(BufferedImage in) {
		for(BufferedImage[] pair : imagePairs) {
			if(pair[0] == in)
				return pair[1];
		}
		
		BufferedImage newOut = transformUnbuffered(in);
		if(imagePairs.length == 0)
			return newOut;
		imagePairs[pairIndex] = new BufferedImage[]{in, newOut};
		
		pairIndex++;
		pairIndex %= imagePairs.length;
		return newOut;
	}
	
	public abstract BufferedImage transformUnbuffered(BufferedImage in);
}
