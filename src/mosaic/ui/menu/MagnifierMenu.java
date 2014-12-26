package mosaic.ui.menu;

import javax.swing.*;
import mosaic.controllers.MagnifierController;
import mosaic.ui.actions.*;

/**
 * @author LD
 */
public class MagnifierMenu extends JMenu {	
	private static final long serialVersionUID = -2610667114212405188L;

	public MagnifierMenu(MagnifierController magnifierController) {
		super("Magnifier");
		add(new ToggleMagnifierColors(magnifierController));
		add(new ToggleMagnifierLegend(magnifierController));
		addSeparator();
		add(new MagnifierTaller(magnifierController));
		add(new MagnifierShorter(magnifierController));
		add(new MagnifierWidener(magnifierController));
		add(new MagnifierSlimmer(magnifierController));
		setMnemonic('a');
		setDisplayedMnemonicIndex(1);
	}
}
