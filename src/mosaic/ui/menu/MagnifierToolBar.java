package mosaic.ui.menu;

import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import mosaic.controllers.MagnifierController;
import mosaic.controllers.UIController;
import mosaic.ui.actions.ToggleMagnifierLegend;
import mosaic.ui.actions.MagnifierShorter;
import mosaic.ui.actions.ToggleMagnifierColors;
import mosaic.ui.actions.MagnifierSlimmer;
import mosaic.ui.actions.MagnifierTaller;
import mosaic.ui.actions.MagnifierWidener;
import mosaic.ui.actions.ToggleMagnifierTotals;

public class MagnifierToolBar extends JToolBar {
	public MagnifierToolBar(MagnifierController magnifierController, UIController uiController) {
		super("Magnifier options", SwingConstants.HORIZONTAL);
		add(new MagnifierTaller(magnifierController));
		add(new MagnifierShorter(magnifierController));
		add(new MagnifierWidener(magnifierController));
		add(new MagnifierSlimmer(magnifierController));
		addSeparator();
		add(new ToggleMagnifierColors(uiController));
		//add(new ToggleMagnifierLegend(uiController));
		//add(new ToggleMagnifierTotals(uiController));
	}
}
