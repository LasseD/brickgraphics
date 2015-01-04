package mosaic.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;
import transforms.ToBricksTransform;
import bricks.ToBricksType;
import colors.LEGOColor;
import mosaic.controllers.ColorController;
import mosaic.controllers.MagnifierController;
import mosaic.ui.ColorLegend;
import mosaic.ui.MainWindow;
import mosaic.ui.menu.*;

public class MagnifierWindow extends JDialog implements ChangeListener {
	private MagnifierController magnifierController;
	private MagnifierCanvas canvas;
	private boolean everShown;
	private ColorLegend legend;
	private JSplitPane splitPane;

	public MagnifierWindow(final MainWindow owner, final MagnifierController magnifierController, ColorController cc) {
		super(owner, "Magnifier", false);
		this.magnifierController = magnifierController;
		everShown = false;
		setAlwaysOnTop(false);
		setFocusableWindowState(false);
		
		// build UI components:
		canvas = new MagnifierCanvas();
		canvas.setPreferredSize(new Dimension(512, 512));
		canvas.setMinimumSize(new Dimension(64, 64));
		
		setLayout(new BorderLayout());
		
		add(new MagnifierToolBar(magnifierController), BorderLayout.NORTH);

		legend = new ColorLegend(magnifierController, owner.getBrickedView(), cc);
		legend.setBackground(getBackground());
		JScrollPane scrollPane = new JScrollPane(legend);
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, canvas, scrollPane);
		add(splitPane, BorderLayout.CENTER);
				
		// listener for key presses:
		addKeyListener(magnifierController);
		magnifierController.addChangeListener(legend);
		magnifierController.addChangeListener(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				magnifierController.flipEnabled();
			}			
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private class MagnifierCanvas extends JPanel {
		private static final long serialVersionUID = 585625214128821252L;

		public Rectangle computeShownRect(final Dimension componentSize) {
			double componentW2H = componentSize.width / (double)componentSize.height;		
			Dimension magnifierSizeInUnits = magnifierController.getSizeInUnits();
			double imageW2H = magnifierSizeInUnits.width / (double)magnifierSizeInUnits.height;
			
			Dimension outSize;
			if(componentW2H < imageW2H) {
				outSize = new Dimension(componentSize.width, componentSize.width * magnifierSizeInUnits.height / magnifierSizeInUnits.width);
			}
			else {
				outSize = new Dimension(componentSize.height * magnifierSizeInUnits.width / magnifierSizeInUnits.height, componentSize.height);				
			}
			
			return new Rectangle((componentSize.width-outSize.width)/2, (componentSize.height-outSize.height)/2, outSize.width, outSize.height);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			Graphics2D g2 = (Graphics2D)g;
			Rectangle viewRect = computeShownRect(getSize());
			Dimension shownMagnifierSize = new Dimension(viewRect.width, viewRect.height);
			setPreferredSize(shownMagnifierSize);
			
			g2.translate(viewRect.x, viewRect.y);

			// draw magnified:
			g2.setColor(Color.BLACK);
			ToBricksTransform tbTransform = magnifierController.getTBTransform();
			int basicUnitWidth = tbTransform.getToBricksType().getUnitWidth();
			int basicUnitHeight = tbTransform.getToBricksType().getUnitHeight();
			Rectangle basicUnitRect = magnifierController.getCoreRect();
			Set<LEGOColor> used;
			if(tbTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
				if(magnifierController.showColors())
					used = tbTransform.drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, 0);
				else
					used = tbTransform.drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize);
			}
			else {
				if(magnifierController.showColors()) {
					int numStuds = 0;
					if(tbTransform.getToBricksType() == ToBricksType.STUD_FROM_TOP)
						numStuds = 1;
					else if(tbTransform.getToBricksType() == ToBricksType.TWO_BY_TWO_PLATES_FROM_TOP)
						numStuds = 2;
					used = tbTransform.getMainTransform().drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, numStuds);
				}
				else
					used = tbTransform.getMainTransform().drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize);
			}
			legend.setHighlightedColors(used);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setTitle(magnifierController.getDisplayPosition());
		boolean visible = magnifierController.isEnabled();
		if(!everShown && visible) {
			pack();
			Point rightOfOwner = new Point(getOwner().getLocation().x + getOwner().getWidth(), Math.max(0,  getOwner().getLocation().y));
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			Point maxOnScreen = new Point(screen.width - getSize().width, screen.height - getSize().height);
			setLocation(Math.min(rightOfOwner.x, maxOnScreen.x), Math.min(rightOfOwner.y, maxOnScreen.y));
			everShown = true;
		}
		setVisible(visible);

		if(visible) {
			if(!magnifierController.enableLegend()) {
				splitPane.setDividerLocation(1.0);
			}
			else if(splitPane.getDividerLocation() > splitPane.getSize().height * 8 / 10){
				splitPane.setDividerLocation(canvas.getPreferredSize().height);				
			}
			canvas.repaint();			
		}
	}
}
