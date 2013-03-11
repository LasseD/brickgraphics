package mosaic.ui.bricked;

import javax.swing.*;
import mosaic.ui.actions.*;

/**
 * Functions: 
 *  - turn on/off, (Toolbar, Key ALT+M)
 *  - set width/height=blocksize (Toolbar -> prompt, ALT+arrows)
 *  - show symbols/colors, (Toolbar, Key ALT+C)
 *  - relative size (Toolbar zoom, ALT+-)
 * @author LD
 */
public class MagnifierFileMenu extends JMenu {	
	private static final long serialVersionUID = -2610667114212405188L;

	public MagnifierFileMenu(Magnifier magnifier) {
		super("Magnifier");
		add(new JCheckBoxMenuItem(magnifier.getEnabledAction()));
		add(new JCheckBoxMenuItem(magnifier.getShowColorsAction()));
		add(new MagnifierTaller(magnifier));
		add(new MagnifierShorter(magnifier));
		add(new MagnifierWidener(magnifier));
		add(new MagnifierSlimmer(magnifier));
		add(new MagnifierZoomIn(magnifier));
		add(new MagnifierZoomOut(magnifier));
		setMnemonic('g');
		setDisplayedMnemonicIndex(2);
	}
}
