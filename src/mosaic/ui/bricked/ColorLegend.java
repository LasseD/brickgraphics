package mosaic.ui.bricked;

import ui.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import colors.LEGOColor;

public class ColorLegend extends JDialog implements PropertyChangeListener {
	private static final long serialVersionUID = 2111134483677184536L;
	private JList list;
	private Action onOffAction;
	private Action enableMagnifier, showColors;
	private LEGOColor[] colors;

	public Action getOnOffAction() {
		return onOffAction;
	}

	public ColorLegend(final JFrame owner, Action enableMagnifier, Action showColors) {
		super(owner, "Legend", false);
		this.enableMagnifier = enableMagnifier;
		this.showColors = showColors;
		enableMagnifier.addPropertyChangeListener(this);
		showColors.addPropertyChangeListener(this);
		setAlwaysOnTop(false);
		setFocusableWindowState(false);
		list = new JList();
		list.setCellRenderer(new CellRenderer());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane scrollPane = new JScrollPane(list);
		getContentPane().add(scrollPane);
		list.setBackground(getBackground());

		onOffAction = new AbstractAction() {
			private static final long serialVersionUID = 9017685908393308200L;
			private boolean first = true;
			public void actionPerformed(ActionEvent e) {
				if(first) {
					pack();
					int x = Toolkit.getDefaultToolkit().getScreenSize().width-getWidth();
					x = Math.min(x, owner.getX()+owner.getWidth());
					setLocation(x, owner.getY()+owner.getHeight()-getHeight());
					first = false;
				}
				setVisible((Boolean)onOffAction.getValue(Action.SELECTED_KEY));
			}
		};
		onOffAction.putValue(Action.SHORT_DESCRIPTION, "Show color legend.");
		onOffAction.putValue(Action.SMALL_ICON, Icons.colorLegend(Icons.SIZE_SMALL));
		onOffAction.putValue(Action.LARGE_ICON_KEY, Icons.colorLegend(Icons.SIZE_LARGE));
		onOffAction.putValue(Action.NAME, "Legend");
		onOffAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		onOffAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Legend".indexOf('L'));
		onOffAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK));	
		onOffAction.putValue(Action.SELECTED_KEY, false);
		addWindowListener(new WindowListener(){
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}

			public void windowClosing(WindowEvent e) {
				onOffAction.putValue(Action.SELECTED_KEY, false);				
			}
		});
	}

	public void setColors(LEGOColor[] colors) {
		this.colors = colors;
		list.setListData(colors);
	}

	public void highlight(Set<LEGOColor> hightlight) {
		list.clearSelection();
		for(int i = 0; i < colors.length; i++) {
			if(hightlight.contains(colors[i])) {
				list.addSelectionInterval(i, i);
				//System.out.println("Highlighting " + i);
			}
		}
	}

	private static class CellRenderer extends JLabel implements ListCellRenderer {
		private static final long serialVersionUID = 21945136035939664L;

		public JComponent getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			final LEGOColor color = (LEGOColor)value;
			setText(color.getShortIdentifier() + " - " + color.getName());
			setIcon(new Icon() {
				public int getIconHeight() {
					return Icons.SIZE_LARGE;
				}

				public int getIconWidth() {
					return Icons.SIZE_LARGE;
				}

				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.setColor(color.rgb);
					g.fillRect(x, y, getIconWidth(), getIconHeight());
					g.setColor(Color.BLACK);
					g.drawRect(x, y, getIconWidth(), getIconHeight());
				}

			});
			setToolTipText(color.getToolTipText());	
			setFont(list.getFont());
			setOpaque(true);

			if(isSelected) {
				setBackground(list.getSelectionBackground());
			}
			else {
				setBackground(list.getBackground());
			}


			return this;
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if((Boolean)enableMagnifier.getValue(Action.SELECTED_KEY) && 
				!(Boolean)showColors.getValue(Action.SELECTED_KEY)) {
			onOffAction.putValue(Action.SELECTED_KEY, true);
			onOffAction.actionPerformed(null);
		}
	}
}
