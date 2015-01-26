package transforms;

import java.awt.geom.*;
import java.awt.image.*;

public class ScaleTransform extends BufferedTransform {
	public enum Type {
		width {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				AffineTransform scaler = AffineTransform.getScaleInstance(scaleX, scaleX);
				return new AffineTransformOp(scaler, st.quality);
			}
		},
		height {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				double scaleY = st.height / inY;				
				AffineTransform scaler = AffineTransform.getScaleInstance(scaleY, scaleY);
				return new AffineTransformOp(scaler, st.quality);
			}
		}, 
		dims {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				double scaleY = st.height / inY;
				AffineTransform scaler = AffineTransform.getScaleInstance(scaleX, scaleY);
				return new AffineTransformOp(scaler, st.quality);
			}
		}, 
		scaleX {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				AffineTransform scaler = AffineTransform.getScaleInstance(st.scaleX, st.scaleX);
				return new AffineTransformOp(scaler, st.quality);
			}
		}, 
		scaleY {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				AffineTransform scaler = AffineTransform.getScaleInstance(st.scaleY, st.scaleY);
				return new AffineTransformOp(scaler, st.quality);
			}
		}, 
		scale {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				AffineTransform scaler = AffineTransform.getScaleInstance(st.scaleX, st.scaleY);
				return new AffineTransformOp(scaler, st.quality);
			}
		}, 
		bounded {
			@Override
			public AffineTransformOp op(double inX, double inY, ScaleTransform st) {
				double scaleX = st.width / inX;
				double scaleY = st.height / inY;
				double scale;
				if(inX / inY > st.width / (double)st.height) {
					scale = scaleX;	
				}
				else {
					scale = scaleY;
				}
				AffineTransform scaler = AffineTransform.getScaleInstance(scale, scale);
				return new AffineTransformOp(scaler, st.quality);
			}
		};		

		public abstract AffineTransformOp op(double inX, double inY, ScaleTransform st);
	}
	private int quality, width, height;
	private double scaleX, scaleY;
	private Type type;

	public ScaleTransform(int quality) {
		this(null, quality, 1);
	}
	
	public ScaleTransform(Type type, int quality, int bufferSize) {
		super(bufferSize);
		if(!(quality == AffineTransformOp.TYPE_NEAREST_NEIGHBOR || 
			 quality == AffineTransformOp.TYPE_BICUBIC || 
			 quality == AffineTransformOp.TYPE_BILINEAR))
			throw new IllegalArgumentException("quality not known!");
		this.type = type;
		this.quality = quality;
	}

	public ScaleTransform(Type type, int quality) {
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
		return type.op(w, h, this).filter(in, null);
	}
}
