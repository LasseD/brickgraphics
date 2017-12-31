package transforms;

import java.awt.*;
import colors.*;

public interface InstructionsTransform extends Transform {
	/**
	 * Draw the instructions of the last transformed picture.
	 * @param g2 to draw on
	 * @param unitBounds which part of the instructions to draw
	 * @param blockWidth width of the "unit block" - SNOT would be 10
	 * @param blockHeight
	 * @param toSize
	 * @param numStuds Number of studs to draw on a single block
	 * @return a set with the colors used.
	 */
	public LEGOColor.CountingLEGOColor[] drawLastInstructions(Graphics2D g2, Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize);
	public LEGOColor.CountingLEGOColor[] drawLastColors(Graphics2D g2, Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize, int numStudsWide, int numStudsTall, boolean drawOutlines);
	public LEGOColor.CountingLEGOColor[] lastUsedColorCounts();
}
