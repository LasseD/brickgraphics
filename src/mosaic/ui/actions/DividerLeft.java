package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import icon.*;
import mosaic.ui.MainWindow;

public class DividerLeft extends AbstractAction {
	private MainWindow mw;	
	
	public DividerLeft(MainWindow mw) {
		this.mw = mw;

		putValue(SHORT_DESCRIPTION, "Move divider left");
		putValue(SMALL_ICON, Icons.leftTriangle(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.leftTriangle(Icons.SIZE_LARGE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JSplitPane sp = mw.getSplitPane();
		int location = sp.getDividerLocation();

		int minLocation = mw.getMinDividerLocation();
		int mid = sp.getWidth()/2;
		int maxLocation = mw.getMaxDividerLocation();

		if(mid < minLocation) {
			mid = minLocation;
		}

		if(location > maxLocation)
			sp.setDividerLocation(Math.min(mid,  maxLocation)); // set in middle
		else
			sp.setDividerLocation(0);
	}
}
