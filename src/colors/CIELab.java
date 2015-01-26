package colors;

import java.awt.color.ColorSpace;

/**
 * Stolen from: http://stackoverflow.com/questions/4593469/java-how-to-convert-rgb-color-to-cie-lab
 * rgb2lab stolen from: http://www.brucelindbloom.com (different author and license)
 * @author finnw at stackoverflow.com
 * @license http://creativecommons.org/licenses/by-sa/2.5/
 */
public class CIELab extends ColorSpace {
	public static void rgb2lab(int R, int G, int B, int[] lab) {
	    //http://www.brucelindbloom.com

	    float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
	    float Ls, as, bs;
	    float eps = 216.f/24389.f;
	    float k = 24389.f/27.f;

	    float Xr = 0.964221f;  // reference white D50
	    float Yr = 1.0f;
	    float Zr = 0.825211f;

	    // RGB to XYZ
	    r = R/255f; //R 0..1
	    g = G/255f; //G 0..1
	    b = B/255f; //B 0..1

	    // assuming sRGB (D65)
	    if (r <= 0.04045)
	        r = r/12;
	    else
	        r = (float) Math.pow((r+0.055)/1.055,2.4);

	    if (g <= 0.04045)
	        g = g/12;
	    else
	        g = (float) Math.pow((g+0.055)/1.055,2.4);

	    if (b <= 0.04045)
	        b = b/12;
	    else
	        b = (float) Math.pow((b+0.055)/1.055,2.4);

	    X =  0.436052025f*r     + 0.385081593f*g + 0.143087414f *b;
	    Y =  0.222491598f*r     + 0.71688606f *g + 0.060621486f *b;
	    Z =  0.013929122f*r     + 0.097097002f*g + 0.71418547f  *b;

	    // XYZ to Lab
	    xr = X/Xr;
	    yr = Y/Yr;
	    zr = Z/Zr;

	    if ( xr > eps )
	        fx =  (float) Math.pow(xr, 1/3.);
	    else
	        fx = (float) ((k * xr + 16.) / 116.);

	    if ( yr > eps )
	        fy =  (float) Math.pow(yr, 1/3.);
	    else
	    fy = (float) ((k * yr + 16.) / 116.);

	    if ( zr > eps )
	        fz =  (float) Math.pow(zr, 1/3.);
	    else
	        fz = (float) ((k * zr + 16.) / 116);

	    Ls = ( 116 * fy ) - 16;
	    as = 500*(fx-fy);
	    bs = 200*(fy-fz);

	    lab[0] = (int) (2.55*Ls + .5);
	    lab[1] = (int) (as + .5); 
	    lab[2] = (int) (bs + .5);       
	} 
	
	public static final long serialVersionUID = 5027741380892134289L;
    public static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);
    public static final double N = 4.0 / 29.0;

    public static final CIELab INSTANCE = new CIELab();

    private CIELab() {
        super(ColorSpace.TYPE_Lab, 3);
    }

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        double l = f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorvalue[0]) - l);
        double b = 200.0 * (l - f(colorvalue[2]));
        return new float[] {(float) L, (float) a, (float) b};
    }

    @Override
    public float[] fromRGB(float[] rgbvalue) {
        float[] xyz = CIEXYZ.fromRGB(rgbvalue);
        return fromCIEXYZ(xyz);
    }

    @Override
    public float getMaxValue(int component) {
        return 128f;
    }

    @Override
    public float getMinValue(int component) {
        return (component == 0)? 0f: -128f;
    }    

    @Override
    public String getName(int idx) {
        return String.valueOf("Lab".charAt(idx));
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        double i = (colorvalue[0] + 16.0) * (1.0 / 116.0);
        double X = fInv(i + colorvalue[1] * (1.0 / 500.0));
        double Y = fInv(i);
        double Z = fInv(i - colorvalue[2] * (1.0 / 200.0));
        return new float[] {(float) X, (float) Y, (float) Z};
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        float[] xyz = toCIEXYZ(colorvalue);
        return CIEXYZ.toRGB(xyz);
    }

    private static double f(double x) {
        if (x > 216.0 / 24389.0) {
            return Math.cbrt(x);
        } 
        return (841.0 / 108.0) * x + N;
    }

    private static double fInv(double x) {
        if (x > 6.0 / 29.0) {
            return x*x*x;
        } 
        return (108.0 / 841.0) * (x - N);
    }
}
