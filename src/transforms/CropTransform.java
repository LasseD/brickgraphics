package transforms;

import icon.Icons;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mosaic.rendering.ProgressCallback;
import mosaic.ui.Cropper;

public class CropTransform extends BufferedTransform {
	private Cropper cropper;
	
	public CropTransform(Cropper cropper) {
		this.cropper = cropper;
		cropper.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				clearBuffer();
			}
		});
	}

	@Override
	public Dimension getTransformedSize(Dimension in) {
		if(!cropper.isEnabled())
			return in;
		
		int w = in.width;
		int h = in.height;
		Rectangle r = cropper.getCrop(0, 0, w, h);
		return new Dimension(r.width, r.height);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in, ProgressCallback cb) {
		if(!cropper.isEnabled())
			return in;
		
		int w = in.getWidth();
		int h = in.getHeight();
		Rectangle r = cropper.getCrop(0, 0, w, h);
		cb.reportProgress(100);
		return in.getSubimage(r.x, r.y, r.width, r.height);		
	}

	@Override
	public void paintIcon(Graphics2D g, int size) {
		Icons.crop(size).paintIcon(null, g, 0, 0);
	}
}
