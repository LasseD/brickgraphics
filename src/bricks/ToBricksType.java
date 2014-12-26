package bricks;

import java.awt.image.*;
import javax.swing.*;

import colors.LEGOColor;
import transforms.*;
import ui.*;

/**
 * @author ld
 */
public enum ToBricksType {
	STUD_FROM_TOP(Icons.studFromTop(Icons.SIZE_LARGE, 1, true), Icons.studFromTop(Icons.SIZE_LARGE, 1, false), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getStudTileTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	TILE_FROM_TOP(Icons.tileFromTop(Icons.SIZE_LARGE, true), Icons.tileFromTop(Icons.SIZE_LARGE, false), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getStudTileTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_FROM_SIDE(Icons.plateFromSide(Icons.SIZE_LARGE, true), Icons.plateFromSide(Icons.SIZE_LARGE, false), SizeInfo.BRICK_WIDTH, SizeInfo.PLATE_HEIGHT) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	BRICK_FROM_SIDE(Icons.brickFromSide(Icons.SIZE_LARGE, true), Icons.brickFromSide(Icons.SIZE_LARGE, false), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_HEIGHT) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	},
	SNOT_IN_2_BY_2(Icons.snot(Icons.SIZE_LARGE, true), Icons.snot(Icons.SIZE_LARGE, false), SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			BufferedImage normal = tbt.getPlateTransform().transform(in);
			BufferedImage sideways = tbt.getSidePlateTransform().transform(in);

			LEGOColorTransform mainTransform = tbt.getMainTransform();
			
			LEGOColor[][] normalColors = mainTransform.lcTransform(normal);
			normal = mainTransform.transform(normal);
			
			LEGOColor[][] sidewaysColors = mainTransform.lcTransform(sideways);			
			sideways = mainTransform.transform(sideways);

			in = tbt.getBasicTransform().transform(in);
			return tbt.bestMatch(normal, normalColors, sideways, sidewaysColors, in);
		}
	}, 
	TWO_BY_TWO_PLATES_FROM_TOP(Icons.studFromTop(Icons.SIZE_LARGE, 2, true), Icons.studFromTop(Icons.SIZE_LARGE, 2, false), SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getTwoByTwoTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	};
	
	private Icon enabledIcon, disabledIcon;
	/**
	 * Unit width and height is the indivisible size of the ToBricksType
	 */
	private int unitWidth, unitHeight;

	private ToBricksType(Icon enabledIcon, Icon disabledIcon, int dw, int dh) {
		this.enabledIcon = enabledIcon;
		this.disabledIcon = disabledIcon;
		unitWidth = dw;
		unitHeight = dh;
	}

	/**
	 * @return The indivisible unit width, such as 5 for a brick and 10 for SNOT.
	 */
	public int getUnitWidth() {
		return unitWidth;
	}

	/**
	 * @return The indivisible unit height, such as 6 for a brick and 10 for SNOT.
	 */
	public int getUnitHeight() {
		return unitHeight;
	}
	
	/**
	 * Changes unit into scm(unit, unitWidth) and finds the nearest width in this changed unit.
	 * @param widthInBasicUnits This is the width in basic units. The basic width of a brick is 5. 
	 * @param unit The amount to be scm'd with unitWidth in order to compute the actual unit to be rounded to.
	 * @return result >= unit
	 */
	public int closestCompatibleWidth(int widthInBasicUnits, int unit) {
		unit = scm(unit, unitWidth);
		int ret = Math.round((widthInBasicUnits / (float)unit)) * unit;
		if(ret <= 0)
			return unit;
		return ret;
	}
	
	/**
	 * Changes unit into scm(unit, unitHeight) and finds the nearest height in this changed unit.
	 * @param heightInBasicUnits This is the height in basic units. The basic height of a brick is 6. 
	 * @param unit The amount to be scm'd with unitHeight in order to compute the actual unit to be rounded to.
	 * @return result >= unit
	 */
	public int closestCompatibleHeight(int heightInBasicUnits, int unit) {
		unit = scm(unit, unitHeight);
		int ret = Math.round((heightInBasicUnits) / (float)unit) * unit;
		if(ret <= 0)
			return unit;
		return ret;
	}
	
	public Icon getEnabledIcon() {
		return enabledIcon;
	}
	public Icon getDisabledIcon() {
		return disabledIcon;
	}
	
	/**
	 * Smallest common multiple (always <= a*b)
	 */
	private static int scm(int a, int b) {
		if(a > b) {
			int tmp = a;
			a = b;
			b = tmp;
		}
		int scm = b;
		while(scm % a != 0) {
			scm += b;
		}
		return scm;
	}
	public abstract BufferedImage transform(BufferedImage in, ToBricksTransform tbt);
}
