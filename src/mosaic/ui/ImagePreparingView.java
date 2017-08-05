package mosaic.ui;

import io.*;
import mosaic.controllers.OptionsController;
import mosaic.controllers.ToBricksController;
import mosaic.io.BrickGraphicsState;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineImageListener;
import mosaic.rendering.ProgressCallback;
import mosaic.ui.menu.ImagePreparingToolBar;
import java.awt.image.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.event.*;

import transforms.*;
import transforms.ScaleTransform.ScaleQuality;

/**
 * crop, sharpness, gamma, brightness, contrast, saturation
 */
public class ImagePreparingView extends JComponent implements ModelHandler<BrickGraphicsState>, ChangeListener {
	private BufferedImage inImage, preparedImage; // PreparedImage to be set from listener on pipeline.
	private Cropper cropper;
	private Pipeline pipeline;

	private RGBTransform brightness, gamma, contrast;
	private StateTransform<Float> saturation, sharpness;
	private CropTransform cropTransform;

	private Transform[] movableTransforms;
	private ImagePreparingToolBar toolBar;
	private OptionsController optionsController;
	private ToBricksController toBricksController;
	
	// For scaling the image before it is transformed before bricked - 
	private boolean scaleBeforePreparing, allowFilterReordering;

	// For showing image:
	private ScaleTransform fullScaler, cropScaler, noCropScaler, toBrickedPixelsSizeScaler;
	private Transform lastTransformUsedAsSource = null;
	private ProgressCallback progressCallbackForLastTransformUsedAsSource = ProgressCallback.NOP;
	
	public ImagePreparingView(final Model<BrickGraphicsState> model, OptionsController optionsController, final ToBricksController toBricksController, final Pipeline pipeline) {
		this.optionsController = optionsController;
		this.toBricksController = toBricksController;
		this.pipeline = pipeline;
		optionsController.addChangeListener(this); // getScaleQuality and allowFiltersReordering
		toBricksController.addChangeListener(this); // getMinimalInputImageSize
		setLayout(new BorderLayout());

		ScaleQuality quality = optionsController.getScaleQuality();		
		allowFilterReordering = optionsController.getAllowFilterReordering();
		scaleBeforePreparing = optionsController.getScaleBeforePreparing();
		fullScaler = new ScaleTransform("Filtered left image", true, quality, 2);
		cropScaler = new ScaleTransform("Crop", false, quality);
		noCropScaler = new ScaleTransform("No crop", false, ScaleQuality.RetainColors);
		toBrickedPixelsSizeScaler = new ScaleTransform("Construction minimal size", false, quality);
		
		cropper = new Cropper(model);		
		model.addModelHandler(this); // Ensure cropper is updated before this - so the crop doesn't lag.
		cropper.addPointerIconListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateCursor();
			}
		});
		cropper.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateWidthToHeight();
				pipeline.invalidate();
			}
		}); // For cursor style.
		cropTransform = new CropTransform(cropper);
		addMouseMotionListener(cropper);
		addMouseListener(cropper);
		{
			sharpness = new SharpnessTransform((Float)model.get(BrickGraphicsState.PrepareSharpness));
			gamma = new GammaTransform((float[])model.get(BrickGraphicsState.PrepareGamma));
			brightness = new BrightnessTransform((float[])model.get(BrickGraphicsState.PrepareBrightness));
			contrast = new ContrastTransform((float[])model.get(BrickGraphicsState.PrepareContrast));
			saturation = new SaturationTransform((Float)model.get(BrickGraphicsState.PrepareSaturation));
			movableTransforms = new Transform[]{sharpness, gamma, brightness, contrast, saturation};
		}
		toolBar = new ImagePreparingToolBar(ImagePreparingView.this, model);
		toolBar.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));
		populatePipeline();
	}
	
	private void updateWidthToHeight() {
		if(cropper.isEnabled()) {
			float cropperWidthToHeight = cropper.getWidthToHeight();
			float w2h = cropperWidthToHeight * inImage.getWidth() / inImage.getHeight();
			toBricksController.setOriginalWidthToHeight(w2h);
		}
		else {
			float originalWidthToHeight = inImage.getWidth()/(float)inImage.getHeight();			
			toBricksController.setOriginalWidthToHeight(originalWidthToHeight);
		}
	}
	
	public ImagePreparingToolBar getToolBar() {
		return toolBar;
	}

	private void populatePipeline() {
		// Crop:
		pipeline.addTransform(cropTransform);
		// Resizing optimization (if enabled):
		pipeline.addTransform(new Transform(){
			private boolean shouldScaleBeforePreparing(BufferedImage in) {
				return scaleBeforePreparing && 
				   in.getWidth() > toBrickedPixelsSizeScaler.getWidth() &&
				   in.getHeight() > toBrickedPixelsSizeScaler.getHeight();				   
			}			
			@Override
			public BufferedImage transform(BufferedImage in) {
				return shouldScaleBeforePreparing(in) ? toBrickedPixelsSizeScaler.transform(in) : in;
			}
			@Override
			public Dimension getTransformedSize(Dimension in) {
				throw new UnsupportedOperationException();
			}
			@Override
			public void paintIcon(Graphics2D g, int size) {
				toBrickedPixelsSizeScaler.paintIcon(g, size); // Use their icon even when skipping.
			}
			@Override
			public void setProgressCallback(ProgressCallback p) {
				toBrickedPixelsSizeScaler.setProgressCallback(p); // They should report progress when applicable.
			}});
		// The main filters:
		for(final Transform t : movableTransforms) {
			pipeline.addTransform(new Transform(){
				@Override
				public BufferedImage transform(BufferedImage in) {
					if(allowFilterReordering && 
							lastTransformUsedAsSource != null && 
							lastTransformUsedAsSource == t)
						return in; // Ignore progress here.
					return t.transform(in); // t reports progress.
				}
				@Override
				public Dimension getTransformedSize(Dimension in) {
					throw new UnsupportedOperationException();
				}
				@Override
				public void paintIcon(Graphics2D g, int size) {
					if(allowFilterReordering && 
							lastTransformUsedAsSource != null && 
							lastTransformUsedAsSource == t)
						g.drawOval(0,  0, size, size); // empty icon.
					else
						t.paintIcon(g, size); // Use same icon as t.
				}
				@Override
				public void setProgressCallback(final ProgressCallback p) {
					t.setProgressCallback(new ProgressCallback() {						
						@Override
						public void reportProgress(int progressInPromilles) {
							if(allowFilterReordering && 
									lastTransformUsedAsSource != null && 
									lastTransformUsedAsSource == t)
								progressCallbackForLastTransformUsedAsSource.reportProgress(progressInPromilles);
							else
								p.reportProgress(progressInPromilles);
						}
					});
				}});
		}
		// Skipped main filter:
		pipeline.addTransform(new Transform(){
			@Override
			public BufferedImage transform(BufferedImage in) {
				if(allowFilterReordering && lastTransformUsedAsSource != null)
					return lastTransformUsedAsSource.transform(in);
				progressCallbackForLastTransformUsedAsSource.reportProgress(1000);
				return in;
			}
			@Override
			public Dimension getTransformedSize(Dimension in) {
				throw new UnsupportedOperationException();
			}
			@Override
			public void paintIcon(Graphics2D g, int size) {
				if(allowFilterReordering && lastTransformUsedAsSource != null)
					lastTransformUsedAsSource.paintIcon(g, size);
				else
					g.drawOval(0,  0, size, size); // empty icon.
			}
			@Override
			public void setProgressCallback(ProgressCallback p) {
				progressCallbackForLastTransformUsedAsSource = p; // Save this callback to be used in the other transforms.				
			}});
		
		pipeline.addInImageListener(new PipelineImageListener() {
			@Override
			public void imageChanged(BufferedImage image) {
				inImage = image;
				updateWidthToHeight();
			}
		});
		pipeline.addPreparedImageListener(new PipelineImageListener() {			
			@Override
			public void imageChanged(BufferedImage image) {
				preparedImage = image;
				repaint();		
			}
		}); // Update prepared image.
	}

	private void transformChangedInvalidatePipeline(Transform source) {
		lastTransformUsedAsSource = source;
		pipeline.invalidate();
	}
	
	public void setContrast(int index, float contrast) {
		float[] get = this.contrast.get();
		get[index] = contrast;
		this.contrast.set(get);
		transformChangedInvalidatePipeline(this.contrast);	
	}
	public void setContrast(float contrast) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = contrast;
		this.contrast.set(get);
		transformChangedInvalidatePipeline(this.contrast);	
	}

	public void setSaturation(float saturation) {
		this.saturation.set(saturation);
		transformChangedInvalidatePipeline(this.saturation);
	}

	public void setSharpness(float sharpness) {
		this.sharpness.set(sharpness);
		transformChangedInvalidatePipeline(this.sharpness);
	}
	
	public void switchCropState() {
		cropper.switchEnabled();
	}
	
	public void switchFiltersEnabled() {
		toolBar.setVisible(!toolBar.isVisible());
	}

	public void setGamma(int index, float gamma) {
		float[] get = this.gamma.get();
		get[index] = gamma;
		this.gamma.set(get);
		transformChangedInvalidatePipeline(this.gamma);
	}
	public void setGamma(float gamma) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = gamma;
		this.gamma.set(get);
		transformChangedInvalidatePipeline(this.gamma);
	}

	public void setBrightness(int index, float brightness) {
		float[] get = this.brightness.get();
		get[index] = brightness;
		this.brightness.set(get);
		transformChangedInvalidatePipeline(this.brightness);
	}
	public void setBrightness(float brightness) {
		float[] get = new float[3];
		for(int i = 0; i < 3; ++i)
			get[i] = brightness;
		this.brightness.set(get);
		transformChangedInvalidatePipeline(this.brightness);
	}

	private boolean updateToBrickedPixelsSizeScaler() {
		Dimension dim = toBricksController.getMinimalInputImageSize();
		
		boolean changedHeight = toBrickedPixelsSizeScaler.setHeight(dim.height);
		boolean changedWidth = toBrickedPixelsSizeScaler.setWidth(dim.width);
		return changedHeight || changedWidth;
	}	
	
	private Cursor cursor;
	public boolean updateCursor() {
		Cursor c;
		if(cropper.isEnabled())
			c = cropper.getCursor();
		else
			c = Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
		if(cursor == c)
			return false;
		setCursor(cursor = c);
		return true;
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		boolean changed = false;
		ScaleQuality quality = optionsController.getScaleQuality();
		changed |= fullScaler.setQuality(quality);
		changed |= cropScaler.setQuality(quality);
		changed |= toBrickedPixelsSizeScaler.setQuality(quality);
		
		if(allowFilterReordering != optionsController.getAllowFilterReordering()) {
			allowFilterReordering = optionsController.getAllowFilterReordering();
			changed = true;
		}		
		if(scaleBeforePreparing != optionsController.getScaleBeforePreparing()) {
			scaleBeforePreparing = optionsController.getScaleBeforePreparing();
			changed = true;			
		}

		//changed |= updateBaseCrop();
		changed |= updateToBrickedPixelsSizeScaler();
		if(changed)
			pipeline.invalidate();
	}

	@Override 
	public void paintComponent(Graphics g) {
		if(inImage == null)
			return;
		Graphics2D g2 = (Graphics2D)g;

		Dimension size = getSize();
		// Reduce with 4 pixels to allow crop marked around image:
		size.height-=4;
		size.width-=4;
		if(size.width <= 4 || size.height <= 4)
			return;
		
		fullScaler.setWidth(size.width);
		fullScaler.setHeight(size.height);
		
		if(cropper.isEnabled()) {
			BufferedImage full = inImage;
			full = fullScaler.transform(full);
			int x = 0;
			int y = 0;
			int w = full.getWidth();
			int h = full.getHeight();
			if(w < size.width) {
				x = (size.width-w)/2;
			}
			cropper.setMouseImageRect(new Rectangle(x, y, w, h)); // Where the mouse is in the image.
			g2.drawImage(cropper.drawImageWithCropRectAndRedLines(full), null, x, 0);

			Rectangle r = cropper.getCrop(x, y, w, h);
			cropScaler.setWidth(r.width);
			cropScaler.setHeight(r.height);

			g2.drawImage(cropScaler.transform(ImagePreparingView.this.preparedImage), null, x+r.x+2, r.y+2);
		}
		else {
			BufferedImage fullScaled = ImagePreparingView.this.preparedImage;
			if(fullScaled == null)
				return; // not ready yet.
			if(scaleBeforePreparing) {
				noCropScaler.setWidth(inImage.getWidth());
				noCropScaler.setHeight(inImage.getHeight());
				fullScaled = noCropScaler.transform(fullScaled);				
			}

			fullScaled = fullScaler.transform(fullScaled);
			int w = fullScaled.getWidth();
			if(w < size.width) {
				g2.translate((size.width-w)/2, 0);
			}
			cropper.setMouseImageRect(new Rectangle(0, 0, fullScaled.getWidth(), fullScaled.getHeight()));
			g2.drawImage(fullScaled, null, 2, 2);
		}
	}

	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		sharpness.set((Float)model.get(BrickGraphicsState.PrepareSharpness));
		gamma.set((float[])model.get(BrickGraphicsState.PrepareGamma));
		brightness.set((float[])model.get(BrickGraphicsState.PrepareBrightness));
		contrast.set((float[])model.get(BrickGraphicsState.PrepareContrast));
		saturation.set((Float)model.get(BrickGraphicsState.PrepareSaturation));

		// TODO: ToolBar should just do this itself...
		toolBar.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));
		toolBar.reloadModel(model);
		//updateBaseImages();
	}

	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareSharpness, sharpness.get());
		model.set(BrickGraphicsState.PrepareGamma, gamma.get());
		model.set(BrickGraphicsState.PrepareBrightness, brightness.get());
		model.set(BrickGraphicsState.PrepareContrast, contrast.get());
		model.set(BrickGraphicsState.PrepareSaturation, saturation.get());
		model.set(BrickGraphicsState.PrepareFiltersEnabled, toolBar.isVisible());
	}
}
