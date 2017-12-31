package colors.parsers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;

/**
 * Return list of the following format (corresponds to ordering of Rebrickable list):
 * rebrickableID, rebrickableName, #rgb, |parts|, |sets|, from, to, LEGO IDs+names, LDraw IDs+names, Bricklink IDs+names, BrickOwl ids+names
 * @author LD
 */
public interface ColorSheetParserI {
	List<String> parse(InputStreamReader isr) throws IOException, ParseException;
}
