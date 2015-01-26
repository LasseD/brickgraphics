package griddy;

import griddy.grid.*;
import griddy.rulers.*;
import io.*;
import java.awt.*;
import java.io.*;

public enum GriddyState implements ModelState {
	MainWindowPlacement(new Rectangle(0, 0, 640, 480)),
	Image(new File("griddy.jpg")),

	// DisplayArea:
	Grid(new Grid()),
	Zoom(1.0),
	
	//ScaleTool:
	ScaleToolHorizontal(new BorderRuler(true)),
	ScaleToolVertical(new BorderRuler(false));

	
	private Object defaultValue;
	private Class<?> objectType;
	GriddyState(Object defaultValue) {
		this.defaultValue = defaultValue;
		this.objectType = defaultValue.getClass();
	}
	public Class<?> getType() {
		return objectType;
	}
	public Object getDefaultValue() {
		return defaultValue;
	}
	public String getName() {
		return name();
	}
}
