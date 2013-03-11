package mosaic.ui.bricked;

import ui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import io.*;
import javax.swing.event.*;

import mosaic.io.BrickGraphicsState;
import transforms.*;
import java.util.*;
import java.util.List;
import bricks.*;
import colors.*;

public class ToBricksToolBar extends JToolBar implements ChangeListener, ModelSaver<BrickGraphicsState> {
	private static final long serialVersionUID = -4411328331321878589L;
	private JButton halfToneTypeButton, toBricksTypeButton, directionButton, sizeMeasureButton;
	private JTextField sizeField;
	private List<ChangeListener> listeners;
	
	private int width, height; // basicUnits
	private float originalScale;
	private ToBricksType toBricksType;
	private HalfToneType halfToneType;
	private SizeType typeWidth, typeHeight;
	private boolean directionIsWidth;
	private ColorChooser colorChooser;
	
	public ToBricksToolBar(final JFrame parent, Model<BrickGraphicsState> model) {
		super("Build options", HORIZONTAL);
		model.addModelSaver(this);

		reloadModel(model);
		listeners = new LinkedList<ChangeListener>();
		colorChooser = new ColorChooser(parent, model);
		colorChooser.addChangeListener(this);
		
		halfToneTypeButton = new JButton();
		halfToneTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int length = HalfToneType.values().length;
				halfToneType = HalfToneType.values()[(halfToneType.ordinal()+1)%length];
				update();
			}
		});
		
		toBricksTypeButton = new JButton();
		toBricksTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int length = ToBricksType.values().length;
				toBricksType = ToBricksType.values()[(toBricksType.ordinal()+1)%length];
				update();
			}
		});

		directionButton = new JButton();
		directionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				directionIsWidth = !directionIsWidth;
				update();
			}
		});
		
		sizeMeasureButton = new JButton();
		sizeMeasureButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int length = SizeType.values().length;
				if(directionIsWidth) {
					typeWidth = SizeType.values()[(typeWidth.ordinal()+1)%length];
				}
				else {
					typeHeight = SizeType.values()[(typeHeight.ordinal()+1)%length];					
				}
				update();
			}
		});
		
		sizeField = new JTextField(4);
		sizeField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(directionIsWidth) {
					try {
						ToBricksToolBar.this.width = typeWidth.getBasicSize(Integer.parseInt(sizeField.getText()));
					}
					catch(NumberFormatException ex) {
						// don't change width.
					}
				}
				else {
					try {
						ToBricksToolBar.this.height = typeHeight.getBasicSize(Integer.parseInt(sizeField.getText()));
					}
					catch(NumberFormatException ex) {
						// don't change height.
					}					
				}
				update();
			}
		});
				
		// add components
		setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		add(halfToneTypeButton);
		add(toBricksTypeButton);
		add(colorChooser.getOnOffAction());
		addSeparator();
		add(directionButton);
		add(new JLabel(" = "));
		add(sizeField);
		add(new JLabel(" x "));
		add(sizeMeasureButton);
		
		// finish:
		update();
	}
	
	public void stateChanged(ChangeEvent arg0) {
		update();
	}

	public LEGOColor[] getColors() {
		return colorChooser.getColors();
	}
	public int getBasicHeight() {
		return height;
	}
	public int getBasicWidth() {
		return width;
	}
	public ToBricksType getToBricksType() {
		return toBricksType;
	}
	public HalfToneType getHalfToneType() {
		return halfToneType;
	}
	
	public void imageUpdated(int newWidth, int newHeight) {
		originalScale = newWidth/(float)newHeight;
		update();
	}

	public void update() {
		halfToneTypeButton.setIcon(halfToneType.getIcon());
		toBricksTypeButton.setIcon(toBricksType.getIcon());
		if(directionIsWidth) {
			width = toBricksType.closestCompatibleWidth(width, typeWidth.getUnit());
			height = toBricksType.closestCompatibleHeight(Math.round(width/originalScale), typeHeight.getUnit());	
			directionButton.setIcon(Icons.width(Icons.SIZE_LARGE));
			sizeMeasureButton.setIcon(typeWidth.getIcon());
			sizeField.setText("" + typeWidth.getUnitSize(width));
		}
		else {
			height = toBricksType.closestCompatibleHeight(height, typeHeight.getUnit());		
			width = toBricksType.closestCompatibleWidth(Math.round(originalScale*height), typeWidth.getUnit());
			directionButton.setIcon(Icons.height(Icons.SIZE_LARGE));
			sizeMeasureButton.setIcon(typeHeight.getIcon());
			sizeField.setText("" + typeHeight.getUnitSize(height));
		}
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public ColorChooser getColorChooser() {
		return colorChooser;
	}
	
	private enum SizeType {
		plate(Icons.plateHeight(Icons.SIZE_LARGE), Sizes.plate.height()), 
		brick(Icons.brickHeight(Icons.SIZE_LARGE), Sizes.brick.height()), 
		stud(Icons.brickWidth(Icons.SIZE_LARGE), Sizes.brick.width());
		
		private Icon icon;
		private int unit;
		private SizeType(Icon icon, int unit) {
			this.icon = icon;
			this.unit = unit;
		}
		public Icon getIcon() {
			return icon;
		}
		public int getUnitSize(int sizeInBasicUnits) {
			assert sizeInBasicUnits % unit == 0 : sizeInBasicUnits + "%" + unit + "=" + 0;
			return sizeInBasicUnits / unit;
		}
		public int getBasicSize(int sizeInUnits) {
			return sizeInUnits*unit;
		}
		public int getUnit() {
			return unit;
		}
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.ToBricksWidth, width);
		model.set(BrickGraphicsState.ToBricksHeight, height);
		model.set(BrickGraphicsState.ToBricksDirectionIsWidth, directionIsWidth);
		model.set(BrickGraphicsState.ToBricksTypeIndex, toBricksType.ordinal());
		model.set(BrickGraphicsState.ToBricksHalfToneTypeIndex, halfToneType.ordinal());
		model.set(BrickGraphicsState.ToBricksSizeTypeWidthIndex, typeWidth.ordinal());
		model.set(BrickGraphicsState.ToBricksSizeTypeHeightIndex, typeHeight.ordinal());
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		this.width = (Integer)model.get(BrickGraphicsState.ToBricksWidth);
		this.height = (Integer)model.get(BrickGraphicsState.ToBricksHeight);
		directionIsWidth = (Boolean)model.get(BrickGraphicsState.ToBricksDirectionIsWidth);
		originalScale = width/(float)height;
		int tbtl = ToBricksType.values().length;
		toBricksType = ToBricksType.values()[((Integer)model.get(BrickGraphicsState.ToBricksTypeIndex))%tbtl];
		int httl = HalfToneType.values().length;
		halfToneType = HalfToneType.values()[((Integer)model.get(BrickGraphicsState.ToBricksHalfToneTypeIndex))%httl];
		int tl = SizeType.values().length;
		typeWidth = SizeType.values()[((Integer)model.get(BrickGraphicsState.ToBricksSizeTypeWidthIndex))%tl];
		typeHeight = SizeType.values()[((Integer)model.get(BrickGraphicsState.ToBricksSizeTypeHeightIndex))%tl];
		if(colorChooser != null) {
			colorChooser.reloadModel(model);
			update();			
		}
	}
}
