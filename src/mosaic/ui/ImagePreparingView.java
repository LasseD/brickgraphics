package mosaic.ui;

import io.*;
import mosaic.controllers.OptionsController;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.menu.ImagePreparingToolBar;
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import transforms.*;
import transforms.ScaleTransform.ScaleQuality;

/**
 * crop, sharpness, gamma, brightness, contrast, saturation
 */
public class ImagePreparingView extends JComponent implements ChangeListener, ModelHandler<BrickGraphicsState> {
	private BufferedImage inImage, prepared, baseImage, baseCrop;
	private Cropper cropper;
	private List<ChangeListener> listeners;

	private RGBTransform brightness, gamma, contrast;
	private StateTransform<Float> saturation, sharpness;

	private Transform[] transforms;
	private ImagePreparingToolBar toolBar;
	private OptionsController optionsController;
	
	// For scaling the image before it is transformed before bricked - 
	private boolean scaleBeforePreparing, allowFilterReordering;

	// For showing image:
	private ScaleTransform fullScaler, cropScaler, toBrickedPixelsSizeScaler;
	private Transform lastTransformUsedAsSource = null;
	
	public ImagePreparingView(final Model<BrickGraphicsState> model, OptionsController optionsController) {
		this.optionsController = optionsController;
		optionsController.addChangeListener(this);
		model.addModelHandler(this);
		setLayout(new BorderLayout());
		listeners = new LinkedList<ChangeListener>();

		ScaleQuality quality = optionsController.getScaleQuality();		
		allowFilterReordering = optionsController.getAllowFilterReordering();
		scaleBeforePreparing = optionsController.getScaleBeforePreparing();
		fullScaler = new ScaleTransform(true, quality, 2);
		cropScaler = new ScaleTransform(true, quality);
		toBrickedPixelsSizeScaler = new ScaleTransform(true, quality);
		
		cropper = new Cropper(model);		
		cropper.addChangeListener(this);
		addMouseMotionListener(cropper);
		addMouseListener(cropper);
		{
			sharpness = new SharpnessTransform((Float)model.get(BrickGraphicsState.PrepareSharpness));
			brightness = new BrightnessTransform((float[])model.get(BrickGraphicsState.PrepareBrightness));
			gamma = new GammaTransform((float[])model.get(BrickGraphicsState.PrepareGamma));
			contrast = new ContrastTransform((float[])model.get(BrickGraphicsState.PrepareContrast));
			saturation = new SaturationTransform((Float)model.get(BrickGraphicsState.PrepareSaturation));
			transforms = new Transform[]{sharpness, brightness, gamma, contrast, saturation};
		}
		toolBar = new ImagePreparingToolBar(ImagePreparingView.this, model);
		toolBar.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));
	}
	
	public ImagePreparingToolBar getToolBar() {
		return toolBar;
	}

	public void updateBaseImages() {
		if(inImage == null)
			return;
		baseImage = inImage;
		
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(0, 0, w, h);
		baseCrop = baseImage.getSubimage(r.x, r.y, r.width, r.height);		
	}
	
	private Rectangle baseCropRect;
	public void updateBaseCrop() {
		if(baseImage == null)
			return;
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(0, 0, w, h);
		if(baseCropRect == null || !baseCropRect.equals(r)) {
			baseCropRect = r;
			baseCrop = baseImage.getSubimage(r.x, r.y, r.width, r.height);		
		}
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners(Object sourceToListeners) {
		if(listeners == null)
			return;
		ChangeEvent e = new ChangeEvent(sourceToListeners == null ? this : sourceToListeners);
		for(ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}

	private void rePrepairImage(Transform source, Object sourceToListeners) {
		if(inImage == null || inImage.getWidth() == 0 || inImage.getHeight() == 0)
			return;

		if(cropper.isEnabled()) {
			prepared = baseCrop;
		}
		else {
			prepared = inImage;
		}
		
		// Scale "prepared" to not use more pixels than bricked.
		if(scaleBeforePreparing) {
			prepared = toBrickedPixelsSizeScaler.transform(prepared);
		}
		
		long startTime = System.currentTimeMillis();
		
		if(allowFilterReordering) {
			// Find last to transform:
			if(source != null) {
				for(Transform t : transforms) {
					if(t == source) {
						lastTransformUsedAsSource = source;
						break;
					}
				}				
			}
			for(Transform t : transforms) {
				if(t != lastTransformUsedAsSource)
					prepared = t.transform(prepared);		
			}
			if(lastTransformUsedAsSource != null) {
				prepared = lastTransformUsedAsSource.transform(prepared);
			}
		}		
		else {
			// No reordering allowed:
			for(Transform t : transforms) {
				prepared = t.transform(prepared);		
			}
		}
		
		long endTime = System.currentTimeMillis();
		Log.log("Performed transformations in " + (endTime-startTime) + "ms. Allow reordering: " + allowFilterReordering);

		notifyListeners(sourceToListeners);
	}

	public BufferedImage getFullyPreparredImage() {
		return prepared;
	}
	
	public void setImage(BufferedImage image, Object source) {
		this.inImage = image;
		updateBaseImages();
		rePrepairImage(null, source);
	}

	public void setContrast(int index, float contrast) {
		float[] get = this.contrast.get();
		get[index] = contrast;
		this.contrast.set(get);
		rePrepairImage(this.contrast, null);	
	}
	public void setContrast(float contrast) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = contrast;
		this.contrast.set(get);
		rePrepairImage(this.contrast, null);	
	}

	public void setSaturation(float saturation) {
		this.saturation.set(saturation);
		rePrepairImage(this.saturation, null);
	}

	public void setSharpness(float sharpness) {
		this.sharpness.set(sharpness);
		rePrepairImage(this.sharpness, null);
	}
	
	public void switchCropState() {
		cropper.switchEnabled();
		rePrepairImage(null, null);
	}
	
	public void switchFiltersEnabled() {
		toolBar.setVisible(!toolBar.isVisible());
	}

	public void setGamma(int index, float gamma) {
		float[] get = this.gamma.get();
		get[index] = gamma;
		this.gamma.set(get);
		rePrepairImage(this.gamma, null);
	}
	public void setGamma(float gamma) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = gamma;
		this.gamma.set(get);
		rePrepairImage(this.gamma, null);
	}

	public void setBrightness(int index, float brightness) {
		float[] get = this.brightness.get();
		get[index] = brightness;
		this.brightness.set(get);
		rePrepairImage(this.brightness, null);
	}
	public void setBrightness(float brightness) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = brightness;
		this.brightness.set(get);
		rePrepairImage(this.brightness, null);
	}
	
	private Cursor cursor;
	public void updateCursor() {
		Cursor c;
		if(cropper.isEnabled())
			c = cropper.getCursor();
		else
			c = Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
		if(cursor != c)
			setCursor(cursor = c);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		ScaleQuality quality = optionsController.getScaleQuality();
		fullScaler.setQuality(quality);
		cropScaler.setQuality(quality);
		toBrickedPixelsSizeScaler.setQuality(quality);
		
		allowFilterReordering = optionsController.getAllowFilterReordering();
		scaleBeforePreparing = optionsController.getScaleBeforePreparing();

		updateCursor();
		updateBaseCrop();
		rePrepairImage(null, null);
	}

	@Override 
	public void paintComponent(Graphics g) {
		if(ImagePreparingView.this.prepared == null)
			return;
		Graphics2D g2 = (Graphics2D)g;

		Dimension size = getSize();
		size.height-=4;
		size.width-=4;
		if(size.width <= 4 || size.height <= 4)
			return;
		
		fullScaler.setWidth(size.width);
		fullScaler.setHeight(size.height);
		
		if(cropper.isEnabled()) {
			BufferedImage full = ImagePreparingView.this.baseImage;
			full = fullScaler.transform(full);
			int x = 0;
			int y = 0;
			int w = full.getWidth();
			int h = full.getHeight();
			if(w < size.width) {
				x = (size.width-w)/2;
			}
			cropper.setMouseImage(new Rectangle(x, y, w, h));
			g2.drawImage(cropper.pollute(full), null, x, 0);

			Rectangle r = cropper.getCrop(x, y, w, h);
			cropScaler.setWidth(r.width);
			cropScaler.setHeight(r.height);

			g2.drawImage(cropScaler.transform(ImagePreparingView.this.prepared), null, x+r.x+2, r.y+2);
		}
		else {
			BufferedImage fullScaled = fullScaler.transform(ImagePreparingView.this.prepared);
			int w = fullScaled.getWidth();
			if(w < size.width) {
				g2.translate((size.width-w)/2, 0);
			}
			cropper.setMouseImage(new Rectangle(0, 0, fullScaled.getWidth(), fullScaled.getHeight()));
			g2.drawImage(fullScaled, null, 2, 2);
		}
	}

	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		sharpness.set((Float)model.get(BrickGraphicsState.PrepareSharpness));
		brightness.set((float[])model.get(BrickGraphicsState.PrepareBrightness));
		gamma.set((float[])model.get(BrickGraphicsState.PrepareGamma));
		contrast.set((float[])model.get(BrickGraphicsState.PrepareContrast));
		saturation.set((Float)model.get(BrickGraphicsState.PrepareSaturation));

		toolBar.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));
		toolBar.reloadModel(model);
		updateBaseImages();
	}
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareSharpness, sharpness.get());
		model.set(BrickGraphicsState.PrepareBrightness, brightness.get());
		model.set(BrickGraphicsState.PrepareGamma, gamma.get());
		model.set(BrickGraphicsState.PrepareContrast, contrast.get());
		model.set(BrickGraphicsState.PrepareSaturation, saturation.get());
		model.set(BrickGraphicsState.PrepareFiltersEnabled, toolBar.isVisible());
	}
}
