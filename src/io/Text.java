package io;

import java.io.*;
import java.util.*;

public class Text {
	private static ResourceBundle bundle;
	
	public static void setLocale(Locale locale) {
		bundle = ResourceBundle.getBundle(locale.toString() + ".properties", locale);
	}
	
	public Locale getLocale() {
		if(bundle == null)
			throw new IllegalStateException("Locale not initialized");
		return bundle.getLocale();
	}
	
	public String get(String key) {
		if(bundle == null)
			throw new IllegalStateException("Locale not initialized");
		return bundle.getString(key);
	}
	
	public static Locale[] getAvailableLocales() {
		File dir = new File("locale");
		if(!dir.exists()) {
			dir.mkdir();
		}
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".properties");
			}
		});
		Locale[] locales = new Locale[files.length];
		int i = 0;
		for(File file : files) {
			String[] parts = file.getName().split("[_\\.]");
			switch(parts.length) {
			case 3:
				locales[i] = new Locale(parts[1]);
				break;
			case 4:
				locales[i] = new Locale(parts[1], parts[2]);
				break;
			case 5:
				locales[i] = new Locale(parts[1], parts[2], parts[3]);
				break;				
			default:
				System.out.println(file + " not a valid properties file. Must be named: \"language[_country][_variant].properties\"");
				break;
			}
			i++;
		}
		return locales;
	}
}
