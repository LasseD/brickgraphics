package transforms;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
//import java.awt.Transparency;
//import java.awt.geom.*;
import java.awt.image.*;

public class ScaleTransform extends BufferedTransform {
	public enum Type {
		width {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				return new Scale(scaleX, scaleX);
			}
		},
		height {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				double scaleY = st.height / inY;				
				return new Scale(scaleY, scaleY);
			}
		}, 
		dims {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				double scaleY = st.height / inY;
				return new Scale(scaleX, scaleY);
			}
		}, 
		scaleX {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				return new Scale(st.scaleX, st.scaleX);
			}
		}, 
		scaleY {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				return new Scale(st.scaleY, st.scaleY);
			}
		}, 
		scale {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				return new Scale(st.scaleX, st.scaleY);
			}
		}, 
		bounded {
			@Override
			public Scale getScale(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				double scaleY = st.height / inY;
				double scale;
				if(inX / inY > st.width / (double)st.height) {
					scale = scaleX;	
				}
				else {
					scale = scaleY;
				}
				return new Scale(scale, scale);
			}
		};		

		public abstract Scale getScale(double inX, double inY, ScaleTransform st);
	}
	private int width, height;
	private double scaleX, scaleY;
	private Type type;
	private Object quality;

	public ScaleTransform(int quality) {
		this(null, quality, 1);
	}
	
	public ScaleTransform(Type type, Object quality, int bufferSize) {
		super(bufferSize);
		if(!(quality == RenderingHints.VALUE_INTERPOLATION_BICUBIC || 
			 quality == RenderingHints.VALUE_INTERPOLATION_BILINEAR || 
			 quality == RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR))
			throw new IllegalArgumentException("quality not known!");
		this.type = type;
		this.quality = quality;
	}

	public ScaleTransform(Type type, Object quality) {
		this(type, quality, 1);
	}
	
	public void setType(Type type) {
		if(this.type == type)
			return;
		this.type = type;
		clearBuffer();
	}

	public void setWidth(int width) {
		if(this.width == width)
			return;
		this.width = width;
		clearBuffer();
	}
	public void setHeight(int height) {
		if(this.height == height)
			return;
		this.height = height;	
		clearBuffer();
	}
	public void setScaleX(double x) {
		if(scaleX == x)
			return;
		scaleX = x;
		clearBuffer();
	}
	public void setScaleY(double y) {
		if(scaleY == y)
			return;
		scaleY = y;
		clearBuffer();
	}

	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();
		if(w <= 0 || h <= 0 || width <= 0 || height <= 0)
			return in;
		Scale scale = type.getScale(w, h, this);
		//AffineTransform scaler = AffineTransform.getScaleInstance(scale.w, scale.h);
		//AffineTransformOp op = new AffineTransformOp(scaler, quality);
		
		w = (int)Math.round(scale.w*w);
		h = (int)Math.round(scale.h*h);
		//BufferedImage outImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resized.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, quality);
        g2.drawImage(in, 0, 0, w, h, null);
        g2.dispose();
		
		return resized;//op.filter(in, outImage);
	}
	
	private static class Scale {
		public Scale(double w, double h) {
			this.w = w;
			this.h = h;
		}
		public double w, h;
	}
}
