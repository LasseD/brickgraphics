package mosaic.controllers;

import io.Model;
import io.ModelHandler;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import transforms.ScaleTransform.ScaleQuality;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.MainWindow;
import mosaic.ui.OptionsDialog;

/**
 * Class for handling the options in the options dialog
 * @author ld
 * Controls the following model states (BrickGraphicsState):
 * - PrepareAllowFilterReordering(true),
 * - PrepareScaleQuality(ScaleTransform.ScaleQuality.NearestNeighbor),
 * - PrepareScaleBeforePreparing(true),
 */
public class OptionsController implements ModelHandler<BrickGraphicsState> {
	private List<ChangeListener> listeners;
	
	private boolean allowFilterReordering, scaleBeforePreparing;
	private ScaleQuality scaleQuality;
	
	private OptionsDialog optionsDialog;

	public OptionsController(Model<BrickGraphicsState> model, MainWindow mw) {
		listeners = new LinkedList<ChangeListener>();
		model.addModelHandler(this);
		handleModelChange(model);
		optionsDialog = new OptionsDialog(mw, this);
	}
	
	public boolean getAllowFilterReordering() {
		return allowFilterReordering;
	}
	public void setAllowFilterReordering(boolean b, Object caller) {
		allowFilterReordering = b;
		notifyListeners(new ChangeEvent(caller));		
	}	
	
	public boolean getScaleBeforePreparing() {
		return scaleBeforePreparing;
	}
	public void setScaleBeforePreparing(boolean b, Object caller) {
		scaleBeforePreparing = b;
		notifyListeners(new ChangeEvent(caller));		
	}	

	public ScaleQuality getScaleQuality() {
		return scaleQuality;
	}
	public void setScaleQuality(ScaleQuality s, Object caller) {
		scaleQuality = s;
		notifyListeners(new ChangeEvent(caller));		
	}	
	
	public OptionsDialog getOptionsDialog() {
		return optionsDialog;
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	private void notifyListeners(ChangeEvent e) {
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.PrepareAllowFilterReordering, allowFilterReordering);
		model.set(BrickGraphicsState.PrepareScaleBeforePreparing, scaleBeforePreparing);	
		model.set(BrickGraphicsState.PrepareScaleQuality, scaleQuality.ordinal());
	}

	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		allowFilterReordering = (Boolean)model.get(BrickGraphicsState.PrepareAllowFilterReordering);
		scaleBeforePreparing = (Boolean)model.get(BrickGraphicsState.PrepareScaleBeforePreparing);
		scaleQuality = ScaleQuality.values()[(Integer)model.get(BrickGraphicsState.PrepareScaleQuality)];
	}
}
