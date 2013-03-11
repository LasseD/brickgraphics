package griddy;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;

public class ColorButton extends JButton {
	private static final long serialVersionUID = 7379914358059569001L;
	private static final int ICON_SIZE = 10;
	private Color color;
	private List<ActionListener> actionListeners;
	
	public ColorButton(final Component parent, Color color) {
		actionListeners = new LinkedList<ActionListener>();
		this.color = color;
		setIcon(new Icon() {
			@Override
			public int getIconHeight() {
				return ICON_SIZE;
			}

			@Override
			public int getIconWidth() {
				return ICON_SIZE;
			}

			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setColor(Color.BLACK);
				g2.drawRect(x, y, ICON_SIZE, ICON_SIZE);
				g2.setColor(ColorButton.this.color);
				g2.fillRect(x+1, y+1, ICON_SIZE-2, ICON_SIZE-2);
			}			
		});
		final SimpleColorChooser cs = new SimpleColorChooser(ColorButton.this.color);
		super.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JColorChooser.createDialog(parent, "Choose a color", true, cs, new ActionListener() {						
					@Override
					public void actionPerformed(ActionEvent e) {
						Color c = cs.getColor();
						if(c != ColorButton.this.color) {
							ColorButton.this.color = c;
							for(ActionListener al : actionListeners)
								al.actionPerformed(e);
							repaint();
						}
					}
				}, null).setVisible(true);
			}				
		});
	}

	@Override
	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
}

