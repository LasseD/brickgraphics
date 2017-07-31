package mosaic.ui.menu;

import io.Model;
import icon.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.ImagePreparingView;

public class ImagePreparingToolBar extends JToolBar {
	private List<ColorSlider> sliderList;
	
	public ImagePreparingToolBar(final ImagePreparingView view, Model<BrickGraphicsState> model) {
		super("Edit picture");
		setOrientation(VERTICAL);
		sliderList = new LinkedList<ColorSlider>();
				
		sliderList.add(new ColorSlider(Icons.sharpness(Icons.SIZE_SMALL), "Sharpness", model, BrickGraphicsState.PrepareSharpness, 0.5f, 1.5f, new ViewSlideUpdater() {			
			@Override
			public void set(int index, float value) {
				throw new UnsupportedOperationException();
			}
			@Override
			public void set(float value) {
				view.setSharpness(value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.gamma(Icons.SIZE_SMALL), "Gamma", model, BrickGraphicsState.PrepareGamma, 0.05f, 6f, new ViewSlideUpdater() {
			@Override
			public void set(int index, float value) {
				view.setGamma(index, value);
			}
			@Override
			public void set(float value) {
				view.setGamma(value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.brightness(Icons.SIZE_SMALL), "Brightness", model, BrickGraphicsState.PrepareBrightness, 0f, 4f, new ViewSlideUpdater() {			
			@Override
			public void set(int index, float value) {
				view.setBrightness(index, value);
			}
			@Override
			public void set(float value) {
				view.setBrightness(value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.contrast(Icons.SIZE_SMALL), "Contrast", model, BrickGraphicsState.PrepareContrast, -1f, 4f, new ViewSlideUpdater() {			
			@Override
			public void set(int index, float value) {
				view.setContrast(index, value);
			}
			@Override
			public void set(float value) {
				view.setContrast(value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.saturation(Icons.SIZE_SMALL), "Saturation", model, BrickGraphicsState.PrepareSaturation, 0f, 4f, new ViewSlideUpdater() {			
			@Override
			public void set(int index, float value) {
				throw new UnsupportedOperationException();
			}
			@Override
			public void set(float value) {
				view.setSaturation(value);
			}
		}));

		for(ColorSlider slider : sliderList) {
			add(slider.getComponent());			
		}
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(resetButton);
		add(buttonPanel);
	}

	public void reloadModel(Model<BrickGraphicsState> model) {
		for(ColorSlider slider : sliderList) {
			slider.reloadModel(model);
		}
	}
	
	public void reset() {
		for(ColorSlider slider : sliderList) {
			slider.reset();
		}
	}
	
	private static interface ViewSlideUpdater {
		public void set(int index, float value);
		public void set(float value);
	}

	private static class ColorSlider {
		public static final Color[] RGB = {Color.RED, Color.GREEN, Color.BLUE};

		private JSlider[] sliders;
		private final float min, max;
		private final int tics;
		private ViewSlideUpdater updater;
		private BrickGraphicsState state;
		private Icon icon;
		private String name;
		private JComponent component;
		
		public ColorSlider(Icon icon, String name, Model<BrickGraphicsState> model,
						   BrickGraphicsState state, 
						   final float min, final float max, 
						   ViewSlideUpdater updater) {
			this.min = min;
			this.max = max;
			tics = Math.round(100*(max-min));
			this.updater = updater;
			this.state = state;
			this.icon = icon;
			this.name = name;
			if(state.getType() == Float.class) {
				makeSlider(model);
			}
			else {
				makeRGBTree(model);
			}
		}
		
		public JComponent getComponent() {
			return component;
		}
		
		private static float mean(float... fs) {
			float sum = 0;
			for(float f : fs)
				sum+=f;
			return sum/fs.length;
		}

		private float read(JSlider slider) {
			return min + slider.getValue()/(float)tics*(max-min);
		}
		
		private void makeRGBTree(Model<BrickGraphicsState> model) {
			float[] init = (float[])model.get(state);
			JPanel singlePanel = new JPanel();
			JPanel gridPanel = new JPanel(new GridLayout(3, 1));
			
			sliders = new JSlider[4];
			for(int i = 0; i < 4; i++) {
				float in = mean(init);
				final boolean B = i < 3;
				final int I = i;
				if(B)
					in = init[i];
				final JSlider slider = new JSlider(0, tics, Math.round((in-min)/(max-min)*tics));
				final JLabel label = new JLabel(String.format("%5.2f", in));
				slider.setToolTipText(name);
				slider.setPaintTicks(true);
				slider.setPaintLabels(false);
				if(B)
					slider.setBackground(RGB[i]);
				slider.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						float newVal = read(sliders[I]);
						label.setText(String.format("%5.2f", newVal));
						if(B)
							ColorSlider.this.updater.set(I, newVal);
						else
							ColorSlider.this.updater.set(newVal);							
					}
				});
				
				if(B) {
					JPanel panel = new JPanel();
					panel.add(label);
					panel.add(slider);
					gridPanel.add(panel);
				}
				else {
					singlePanel.add(label);
					singlePanel.add(slider);					
				}

				sliders[i] = slider;
			}

			final CardLayout cardLayout = new CardLayout();
			final JPanel cardPanel = new JPanel(cardLayout);
			cardPanel.add(singlePanel, "first");
			final Dimension small = cardPanel.getPreferredSize();
			cardPanel.add(gridPanel, "last");
			final Dimension large = cardPanel.getPreferredSize();
			cardPanel.setPreferredSize(small);
			
			final JCheckBox button = new JCheckBox(Icons.plus(Icons.SIZE_SMALL), false);
			button.setSelectedIcon(Icons.minus(Icons.SIZE_SMALL));
			button.setToolTipText(name);
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if(button.isSelected()) {
						cardLayout.last(cardPanel);	
						cardPanel.setPreferredSize(large);
						for(int i = 0; i < 3; i++)
							ColorSlider.this.updater.set(i, read(sliders[i]));							
					}
					else {
						cardLayout.first(cardPanel);						
						cardPanel.setPreferredSize(small);
						ColorSlider.this.updater.set(read(sliders[3]));							
					}
				}
			});
			JPanel buttonPanel = new JPanel(new FlowLayout());
			buttonPanel.add(new JLabel(icon));
			buttonPanel.add(button);
			
			JPanel mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(buttonPanel, BorderLayout.WEST);
			mainPanel.add(cardPanel, BorderLayout.CENTER);
			mainPanel.setToolTipText(name);

			component = mainPanel;
		}
		
		private void makeSlider(Model<BrickGraphicsState> model) {
			float init = (Float)model.get(state);
			
			sliders = new JSlider[1];
			JLabel[] labels = new JLabel[1];
			
			final JSlider slider = new JSlider(0, tics, Math.round((init-min)/(max-min)*tics));
			slider.setToolTipText(name);
			slider.setPaintTicks(true);
			slider.setPaintLabels(false);
				
			final JLabel label = new JLabel(String.format("%.2f", init), icon, SwingConstants.HORIZONTAL);
			
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					float newVal = min + slider.getValue()/(float)tics*(max-min);
					label.setText(String.format("%.2f", newVal));
					ColorSlider.this.updater.set(newVal);
				}
			});

			sliders[0] = slider;
			labels[0] = label;

			JPanel panel = new JPanel();
			panel.add(label);
			panel.add(slider);
			panel.setToolTipText(name);
			component = panel;
		}
		
		public void reset() {
			reloadModel(state.getDefaultValue());
		}
		
		public void reloadModel(Model<BrickGraphicsState> model) {
			reloadModel(model.get(state));
		}

		private void reloadModel(Object stateValue) {
			if(state.getType() == Float.class) {
				float init = (Float)stateValue;
				sliders[0].setValue(Math.round((init-min)/(max-min)*tics));			
			}
			else {
				float[] init = (float[])stateValue;
				for(int i = 0; i < 3; i++) {
					sliders[i].setValue(Math.round((init[i]-min)/(max-min)*tics));							
				}
				sliders[3].setValue(Math.round((init[0]-min)/(max-min)*tics));
			}
		}
	}
}
