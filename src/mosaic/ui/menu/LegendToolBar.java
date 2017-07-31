package mosaic.ui.menu;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import mosaic.controllers.UIController;
import mosaic.ui.actions.ToggleMagnifierTotals;

public class LegendToolBar extends JToolBar {
	public LegendToolBar(UIController uiController) {
		super("Legend options", SwingConstants.HORIZONTAL);
		add(new ToggleMagnifierTotals(uiController));
	}
}
