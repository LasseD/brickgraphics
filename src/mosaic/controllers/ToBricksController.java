package mosaic.controllers;

import icon.*;
import icon.ToBricksIcon.ToBricksIconType;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;
import io.*;
import javax.swing.event.*;
import mosaic.io.*;
import mosaic.ui.MainWindow;
import mosaic.ui.actions.ToggleDivider;
import ui.IconizedTextfield;
import ui.LividTextField;
import java.util.*;
import bricks.*;

public class ToBricksController implements ChangeListener, ModelHandler<BrickGraphicsState> {
	private JButton[] toBricksTypeButtons;
	private LividTextField propagationPercentageField;
	private IconizedTextfield sizeFieldWidth, sizeFieldHeight; 
	private JButton buttonLessPP, buttonMorePP, buttonToggleLockSizeRatio, buttonToggleDividerLocation;
	private List<ChangeListener> listeners;
	private UIController uiController;
	private JLabel labelPercent, labelX;
	
	private int constructionWidthInBasicUnits, constructionHeightInBasicUnits; // in basicUnits
	private float originalWidthToHeight;
	private ToBricksType toBricksType;
	private boolean[] availableToBricksTypes;
	private boolean sizeChoiceFromWidth, sizeRatioLocked, showDividerLocationButton;
	private int propagationPercentage;
	private volatile boolean uiReady;
	
	public ToBricksController(final MainController controller, final Model<BrickGraphicsState> model) {
		uiReady = false;
		model.addModelHandler(this);
		controller.getColorController().addChangeListener(this);
		controller.getUIController().addChangeListener(this);
		uiController = controller.getUIController();

		listeners = new LinkedList<ChangeListener>();
		toBricksTypeButtons = new JButton[ToBricksType.values().length];
		availableToBricksTypes = new boolean[ToBricksType.values().length];
		handleModelChange(model);
	}
	
	public void initiateUI(final MainWindow mw) {
		SwingUtilities.invokeLater(new Runnable() {			
			@Override
			public void run() {
				setUI(mw);
			}
		});		
	}
	
	public void setOriginalWidthToHeight(float originalWidthToHeight) {
		this.originalWidthToHeight = originalWidthToHeight;
		update();		
	}
	
	public void addComponents(JToolBar toolBar, MainController mc) {
		if(!uiReady)
			throw new IllegalStateException();
		
		toolBar.add(buttonToggleDividerLocation);	
				
		for(JButton b : toBricksTypeButtons)
			toolBar.add(b);
		//toolBar.add(new ShowToBricksTypeFilterDialog(mc));
		toolBar.add(buttonLessPP);
		toolBar.add(propagationPercentageField);
		toolBar.add(labelPercent);
		toolBar.add(buttonMorePP);		

		toolBar.add(sizeFieldWidth);
		toolBar.add(labelX);
		toolBar.add(sizeFieldHeight);
		toolBar.add(buttonToggleLockSizeRatio);
	}
	
	private void setUI(MainWindow mw) {
		if(mw == null)
			throw new IllegalArgumentException("MainWindow is null!");
		buttonToggleDividerLocation = new JButton(new ToggleDivider(mw));
		
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
			toBricksTypeButton.setToolTipText(type.getDescription());
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
		buttonLessPP.setToolTipText("Decrease dithering by 10%");
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
		propagationPercentageField.setToolTipText("Amount of dithering/error correction used.");

		buttonMorePP = new JButton(Icons.floydSteinberg(Icons.SIZE_LARGE));
		buttonMorePP.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				propagationPercentage += 10;
				update();
			}
		});
		buttonMorePP.setToolTipText("Increase dithering by 10%");

		sizeFieldWidth = new IconizedTextfield(4, toBricksType.getMeasureIcon().get(ToBricksIconType.MeasureWidth, Icons.SIZE_SMALL));
		sizeFieldWidth.setMargin(new Insets(PAD, PAD, PAD, Icons.SIZE_SMALL));
		sizeFieldWidth.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = true;
				try {
					int w = toBricksType.getUnitWidth()*Integer.parseInt(sizeFieldWidth.getText().trim());
					if(w == constructionWidthInBasicUnits)
						return;
					constructionWidthInBasicUnits = w;
					update();
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
			}
		});
		sizeFieldWidth.setToolTipText("Set the width.");
		sizeFieldHeight = new IconizedTextfield(4, toBricksType.getMeasureIcon().get(ToBricksIconType.MeasureHeight, Icons.SIZE_SMALL));
		sizeFieldHeight.setMargin(new Insets(PAD, PAD, PAD, Icons.SIZE_SMALL));
		sizeFieldHeight.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sizeChoiceFromWidth = false;
				try {
					int h = toBricksType.getUnitHeight()*Integer.parseInt(sizeFieldHeight.getText().trim());
					if(constructionHeightInBasicUnits == h)
						return;
					constructionHeightInBasicUnits = h;
					update();
				}
				catch(NumberFormatException ex) {
					// don't change width.
				}
			}
		});
		sizeFieldHeight.setToolTipText("Set the height.");
		
		buttonToggleLockSizeRatio = new JButton();
		buttonToggleLockSizeRatio.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sizeRatioLocked = !sizeRatioLocked;
				update();
			}
		});
		buttonToggleLockSizeRatio.setToolTipText("Lock or unlock the width/height ratio.");
				
		labelPercent = new JLabel("%");
		labelX = new JLabel(" X ");
		
		// finish:
		uiReady = true;
		
		mw.finishUpRibbonMenuAndIcon();
		update();		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		update();
	}

	public int getConstructionHeightInBasicUnits() {
		return constructionHeightInBasicUnits;
	}
	public int getConstructionWidthInBasicUnits() {
		return constructionWidthInBasicUnits;
	}	
	public ToBricksType getToBricksType() {
		return toBricksType;
	}
	public Dimension getMinimalInputImageSize() {
		int w = constructionWidthInBasicUnits/toBricksType.getUnitWidth();
		int h = constructionHeightInBasicUnits/toBricksType.getUnitHeight();
		if(toBricksType == ToBricksType.SNOT_IN_2_BY_2) {
			w *= 10;
			h *= 10;
		}
		
		return new Dimension(w, h);
	}
	
	public int getPropagationPercentage() {
		return propagationPercentage;
	}
	
	public boolean[] getAvailableToBricksTypes() {
		return availableToBricksTypes;
	}
	
	public boolean getSizeRatioLocked() {
		return sizeRatioLocked;
	}
	
	public void setAvailableToBricksTypes(boolean[] availableToBricksTypes) {
		if(availableToBricksTypes == null)
			throw new NullPointerException("UIController::setAvailableToBricksTypes called with null argument.");
		if(availableToBricksTypes.length != ToBricksType.values().length)
			availableToBricksTypes = ToBricksType.getDefaultTypes();

		this.availableToBricksTypes = availableToBricksTypes;
		update();
	}
	
	public void toggleShowDividerLocationButton() {
		showDividerLocationButton = !showDividerLocationButton;
		update();
	}

	private void update() {		
		if(!uiReady)
			return;
		
		{
			// Handle ToBricksTypesButtons:
			ToBricksType[] tbtValues = ToBricksType.values();
			for(int i = 0; i < availableToBricksTypes.length; ++i) {
				ToBricksType type = tbtValues[i];
				toBricksTypeButtons[i].setVisible(!uiController.showMagnifier() && availableToBricksTypes[i]);
				toBricksTypeButtons[i].setIcon(toBricksType == type ? 
						type.getIcon().get(ToBricksIconType.Enabled, Icons.SIZE_LARGE) : 
						type.getIcon().get(ToBricksIconType.Disabled, Icons.SIZE_LARGE));					
			}
		}
		sizeFieldWidth.setIcon(toBricksType.getMeasureIcon().get(ToBricksIconType.MeasureWidth, Icons.SIZE_SMALL));
		sizeFieldWidth.setVisible(!uiController.showMagnifier());
		sizeFieldHeight.setIcon(toBricksType.getMeasureIcon().get(ToBricksIconType.MeasureHeight, Icons.SIZE_SMALL));
		sizeFieldHeight.setVisible(!uiController.showMagnifier());
		buttonToggleLockSizeRatio.setIcon(sizeRatioLocked ? 
				Icons.dimensionLockClosed(Icons.SIZE_LARGE) : 
				Icons.dimensionLockOpen(Icons.SIZE_LARGE));
		buttonToggleLockSizeRatio.setVisible(!uiController.showMagnifier());
		buttonToggleDividerLocation.setVisible(showDividerLocationButton);
		
		if(propagationPercentage < 0)
			propagationPercentage = 0;
		else if(propagationPercentage > 100)
			propagationPercentage = 100;
		if(!propagationPercentageField.getText().trim().equals(propagationPercentage+""))
			propagationPercentageField.setText(propagationPercentage+"");
		propagationPercentageField.setVisible(!uiController.showMagnifier());
		
		labelX.setVisible(!uiController.showMagnifier());
		labelPercent.setVisible(!uiController.showMagnifier());
		buttonMorePP.setVisible(!uiController.showMagnifier());
		buttonLessPP.setVisible(!uiController.showMagnifier());
		
		if(sizeChoiceFromWidth) {
			constructionWidthInBasicUnits = toBricksType.closestCompatibleWidth(
					constructionWidthInBasicUnits, toBricksType.getUnitWidth());
			if(sizeRatioLocked)
				constructionHeightInBasicUnits = toBricksType.closestCompatibleHeight(
						Math.round(constructionWidthInBasicUnits/originalWidthToHeight), toBricksType.getUnitHeight());
			else
				constructionHeightInBasicUnits = toBricksType.closestCompatibleHeight(
						constructionHeightInBasicUnits, toBricksType.getUnitHeight());
		}
		else {
			constructionHeightInBasicUnits = toBricksType.closestCompatibleHeight(
					constructionHeightInBasicUnits, toBricksType.getUnitHeight());		
			if(sizeRatioLocked)
				constructionWidthInBasicUnits = toBricksType.closestCompatibleWidth(
						Math.round(originalWidthToHeight*constructionHeightInBasicUnits), toBricksType.getUnitWidth());
			else
				constructionWidthInBasicUnits = toBricksType.closestCompatibleWidth(
						constructionWidthInBasicUnits, toBricksType.getUnitWidth());
		}
		String w = "" + (constructionWidthInBasicUnits/toBricksType.getUnitWidth());
		if(!sizeFieldWidth.getText().trim().equals(w))
			sizeFieldWidth.setText(w);
		String h = "" + (constructionHeightInBasicUnits/toBricksType.getUnitHeight());
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
		model.set(BrickGraphicsState.ToBricksWidth, constructionWidthInBasicUnits);
		model.set(BrickGraphicsState.ToBricksHeight, constructionHeightInBasicUnits);
		model.set(BrickGraphicsState.ToBricksTypeIndex, toBricksType.ordinal());
		model.set(BrickGraphicsState.ToBricksPropagationPercentage, propagationPercentage);
		model.set(BrickGraphicsState.ToBricksFiltered, availableToBricksTypes);
		model.set(BrickGraphicsState.ToBricksSizeRatioLocked, sizeRatioLocked);
		model.set(BrickGraphicsState.DividerLocationButtonShow, showDividerLocationButton);
	}
	@Override
	public void handleModelChange(Model<BrickGraphicsState> model) {
		this.constructionWidthInBasicUnits = (Integer)model.get(BrickGraphicsState.ToBricksWidth);
		this.constructionHeightInBasicUnits = (Integer)model.get(BrickGraphicsState.ToBricksHeight);
		propagationPercentage = (Integer)model.get(BrickGraphicsState.ToBricksPropagationPercentage);
		originalWidthToHeight = constructionWidthInBasicUnits/(float)constructionHeightInBasicUnits;
		int tbtl = ToBricksType.values().length;
		toBricksType = ToBricksType.values()[((Integer)model.get(BrickGraphicsState.ToBricksTypeIndex))%tbtl];
		availableToBricksTypes = (boolean[])model.get(BrickGraphicsState.ToBricksFiltered);
		sizeRatioLocked = (Boolean)model.get(BrickGraphicsState.ToBricksSizeRatioLocked);
		showDividerLocationButton = (Boolean)model.get(BrickGraphicsState.DividerLocationButtonShow);
		update();
	}
}
