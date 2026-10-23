package io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;
    
import javax.imageio.ImageIO;

import sun.misc.*;

public class DataFile {
	private byte[] data;
	
	public DataFile(File file) throws IOException {
		data = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
	}
	
	public DataFile(String base64) throws IOException {
	    data = Base64.getDecoder().decode(base64.getBytes());
	}
	
	public DataFile(BufferedImage image) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image, "png", os);
		data = os.toByteArray();
	}
	
	public DataFile() {
		data = null;
	}
	
	public boolean isValid() {
		return data != null;
	}
	
	public String toBase64() {
		if(!isValid())
			return "";
		Base64.Encoder encoder = Base64.getEncoder();
		String out = encoder.encodeToString(data);
		return '"'+out+'"';
	}
	
	public InputStream fakeStream() {
		return new ByteArrayInputStream(data);
	}
}
