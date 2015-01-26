package mosaic.ui.menu;

import javax.swing.*;
import ui.*;
import mosaic.ui.*;
import mosaic.ui.actions.*;

public class Ribbon extends JToolBar {
	public Ribbon(MainWindow mw) {
		setFloatable(true);
		setLayout(new WrapLayout(0, 0));

		ImagePreparingView imagePreparingView = mw.getImagePreparingView();
		
		add(new ToggleCrop(imagePreparingView));
		add(new ToggleFilters(imagePreparingView));
		add(new ToggleDivider(mw));
		add(new ToggleColorChooser(mw.getColorChooser()));
		add(new ToggleMagnifier(mw.getUIController()));
	}
	
	/*private static JButton makeButton(Action a) {
		JButton res = new JButton(a);
		res.setAction(a);
		return res;
	}*/
}
