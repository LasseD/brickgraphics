package io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.*;

/**
 * @author LD
 */
public class Log {
	private static Log instance;	
	private List<JTextField> fields;
	private PrintWriter out;
	
	public static void initializeLog(String fileName) throws IOException {
		instance = new Log();
		instance.fields = new LinkedList<JTextField>();
		FileOutputStream os = new FileOutputStream(new File(fileName), true);
		instance.out = new PrintWriter(os);
	}

	private Log() {
	}
	
	public static JTextField makeStatusBar() {
		JTextField tf = new JTextField();
		tf.setEditable(false);
		instance.fields.add(tf);
		return tf;
	}
	
	public static void log(String message) {
		System.out.println(message);
		for(JTextField tf : instance.fields) {
			tf.setText(message);			
		}
		if(instance.out != null)
			instance.out.println(message);
	}

	public static void log(Exception e) {
		log(e.getMessage());
		e.printStackTrace();
		if(instance.out != null)
			e.printStackTrace(instance.out);
	}
	
	public static void close() {
		if(instance.out == null)
			return;
		instance.out.flush();
		instance.out.close();
	}
}
