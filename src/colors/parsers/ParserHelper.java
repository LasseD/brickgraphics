package colors.parsers;

import java.io.BufferedReader;
import java.io.IOException;

public class ParserHelper {
	public static void skipPastLine(BufferedReader br, String line, boolean trimLines) throws IOException {
		String l;
		while((l = br.readLine()) != null) {
			if(line.equals(l) || trimLines && l.trim().equals(line)) {
				return;
			}
		}
	}
	public static void skipPastLineStartingWith(BufferedReader br, String line, boolean trimLines) throws IOException {
		String l;
		while((l = br.readLine()) != null) {
			if(line.equals(l) || trimLines && l.trim().startsWith(line)) {
				return;
			}
		}
	}
}
