package mosaic.ui.menu;

import ui.*;
import mosaic.ui.*;
import javax.swing.*;
import java.awt.event.*;
import io.*;
import javax.swing.event.*;
import mosaic.io.*;
import transforms.*;
import java.util.*;
import bricks.*;
import mosaic.controllers.*;

public class ToBricksTools implements ChangeListener, ModelSaver<BrickGraphicsState> {
	private JButton halfToneTypeButton, sizeMeasureButtonWidth, sizeMeasureButtonHeight;
	private JButton[] toBricksTypeButtons;
	private JTextField sizeFieldWidth, sizeFieldHeight, propagationPercentageField;
	private JButton buttonLessPP, buttonMorePP;
	private List<ChangeListener> listeners;
	
	private int width, height; // in basicUnits
	private float originalScale;
	private ToBricksType toBricksType;
	private HalfToneType halfToneType;
	private SizeType typeWidth, typeHeight;
	private boolean sizeChoiceFromWidth;
	private ColorChooserDialog colorChooser;
	private int propagationPercentage;
	private volatile boolean uiReady;
	
	public ToBricksTools(final JFrame parent, final Model<BrickGraphicsState> model, ColorController cc) {
		uiReady = false;
		model.addModelSaver(this);
		cc.addChangeListener(this);

		reloadModel(model);
		listeners = new LinkedList<ChangeListener>();
		colorChooser = new ColorChooserDialog(parent, cc);
		toBricksTypeButtons = new JButton[ToBricksType.values().length];

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI(parent, model);
			}
		});
	}
	
	public void addComponents(JToolBar toolBar, boolean append) {
		if(!uiReady)
			throw new IllegalStateException();
		if(append)
			toolBar.add(Box.createHorizontalStrut(10));
		for(JButton b : toBricksTypeButtons)
			toolBar.add(b);
		toolBar.add(halfToneTypeButton);
		//toolBar.add(Box.createHorizontalStrut(10));
		toolBar.add(buttonLessPP);
		toolBar.add(propagationPercentageField);
		toolBar.add(buttonMorePP);		
		toolBar.add(Box.createHorizontalStrut(10));
		toolBar.add(new JLabel("Width = "));
		toolBar.add(sizeFieldWidth);
		toolBar.add(sizeMeasureButtonWidth);
		toolBar.add(new JLabel("Height = "));
		toolBar.add(sizeFieldHeight);
		toolBar.add(sizeMeasureButtonHeight);
	}
	
	private void setUI(final JFrame parent, final Model<BrickGraphicsState> model) {
		int i = 0;
		for(final ToBricksType type : ToBricksType.values()) {
			JButton toBricksTypeButton = new JButton();
			toBricksTypeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					toBricksType = type;
					update();
				}
			});		
			toBricksTypeButtons[i++] = toBricksTypeButton;
		}
		
		halfToneTypeButton = new JButton();
		halfToneTypeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int length = HalfToneType.values().length;
				halfToneType = HalfToneType.values()[(halfToneType.ordinal()+1)%length];
				update();
			}
		});		
		
		buttonLessPP = new JButton(Icons.get(Icons.SIZE_LARGE, "propagate_less"));
		buttonLessPP.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propagationPercentage -= 10;
				update();
			}
		});		
		propagationPercentageField = new JTextField(propagationPercentage + "%", 4);
		propagationPercentageField.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get percentage:
				String s = propagationPercentageField.getText().trim();
				int sum = 0;
				for(int i = 0; i < s.length(); ++i) {
					char c = s.charAt(i);
					if(c < '0' || c > '9')
						break;
					sum = 10*sum + (c-'0');
				}
				propagationPercentage = sum;				
				update();
			}
		});
		buttonMorePP = new JButton(Icons.get(Icons.SIZE_LARGE, "propagate_more"));
		buttonMorePP.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propagationPercentage += 10;
				update();
			}
		});

		sizeMeasureButtonWidth = new JButton();
		sizeMeasureButtonHeight = new JButton();
		sizeMeasureButtonWidth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = true;
				typeWidth = SizeType.values()[(typeWidth.ordinal()+1)%SizeType.values().length];
				sizeMeasureButtonWidth.setIcon(typeWidth.iconWidth);
				update();
			}
		});
		sizeMeasureButtonHeight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = false;
				typeHeight = SizeType.values()[(typeHeight.ordinal()+1)%SizeType.values().length];
				sizeMeasureButtonHeight.setIcon(typeHeight.iconHeight);
				update();
			}
		});
		
		sizeFieldWidth = new JTextField(4);
		sizeFieldHeight = new JTextField(4);
		sizeFieldWidth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = true;
				try {
					ToBricksTools.this.width = typeWidth.getBasicSize(Integer.parseInt(sizeFieldWidth.getText().trim()));
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
				update();
			}
		});
		sizeFieldHeight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = false;
				try {
					ToBricksTools.this.height = typeHeight.getBasicSize(Integer.parseInt(sizeFieldHeight.getText().trim()));
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
				update();
			}
		});
				
		// finish:
		uiReady = true;
		update();		
	}
	
	public void stateChanged(ChangeEvent arg0) {
		update();
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
	public int getPropagationPercentage() {
		return propagationPercentage;
	}
	
	public void imageUpdated(int newWidth, int newHeight) {
		originalScale = newWidth/(float)newHeight;
		update();
	}

	public void update() {		
		if(!uiReady)
			return;
		halfToneTypeButton.setIcon(halfToneType.getIcon());
		
		int i = 0;
		for(final ToBricksType type : ToBricksType.values()) {
			toBricksTypeButtons[i].setIcon(toBricksType.ordinal() == i ? type.getEnabledIcon() : type.getDisabledIcon());
			++i;
		}
		sizeMeasureButtonWidth.setIcon(typeWidth.iconWidth);
		sizeMeasureButtonHeight.setIcon(typeHeight.iconHeight);
		
		//propagationPercentagePanel.setEnabled(halfToneType == HalfToneType.FloydSteinberg);
		propagationPercentageField.setEnabled(halfToneType == HalfToneType.FloydSteinberg);
		buttonLessPP.setEnabled(halfToneType == HalfToneType.FloydSteinberg);
		buttonMorePP.setEnabled(halfToneType == HalfToneType.FloydSteinberg);
		
		if(propagationPercentage < 0)
			propagationPercentage = 0;
		else if(propagationPercentage > 100)
			propagationPercentage = 100;
		propagationPercentageField.setText(propagationPercentage + "%");
				
		if(sizeChoiceFromWidth) {
			width = toBricksType.closestCompatibleWidth(width, typeWidth.unit);
			height = toBricksType.closestCompatibleHeight(Math.round(width/originalScale), typeHeight.unit);	
		}
		else {
			height = toBricksType.closestCompatibleHeight(height, typeHeight.unit);		
			width = toBricksType.closestCompatibleWidth(Math.round(originalScale*height), typeWidth.unit);
		}
		sizeFieldWidth.setText("" + typeWidth.getUnitSize(width));
		sizeFieldHeight.setText("" + typeHeight.getUnitSize(height));
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	public ColorChooserDialog getColorChooser() {
		if(colorChooser == null)
			throw new IllegalStateException();
		return colorChooser;
	}
	
	private enum SizeType {
		plate("plate", SizeInfo.PLATE_HEIGHT), 
		brick("brick", SizeInfo.BRICK_HEIGHT), 
		stud("stud", SizeInfo.BRICK_WIDTH);
		
		public final Icon iconWidth, iconHeight;
		public final int unit;
		private SizeType(String icon, int unit) {
			this.iconWidth = Icons.get(Icons.SIZE_LARGE, "width_" + icon);
			this.iconHeight = Icons.get(Icons.SIZE_LARGE, "height_" + icon);
			this.unit = unit;
		}
		public int getUnitSize(int sizeInBasicUnits) {
			return sizeInBasicUnits/unit;
		}
		public int getBasicSize(int sizeInUnits) {
			return sizeInUnits*unit;
		}
	}

	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.ToBricksWidth, width);
		model.set(BrickGraphicsState.ToBricksHeight, height);
		model.set(BrickGraphicsState.ToBricksTypeIndex, toBricksType.ordinal());
		model.set(BrickGraphicsState.ToBricksHalfToneTypeIndex, halfToneType.ordinal());
		model.set(BrickGraphicsState.ToBricksSizeTypeWidthIndex, typeWidth.ordinal());
		model.set(BrickGraphicsState.ToBricksSizeTypeHeightIndex, typeHeight.ordinal());
		model.set(BrickGraphicsState.ToBricksPropagationPercentage, propagationPercentage);
	}
	
	public void reloadModel(Model<BrickGraphicsState> model) {
		this.width = (Integer)model.get(BrickGraphicsState.ToBricksWidth);
		this.height = (Integer)model.get(BrickGraphicsState.ToBricksHeight);
		propagationPercentage = (Integer)model.get(BrickGraphicsState.ToBricksPropagationPercentage);
		originalScale = width/(float)height;
		int tbtl = ToBricksType.values().length;
		toBricksType = ToBricksType.values()[((Integer)model.get(BrickGraphicsState.ToBricksTypeIndex))%tbtl];
		int httl = HalfToneType.values().length;
		halfToneType = HalfToneType.values()[((Integer)model.get(BrickGraphicsState.ToBricksHalfToneTypeIndex))%httl];
		typeWidth = SizeType.values()[((Integer)model.get(BrickGraphicsState.ToBricksSizeTypeWidthIndex))%SizeType.values().length];
		typeHeight = SizeType.values()[((Integer)model.get(BrickGraphicsState.ToBricksSizeTypeHeightIndex))%SizeType.values().length];
		update();			
	}
}
