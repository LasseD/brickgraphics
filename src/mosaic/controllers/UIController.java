package mosaic.controllers;

import io.*;

import java.util.*;
import javax.swing.event.*;

import mosaic.io.*;

public class UIController implements ChangeListener, ModelSaver<BrickGraphicsState> {
	private boolean showColorDistributionChart, showMagnifier, showColors, showLegend, showTotals;
	
	private List<ChangeListener> listeners;
	
	public UIController(Model<BrickGraphicsState> model) {
		listeners = new LinkedList<ChangeListener>();
		reloadModel(model);
		model.addModelSaver(this);
	}
	
	public boolean showColorDistributionChart() {
		return showColorDistributionChart;
	}
	public boolean showColors() {
		return showColors;
	}
	public boolean enableLegend() {
		return showLegend;
	}
	public boolean showTotals() {
		return showTotals;
	}
	public boolean showMagnifier() {
		return showMagnifier;
	}
	
	public void flipShowColors() {
		showColors = !showColors;
		if(!showMagnifier)
			showMagnifier = true;
		notifyListeners();
	}	
	public void flipShowMagnifier() {
		showMagnifier = !showMagnifier;
		notifyListeners();
	}	
	public void flipLegendEnabled() {
		showLegend = !showLegend;
		if(!showMagnifier)
			showMagnifier = true;
		notifyListeners();
	}	
	public void flipViewTotals() {
		showTotals = !showTotals;
		if(!showMagnifier)
			showMagnifier = true;
		notifyListeners();
	}	
	public void flipViewColorDistributionChart() {
		showColorDistributionChart = !showColorDistributionChart;
		notifyListeners();
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.MagnifierShowLegend, showLegend);
		model.set(BrickGraphicsState.MagnifierShowColors, showColors);
		model.set(BrickGraphicsState.MagnifierShowTotals, showTotals);
		model.set(BrickGraphicsState.ColorDistributionChartShow, showColorDistributionChart);
		model.set(BrickGraphicsState.MagnifierShow, showMagnifier);
	}
	public void reloadModel(Model<BrickGraphicsState> model) {
		showLegend = (Boolean)model.get(BrickGraphicsState.MagnifierShowLegend);
		showColors = (Boolean)model.get(BrickGraphicsState.MagnifierShowColors);
		showTotals = (Boolean)model.get(BrickGraphicsState.MagnifierShowTotals);
		showColorDistributionChart = (Boolean)model.get(BrickGraphicsState.ColorDistributionChartShow);
		showMagnifier = (Boolean)model.get(BrickGraphicsState.MagnifierShow);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		notifyListeners();
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public void notifyListeners() {
		for(ChangeListener l : listeners) {
			l.stateChanged(null);
		}
	}
}
