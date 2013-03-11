package mosaic.ui.prepare;

import io.Model;
import ui.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import mosaic.io.BrickGraphicsState;

public class ImagePreparingToolBar extends JToolBar {
	private static final long serialVersionUID = 2975704528356362833L;
	private List<ColorSlider> sliderList;
	
	public ImagePreparingToolBar(final ImagePreparingView view, Model<BrickGraphicsState> model) {
		super("Edit picture");
		sliderList = new LinkedList<ColorSlider>();
				
		sliderList.add(new ColorSlider(Icons.sharpness(Icons.SIZE_SMALL), "Sharpness", model, BrickGraphicsState.PrepareSharpness, 0.5f, 1.5f, new ViewSlideUpdater() {			
			public void set(int index, float value) {
				throw new UnsupportedOperationException();
			}
			public void set(float value) {
				view.setSharpness(value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.gamma(Icons.SIZE_SMALL), "Gamma", model, BrickGraphicsState.PrepareGamma, 0.05f, 6f, new ViewSlideUpdater() {			
			public void set(int index, float value) {
				view.setGamma(index, value);
			}
			public void set(float value) {
				for(int i = 0; i < 3; i++)
					view.setGamma(i, value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.brightness(Icons.SIZE_SMALL), "Brightness", model, BrickGraphicsState.PrepareBrightness, 0f, 4f, new ViewSlideUpdater() {			
			public void set(int index, float value) {
				view.setBrightness(index, value);
			}
			public void set(float value) {
				for(int i = 0; i < 3; i++)
					view.setBrightness(i, value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.contrast(Icons.SIZE_SMALL), "Contrast", model, BrickGraphicsState.PrepareContrast, -1f, 4f, new ViewSlideUpdater() {			
			public void set(int index, float value) {
				view.setContrast(index, value);
			}
			public void set(float value) {
				for(int i = 0; i < 3; i++)
					view.setContrast(i, value);
			}
		}));
		sliderList.add(new ColorSlider(Icons.saturation(Icons.SIZE_SMALL), "Saturation", model, BrickGraphicsState.PrepareSaturation, 0f, 4f, new ViewSlideUpdater() {			
			public void set(int index, float value) {
				throw new UnsupportedOperationException();
			}
			public void set(float value) {
				view.setSaturation(value);
			}
		}));

		for(ColorSlider slider : sliderList) {
			add(slider.getComponent());			
		}
		setOrientation(VERTICAL);
	}

	public void reloadModel(Model<BrickGraphicsState> model) {
		for(ColorSlider slider : sliderList) {
			slider.reloadModel(model);
		}
	}
	
	private static interface ViewSlideUpdater {
		public void set(int index, float value);
		public void set(float value);
	}

	private static class ColorSlider {
		public static final Color[] RGB = {Color.RED, Color.GREEN, Color.BLUE};

		private JSlider[] sliders;
		private float min, max;
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
			return min + slider.getValue()/100f*(max-min);
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
				final JSlider slider = new JSlider(0, 100, Math.round((in-min)/(max-min)*100));
				final JLabel label = new JLabel(String.format("%5.2f", in));
				slider.setToolTipText(name);
				slider.setPaintTicks(true);
				slider.setPaintLabels(false);
				if(B)
					slider.setBackground(RGB[i]);
				slider.addChangeListener(new ChangeListener() {
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
			cardPanel.add(singlePanel, (Object)"first");
			final Dimension small = cardPanel.getPreferredSize();
			cardPanel.add(gridPanel, (Object)"last");
			final Dimension large = cardPanel.getPreferredSize();
			cardPanel.setPreferredSize(small);
			
			final JCheckBox button = new JCheckBox(Icons.plus(Icons.SIZE_SMALL), false);
			button.setSelectedIcon(Icons.minus(Icons.SIZE_SMALL));
			button.setToolTipText(name);
//			button.setBorder(new LineBorder(Color.BLACK, 1));
//			button.setBorderPainted(true);
			button.addActionListener(new ActionListener() {
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
			
			final JSlider slider = new JSlider(0, 100, Math.round((init-min)/(max-min)*100));
			slider.setToolTipText(name);
			slider.setPaintTicks(true);
			slider.setPaintLabels(false);
				
			final JLabel label = new JLabel(String.format("%.2f", init), icon, JLabel.HORIZONTAL);
			
			slider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					float newVal = min + slider.getValue()/100f*(max-min);
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
		
		public void reloadModel(Model<BrickGraphicsState> model) {
			if(state.getType() == Float.class) {
				float init = (Float)model.get(state);
				sliders[0].setValue(Math.round((init-min)/(max-min)*100));			
			}
			else {
				float[] init = (float[])model.get(state);
				for(int i = 0; i < 3; i++) {
					sliders[i].setValue(Math.round((init[i]-min)/(max-min)*100));							
				}
				sliders[3].setValue(Math.round((init[0]-min)/(max-min)*100));
			}
		}
	}
}
