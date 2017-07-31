package mosaic.ui;

import icon.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import mosaic.controllers.*;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineListener;
import colors.LEGOColor;

public class ColorLegend extends JToolBar implements ChangeListener, PipelineListener {
	private BrickedView brickedView;
	private LEGOColor.CountingLEGOColor[] colors;
	private ColorController cc;
	private UIController uc;
	private JScrollPane scrollPane;
	private JList<LEGOColor.CountingLEGOColor> list;

	public ColorLegend(MainController mc, MainWindow mw, Pipeline pipeline) {
		super("Legend");
		cc = mc.getColorController();
		uc = mc.getUIController();
		uc.addChangeListener(this);
		brickedView = mw.getBrickedView();
		pipeline.addMosaicImageListener(this);
		
		list = new JList<LEGOColor.CountingLEGOColor>();
		list.setAutoscrolls(true);
		list.setCellRenderer(new CellRenderer());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list.setBackground(getBackground());
		mc.getMagnifierController().addChangeListener(this);

		setLayout(new BorderLayout());
		scrollPane = new JScrollPane(list);
		add(scrollPane, BorderLayout.CENTER);
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
		if(!uc.showLegend() || colors == null || highlights.isEmpty())
			return;
		// Remove already selected indices:
		int[] alreadySelected = list.getSelectedIndices();
		for(int index : alreadySelected) {
			if(highlights.contains(colors[index].c)) {
				highlights.remove(colors[index].c);
			}
			else {
				list.removeSelectionInterval(index,  index);
			}
		}		
		if(highlights.isEmpty())
			return;
		
		for(int i = 0; i < colors.length; i++) {
			if(highlights.contains(colors[i].c)) {
				list.addSelectionInterval(i, i);
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		setVisible(uc.showLegend());
		colors = brickedView.getLegendColors();
		list.setListData(colors);
	}

	@Override
	public void imageChanged(BufferedImage image) {
		colors = brickedView.getLegendColors();
		list.setListData(colors);
	}
}
