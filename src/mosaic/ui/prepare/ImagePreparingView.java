package mosaic.ui.prepare;

import io.*;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.actions.*;
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import transforms.*;

/**
 * crop, sharpness, gamma, brightness, contrast, saturation
 */
public class ImagePreparingView extends JComponent implements ChangeListener, ModelSaver<BrickGraphicsState> {
	private static final long serialVersionUID = -1814153546542713621L;
	private BufferedImage inImage, prepared, baseImage, baseCrop;
	private Cropper cropper;
	private JComponent imageDisplayer;
	private List<ChangeListener> listeners;

	private CropEnable crop;
	private RGBTransform brightness, gamma, contrast;
	private StateTransform<Float> saturation, sharpness;
	private Dimension imageRestriction; // not if null

	private List<StateTransformRanker> transforms;
	private StateTransformRanker brightnessR, gammaR, contrastR, saturationR, sharpnessR;
	private int maxRank;
	private ImagePreparingToolBar toolBar;

	public ImagePreparingView(BufferedImage inImage, Model<BrickGraphicsState> model) {
		this.inImage = inImage;
		model.addModelSaver(this);

		crop = new CropEnable(this);
		cropper = new Cropper(model, crop);
		crop.putValue(Action.SELECTED_KEY, (Boolean)model.get(BrickGraphicsState.PrepareCropEnabled));

		{
			transforms = new ArrayList<StateTransformRanker>(5);

			sharpness = new SharpnessTransform((Float)model.get(BrickGraphicsState.PrepareSharpness));
			sharpnessR = new StateTransformRanker(sharpness, model, BrickGraphicsState.PrepareSharpnessRank);
			transforms.add(sharpnessR);
			brightness = new BrightnessTransform((float[])model.get(BrickGraphicsState.PrepareBrightness));
			brightnessR = new StateTransformRanker(brightness, model, BrickGraphicsState.PrepareBrightnessRank);
			transforms.add(brightnessR);
			gamma = new GammaTransform((float[])model.get(BrickGraphicsState.PrepareGamma));
			gammaR = new StateTransformRanker(gamma, model, BrickGraphicsState.PrepareGammaRank);
			transforms.add(gammaR);
			contrast = new ContrastTransform((float[])model.get(BrickGraphicsState.PrepareContrast));
			contrastR = new StateTransformRanker(contrast, model, BrickGraphicsState.PrepareContrastRank);
			transforms.add(contrastR);
			saturation = new SaturationTransform((Float)model.get(BrickGraphicsState.PrepareSaturation));
			saturationR = new StateTransformRanker(saturation, model, BrickGraphicsState.PrepareSaturationRank);
			transforms.add(saturationR);

			updateMaxRank();
			Collections.sort(transforms);		
		}

		listeners = new LinkedList<ChangeListener>();

		cropper.addChangeListener(this);
		addMouseMotionListener(cropper);
		addMouseListener(cropper);

		setLayout(new BorderLayout());
		imageDisplayer = new ImageDisplayComponent();
		add(imageDisplayer, BorderLayout.CENTER);

		toolBar = new ImagePreparingToolBar(this, model);
		JPanel toolBarPanel = new JPanel();
		toolBarPanel.add(toolBar);		
		add(toolBarPanel, BorderLayout.EAST);

		updateBaseImages();
		rePrepairImage(null);
	}
	
	private void updateMaxRank() {
		maxRank = 0;
		for(StateTransformRanker s : transforms) {
			maxRank = Math.max(maxRank, s.getRank());
		}
	}

	public Action getCropEnabledAction() {
		return crop;
	}

	public void loadModel(Model<BrickGraphicsState> model) {
		sharpness.set((Float)model.get(BrickGraphicsState.PrepareSharpness));
		brightness.set((float[])model.get(BrickGraphicsState.PrepareBrightness));
		gamma.set((float[])model.get(BrickGraphicsState.PrepareGamma));
		contrast.set((float[])model.get(BrickGraphicsState.PrepareContrast));
		saturation.set((Float)model.get(BrickGraphicsState.PrepareSaturation));

		for(StateTransformRanker s : transforms) {
			s.load(model);
		}
		Collections.sort(transforms);
		updateMaxRank();

		if((Boolean)model.get(BrickGraphicsState.ImageRestrictionEnabled)) {
			imageRestriction = (Dimension)model.get(BrickGraphicsState.ImageRestriction);
		}
		else {
			imageRestriction = null;
		}

		if(toolBar != null)
			toolBar.reloadModel(model);
		updateBaseImages();
	}

	public void updateBaseImages() {
		if(imageRestriction == null || inImage.getWidth() < imageRestriction.width && 
								       inImage.getHeight() < imageRestriction.height) {
			baseImage = inImage;
		}
		else {
			ScaleTransform scaler = new ScaleTransform(ScaleTransform.Type.bounded, 
					AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
			scaler.setWidth(imageRestriction.width);
			scaler.setHeight(imageRestriction.height);
			baseImage = scaler.transform(inImage);
		}
		
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(w, h);
		//System.out.println(r);
		baseCrop = baseImage.getSubimage(r.x, r.y, r.width, r.height);		
	}
	
	private Rectangle baseCropRect;
	public void updateBaseCrop() {
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(w, h);
		//System.out.println(r);
		if(baseCropRect == null || !baseCropRect.equals(r)) {
			baseCropRect = r;
			baseCrop = baseImage.getSubimage(r.x, r.y, r.width, r.height);		
		}
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public void notifyListeners() {
		if(listeners == null)
			return;
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}

	private void rePrepairImage(StateTransformRanker source) {
		if(source != null) {
			maxRank++;
			source.setRank(maxRank);
			Collections.sort(transforms);
		}

		if(isCrop()) {
			prepared = baseCrop;
		}
		else {
			prepared = inImage;
		}
		for(StateTransformRanker t : transforms) {
			prepared = t.getTransform().transform(prepared);		
		}

		notifyListeners();
	}

	public BufferedImage getFullyPreparredImage() {
		return prepared;
	}

	public boolean isCrop() {
		return (Boolean)crop.getValue(Action.SELECTED_KEY);
	}

	public void setImage(BufferedImage image) {
		if(image == null)
			throw new IllegalArgumentException("image is null");
		this.inImage = image;
		updateBaseImages();
		rePrepairImage(null);
	}

	public void setContrast(int index, float contrast) {
		float[] get = this.contrast.get();
		get[index] = contrast;
		this.contrast.set(get);
		rePrepairImage(this.contrastR);	
	}

	public void setSaturation(float saturation) {
		this.saturation.set(saturation);
		rePrepairImage(this.saturationR);
	}

	public void update() {
		rePrepairImage(null);
	}

	public void setSharpness(float sharpness) {
		this.sharpness.set(sharpness);
		rePrepairImage(this.sharpnessR);
	}

	public void setGamma(int index, float gamma) {
		float[] get = this.gamma.get();
		get[index] = gamma;
		this.gamma.set(get);
		rePrepairImage(this.gammaR);
	}

	public void setBrightness(int index, float brightness) {
		float[] get = this.brightness.get();
		get[index] = brightness;
		this.brightness.set(get);
		rePrepairImage(this.brightnessR);
	}
	
	private Cursor cursor;
	public void updateCursor() {
		Cursor c;
		if(isCrop())
			c = cropper.getCursor();
		else
			c = Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
		if(cursor != c)
			setCursor(cursor = c);
	}

	public void stateChanged(ChangeEvent e) {
		updateCursor();
		updateBaseCrop();
		rePrepairImage(null);
		notifyListeners();
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareSharpness, sharpness.get());
		model.set(BrickGraphicsState.PrepareBrightness, brightness.get());
		model.set(BrickGraphicsState.PrepareGamma, gamma.get());
		model.set(BrickGraphicsState.PrepareContrast, contrast.get());
		model.set(BrickGraphicsState.PrepareSaturation, saturation.get());
		model.set(BrickGraphicsState.PrepareCropEnabled, isCrop());
		
		Collections.sort(transforms);
		int i = 0;
		for(StateTransformRanker s : transforms) {
			s.setRank(i);
			s.save(model);
			i++;
		}
	}

	private static class StateTransformRanker implements Comparable<StateTransformRanker> {
		private StateTransform<?> transform;
		private int rank;
		private BrickGraphicsState toSave;

		public StateTransformRanker(StateTransform<?> transform, Model<BrickGraphicsState> model, BrickGraphicsState toSave) {
			this.transform = transform;
			this.toSave = toSave;
			load(model);
		}

		public void load(Model<BrickGraphicsState> model) {
			rank = (Integer) model.get(toSave);
		}

		public int compareTo(StateTransformRanker other) {
			return rank - other.rank;
		}

		public void save(Model<BrickGraphicsState> model) {
			model.set(toSave, rank);
		}
		
		public void setRank(int rank) {
			this.rank = rank;
		}
		
		public int getRank() {
			return rank;
		}

		public StateTransform<?> getTransform() {
			return transform;
		}
	}

	private class ImageDisplayComponent extends JComponent {
		private static final long serialVersionUID = 7842211650429220481L;
		private ScaleTransform fullScaler = new ScaleTransform(ScaleTransform.Type.bounded, 
				AffineTransformOp.TYPE_BILINEAR, 2);
		private ScaleTransform cropScaler = new ScaleTransform(ScaleTransform.Type.bounded, 
				AffineTransformOp.TYPE_BILINEAR);

		public @Override void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;

			Dimension size = getSize();
			size.height-=4;
			size.width-=4;
			
			fullScaler.setWidth(size.width);
			fullScaler.setHeight(size.height);

			if(isCrop()) {
				BufferedImage full = ImagePreparingView.this.baseImage;
				full = fullScaler.transform(full);
				cropper.setMouseImage(full);
				g2.drawImage(cropper.pollute(full), null, 0, 0);

				int w = full.getWidth();
				int h = full.getHeight();
				Rectangle r = cropper.getCrop(w, h);
				cropScaler.setWidth(r.width);
				cropScaler.setHeight(r.height);

				g2.drawImage(cropScaler.transform(ImagePreparingView.this.prepared), null, r.x+2, r.y+2);
			}
			else {
				BufferedImage fullScaled = fullScaler.transform(ImagePreparingView.this.prepared);
				cropper.setMouseImage(fullScaled);
				g2.drawImage(fullScaled, null, 2, 2);					
			}
		}
	};
}
