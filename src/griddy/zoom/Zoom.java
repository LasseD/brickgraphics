package griddy.zoom;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import io.*;
import javax.swing.*;
import griddy.*;

public class Zoom implements ModelSaver<GriddyState>, ModelChangeListener {
	private double zoom;
	private List<ZoomListener> listeners;
	
	public Zoom(Model<GriddyState> model) {
		zoom = (Double)model.get(GriddyState.Zoom);
		listeners = new LinkedList<ZoomListener>();
		model.addModelChangeListener(this, GriddyState.Zoom);
		model.addModelSaver(this);
	}
	
	public double getZoom() {
		return zoom;
	}
	
	public void addZoomListener(ZoomListener listener) {
		listeners.add(listener);
	}
	
	public void setZoom(double zoom) {
		double oldZoom = this.zoom;
		this.zoom = zoom;

		for(ZoomListener l : listeners) {
			l.zoomChanged(zoom, zoom/oldZoom);
		}
	}

	@Override
	public void save(Model<GriddyState> model) {
		model.set(GriddyState.Zoom, zoom);
	}
	
	private class WellBehaveComboBox extends JComboBox {
		private static final long serialVersionUID = -5402008932718013L;
		private boolean ignoreActions;
		private List<ActionListener> actionListeners;
		
		public WellBehaveComboBox(String[] values) {
			super(values);
			ignoreActions = false;
			actionListeners = new LinkedList<ActionListener>();
			super.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(ignoreActions)
						return;
					for(ActionListener l : actionListeners)
						l.actionPerformed(e);
				}
			});
		}
		
		@Override
		public void addActionListener(ActionListener l) {
			actionListeners.add(l);
		}

		public void setSelectedItemNoNotify(double zoom) {
			zoom *= 100;
			ignoreActions = true;
			if(zoom == (int)zoom)
				setSelectedItem(String.format("%d%%", (int)zoom));
			else
				setSelectedItem(String.format("%.2f%%", zoom));
			ignoreActions = false;
		}
	}
	
	public JPanel makeGUI(final Griddy imageAndSizeGiver) {
		final JPanel mainPanel = new JPanel(new FlowLayout());
		
		// "Zoom:"
		mainPanel.add(new JLabel("Zoom:"));
		// -
		final JButton minusButton = new JButton("-");
		minusButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				double newZoom = getZoom()*0.5;
				setZoom(newZoom);
			}
		});
		mainPanel.add(minusButton);
		// main%:
		final WellBehaveComboBox inputBox = new WellBehaveComboBox(new String[]{"25%", "50%", "75%", "100%", "150%", "200%", "400%"});
		inputBox.setEditable(true);
		inputBox.setSelectedItemNoNotify(zoom);
		inputBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = ((String)inputBox.getSelectedItem()).trim();
				if(text.endsWith("%"))
					text = text.substring(0, text.length()-1);
				try {
					double newZoom = Double.parseDouble(text);
					setZoom(newZoom/100);					
				}
				catch(NumberFormatException e2) {
					inputBox.setSelectedItemNoNotify(zoom);
				}
			}
		});
		addZoomListener(new ZoomListener() {
			@Override
			public void zoomChanged(double newZoom, double zoomChangeFactor) {
				inputBox.setSelectedItemNoNotify(newZoom);
			}
		});		
		mainPanel.add(inputBox);
		// +
		final JButton plusButton = new JButton("+");
		plusButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				double newZoom = getZoom()*2;
				setZoom(newZoom);
			}
		});
		mainPanel.add(plusButton);
		// fit:
		final JButton fitButton = new JButton("Zoom fit");
		fitButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				int imageWidth = imageAndSizeGiver.getImage().getWidth();
				int imageHeight = imageAndSizeGiver.getImage().getHeight();
				double viewWidth = imageAndSizeGiver.getDisplayAreaSize().width;
				double viewHeight = imageAndSizeGiver.getDisplayAreaSize().height;
				
				double newZoom = Math.min(viewWidth/imageWidth, viewHeight/imageHeight);
				setZoom(newZoom);
			}
		});
		mainPanel.add(fitButton);		
		
		return mainPanel;
	}

	@Override
	public void modelChanged(Object o) {
		setZoom((Double)o);
	}
}
