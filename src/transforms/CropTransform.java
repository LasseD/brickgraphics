package transforms;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	public Dimension getTransformedSize(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();

		if(!cropper.isEnabled())
			return new Dimension(w, h);
		
		Rectangle r = cropper.getCrop(0, 0, w, h);
		return new Dimension(r.width, r.height);
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		if(!cropper.isEnabled())
			return in;
		
		int w = in.getWidth();
		int h = in.getHeight();
		Rectangle r = cropper.getCrop(0, 0, w, h);
		return in.getSubimage(r.x, r.y, r.width, r.height);		
	}
}
