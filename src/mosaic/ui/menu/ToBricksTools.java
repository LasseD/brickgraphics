package mosaic.ui.menu;

import icon.*;
import icon.ToBricksIcon.ToBricksIconType;
import javax.swing.*;

import java.awt.Insets;
import java.awt.event.*;
import io.*;
import javax.swing.event.*;
import mosaic.controllers.MainController;
import mosaic.io.*;
import transforms.*;
import ui.IconizedTextfield;
import ui.LividTextField;

import java.util.*;
import bricks.*;

public class ToBricksTools implements ChangeListener, ModelHandler<BrickGraphicsState> {
	private JButton[] toBricksTypeButtons;
	private LividTextField propagationPercentageField;
	private IconizedTextfield sizeFieldWidth, sizeFieldHeight; 
	private JButton buttonLessPP, buttonMorePP;
	private List<ChangeListener> listeners;
	
	private int width, height; // in basicUnits
	private float originalScale;
	private ToBricksType toBricksType;
	private HalfToneType halfToneType;
	private boolean sizeChoiceFromWidth;
	private int propagationPercentage;
	private volatile boolean uiReady;
	
	public ToBricksTools(final MainController mw, final Model<BrickGraphicsState> model) {
		uiReady = false;
		model.addModelHandler(this);
		mw.getColorController().addChangeListener(this);

		handleModelChange(model);
		listeners = new LinkedList<ChangeListener>();
		toBricksTypeButtons = new JButton[ToBricksType.values().length];

		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI();
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
		toolBar.add(Box.createHorizontalStrut(10));
		toolBar.add(buttonLessPP);
		toolBar.add(propagationPercentageField);
		toolBar.add(new JLabel("%"));
		toolBar.add(buttonMorePP);		
		toolBar.add(Box.createHorizontalStrut(10));
		toolBar.add(sizeFieldWidth);
		toolBar.add(new JLabel(" X "));
		toolBar.add(sizeFieldHeight);
	}
	
	private void setUI() {
		int i = 0;
		for(final ToBricksType type : ToBricksType.values()) {
			JButton toBricksTypeButton = new JButton();
			toBricksTypeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					toBricksType = type;
					update();
				}
			});		
			toBricksTypeButtons[i++] = toBricksTypeButton;
		}		
		
		buttonLessPP = new JButton(Icons.treshold(Icons.SIZE_LARGE));
		buttonLessPP.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propagationPercentage -= 10;
				update();
			}
		});		
		final int PAD = IconizedTextfield.PADDING;
		propagationPercentageField = new LividTextField(propagationPercentage + "", 3);
		propagationPercentageField.setMargin(new Insets(PAD, PAD, PAD, PAD));
		propagationPercentageField.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				// Get percentage:
				try {
					int sum = Integer.parseInt(propagationPercentageField.getText().trim());
					if(propagationPercentage == sum)
						return;
					propagationPercentage = sum;
					update();
				}
				catch(NumberFormatException e2) {
					// nop.
				}
			}
		});
		buttonMorePP = new JButton(Icons.floydSteinberg(Icons.SIZE_LARGE));
		buttonMorePP.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propagationPercentage += 10;
				update();
			}
		});

		sizeFieldWidth = new IconizedTextfield(4, toBricksType.getIcon().get(ToBricksIconType.MeasureWidth, Icons.SIZE_SMALL));
		sizeFieldWidth.setMargin(new Insets(PAD, PAD, PAD, Icons.SIZE_SMALL));
		sizeFieldWidth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = true;
				try {
					int w = toBricksType.getUnitWidth()*Integer.parseInt(sizeFieldWidth.getText().trim());
					if(w == ToBricksTools.this.width)
						return;
					ToBricksTools.this.width = w;
					update();
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
			}
		});
		sizeFieldHeight = new IconizedTextfield(4, toBricksType.getIcon().get(ToBricksIconType.MeasureHeight, Icons.SIZE_SMALL));
		sizeFieldHeight.setMargin(new Insets(PAD, PAD, PAD, Icons.SIZE_SMALL));
		sizeFieldHeight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = false;
				try {
					int h = toBricksType.getUnitHeight()*Integer.parseInt(sizeFieldHeight.getText().trim());
					if(ToBricksTools.this.height == h)
						return;
					ToBricksTools.this.height = h;
					update();
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
			}
		});
				
		// finish:
		uiReady = true;
		update();		
	}
	
	@Override
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
		
		int i = 0;
		for(final ToBricksType type : ToBricksType.values()) {
			toBricksTypeButtons[i].setIcon(toBricksType.ordinal() == i ? type.getIcon().get(ToBricksIconType.Enabled, Icons.SIZE_LARGE) : 
																		 type.getIcon().get(ToBricksIconType.Disabled, Icons.SIZE_LARGE));
			++i;
		}
		sizeFieldWidth.setIcon(toBricksType.getIcon().get(ToBricksIconType.MeasureWidth, Icons.SIZE_SMALL));
		sizeFieldHeight.setIcon(toBricksType.getIcon().get(ToBricksIconType.MeasureHeight, Icons.SIZE_SMALL));
		
		if(propagationPercentage < 0)
			propagationPercentage = 0;
		else if(propagationPercentage > 100)
			propagationPercentage = 100;
		if(!propagationPercentageField.getText().trim().equals(propagationPercentage+""))
			propagationPercentageField.setText(propagationPercentage+"");
		halfToneType = propagationPercentage == 0 ? HalfToneType.Threshold : HalfToneType.FloydSteinberg;

		
		if(sizeChoiceFromWidth) {
			width = toBricksType.closestCompatibleWidth(width, toBricksType.getUnitWidth());
			height = toBricksType.closestCompatibleHeight(Math.round(width/originalScale), toBricksType.getUnitHeight());	
		}
		else {
			height = toBricksType.closestCompatibleHeight(height, toBricksType.getUnitHeight());		
			width = toBricksType.closestCompatibleWidth(Math.round(originalScale*height), toBricksType.getUnitWidth());
		}
		String w = "" + (width/toBricksType.getUnitWidth());
		if(!sizeFieldWidth.getText().trim().equals(w))
			sizeFieldWidth.setText(w);
		String h = "" + (height/toBricksType.getUnitHeight());
		if(!sizeFieldHeight.getText().trim().equals(h))
			sizeFieldHeight.setText(h);
		ChangeEvent e = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(e);
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void save(Model<BrickGraphicsState> model) {
		model.set(BrickGraphicsState.ToBricksWidth, width);
		model.set(BrickGraphicsState.ToBricksHeight, height);
		model.set(BrickGraphicsState.ToBricksTypeIndex, toBricksType.ordinal());
		model.set(BrickGraphicsState.ToBricksPropagationPercentage, propagationPercentage);
	}	
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		this.width = (Integer)model.get(BrickGraphicsState.ToBricksWidth);
		this.height = (Integer)model.get(BrickGraphicsState.ToBricksHeight);
		propagationPercentage = (Integer)model.get(BrickGraphicsState.ToBricksPropagationPercentage);
		originalScale = width/(float)height;
		int tbtl = ToBricksType.values().length;
		toBricksType = ToBricksType.values()[((Integer)model.get(BrickGraphicsState.ToBricksTypeIndex))%tbtl];
		update();			
	}
}
