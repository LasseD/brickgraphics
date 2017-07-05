package mosaic.rendering;

import io.Log;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import transforms.Transform;

public class Pipeline extends Thread {
	public static final int WAIT_TIME_MS = 100;
	
	private ArrayList<Transform> transforms;
	private ArrayList<PipelineListener> inImageListeners, finalImageListeners;
	private Object token = new Object(); // For locking.
	private BufferedImage startImage;
	private long lastInvalidated; // Synchronized. May only be accessed when token is held!

	public Pipeline() {
		transforms = new ArrayList<Transform>();
		finalImageListeners = new ArrayList<PipelineListener>();
		inImageListeners = new ArrayList<PipelineListener>();
	}
	
	public void addTransform(Transform t) {
		transforms.add(t);
	}
	public void addInImageListener(PipelineListener l) {
		inImageListeners.add(l);
		if(startImage != null)
			l.imageChanged(startImage);
	}
	public void addFinalImageListener(PipelineListener l) {
		finalImageListeners.add(l);
	}
	
	public void invalidate() {
		synchronized(token) {
			lastInvalidated = System.currentTimeMillis();
		}
	}
	
	public void setStartImage(BufferedImage image) {
		if(image == null || startImage == image || image.getWidth() == 0 || image.getHeight() == 0)
			return;
		startImage = image;
		for(PipelineListener l : inImageListeners)
			l.imageChanged(image);
		invalidate();
	}
	
	@Override
	public void run() {
		long lastRunFor = 0;
		while(true) {
			try {
				sleep(WAIT_TIME_MS);
			} catch (InterruptedException e) {
				Log.log(e);
				Log.log("Pipeline thread dead. Closing down.");
				System.exit(1);
			}
			synchronized(token) {
				if(lastInvalidated <= lastRunFor)
					continue;
				lastRunFor = lastInvalidated;
			}
			runRound();
		}
	}
	
	private void runRound() {
		if(startImage == null)
			return;
		long timeThatThisRoundRunsFor;
		synchronized(token) {
			timeThatThisRoundRunsFor = lastInvalidated;
		}
		// Run pipeline:
		BufferedImage image = startImage;
		for(Transform t : transforms) {
			synchronized(token) {
				if(timeThatThisRoundRunsFor != lastInvalidated)
					return; // Start new round.
			}
			image = t.transform(image);
		}
		for(PipelineListener l : finalImageListeners)
			l.imageChanged(image);
	}
}
