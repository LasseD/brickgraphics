package mosaic.ui.prepare;

import io.*;
import mosaic.io.BrickGraphicsState;
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
	private List<ChangeListener> listeners;

	private RGBTransform brightness, gamma, contrast;
	private StateTransform<Float> saturation, sharpness;

	private List<StateTransformRanker> transforms;
	private StateTransformRanker brightnessR, gammaR, contrastR, saturationR, sharpnessR;
	private int maxRank;
	private ImagePreparingToolBar toolBar;
	private JPanel toolBarPanel;

	public ImagePreparingView(final Model<BrickGraphicsState> model) {
		model.addModelSaver(this);
		listeners = new LinkedList<ChangeListener>();

		cropper = new Cropper(model);		
		cropper.addChangeListener(this);
		addMouseMotionListener(cropper);
		addMouseListener(cropper);
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

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setLayout(new BorderLayout());
				JComponent imageDisplayer = new ImageDisplayComponent();
				add(imageDisplayer, BorderLayout.CENTER);
				toolBar = new ImagePreparingToolBar(ImagePreparingView.this, model);
				toolBarPanel = new JPanel();
				toolBarPanel.add(toolBar);
				add(toolBarPanel, BorderLayout.WEST);
				toolBarPanel.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));
			}
		});
	}
	
	private void updateMaxRank() {
		maxRank = 0;
		for(StateTransformRanker s : transforms) {
			maxRank = Math.max(maxRank, s.getRank());
		}
	}

	public void loadModel(Model<BrickGraphicsState> model) {
		sharpness.set((Float)model.get(BrickGraphicsState.PrepareSharpness));
		brightness.set((float[])model.get(BrickGraphicsState.PrepareBrightness));
		gamma.set((float[])model.get(BrickGraphicsState.PrepareGamma));
		contrast.set((float[])model.get(BrickGraphicsState.PrepareContrast));
		saturation.set((Float)model.get(BrickGraphicsState.PrepareSaturation));
		if(toolBarPanel != null)
			toolBarPanel.setVisible((Boolean)model.get(BrickGraphicsState.PrepareFiltersEnabled));

		for(StateTransformRanker s : transforms) {
			s.load(model);
		}
		Collections.sort(transforms);
		updateMaxRank();

		if(toolBar != null)
			toolBar.reloadModel(model);
		updateBaseImages();
	}

	public void updateBaseImages() {
		if(inImage == null)
			return;
		baseImage = inImage;
		
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(w, h);
		baseCrop = baseImage.getSubimage(r.x, r.y, r.width, r.height);		
	}
	
	private Rectangle baseCropRect;
	public void updateBaseCrop() {
		int w = baseImage.getWidth();
		int h = baseImage.getHeight();
		Rectangle r = cropper.getCrop(w, h);
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
		if(inImage == null || inImage.getWidth() == 0 || inImage.getHeight() == 0)
			return;
		if(source != null) {
			maxRank++;
			source.setRank(maxRank);
			Collections.sort(transforms);
		}

		if(cropper.isEnabled()) {
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
	
	public void setImage(BufferedImage image) {
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

	public void setSharpness(float sharpness) {
		this.sharpness.set(sharpness);
		rePrepairImage(this.sharpnessR);
	}
	
	public void switchCropState() {
		cropper.switchEnabled();
		rePrepairImage(null);
	}
	
	public void switchFiltersEnabled() {
		toolBarPanel.setVisible(!toolBarPanel.isVisible());
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
		if(cropper.isEnabled())
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
		model.set(BrickGraphicsState.PrepareFiltersEnabled, toolBarPanel != null && toolBarPanel.isVisible());
		
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

		@Override 
		public void paintComponent(Graphics g) {
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
