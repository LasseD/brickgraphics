package mosaic.ui;

import javax.swing.*;

import colors.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.event.*;
import mosaic.controllers.*;

public class ColorChooserDialog extends JDialog implements ChangeListener {
	private List<ColorGroupPanel> panels;
	private boolean everEnabled, enabled;
	private ColorController cc;
	private JPanel mainPanel;
	
	public ColorChooserDialog(final JFrame owner, ColorController cc) {
		super(owner, "Colors", false);

		this.cc = cc;
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
		getContentPane().add(new JScrollPane(mainPanel, 
				 ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
				 ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		
		stateChanged(null);
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
		Set<ColorGroup> selectedGroups = new TreeSet<ColorGroup>();
		Set<LEGOColor> selectedColors = new TreeSet<LEGOColor>();
		
		for(ColorGroupPanel panel : panels) {
			panel.getSelected(selectedGroups, selectedColors);
		}

		if(selectedColors.size() < 2) {
			selectedColors.clear();
			selectedColors.add(LEGOColor.BW[0]);
			selectedColors.add(LEGOColor.BW[1]);
		}

		cc.setselectedColorsAndGroups(selectedColors, selectedGroups, new ChangeEvent(ColorChooserDialog.this));
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
		for(ColorGroup group : colorGroups) {
			ColorGroupPanel panel = ColorGroupPanel.create(this, group, widest, cc, filteredColors);
			if(panel == null)
				continue;
			mainPanel.add(panel);
			panels.add(panel);
		}
		if(isVisible())
			setVisible(true); // Force repaint.
		tellController();
	}
	
	private static class ColorGroupPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 9097335036243041400L;
		private List<ColorCheckBox> boxes;
		private JCheckBox mainBox;
		private volatile boolean ignoreEvents;
		private ColorGroup group;
		private ColorChooserDialog ccd;
		
		public static ColorGroupPanel create(ColorChooserDialog ccd, ColorGroup group, int labelWidth, ColorController cc, Set<LEGOColor> allowedColors) {
			ColorGroupPanel out = new ColorGroupPanel();
			out.ccd = ccd;
			out.group = group;
			out.mainBox = new JCheckBox();
			out.mainBox.setSelected(cc.getSelectedColorGroups().contains(group));
			out.mainBox.addActionListener(out);
			
			// Set up label:
			JLabel label = new JLabel(group.getName());
			label.setPreferredSize(new Dimension(labelWidth, label.getPreferredSize().height));
			label.setHorizontalAlignment(SwingConstants.RIGHT);

			// Set up self:
			out.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
			out.add(label);
			out.add(out.mainBox);

			out.boxes = new LinkedList<ColorCheckBox>();
			for(LEGOColor color : group.getColors()) {
				if(!allowedColors.contains(color))
					continue;
				ColorCheckBox box = new ColorCheckBox(group, color, cc);
				out.boxes.add(box);
				out.add(box);
				box.addActionListener(out);
			}
			if(out.boxes.isEmpty())
				return null;
			return out;
		}
		
		private ColorGroupPanel() {}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if(ignoreEvents)
				return;
			if(e.getSource() == mainBox) {
				ignoreEvents = true;
				for(ColorCheckBox box : boxes) {
					box.setEnabled(!mainBox.isSelected());
				}
				ignoreEvents = false;
			}
			ccd.tellController();
		}
		
		public void getSelected(Set<ColorGroup> selectedGroups, Set<LEGOColor> selectedColors) {
			if(mainBox.isSelected())
				selectedGroups.add(group);
			for(ColorCheckBox box : boxes) {
				if(box.shouldBeSaved())
					selectedColors.add(box.getColor());
			}
		}
	}

	private static class ColorCheckBox extends JCheckBox {
		private static final long serialVersionUID = -2531084719234352355L;
		private LEGOColor color;
		private boolean selectedWhenNotSupressed;
		
		public ColorCheckBox(ColorGroup group, LEGOColor color, ColorController cc) {
			this.color = color;
			int size = getPreferredSize().width*2;
			setToolTipText(ColorController.getLongIdentifier(color));
			setHorizontalAlignment(SwingConstants.CENTER);
			setPreferredSize(new Dimension(size, size));
			setBackground(color.getRGB());
			
			selectedWhenNotSupressed = cc.getSelectedColors().contains(color);
			boolean supressed = cc.getSelectedColorGroups().contains(group);
			super.setEnabled(!supressed);
			setSelected(supressed || selectedWhenNotSupressed);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);				
			if(enabled) {
				setSelected(selectedWhenNotSupressed);
			}
			else {
				selectedWhenNotSupressed = isSelected();
				setSelected(true);
			}
		}
		
		public LEGOColor getColor() {
			return color;
		}

		public boolean shouldBeSaved() {
			return isEnabled() ? isSelected() : selectedWhenNotSupressed;
		}
	}
}
