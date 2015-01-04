package mosaic.ui.menu;

import javax.swing.*;

import ui.WrapLayout;
import mosaic.controllers.MagnifierController;
import mosaic.ui.BrickedView;
import mosaic.ui.MainWindow;
import mosaic.ui.actions.*;
import mosaic.ui.prepare.ImagePreparingView;

public class Ribbon extends JToolBar {
	public Ribbon(MainWindow mw) {
		setFloatable(true);
		setLayout(new WrapLayout(0, 0));

		ImagePreparingView imagePreparingView = mw.getImagePreparingView();
		BrickedView brickedView = mw.getBrickedView();
		MagnifierController magnifierController = brickedView.getMagnifierController();
		
		add(new ToggleCrop(imagePreparingView));
		add(new ToggleFilters(imagePreparingView));
		add(new ToggleDivider(mw));
		add(new ToggleColorChooser(brickedView.getToolBar().getColorChooser()));
		add(new ToggleMagnifier(magnifierController));
	}
	
	/*private static JButton makeButton(Action a) {
		JButton res = new JButton(a);
		res.setAction(a);
		return res;
	}*/
}
