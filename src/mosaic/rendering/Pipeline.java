package mosaic.rendering;

import io.Log;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import transforms.ToBricksTransform;
import transforms.Transform;

public class Pipeline extends Thread {
	public static final int WAIT_TIME_MS = 150;
	
	private ArrayList<Transform> transforms;
	private ToBricksTransform toBricksTransform;

	private ArrayList<PipelineListener> inImageListeners, preparedImageListeners, mosaicImageListeners;
	private Object token = new Object(); // For locking.
	private BufferedImage startImage;
	private long lastInvalidated; // Synchronized. May only be accessed when token is held!
	private RenderingProgressBar renderingProgressBar;

	public Pipeline(RenderingProgressBar renderingProgressBar) {
		transforms = new ArrayList<Transform>();
		inImageListeners = new ArrayList<PipelineListener>();
		preparedImageListeners = new ArrayList<PipelineListener>();
		mosaicImageListeners = new ArrayList<PipelineListener>();
		this.renderingProgressBar = renderingProgressBar;
	}
	
	public void addTransform(Transform t) {
		transforms.add(t);
		renderingProgressBar.registerTransform(t);
	}
	public void setToBricksTransform(ToBricksTransform toBricksTransform) {
		this.toBricksTransform = toBricksTransform;
		renderingProgressBar.registerTransform(toBricksTransform);
	}
	public void addInImageListener(PipelineListener l) {
		inImageListeners.add(l);
		if(startImage != null)
			l.imageChanged(startImage);
	}
	public void addPreparedImageListener(PipelineListener l) {
		preparedImageListeners.add(l);
	}
	public void addMosaicImageListener(PipelineListener l) {
		mosaicImageListeners.add(l);
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
		for(PipelineListener l : preparedImageListeners) {
			l.imageChanged(image);			
		}
		// Run toBrickStep:
		if(toBricksTransform != null) {
			image = toBricksTransform.transform(image);
			for(PipelineListener l : mosaicImageListeners) {
				l.imageChanged(image);			
			}			
		}
		renderingProgressBar.resetProgress();
	}
}
