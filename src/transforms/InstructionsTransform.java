package transforms;

import java.awt.*;
import colors.*;
import java.util.*;

public interface InstructionsTransform extends Transform {
	/**
	 * Draw the instructions of the last transformed picture.
	 * @param g2
	 * @param unitBounds
	 * @param blockWidth
	 * @param blockHeight
	 * @param toSize
	 * @return a set with the colors used.
	 */
	public Set<LEGOColor> drawLastInstructions(Graphics2D g2, 
			Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize);
	public Set<LEGOColor> drawLastColors(Graphics2D g2, 
			Rectangle unitBounds, int blockWidth, int blockHeight, Dimension toSize);
	public LEGOColor[] lastUsedColors();
}
