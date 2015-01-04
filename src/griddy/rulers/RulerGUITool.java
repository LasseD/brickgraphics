package griddy.rulers;

import griddy.*;
import javax.swing.*;
import icon.*;
import java.awt.*;
import java.awt.event.*;

public class RulerGUITool extends JPanel {
	private static final long serialVersionUID = 3181093137596405592L;

	public RulerGUITool(BorderRuler ruler) {
		add(new LengthField(ruler));
		add(new LengthTypeButton(ruler));
		add(new LockButton(ruler));
	}
	
	public RulerGUITool(Griddy parent, final Ruler ruler) {
		add(new LengthField(ruler));
		add(new LengthTypeButton(ruler));
		final ColorButton colorButton = new ColorButton(parent, ruler.getColor());
		colorButton.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				ruler.setColor(colorButton.getColor());
			}
		});
		ruler.addListener(new RulerListener() {			
			@Override
			public void rulerChanged(Ruler ruler) {
				colorButton.setColor(ruler.getColor());
			}
		});
		add(colorButton);	
	}
	
	public void draw(Graphics2D g2, boolean vertically) {
		// TODO
	}
	
	private class LockButton extends JButton implements BorderRulerListener {
		private static final long serialVersionUID = -3835439137034883999L;

		public LockButton(final BorderRuler ruler) {
			setIcon(ruler.isRulerLocked());				
			
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					ruler.setRulerLocked(!ruler.isRulerLocked());
					setIcon(ruler.isRulerLocked());
				}
			});
		}

		private void setIcon(boolean locked) {
			if(locked) {
				super.setIcon(Icons.get(Icons.SIZE_SMALL, "lock"));				
			}
			else {
				super.setIcon(Icons.get(Icons.SIZE_SMALL, "unlock"));								
			}			
		}
		
		@Override
		public void borderRulerChanged(BorderRuler br) {
			setIcon(br.isRulerLocked());
		}
	}
	
	private class LengthField extends JTextField implements BorderRulerListener, RulerListener {
		private static final long serialVersionUID = 5100683684954966380L;

		private LengthField(double d) {
			super(String.format("%.2f", d), 6);			
		}
		
		public LengthField(final BorderRuler ruler) {
			this(ruler.getDist());

			ruler.addScaleListener(this, true);
			
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						double dist = Double.parseDouble(getText());
						if(dist > 0)
							ruler.setDist(dist);
						else
							setText(String.format("%.2f", ruler.getDist()));						
					}
					catch(NumberFormatException e2) {						
						setText(String.format("%.2f", ruler.getDist()));						
					}
				}
			});
		}
		
		public LengthField(final Ruler ruler) {
			this(ruler.getDist());

			ruler.addListener(this);
			
			setEditable(false);
		}
		
		@Override
		public void borderRulerChanged(BorderRuler br) {
			setText(String.format("%.2f", br.getDist()));
		}

		@Override
		public void rulerChanged(Ruler ruler) {
			setText(String.format("%.2f", ruler.getDist()));
		}
	}
	
	private class LengthTypeButton extends JButton implements BorderRulerListener, RulerListener {
		private static final long serialVersionUID = -8935555957569779044L;
		private LengthType lengthType;

		public LengthTypeButton(final BorderRuler ruler) {
			super(ruler.getLengthType().getText(), ruler.getLengthType().getIcon());
			lengthType = ruler.getLengthType();
			
			ruler.addScaleListener(this, true);
			
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					ruler.setLengthType(LengthType.values()[(lengthType.ordinal()+1)%LengthType.values().length]);
				}
			});
		}
		
		public LengthTypeButton(final Ruler ruler) {
			super(ruler.getLengthType().getText(), ruler.getLengthType().getIcon());
			lengthType = ruler.getLengthType();
			
			ruler.addListener(this);
			
			addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					ruler.setLengthType(LengthType.values()[(lengthType.ordinal()+1)%LengthType.values().length]);
				}
			});
		}
		
		public void setLengthType(LengthType lengthType) {
			this.lengthType = lengthType;
			setIcon(lengthType.getIcon());
			setText(lengthType.getText());
		}

		@Override
		public void borderRulerChanged(BorderRuler br) {
			setLengthType(br.getLengthType());
		}

		@Override
		public void rulerChanged(Ruler r) {
			setLengthType(r.getLengthType());
		}
	}
}
