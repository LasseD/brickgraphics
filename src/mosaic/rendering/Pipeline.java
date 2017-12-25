package mosaic.rendering;

import io.Log;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import transforms.ToBricksTransform;
import transforms.Transform;

public class Pipeline extends Thread {
	public static final int WAIT_TIME_MS = 150;
	
	private ArrayList<Transform> transforms;
	private ToBricksTransform toBricksTransform;

	private ArrayList<PipelineImageListener> inImageListeners, preparedImageListeners;
	private ArrayList<PipelineMosaicListener> mosaicListeners;
	private Object token = new Object(); // For locking (lastInvalidated, transforms, preparedImageListeners and mosaicListeners).
	private BufferedImage startImage;
	private long lastInvalidated; // Synchronized. May only be accessed when token is held!
	private RenderingProgressBar renderingProgressBar;

	public Pipeline(RenderingProgressBar renderingProgressBar) {
		transforms = new ArrayList<Transform>();
		inImageListeners = new ArrayList<PipelineImageListener>();
		preparedImageListeners = new ArrayList<PipelineImageListener>();
		mosaicListeners = new ArrayList<PipelineMosaicListener>();
		this.renderingProgressBar = renderingProgressBar;
	}
	
	public void addTransform(Transform t) {
		synchronized(token) {
			transforms.add(t);
		}
		renderingProgressBar.registerTransform(t);
	}
	public void setToBricksTransform(ToBricksTransform toBricksTransform) {
		this.toBricksTransform = toBricksTransform;
		renderingProgressBar.registerTransform(toBricksTransform);
	}
	public void addInImageListener(PipelineImageListener l) {
		inImageListeners.add(l);
		if(startImage != null)
			l.imageChanged(startImage);
	}
	public void addPreparedImageListener(PipelineImageListener l) {
		synchronized(token) {
			preparedImageListeners.add(l);
		}
	}
	public void addMosaicListener(PipelineMosaicListener l) {
		synchronized(token) {
			mosaicListeners.add(l);
		}
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
		for(PipelineImageListener l : inImageListeners)
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
			}
			lastRunFor = lastInvalidated;
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
		Transform[] copyTransforms;
		synchronized(token) {
			copyTransforms = new Transform[transforms.size()];
			copyTransforms = transforms.toArray(copyTransforms);
		}		
		for(Transform t : copyTransforms) {
			synchronized(token) {
				if(timeThatThisRoundRunsFor != lastInvalidated)
					return; // Start new round.
			}
			image = t.transform(image);
		}
		// Notify listeners:
		synchronized(token) {
			for(PipelineImageListener l : preparedImageListeners) {
				l.imageChanged(image);			
			}	
			if(toBricksTransform != null) {
				Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
				toBricksTransform.transform(image); // Returns null.
				imageSize = toBricksTransform.getTransformedSize(imageSize);
				for(PipelineMosaicListener l : mosaicListeners) {
					l.mosaicChanged(imageSize);
				}			
			}
		}
		renderingProgressBar.resetProgress();
	}
}
