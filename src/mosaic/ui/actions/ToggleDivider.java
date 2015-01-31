package mosaic.ui.actions;

import java.awt.event.*;
import javax.swing.*;
import mosaic.ui.MainWindow;
import icon.*;

public class ToggleDivider extends AbstractAction {
	private MainWindow mw;	
	
	public ToggleDivider(MainWindow mw) {
		this.mw = mw;

		putValue(SHORT_DESCRIPTION, "Toggle the divider location.");
		putValue(SMALL_ICON, Icons.dividerTriangles(Icons.SIZE_SMALL));
		putValue(LARGE_ICON_KEY, Icons.dividerTriangles(Icons.SIZE_LARGE));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JSplitPane sp = mw.getSplitPane();
		int location = sp.getDividerLocation();

		int one3 = sp.getWidth()/3;
		int two3 = sp.getWidth()*2/3;

		if(location <= one3)
			sp.setDividerLocation((sp.getWidth()-sp.getDividerSize())/2); // set in middle
		else if(location >= two3)
			sp.setDividerLocation(0);
		else
			sp.setDividerLocation(sp.getWidth()-sp.getDividerSize());
	}
}
