package mosaic.ui.menu;

import javax.swing.JToolBar;

import mosaic.controllers.MagnifierController;
import mosaic.ui.actions.ToggleMagnifierLegend;
import mosaic.ui.actions.MagnifierShorter;
import mosaic.ui.actions.ToggleMagnifierColors;
import mosaic.ui.actions.MagnifierSlimmer;
import mosaic.ui.actions.MagnifierTaller;
import mosaic.ui.actions.MagnifierWidener;

public class MagnifierToolBar extends JToolBar {
	private static final long serialVersionUID = -8687639678741087782L;

	public MagnifierToolBar(MagnifierController magnifierController) {
		super("Magnifier options", JToolBar.HORIZONTAL);
		add(new MagnifierTaller(magnifierController));
		add(new MagnifierShorter(magnifierController));
		add(new MagnifierWidener(magnifierController));
		add(new MagnifierSlimmer(magnifierController));
		addSeparator();
		add(new ToggleMagnifierColors(magnifierController));
		add(new ToggleMagnifierLegend(magnifierController));
	}
}
