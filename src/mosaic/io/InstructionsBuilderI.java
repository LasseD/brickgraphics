package mosaic.io;

import colors.LEGOColor;

public interface InstructionsBuilderI {
	void addSideways(int id, int x, int y, LEGOColor color);
	void add(int id, int x, int y, LEGOColor color);
}
