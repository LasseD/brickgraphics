package mosaic.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.*;
import javax.swing.event.*;

import ui.LividTextField;
import mosaic.controllers.*;
import mosaic.controllers.ColorController.ShownName;
import mosaic.controllers.PrintController.CoverPagePictureType;
import mosaic.controllers.PrintController.ShowPosition;
import mosaic.rendering.Pipeline;
import mosaic.ui.MainWindow;
import mosaic.ui.PrintPreviewPanel;

/**
 * @author LD
 */
public class PrintDialog extends JDialog implements ChangeListener {
	private PrintController pc;
	private ColorController cc;
	private MagnifierController mc;
	// Input boxes:
	private JTextField tfMagnifiersPerPageWidth, tfMagnifiersPerPageHeight, tfMagnifierSizeWidth, 
					   tfMagnifierSizeHeight, tfFontSize, tfMagnifierSizePercentage,
					   tfRightCountDisplayText, tfDownCountDisplayText;
	private JCheckBox cbCoverPageShow, cbCoverPageShowFileName, cbCoverPageShowLegend, 
					  cbShowColors, cbShowLegend, cbShowPageNumber;
	private JRadioButton[] rbCoverPagePictureType, rbShowPosition;
	private JComboBox<ColorController.ShownID> cColorNumber;
	private JComboBox<String> cColorName;
	private String[] lastSeenLocalizedNames;
	private JButton bOK; // With initial focus.
	
	public PrintDialog(MainWindow mw, PrintController pc, ColorController cc, MagnifierController mc, Pipeline pipeline) {
		super(mw, "Print Setup");
		this.pc = pc;
		this.cc = cc;
		this.mc = mc;
		lastSeenLocalizedNames = new String[]{};
		pc.addChangeListener(this);
		mc.addChangeListener(this);
		
		// Set up:
		setModal(true);
		buildUI(pipeline);
    }
	
	@Override
	public void setVisible(boolean v) {
		if(v) {
			update();			
		}
		super.setVisible(v);
	}
	
	private void setSelectedColorName() {
		// Set selected index:
		ColorController.ShownName shownName = cc.getShownName();
		if(shownName != ShownName.LOCALIZED) {
			cColorName.setSelectedIndex(shownName.ordinal());
		}
		else {
			int i = ColorController.ShownName.values().length-1;
			for(final String localizationName : cc.getLocalizedFileNamesNoTXT()) {
				if(cc.getLocalizedFileNameNoTXT().equals(localizationName)) {
					cColorName.setSelectedIndex(i);
					break;
				}
				++i;
			}			
		}
	}
	
	private void loadColorNames(boolean force) {
		// Only change if files have changed!
		boolean same = true;
		String[] newLocalizedNames = cc.getLocalizedFileNamesNoTXT();
		if(lastSeenLocalizedNames.length != newLocalizedNames.length)
			same = false;
		else {
			for(int i = 0; i < lastSeenLocalizedNames.length; ++i) {
				if(!lastSeenLocalizedNames[i].equals(newLocalizedNames[i])) {
					same = false;
					break;
				}
			}
		}
		if(same && !force)
			return;
		lastSeenLocalizedNames = newLocalizedNames;
		
		cColorName.removeAllItems();
		for(final ColorController.ShownName shownName : ColorController.ShownName.values()) {
			if(shownName.toString() == null)
				continue;
			cColorName.addItem(shownName.toString());
		}
		for(final String localizationName : newLocalizedNames) {
			cColorName.addItem(localizationName);
		}
	}
	
	private void buildUI(Pipeline pipeline) {
		setLayout(new BorderLayout());
		JTabbedPane tp = new JTabbedPane();
		
		{
			// 1: Common settings
			JPanel topPanel = new JPanel();
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
			topPanel.setBorder(BorderFactory.createTitledBorder("Common settings"));
			
			// Page layout button
			JPanel pPageLayout = new JPanel(new FlowLayout(FlowLayout.LEFT));			
			JButton bPageFormat = new JButton("Page layout");
			bPageFormat.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) {
					PrinterJob job = pc.getPrinterJob();
			        PageFormat pf = job.pageDialog(pc.getPageFormat());
			        pc.setPageFormat(pf, PrintDialog.this);
				}
			});
			pPageLayout.add(bPageFormat);
			topPanel.add(pPageLayout);
			
			// Text size:
			tfFontSize = new LividTextField(3);
			tfFontSize.addActionListener(new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						float s = Float.parseFloat(tfFontSize.getText());
						if(s < 0.2)
							return;
						pc.setFontSize(s, PrintDialog.this);
						update();
					}
					catch(NumberFormatException ignore) {}
				}
			});
			JPanel pFontSize = new JPanel(new FlowLayout(FlowLayout.LEFT));
			pFontSize.add(new JLabel("Text size"));
			pFontSize.add(tfFontSize);
			topPanel.add(pFontSize);
			
			{
				// Color number
				JPanel pColorNumber = new JPanel(new FlowLayout(FlowLayout.LEFT));
				pColorNumber.add(new JLabel("Color number"));
				cColorNumber = new JComboBox<ColorController.ShownID>(ColorController.ShownID.values());
				cColorNumber.setSelectedItem(cc.getShownID());
				cColorNumber.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cc.setShownID((ColorController.ShownID)cColorNumber.getSelectedItem());
					}
				});			
				pColorNumber.add(cColorNumber);
				topPanel.add(pColorNumber);				
			}

			{
				// Color name 
				JPanel pColorName = new JPanel(new FlowLayout(FlowLayout.LEFT));
				pColorName.add(new JLabel("Color name"));
				cColorName = new JComboBox<String>();
				// Separate reloader function (so the list is updated when locales change)
				loadColorNames(true);
				setSelectedColorName();
				cColorName.addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						int selectedIndex = cColorName.getSelectedIndex();
						if(selectedIndex < 0)
							return; // Rebuilding.
						if(selectedIndex < ColorController.ShownName.values().length-1) {
							cc.setShownName(ColorController.ShownName.values()[selectedIndex]);							
						}
						else {
							cc.setShownName(ShownName.LOCALIZED);
							cc.setLocalizedFileNameNoTXT((String)cColorName.getSelectedItem(), PrintDialog.this);						
						}
					}
				});
				pColorName.add(cColorName);
				topPanel.add(pColorName);				
			}
			
			add(topPanel, BorderLayout.NORTH);			
		}
		
		// Middle: Main components + preview.
		
		JPanel midTopPanel = new JPanel(new BorderLayout());
		midTopPanel.setBorder(BorderFactory.createTitledBorder("Cover page"));
		PrintPreviewPanel previewCover = new PrintPreviewPanel(true, pc, cc, pipeline);
		pc.addChangeListener(previewCover);
		midTopPanel.add(previewCover, BorderLayout.CENTER);
		JPanel midTopLeftPanel = new JPanel();
		midTopLeftPanel.setLayout(new BoxLayout(midTopLeftPanel, BoxLayout.Y_AXIS));
		// Settings for cover page:
		cbCoverPageShow = new JCheckBox("Include cover page");
		cbCoverPageShow.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbCoverPageShow.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShow(cbCoverPageShow.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShow);
		cbCoverPageShowFileName = new JCheckBox("Show file name");
		cbCoverPageShowFileName.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbCoverPageShowFileName.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShowFileName(cbCoverPageShowFileName.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShowFileName);
		cbCoverPageShowLegend = new JCheckBox("Show complete parts callout");
		cbCoverPageShowLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbCoverPageShowLegend.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShowLegend(cbCoverPageShowLegend.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShowLegend);

		JPanel topButtonGroupPanel = new JPanel();
		topButtonGroupPanel.setLayout(new BoxLayout(topButtonGroupPanel, BoxLayout.Y_AXIS));
		topButtonGroupPanel.setBorder(BorderFactory.createTitledBorder("Picture"));
		ButtonGroup bgCoverPagePictureType = new ButtonGroup();
		rbCoverPagePictureType = new JRadioButton[CoverPagePictureType.values().length];
		int i = 0;
		for(final CoverPagePictureType t : CoverPagePictureType.values()) {
			rbCoverPagePictureType[i] = new JRadioButton(t.name());
			rbCoverPagePictureType[i].addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					pc.setCoverPagePictureType(t, PrintDialog.this);
				}
			});
			bgCoverPagePictureType.add(rbCoverPagePictureType[i]);
			topButtonGroupPanel.add(rbCoverPagePictureType[i]);
			++i;
		}
		midTopLeftPanel.add(topButtonGroupPanel);
		midTopPanel.add(midTopLeftPanel, BorderLayout.WEST);
		
		JPanel midBottomPanel = new JPanel(new BorderLayout());
		midBottomPanel.setBorder(BorderFactory.createTitledBorder("Page setup"));
		PrintPreviewPanel previewPage = new PrintPreviewPanel(false, pc, cc, pipeline);
		pc.addChangeListener(previewPage);
		midBottomPanel.add(previewPage, BorderLayout.CENTER);			
		JPanel midBottomLeftPanel = new JPanel();	
		midBottomLeftPanel.setLayout(new BoxLayout(midBottomLeftPanel, BoxLayout.Y_AXIS));

		// Magnifier size:
		{
			JPanel pMagnifierSize = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//pMagnifierSize.setAlignmentX(Component.LEFT_ALIGNMENT);
			pMagnifierSize.add(new JLabel("Block size: "));
			tfMagnifierSizeWidth = new LividTextField(3);		
			pMagnifierSize.add(tfMagnifierSizeWidth);
			pMagnifierSize.add(new JLabel("X"));
			tfMagnifierSizeHeight = new LividTextField(3);		
			pMagnifierSize.add(tfMagnifierSizeHeight);	
			tfMagnifierSizeWidth.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						int w = Integer.parseInt(tfMagnifierSizeWidth.getText());
						if(w <= 0)
							return;
						mc.setWidthInMosaicBlocks(w);
					}
					catch(NumberFormatException ignore) {
					}
				}
			});
			tfMagnifierSizeHeight.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						int h = Integer.parseInt(tfMagnifierSizeHeight.getText());
						if(h <= 0)
							return;
						mc.setHeightInMosaicBlocks(h);
					}
					catch(NumberFormatException ignore) {
					}
				}
			});
			midBottomLeftPanel.add(pMagnifierSize);
		}
		
		// Magnifiers per page:
		{
			JPanel pMagnifiersPerPage = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//pMagnifiersPerPage.setAlignmentX(Component.LEFT_ALIGNMENT);
			pMagnifiersPerPage.add(new JLabel("Blocks per page: "));
			tfMagnifiersPerPageWidth = new LividTextField(3);		
			pMagnifiersPerPage.add(tfMagnifiersPerPageWidth);
			pMagnifiersPerPage.add(new JLabel("X"));
			tfMagnifiersPerPageHeight = new LividTextField(3);		
			pMagnifiersPerPage.add(tfMagnifiersPerPageHeight);	
			ActionListener aMagnifiersPerPage = new ActionListener() {			
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						int w = Integer.parseInt(tfMagnifiersPerPageWidth.getText());
						int h = Integer.parseInt(tfMagnifiersPerPageHeight.getText());
						if(w <= 0 || h <= 0)
							return;
						pc.setMagnifiersPerPage(new Dimension(w, h), PrintDialog.this);
						update();
					}
					catch(NumberFormatException ignore) {
					}
				}
			};
			tfMagnifiersPerPageWidth.addActionListener(aMagnifiersPerPage);
			tfMagnifiersPerPageHeight.addActionListener(aMagnifiersPerPage);
			midBottomLeftPanel.add(pMagnifiersPerPage);			
		}
		
		cbShowColors = new JCheckBox("Show colors");
		cbShowColors.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbShowColors.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowColors(cbShowColors.isSelected(), PrintDialog.this);
			}
		});
		JPanel pShowColors = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pShowColors.add(cbShowColors);
		midBottomLeftPanel.add(pShowColors);
		
		// Show legend:
		cbShowLegend = new JCheckBox("Show parts callout for each page");
		cbShowLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbShowLegend.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowLegend(cbShowLegend.isSelected(), PrintDialog.this);
			}
		});
		JPanel pShowLegend = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pShowLegend.add(cbShowLegend);
		midBottomLeftPanel.add(pShowLegend);
		// Magnifier size (percentage):
		{
			JPanel pMagnifierSizePercentage = new JPanel(new FlowLayout(FlowLayout.LEFT));
			//pMagnifierSize.setAlignmentX(Component.LEFT_ALIGNMENT);
			tfMagnifierSizePercentage = new LividTextField(3);		
			pMagnifierSizePercentage.add(new JLabel("Size of the building block:"));
			pMagnifierSizePercentage.add(tfMagnifierSizePercentage);
			pMagnifierSizePercentage.add(new JLabel("%"));
			tfMagnifierSizePercentage.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						int p = Integer.parseInt(tfMagnifierSizePercentage.getText());
						if(p < 1 || p > 100)
							return;
						pc.setMagnifierSizePercentage(p, PrintDialog.this);
					}
					catch(NumberFormatException ignore) {
					}
				}
			});
			midBottomLeftPanel.add(pMagnifierSizePercentage);
		}		
		// Show page number:
		cbShowPageNumber = new JCheckBox("Show page numbers");
		cbShowPageNumber.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbShowPageNumber.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowPageNumber(cbShowPageNumber.isSelected(), PrintDialog.this);
			}
		});
		JPanel pShowPageNumber = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pShowPageNumber.add(cbShowPageNumber);
		midBottomLeftPanel.add(pShowPageNumber);
		
		// Position display buttons:
		JPanel bottomButtonGroupPanel = new JPanel();
		bottomButtonGroupPanel.setLayout(new BoxLayout(bottomButtonGroupPanel, BoxLayout.Y_AXIS));
		ButtonGroup bgShowPosition = new ButtonGroup();
		rbShowPosition = new JRadioButton[ShowPosition.values().length];
		i = 0;
		for(final ShowPosition p : ShowPosition.values()) {
			rbShowPosition[i] = new JRadioButton(p.title);
			rbShowPosition[i].addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					pc.setShowPosition(p, PrintDialog.this);
				}
			});
			bgShowPosition.add(rbShowPosition[i]);
			bottomButtonGroupPanel.add(rbShowPosition[i]);
			++i;
		}
		
		// Position text:
		tfRightCountDisplayText = new LividTextField(10);		
		tfRightCountDisplayText.addActionListener(new ActionListener() {				
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pc.setRightCountDisplayText(tfRightCountDisplayText.getText(), PrintDialog.this);
			}
		});
		tfDownCountDisplayText = new LividTextField(10);		
		tfDownCountDisplayText.addActionListener(new ActionListener() {				
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pc.setDownCountDisplayText(tfDownCountDisplayText.getText(), PrintDialog.this);
			}
		});
		JPanel pTextPositionText = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pTextPositionText.add(new JLabel("X"));
		pTextPositionText.add(tfRightCountDisplayText);
		pTextPositionText.add(new JLabel("Y"));
		pTextPositionText.add(tfDownCountDisplayText);
		
		// Position panel:
		JPanel pBottomButtonGroupPanel = new JPanel(new BorderLayout());
		pBottomButtonGroupPanel.setBorder(BorderFactory.createTitledBorder("Position"));
		pBottomButtonGroupPanel.add(bottomButtonGroupPanel, BorderLayout.CENTER);
		pBottomButtonGroupPanel.add(pTextPositionText, BorderLayout.SOUTH);
		midBottomLeftPanel.add(pBottomButtonGroupPanel);
		midBottomPanel.add(midBottomLeftPanel, BorderLayout.WEST);		
		
		tp.addTab("Cover Page", midTopPanel);
		tp.addTab("Page Setup", midBottomPanel);
		add(tp, BorderLayout.CENTER);
		
		// Bottom: OK/Cancel
		JPanel bottomPanel = new JPanel(new FlowLayout());
		bOK = new JButton("OK");
		bOK.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				pc.print();
			}
		});
		bottomPanel.add(bOK);
		JButton bCancel = new JButton("Cancel");
		bCancel.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		bottomPanel.add(bCancel);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private void update() {
		String fs = "" + pc.getFontSize();
		if(!tfFontSize.getText().trim().equals(fs))
			tfFontSize.setText(fs);
		{
			String w = ""+pc.getMagnifiersPerPage().width;
			if(!tfMagnifiersPerPageWidth.getText().trim().equals(w))
				tfMagnifiersPerPageWidth.setText(w);
			String h = ""+pc.getMagnifiersPerPage().height;
			if(!tfMagnifiersPerPageHeight.getText().trim().equals(h))
				tfMagnifiersPerPageHeight.setText(h);			
		}
		{
			String w = ""+mc.getSizeInMosaicBlocks().width;
			if(!tfMagnifierSizeWidth.getText().trim().equals(w))
				tfMagnifierSizeWidth.setText(w);
			String h = ""+mc.getSizeInMosaicBlocks().height;
			if(!tfMagnifierSizeHeight.getText().trim().equals(h))
				tfMagnifierSizeHeight.setText(h);			
		}
		{
			String p = ""+pc.getMagnifierSizePercentage();
			if(!tfMagnifierSizePercentage.getText().trim().equals(p))
				tfMagnifierSizePercentage.setText(p);
		}		
		cbShowColors.setSelected(pc.getShowColors());
		cbShowLegend.setSelected(pc.getShowLegend());
		cbShowPageNumber.setSelected(pc.getShowPageNumber());
		rbShowPosition[pc.getShowPosition().ordinal()].setSelected(true);
		tfRightCountDisplayText.setText(pc.getRightCountDisplayText());
		tfDownCountDisplayText.setText(pc.getDownCountDisplayText());

		if(cColorNumber.getSelectedIndex() != cc.getShownID().ordinal())
			cColorNumber.setSelectedItem(cc.getShownID());
		loadColorNames(false);
		setSelectedColorName();
		
		boolean cps = pc.getCoverPageShow();
		cbCoverPageShow.setSelected(cps);

		cbCoverPageShowFileName.setSelected(pc.getCoverPageShowFileName());
		cbCoverPageShowLegend.setSelected(pc.getCoverPageShowLegend());
		rbCoverPagePictureType[pc.getCoverPagePictureType().ordinal()].setSelected(true);				
		updateEnabledFields();
	}
	
	private void updateEnabledFields() {
		boolean cps = pc.getCoverPageShow();
		cbCoverPageShowFileName.setEnabled(cps);
		cbCoverPageShowLegend.setEnabled(cps);
		for(int i = 0; i < CoverPagePictureType.values().length; ++i) {
			rbCoverPagePictureType[i].setEnabled(cps);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(!isVisible())
			return;
		update();
	}
	
	@Override
	public void pack() {
		super.pack();
		int middle = getOwner().getX() + (getOwner().getWidth()-getWidth())/2;
		setLocation(Math.max(0, middle), Math.max(0,  getOwner().getY()));
		bOK.requestFocusInWindow();
	}
}
