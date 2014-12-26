package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;

import mosaic.ui.MainWindow;
import ui.*;

public class DividerRight extends AbstractAction {
	private static final long serialVersionUID = 3048201246080553608L;
	private MainWindow mw;	
	
	public DividerRight(MainWindow mw) {
		this.mw = mw;

		putValue(SHORT_DESCRIPTION, "Move divider right");
		putValue(SMALL_ICON, Icons.rightTriangle(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.rightTriangle(Icons.SIZE_LARGE));
	}

	public void actionPerformed(ActionEvent e) {
		JSplitPane sp = mw.getSplitPane();
		int location = sp.getDividerLocation();

		int minLocation = mw.getMinDividerLocation();
		int mid = sp.getWidth()/2;
		int maxLocation = mw.getMaxDividerLocation();

		if(mid > maxLocation) {
			mid = maxLocation;
		}

		if(location < minLocation)
			sp.setDividerLocation(Math.max(mid,  minLocation)); // set in middle
		else
			sp.setDividerLocation(sp.getWidth()-sp.getDividerSize());
	}
}
