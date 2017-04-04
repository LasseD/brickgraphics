package bricks;

import icon.*;
import java.awt.image.*;
import colors.LEGOColor;
import transforms.*;

/**
 * @author ld
 */
public enum ToBricksType {
	STUD_FROM_TOP(Icons.studsFromTop(1, 1), "1 x 1 plates seen from the top.", SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH, 1, 1) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(1, 1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_FROM_SIDE(Icons.plateFromSide(1), "1 x 1 plates seen from the side.", SizeInfo.BRICK_WIDTH, SizeInfo.PLATE_HEIGHT, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateFromSideTransform(1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	BRICK_FROM_SIDE(Icons.brickFromSide(), "1 x 1 bricks seen from the side.", SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_HEIGHT, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromSideTransform(1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	},
	SNOT_IN_2_BY_2(Icons.snot(), "1 x 1 plates and tiles placed both studs up and sideways.", SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			//long start = System.currentTimeMillis();
			BufferedImage normal = tbt.getPlateFromSideTransform(1).transform(in);
			BufferedImage sideways = tbt.getVerticalPlateFromSideTransform().transform(in);

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
	TILE_FROM_TOP(Icons.tileFromTop(), "1 x 1 tiles seen from the top.", SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(1, 1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_2_1_FROM_SIDE(Icons.plateFromSide(2), "1 x 2 plates seen from the side.", SizeInfo.BRICK_WIDTH*2, SizeInfo.PLATE_HEIGHT, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateFromSideTransform(2).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_3_1_FROM_SIDE(Icons.plateFromSide(3), "1 x 3 plates seen from the side.", SizeInfo.BRICK_WIDTH*3, SizeInfo.PLATE_HEIGHT, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateFromSideTransform(3).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	PLATE_4_1_FROM_SIDE(Icons.plateFromSide(4), "1 x 4 plates seen from the side.", SizeInfo.BRICK_WIDTH*4, SizeInfo.PLATE_HEIGHT, 0, 0) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateFromSideTransform(4).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	ONE_BY_TWO_STUDS_FROM_TOP(Icons.studsFromTop(2, 1), "1 x 2 plates seen from the top.", 2*SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH, 2, 1) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(2, 1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	ONE_BY_THREE_STUDS_FROM_TOP(Icons.studsFromTop(3, 1), "1 x 3 plates seen from the top.", 3*SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH, 3, 1) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(3, 1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	ONE_BY_FOUR_STUDS_FROM_TOP(Icons.studsFromTop(4, 1), "1 x 4 plates seen from the top.", 4*SizeInfo.BRICK_WIDTH, SizeInfo.BRICK_WIDTH, 4, 1) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(4, 1).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	TWO_BY_TWO_PLATES_FROM_TOP(Icons.studsFromTop(2, 2), "2 x 2 plates seen from the top.", SizeInfo.SNOT_BLOCK_WIDTH, SizeInfo.SNOT_BLOCK_WIDTH, 2, 2) {
		@Override
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickFromTopTransform(2, 2).transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	};
	
	private ToBricksIcon icon;
	private String description;
	/**
	 * Unit width and height is the indivisible size of the ToBricksType
	 */
	private int unitWidth, unitHeight, studsShownWide, studsShownTall;

	private ToBricksType(ToBricksIcon icon, String description, int dw, int dh, int studsShownWide, int studsShownTall) {
		this.icon = icon;
		this.description = description;
		unitWidth = dw;
		unitHeight = dh;
		this.studsShownWide = studsShownWide;
		this.studsShownTall = studsShownTall;
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
	
	public int getStudsShownWide() {
		return studsShownWide;
	}
	public int getStudsShownTall() {
		return studsShownTall;
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
	
	public String getDescription() {
		return description;
	}
	
	public static boolean[] getDefaultTypes() {
		boolean[] ret = new boolean[values().length];
		for(int i = 0; i < 5; ++i) // The first some values are default...
			ret[i] = true;
		return ret;
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
