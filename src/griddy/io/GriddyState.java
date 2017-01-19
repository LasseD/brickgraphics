package griddy.io;

import io.*;
import java.awt.*;

public enum GriddyState implements ModelState {
	MainWindowPlacement(new Rectangle(0, 0, 640, 480)),
	ImageFileName("griddy_sample_input.jpg"),
	ImageFile(new DataFile()),
	Zoom(1.0);

	// Sample values:
	//SelectedColors(new int[]{0, 1, 2, 14, 15, 19, 70, 71, 72, 320}),
		
	private Object defaultValue;
	private Class<?> objectType;
	private GriddyState(Object defaultValue) {
		this.defaultValue = defaultValue;
		this.objectType = defaultValue.getClass();
	}
	@Override
	public Class<?> getType() {
		return objectType;
	}
	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}
	@Override
	public String getName() {
		return name();
	}
}
