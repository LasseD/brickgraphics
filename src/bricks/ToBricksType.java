package bricks;

import icon.*;
import java.awt.image.*;
import colors.LEGOColor;
import transforms.*;

/**
 * @author ld
 */
public enum ToBricksType {
	STUD_FROM_TOP(Icons.studFromTop(1), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getStudTileTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	TILE_FROM_TOP(Icons.tileFromTop(), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getStudTileTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_FROM_SIDE(Icons.plateFromSide(), SizeInfo.BRICK_WIDTH, SizeInfo.PLATE_HEIGHT) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	BRICK_FROM_SIDE(Icons.brickFromSide(), SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_HEIGHT) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	},
	SNOT_IN_2_BY_2(Icons.snot(), SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			//long start = System.currentTimeMillis();
			BufferedImage normal = tbt.getPlateTransform().transform(in);
			BufferedImage sideways = tbt.getSidePlateTransform().transform(in);

			LEGOColorTransform mainTransform = tbt.getMainTransform();
			
			LEGOColor[][] normalColors = mainTransform.lcTransform(normal);
			LEGOColor[][] sidewaysColors = mainTransform.lcTransform(sideways);			

			in = tbt.getBasicTransform().transform(in);
			//System.out.println("SNOT transform prepared in: " + (System.currentTimeMillis()-start) + "ms.");
			BufferedImage res = tbt.bestMatch(normalColors, sidewaysColors, in);
			//System.out.println("SNOT written in: " + (System.currentTimeMillis()-start) + "ms. total.");			
			return res;
		}
	}, 
	TWO_BY_TWO_PLATES_FROM_TOP(Icons.studFromTop(2), SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getTwoByTwoTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	};
	
	private ToBricksIcon icon;
	/**
	 * Unit width and height is the indivisible size of the ToBricksType
	 */
	private int unitWidth, unitHeight;

	private ToBricksType(ToBricksIcon icon, int dw, int dh) {
		this.icon = icon;
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
	
	public ToBricksIcon getIcon() {
		return icon;
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
