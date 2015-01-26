package colors.parsers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import mosaic.controllers.ColorController;

/**
 * Return list of the following format (corresponds to ordering of Rebrickable list):
 * ID, Name, #rgb, |parts|, |sets|, from, to, LEGO names, LDraw IDs, Bricklink IDs, Peeron names
 * @author ld
 */
public interface ColorSheetParserI {
	List<String> parse(InputStreamReader isr, ColorController cc) throws IOException;
}
