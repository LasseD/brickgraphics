package mosaic.ui;

import icon.Icons;

import javax.swing.*;

import colors.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.event.*;
import mosaic.controllers.*;
import mosaic.ui.menu.ColorChooserToolBar;

public class ColorChooserDialog extends JDialog implements ChangeListener {
	private List<ColorGroupPanel> panels;
	private boolean everEnabled, enabled;
	private ColorController cc;
	private JPanel mainPanel;
	private JSplitPane splitPane;
	
	public ColorChooserDialog(final MainController mc, MainWindow mw) {
		super(mw, "Colors", false);

		this.cc = mc.getColorController();
		mainPanel = new JPanel();
		panels = new LinkedList<ColorGroupPanel>();

		cc.addChangeListener(this);
		setFocusableWindowState(false);
		setAlwaysOnTop(false);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				enabled = false;				
			}
		});
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.add(new JScrollPane(mainPanel, 
				 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		splitPane.add(new ColorDistributionChart(mc, mw));
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(new ColorChooserToolBar(cc, mc.getUIController(), this), BorderLayout.NORTH);		
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		stateChanged(null);
	}
	
	@Override
	public void pack() {
		halfPack();
		super.pack();
	}
	public void halfPack() {
		splitPane.setDividerLocation(mainPanel.getPreferredSize().height + splitPane.getDividerSize());
	}
	
	public void switchEnabled() {
		enabled = !enabled;
		pack();
		if(!everEnabled) {
			Window owner = getOwner();
			setLocation(Math.max(0,  getX()), Math.max(0, owner.getY()));
			everEnabled = true;
		}
		setVisible(enabled);
	}
	
	public void tellController() {
		Set<LEGOColor> selectedColors = new TreeSet<LEGOColor>();
		
		for(ColorGroupPanel panel : panels) {
			panel.getSelected(selectedColors);
		}

		if(selectedColors.size() < 2) {
			selectedColors.clear();
			selectedColors.add(LEGOColor.BW[0]);
			selectedColors.add(LEGOColor.BW[1]);
		}

		cc.setColorChooserSelectedColors(selectedColors, new ChangeEvent(ColorChooserDialog.this));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && e.getSource() == this)
			return;
		mainPanel.removeAll();
		panels.clear();
		
		// add colors groups:
		ColorGroup[] colorGroups = cc.getColorGroupsFromDisk();
		int widest = 0;
		for(ColorGroup group : colorGroups) {
			JLabel label = new JLabel(group.getName());
			widest = Math.max(widest, label.getPreferredSize().width+2);
		}
		Set<LEGOColor> filteredColors = new TreeSet<LEGOColor>(cc.getFilteredColors());
		Set<LEGOColor> colorChooserSelectedColors = new TreeSet<LEGOColor>();
		for(LEGOColor c : cc.getColorChooserSelectedColors())
			colorChooserSelectedColors.add(c);
		for(ColorGroup group : colorGroups) {
			if(!cc.getShowOtherColorsGroup() && group.isOtherColorsGroup())
				continue;
			ColorGroupPanel panel = new ColorGroupPanel(group, widest, filteredColors, colorChooserSelectedColors);
			if(panel.isEmpty())
				continue;
			mainPanel.add(panel);
			panels.add(panel);
		}
		if(isVisible())
			setVisible(true); // Force repaint.
		tellController();
	}
	
	public Action createPackAction() {
		Action a = new AbstractAction() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pack();
			}
		};		
		a.putValue(Action.SHORT_DESCRIPTION, "Pack the color chooser.");
		a.putValue(Action.SMALL_ICON, Icons.pack(Icons.SIZE_SMALL));
		a.putValue(Action.LARGE_ICON_KEY, Icons.pack(Icons.SIZE_LARGE));
		a.putValue(Action.NAME, "Pack");		
		return a;
	}
	
	private class ColorGroupPanel extends JPanel implements ActionListener {
		private List<ColorCheckBox> boxes;
		private boolean ignoreEvents;
		
		private ColorGroupPanel(ColorGroup group, int labelWidth, Set<LEGOColor> remainingColors, Set<LEGOColor> colorChooserSelectedColors) {
			ignoreEvents = true;
			
			JButton buttonAll = new JButton(Icons.checkbox(Icons.SIZE_SMALL, true));//"\u2200");
			buttonAll.setToolTipText("Select all colors in this row.");
			buttonAll.setPreferredSize(new Dimension(24, 40));
			buttonAll.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					ignoreEvents = true;
					for(ColorCheckBox box : boxes)
						box.setSelected(true);
					ignoreEvents = false;
					ColorGroupPanel.this.actionPerformed(e);
				}
			});
			JButton buttonNone = new JButton(Icons.checkbox(Icons.SIZE_SMALL, false));//"\u2205");
			buttonNone.setToolTipText("Deselect all colors in this row.");
			buttonNone.setPreferredSize(new Dimension(24, 40));
			buttonNone.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					ignoreEvents = true;
					for(ColorCheckBox box : boxes)
						box.setSelected(false);
					ignoreEvents = false;
					ColorGroupPanel.this.actionPerformed(e);
				}
			});
			
			// Set up label:
			JLabel label = new JLabel(group.getName());
			label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
			label.setHorizontalAlignment(SwingConstants.RIGHT);

			// Set up self:
			setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
			add(label);
			add(buttonAll);
			add(buttonNone);

			boxes = new LinkedList<ColorCheckBox>();
			for(LEGOColor color : remainingColors) {
				if(group.containsColor(color)) {
					ColorCheckBox box = new ColorCheckBox(colorChooserSelectedColors.contains(color), color, cc);
					boxes.add(box);
					add(box);
					box.addActionListener(this);					
				}					
			}
			for(ColorCheckBox box : boxes) {
				remainingColors.remove(box.color);
			}

			ignoreEvents = false;
		}
		
		public boolean isEmpty() {
			return boxes.isEmpty();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ignoreEvents)
				return;
			tellController();
		}
		
		public void getSelected(Set<LEGOColor> selectedColors) {
			for(ColorCheckBox box : boxes) {
				if(box.isSelected())
					selectedColors.add(box.getColor());
			}
		}
	}

	private static class ColorCheckBox extends JCheckBox {
		private LEGOColor color;
		
		public ColorCheckBox(boolean selected, LEGOColor color, ColorController cc) {
			this.color = color;
			int size = getPreferredSize().width*2;
			setToolTipText(ColorController.getLongIdentifier(color));
			setHorizontalAlignment(SwingConstants.CENTER);
			setPreferredSize(new Dimension(size, size));
			setBackground(color.getRGB());
			
			setSelected(selected);
		}
		
		public LEGOColor getColor() {
			return color;
		}
	}
}
