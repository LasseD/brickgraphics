package transforms;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
//import java.awt.Transparency;
//import java.awt.geom.*;
import java.awt.image.*;

public class ScaleTransform extends BufferedTransform {
	private int width, height;
	private double scaleX, scaleY;
	private boolean bounded;
	private ScaleQuality quality;
	private String usage;

	public ScaleTransform(String usage, boolean bounded, ScaleQuality quality, int bufferSize) {
		super(bufferSize);
		this.bounded = bounded;
		this.quality = quality;
		this.usage = usage;
	}

	public ScaleTransform(String usage, boolean bounded, ScaleQuality quality) {
		this(usage, bounded, quality, 1);
	}
	
	public boolean setQuality(ScaleQuality quality) {
		if(this.quality == quality)
			return false;
		this.quality = quality;		
		clearBuffer();
		return true;
	}
	
	public boolean setWidth(int width) {
		if(this.width == width)
			return false;
		this.width = width;
		clearBuffer();
		return true;
	}
	public int getWidth() {
		return width;
	}
	public boolean setHeight(int height) {
		if(this.height == height)
			return false;
		this.height = height;	
		clearBuffer();
		return true;
	}
	public int getHeight() {
		return height;
	}
	public boolean setScaleX(double x) {
		if(scaleX == x)
			return false;
		scaleX = x;
		clearBuffer();
		return true;
	}
	public boolean setScaleY(double y) {
		if(scaleY == y)
			return false;
		scaleY = y;
		clearBuffer();
		return true;
	}
	
	public Scale getScale(double inX, double inY) {
		double scaleX = width / inX;
		double scaleY = height / inY;
		if(!bounded)
			return new Scale(scaleX, scaleY);
		
		double scale;
		if(inX / inY > width / (double)height) {
			scale = scaleX;	
		}
		else {
			scale = scaleY;
		}
		return new Scale(scale, scale);
	}
	
	@Override
	public BufferedImage transformUnbuffered(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();
		if(w <= 0 || h <= 0 || width <= 0 || height <= 0)
			return in;
				
		Scale scale = getScale(w, h);
		
		w = (int)Math.round(scale.w*w);
		h = (int)Math.round(scale.h*h);

        BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Object renderingHint = quality.renderingHint;
        if(renderingHint == null) {
        	// Fill arrays:
        	int fromPixels[] = new int[in.getWidth() * in.getHeight()];
        	fromPixels = in.getRGB(0, 0, in.getWidth(), in.getHeight(), fromPixels, 0, in.getWidth());
        	int rgbArray[] = new int[w*h];

        	// First the dimensional arrays:
        	int xArray[] = new int[w];
        	for(int x = 0; x < w; ++x)
        		xArray[x] = (int)(x*in.getWidth()/(double)w);
        	
        	// Fill the output array:
        	for(int y = 0; y < h; ++y) {
        		int fromY = (int)(y*in.getHeight()/(double)h);
            	for(int x = 0; x < w; ++x) {
            		rgbArray[y*w + x] = fromPixels[fromY*in.getWidth() + xArray[x]];
            	}        		
        	}        	        	
        	resized.setRGB(0, 0, w, h, rgbArray, 0, w);
        }
        else {
            Graphics2D g2 = resized.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderingHint);
            g2.drawImage(in, 0, 0, w, h, null);        	
            g2.dispose();
        }
		
        //System.out.print(usage + ": " + in.getWidth() + "x" + in.getHeight() + "->" + resized.getWidth() + "x" + resized.getHeight() + ". ");
		return resized;
	}
	
	private static class Scale {
		public Scale(double w, double h) {
			this.w = w;
			this.h = h;
		}
		public double w, h;
	}
	
	public enum ScaleQuality {
		BiCubic("Bicubic (slow)", RenderingHints.VALUE_INTERPOLATION_BICUBIC), 
		BiLinear("Bilinear (medium speed)", RenderingHints.VALUE_INTERPOLATION_BILINEAR), 
		NearestNeighbor("Nearest neighbor (fast)", RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR),
		RetainColors("Nearest neighbor, don't change colors (fast)", null);
		
		public Object renderingHint;
		public String title;
		
		private ScaleQuality(String title, Object renderingHint) {
			this.title = title;
			this.renderingHint = renderingHint;
		}
	}

	@Override
	public Dimension getTransformedSize(BufferedImage in) {
		int w = in.getWidth();
		int h = in.getHeight();
		if(w <= 0 || h <= 0 || width <= 0 || height <= 0)
			return new Dimension(0,0);
				
		Scale scale = getScale(w, h);
		
		w = (int)Math.round(scale.w*w);
		h = (int)Math.round(scale.h*h);
		return new Dimension(w, h);
	}
}
