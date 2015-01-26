package io;

import java.util.*;
import java.util.List;
import javax.swing.*;

public class Log {
	private static List<JTextField> fields = new LinkedList<JTextField>();
	private Log() {
		
	}
	
	public static JTextField makeStatusBar() {
		JTextField tf = new JTextField();
		tf.setEditable(false);
		fields.add(tf);
		return tf;
	}
	
	public static void log(String message) {
		for(JTextField tf : fields)
			tf.setText(message);
		// TODO: write to a log file as well.
		//System.out.println(message);
	}
}
