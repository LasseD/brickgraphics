package mosaic.ui.bricked;

import ui.*;
import javax.swing.*;
import colors.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.event.*;

import mosaic.io.BrickGraphicsState;
import io.*;

public class ColorChooser extends JDialog implements ChangeListener {
	private static final long serialVersionUID = 801460023279742615L;
	private LEGOColor[] colors;
	private List<ChangeListener> listeners;
	private List<ColorGroupPanel> panels;
	private Action onOffAction;
	
	public ColorChooser(final JFrame owner, Model<BrickGraphicsState> model) {
		super(owner, "Colors", false);
		//setIconImage(new Image(Icons.colors(Icons.SIZE_LARGE)));
		setFocusableWindowState(false);
		listeners = new LinkedList<ChangeListener>();
		panels = new LinkedList<ColorGroupPanel>();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// add colors groups:
		ColorGroup[] colorGroups;
		try {
			colorGroups = ColorGroup.generateColorGroups();
		} catch (IOException e) {
			// notify user:
			JOptionPane.showMessageDialog(owner, e.getMessage(), "Error while reading color files!", JOptionPane.ERROR_MESSAGE);
			// make new default group:
			colorGroups = ColorGroup.generateBackupColorGroups();
		}		
		int widest = 0;
		for(ColorGroup group : colorGroups) {
			widest = Math.max(widest, new JLabel(group.getName()).getPreferredSize().width+2);
		}
		for(ColorGroup group : colorGroups) {
			ColorGroupPanel panel = new ColorGroupPanel(group, widest, model);
			mainPanel.add(panel);
			panel.addChangeListener(this);
			panels.add(panel);
		}

		getContentPane().add(new JScrollPane(mainPanel, 
											 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
											 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		updateColors();
		setAlwaysOnTop(false);
		setSize(400, 300);
		
		onOffAction = new AbstractAction() {
			private static final long serialVersionUID = 154345236L;
			private boolean firstTime = true;
			private boolean on = false;
			public void actionPerformed(ActionEvent e) {
				if(firstTime) {
					int x = Toolkit.getDefaultToolkit().getScreenSize().width-getWidth();
					x = Math.min(x, owner.getX()+owner.getWidth());
					setLocation(x, owner.getY());
					firstTime = false;
				}
				boolean keyOn = (Boolean)onOffAction.getValue(Action.SELECTED_KEY);
				if(on != keyOn) {
					on = keyOn;
				}
				else {
					on = !keyOn;
					onOffAction.putValue(Action.SELECTED_KEY, on);
				}
				setVisible(on);
			}
		};
		onOffAction.putValue(Action.SHORT_DESCRIPTION, "Enable the color chooser.");
		onOffAction.putValue(Action.SMALL_ICON, Icons.colors(Icons.SIZE_SMALL));
		onOffAction.putValue(Action.LARGE_ICON_KEY, Icons.colors(Icons.SIZE_LARGE));
		onOffAction.putValue(Action.NAME, "ColorChooser");
		onOffAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		onOffAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "ColorChooser".indexOf('C'));
		onOffAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK));
		onOffAction.putValue(Action.SELECTED_KEY, false);
		addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}

			public void windowClosing(WindowEvent e) {
				onOffAction.actionPerformed(null);				
			}
		});
		
		((TreeSet<String>)model.get(BrickGraphicsState.ToBricksColorGroups)).clear();
		((TreeSet<LEGOColor>)model.get(BrickGraphicsState.ToBricksColors)).clear();	
	}
	
	public Action getOnOffAction() {
		return onOffAction;
	}
	
	public void updateColors() {
		Set<LEGOColor> colors = new TreeSet<LEGOColor>();
		for(ColorGroupPanel panel : panels) {
			colors.addAll(panel.selectedColors());
		}
		this.colors = colors.toArray(new LEGOColor[]{});
	}
	
	public LEGOColor[] getColors() {
		if(colors == null || colors.length == 0) {
			return LEGOColor.BW;
		}
		else {
			return colors;			
		}
	}
	
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}
	
	public void stateChanged(ChangeEvent e) {
		updateColors();
		
		// propagate events:
		for(ChangeListener listener : listeners) {
			listener.stateChanged(e);
		}
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		for(ColorGroupPanel panel : panels)
			panel.reloadModel(model);
		updateColors();
	}

	private static class ColorGroupPanel extends JPanel implements ActionListener, ModelSaver<BrickGraphicsState> {
		private static final long serialVersionUID = 9097335036243041400L;
		private List<ColorCheckBox> boxes;
		private JCheckBox mainBox;
		private List<ChangeListener> listeners;
		private boolean ignoreEvents;
		private String name;
		
		public ColorGroupPanel(ColorGroup group, int labelWidth, Model<BrickGraphicsState> model) {
			model.addModelSaver(this);

			boxes = new LinkedList<ColorCheckBox>();
			listeners = new LinkedList<ChangeListener>();
			mainBox = new JCheckBox();
			name = group.getName();
			reloadModel(model);
			mainBox.addActionListener(this);
			
			setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
			JLabel label = new JLabel(name);
			label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
			label.setHorizontalAlignment(JLabel.RIGHT);
			add(label);
			add(mainBox);
			for(LEGOColor color : group.getColors()) {
				ColorCheckBox box = new ColorCheckBox(color, model);
				boxes.add(box);
				add(box);
				box.addActionListener(this);
			}
			updateBoxes();
		}
		
		public void reloadModel(Model<BrickGraphicsState> model) {
			Set<String> selectedGroups = (TreeSet<String>)model.get(BrickGraphicsState.ToBricksColorGroups);
			boolean selected = selectedGroups.contains(name);
			//System.out.println("Updating " + name + ":" + selected);
			mainBox.setSelected(selected);
			for(ColorCheckBox box : boxes)
				box.reloadModel(model);
			updateBoxes();
		}
		
		public void actionPerformed(ActionEvent e) {
			if(ignoreEvents)
				return;
			if(e.getSource() == mainBox) {
				ignoreEvents = true;
				updateBoxes();
				ignoreEvents = false;
			}
				
			// propagate events:
			ChangeEvent ce = new ChangeEvent(this);
			for(ChangeListener listener : listeners) {
				listener.stateChanged(ce);
			}
		}
		
		private void updateBoxes() {
			for(ColorCheckBox box : boxes) {
				box.setEnabled(!mainBox.isSelected());
			}
		}

		public void addChangeListener(ChangeListener l) {
			listeners.add(l);
		}
		
		public List<LEGOColor> selectedColors() {
			List<LEGOColor> selected = new LinkedList<LEGOColor>();
			for(ColorCheckBox box : boxes) {
				if(mainBox.isSelected() || box.isSelected())
					selected.add(box.getColor());
			}
			return selected;
		}

		public void save(Model<BrickGraphicsState> model) {
			//System.out.println("Saving " + name + ":" + mainBox.isSelected());
			if(mainBox.isSelected())
				((TreeSet<String>)model.get(BrickGraphicsState.ToBricksColorGroups)).add(name);
		}
	}

	private static class ColorCheckBox extends JCheckBox implements ModelSaver<BrickGraphicsState> {
		private static final long serialVersionUID = -2531084719234352355L;
		private LEGOColor color;
		private int size;
		
		public ColorCheckBox(LEGOColor color, Model<BrickGraphicsState> model) {
			model.addModelSaver(this);

			this.color = color;
			size = getPreferredSize().width*2;
			setToolTipText(color.getToolTipText());
			setHorizontalAlignment(SwingConstants.CENTER);
			setPreferredSize(new Dimension(size, size));
			setBackground(color.rgb);
			reloadModel(model);
		}
		
		public LEGOColor getColor() {
			return color;
		}
		
		public @Override String toString() {
			return getClass().getName() + "[" + color + "]";
		}
		
		public void reloadModel(Model<BrickGraphicsState> model) {
			Set<LEGOColor> selectedColors = (TreeSet<LEGOColor>)model.get(BrickGraphicsState.ToBricksColors);		
			setSelected(selectedColors.contains(color));			
		}

		public void save(Model<BrickGraphicsState> model) {
			if(isSelected())
				((TreeSet<LEGOColor>)model.get(BrickGraphicsState.ToBricksColors)).add(color);
		}
	}
}
