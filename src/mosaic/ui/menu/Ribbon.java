package mosaic.ui.menu;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ui.*;
import mosaic.controllers.*;
import mosaic.io.MosaicIO;
import mosaic.ui.*;
import mosaic.ui.actions.*;

public class Ribbon extends JToolBar {
	public Ribbon(MainController mc, MainWindow mw) {
		setFloatable(true);
		setLayout(new WrapLayout(0, 0));

		ImagePreparingView imagePreparingView = mw.getImagePreparingView();

		// Load & Save:
		add(MosaicIO.createOpenAction(mc, mw));
		add(MosaicIO.createSaveAction(mc, mw));
		add(PrintController.createPrintAction(mc.getPrintDialog()));
		addSeparator();
		
		// Toggles:
		final UIController uiController = mc.getUIController();
		add(new ToggleFilters(imagePreparingView));
		add(new ToggleCrop(imagePreparingView));
		add(new ToggleColorChooser(mw.getColorChooser()));
		add(new ToggleMagnifier(uiController));
		add(new ToggleMagnifierLegend(uiController));		
		add(createHidingButton(new ToggleMagnifierTotals(uiController), new IHideButton() {			
			@Override
			public boolean hide() {
				return !uiController.showLegend();
			}
		}, uiController));
		
		// Add ToBricks buttons:
		mc.getToBricksController().addComponents(this, mc);
		
		// Add magnifier buttons:
		final MagnifierController magnifierController = mc.getMagnifierController();
		IHideButton hideWhenMagnifierDisabled = new IHideButton() {			
			@Override
			public boolean hide() {
				return !uiController.showMagnifier();
			}
		};
		add(createHidingButton(new MagnifierTaller(magnifierController), hideWhenMagnifierDisabled, magnifierController));
		add(createHidingButton(new MagnifierShorter(magnifierController), hideWhenMagnifierDisabled, magnifierController));
		add(createHidingButton(new MagnifierWidener(magnifierController), hideWhenMagnifierDisabled, magnifierController));
		add(createHidingButton(new MagnifierSlimmer(magnifierController), hideWhenMagnifierDisabled, magnifierController));
		add(createHidingButton(new ToggleMagnifierColors(uiController), hideWhenMagnifierDisabled, magnifierController));
		add(createHidingButton(new TogglePositionDisplay(mc.getPrintController()), hideWhenMagnifierDisabled, magnifierController));
	}
	
	public static interface IHideButton {
		boolean hide();
	}

	public static JButton createHidingButton(Action action, final IHideButton hideButton, IChangeMonitor monitor) {
		final JButton ret = new JButton(action);
		if(ret.getIcon() != null)
			ret.setText(null);
		monitor.addChangeListener(new ChangeListener() {			
			@Override
			public void stateChanged(ChangeEvent e) {
				ret.setVisible(!hideButton.hide());
			}
		});
		ret.setVisible(!hideButton.hide());
		return ret;
	}
}
