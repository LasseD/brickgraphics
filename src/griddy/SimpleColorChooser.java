package griddy;

import java.awt.Color;
import javax.swing.*;

public class SimpleColorChooser extends JColorChooser {
	private static final long serialVersionUID = -8564273862446395765L;

	public SimpleColorChooser(Color color) {
		setPreviewPanel(new JPanel());
		setColor(color);
	}
}
