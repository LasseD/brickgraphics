package mosaic.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.*;
import transforms.ToBricksTransform;
import colors.LEGOColor;
import mosaic.controllers.*;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineMosaicListener;
import mosaic.ui.menu.*;

public class MagnifierWindow extends JDialog implements ChangeListener, PipelineMosaicListener {
	private MagnifierController magnifierController;
	private UIController uiController;
	private MagnifierCanvas canvas;
	private boolean everShown;
	private ColorLegend legend;

	public MagnifierWindow(final MainController mc, MainWindow mw, Pipeline pipeline) {
		super(mw, "Magnifier", false);
		magnifierController = mc.getMagnifierController();
		uiController = mc.getUIController();
		everShown = false;
		setAlwaysOnTop(false);
		setFocusableWindowState(false);
		
		// build UI components:
		canvas = new MagnifierCanvas();
		canvas.setPreferredSize(new Dimension(512, 512));
		canvas.setMinimumSize(new Dimension(64, 64));
		
		setLayout(new BorderLayout());
		
		add(new MagnifierToolBar(magnifierController, uiController), BorderLayout.NORTH);

		legend = new ColorLegend(mc, mw, pipeline);
		pipeline.addMosaicListener(this);
		add(canvas, BorderLayout.CENTER);
				
		// listener for key presses:
		addKeyListener(magnifierController);
		magnifierController.addChangeListener(this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				uiController.flipShowMagnifier();
			}
		});
		setDefaultCloseOperation(DISPOSE_ON_CLOSE); // So the listener above is called.
	}
	
	public ColorLegend getLegend() {
		return legend;
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
			//int basicUnitWidth = tbTransform.getToBricksType().getUnitWidth();
			//int basicUnitHeight = tbTransform.getToBricksType().getUnitHeight();
			Rectangle basicUnitRect = magnifierController.getCoreRect();
			Set<LEGOColor> used = tbTransform.draw(g2, basicUnitRect, shownMagnifierSize, uiController.showColors(), true);
			/*if(tbTransform.getToBricksType() == ToBricksType.SNOT_IN_2_BY_2) {
				if(uiController.showColors())
					used = tbTransform.drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, 0, 0);
				else
					used = tbTransform.drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize);
			}
			else {
				if(uiController.showColors()) {
					ToBricksType tbt = tbTransform.getToBricksType();
					used = tbTransform.getMainTransform().drawLastColors(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize, tbt.getStudsShownWide(), tbt.getStudsShownTall());
				}
				else
					used = tbTransform.getMainTransform().drawLastInstructions(g2, basicUnitRect, basicUnitWidth, basicUnitHeight, shownMagnifierSize);
			}*/
			legend.setHighlightedColors(used);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setTitle(magnifierController.getDisplayPosition());
		boolean visible = uiController.showMagnifier();
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
			canvas.repaint();			
		}
	}

	@Override
	public void mosaicChanged(Dimension ignore) {
		stateChanged(null);
	}
}
