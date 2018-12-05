package colors;

import java.awt.Color;

/**
 * Stolen from: https://github.com/THEjoezack/ColorMine/blob/master/ColorMine/ColorSpaces/Comparisons/Cie94Comparison.cs
 * Color difference: dE < 1.0 => indistinguishable 
 * For Lab2000 use: https://github.com/THEjoezack/ColorMine/blob/master/ColorMine/ColorSpaces/Comparisons/CieDe2000Comparison.cs (not converted to Java here)
 * @author THEjoezack at github
 */
public class ColorDifference {
	public static double diffCIE94(Color a, Color b) {
		int[] labA = new int[3];
		CIELab.rgb2lab(a.getRed(), a.getGreen(), a.getBlue(), labA);
		int[] labB = new int[3];		
		CIELab.rgb2lab(b.getRed(), b.getGreen(), b.getBlue(), labB);
		return diffCIE94(labA, labB);
	}
	
	public static double diffCIE94(Color a, int b) {
		int[] labA = new int[3];
		CIELab.rgb2lab(a.getRed(), a.getGreen(), a.getBlue(), labA);
		int[] labB = new int[3];		
		CIELab.rgb2lab(LEGOColorLookUp.getRed(b), LEGOColorLookUp.getGreen(b), LEGOColorLookUp.getBlue(b), labB);
		return diffCIE94(labA, labB);
	}
	
	public static final double Kl = 1.0;
	public static final double K1 = .045;
	public static final double K2 = .015;
	
	public static double diffCIE94(int[] labA, int[] labB) {
		final int deltaL = labA[0] - labB[0];
		final int deltaA = labA[1] - labB[1];
		final int deltaB = labA[2] - labB[2];
	
		final double c1 = Math.sqrt(labA[1]*labA[1] + labA[2]*labA[2]);
		final double c2 = Math.sqrt(labB[1]*labB[1] + labB[2]*labB[2]);
		final double deltaC = c1 - c2;
	
		double deltaH = deltaA*deltaA + deltaB*deltaB - deltaC*deltaC;
		deltaH = deltaH < 0 ? 0 : Math.sqrt(deltaH);
	
		final double sl = 1.0;
		final double kc = 1.0;
		final double kh = 1.0;
	
		final double sc = 1.0 + K1*c1;
		final double sh = 1.0 + K2*c1;
	
		final double i = deltaL/(Kl*sl) * deltaL/(Kl*sl) +
						 deltaC/(kc*sc) * deltaC/(kc*sc) +
						 deltaH/(kh*sh) * deltaH/(kh*sh);
		           
		return i < 0 ? 0 : Math.sqrt(i);
	}
	
	public static double diffRGB(int r, int g, int b, Color c) {
		double R = r-c.getRed();
		double G = g-c.getGreen();
		double B = b-c.getBlue();
		return R*R+G*G+B*B;
	}
}
