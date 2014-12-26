package colors.parsers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Return list of the following format (corresponds to ordering of Rebrickable list):
 * ID, Name, #rgb, |parts|, |sets|, from, to, LEGO names, LDraw IDs, Bricklink IDs, Peeron names
 * @author ld
 */
public interface ColorSheetParserI {
	List<String> parse(InputStreamReader isr) throws IOException;
}
