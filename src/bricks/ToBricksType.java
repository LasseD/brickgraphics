package bricks;

import java.awt.image.*;

import javax.swing.*;

import mosaic.ui.*;

import colors.LEGOColor;
import transforms.*;

import ui.*;
public enum ToBricksType {
	stud(Icons.stud(Icons.SIZE_LARGE), Sizes.brick.width(), Sizes.brick.width()) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getStudTileTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	plate(Icons.plate(Icons.SIZE_LARGE), Sizes.plate.width(), Sizes.plate.height()) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getPlateTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	}, 
	snot(Icons.snot(Icons.SIZE_LARGE), Sizes.block.width(), Sizes.block.height()) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			BufferedImage normal = tbt.getPlateTransform().transform(in);
			BufferedImage sideways = tbt.getSidePlateTransform().transform(in);

			LEGOColorTransform mainTransform = tbt.getMainTransform();
			
			LEGOColor[][] normalColors = mainTransform.lcTransform(normal);
			normal = mainTransform.transform(normal);
			
			LEGOColor[][] sidewaysColors = mainTransform.lcTransform(sideways);			
			sideways = mainTransform.transform(sideways);

			in = tbt.getBasicTransform().transform(in);
			//assert in.getWidth() == normal.getWidth()*5 : in.getWidth() + "!=" + normal.getWidth()*5;
			//assert in.getHeight() == normal.getHeight()*2 : in.getHeight() + "!=" + normal.getHeight()*2;
			return tbt.bestMatch(normal, normalColors, sideways, sidewaysColors, in);
		}
	}, 
	brick(Icons.brick(Icons.SIZE_LARGE), Sizes.brick.width(), Sizes.brick.height()) {
		public BufferedImage transform(BufferedImage in, ToBricksTransform tbt) {
			in = tbt.getBrickTransform().transform(in);
			in = tbt.getMainTransform().transform(in);
			in = tbt.getRTransform().transform(in);
			return in;
		}
	};
	
	private Icon icon;
	private int divForWidth, divForHeight;

	private ToBricksType(Icon icon, int dw, int dh) {
		this.icon = icon;
		divForWidth = dw;
		divForHeight = dh;
	}

	public int getUnitWidth() {
		return divForWidth;
	}
	public int getUnitHeight() {
		return divForHeight;
	}
	
	public int closestCompatibleWidth(int widthInBasicUnits, int unit) {
		unit = scm(unit, divForWidth);
		int ret = Math.round((widthInBasicUnits / (float)unit)) * unit;
		if(ret <= 0)
			return unit;
		return ret;
	}
	
	public int closestCompatibleHeight(int heightInBasicUnits, int unit) {
		unit = scm(unit, divForHeight);
		int ret = Math.round((heightInBasicUnits) / (float)unit) * unit;
		if(ret <= 0)
			return unit;
		return ret;
	}
	
	public Icon getIcon() {
		return icon;
	}
	
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
