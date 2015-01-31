package io;

import java.io.*;
import java.nio.file.*;
import sun.misc.*;

public class DataFile {
	private byte[] data;
	
	public DataFile(File file) throws IOException {
		data = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
	}
	
	public DataFile(String base64) throws IOException {
		data = new BASE64Decoder().decodeBuffer(base64);
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
		BASE64Encoder encoder = new BASE64Encoder();
		String out = encoder.encode(data);
		return '"'+out+'"';
	}
	
	public InputStream fakeStream() {
		return new ByteArrayInputStream(data);
	}
}
