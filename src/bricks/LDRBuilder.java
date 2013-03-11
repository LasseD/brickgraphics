package bricks;

import colors.LEGOColor;

public interface LDRBuilder {
	void addSideways(int xPlateIndent, int yPlateIndent, LEGOColor color);
	void add(int x, int y, LEGOColor color);
}
