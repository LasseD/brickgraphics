package mosaic.ui;

import icon.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mosaic.controllers.*;
import colors.LEGOColor;

public class ColorLegend extends JList<LEGOColor.CountingLEGOColor> implements ChangeListener {
	private BrickedView brickedController;
	private LEGOColor.CountingLEGOColor[] colors;
	private ColorController cc;
	private UIController uc;

	public ColorLegend(MainWindow mw) {
		cc = mw.getColorController();
		uc = mw.getUIController();
		brickedController = mw.getBrickedView();
		setAutoscrolls(true);
		setCellRenderer(new CellRenderer());
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		mw.getMagnifierController().addChangeListener(this);
	}

	private class CellRenderer extends JLabel implements ListCellRenderer<LEGOColor.CountingLEGOColor> {
		@Override
		public JComponent getListCellRendererComponent(JList<? extends LEGOColor.CountingLEGOColor> list, 
				final LEGOColor.CountingLEGOColor color, int index, boolean isSelected, boolean cellHasFocus) {
			String identifier = cc.getNormalIdentifier(color.c);
			String text = "";
			if(identifier != null)
				text = identifier + ".";
			if(uc.showTotals())
				text += " TOTAL: " + color.cnt;
			setText(text);
			setIcon(new Icon() {
				@Override
				public int getIconHeight() {
					return Icons.SIZE_LARGE;
				}

				@Override
				public int getIconWidth() {
					return Icons.SIZE_LARGE;
				}

				@Override
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.setColor(color.c.getRGB());
					g.fillRect(x, y, getIconWidth(), getIconHeight());
					g.setColor(Color.BLACK);
					g.drawRect(x, y, getIconWidth(), getIconHeight());
				}

			});
			setToolTipText(ColorController.getLongIdentifier(color.c));	
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
	
	public void setHighlightedColors(Set<LEGOColor> highlights) {
		if(!uc.enableLegend() || colors == null || highlights.isEmpty())
			return;
		// Remove already selected indices:
		int[] alreadySelected = getSelectedIndices();
		for(int index : alreadySelected) {
			if(highlights.contains(colors[index].c)) {
				highlights.remove(colors[index].c);
			}
			else {
				this.removeSelectionInterval(index,  index);
			}
		}		
		if(highlights.isEmpty())
			return;
		
		for(int i = 0; i < colors.length; i++) {
			if(highlights.contains(colors[i].c)) {
				addSelectionInterval(i, i);
			}
		}			
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(!uc.enableLegend())
			return;
		colors = brickedController.getLegendColors();
		setListData(colors);
	}
}
