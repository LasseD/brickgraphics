package mosaic.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
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

/**
 * Construction:
 * 
 * 1: Common settings:
 *  - Button for page layout.
 *  - Common settings
 * 3: Settings for cover page on left, preview on right.
 * 4: Settings for normal pages: COntrols on left, preview on right. 
 * 5: OK/Cancel buttons. OK => Call to controller.
 * 
 * @author LD
 */
public class PrintDialog extends JDialog implements ChangeListener {
	private PrintController pc;
	private ColorController cc;
	private MagnifierController mc;
	// Input boxes:
	private LividTextField tfMagnifiersPerPageWidth, tfMagnifiersPerPageHeight, tfMagnifierSizeWidth, tfMagnifierSizeHeight, tfFontSize;
	private JCheckBox cbCoverPageShow, cbCoverPageShowFileName, cbCoverPageShowLegend, cbShowColors, cbShowLegend, cbShowPageNumber;
	private JRadioButton[] rbCoverPagePictureType, rbShowPosition;
	
	public PrintDialog(MainWindow mw, PrintController pc, ColorController cc, MagnifierController mc, Pipeline pipeline) {
		super(mw, "Print Setup");
		this.pc = pc;
		this.cc = cc;
		this.mc = mc;
		pc.addChangeListener(this);
		mc.addChangeListener(this);
		
		// Set up:
		setModal(true);
		buildUI(pipeline);
    }
	
	@Override
	public void setVisible(boolean v) {
		if(v)
			update();
		super.setVisible(v);
	}
	
	private void buildUI(Pipeline pipeline) {
		setLayout(new BorderLayout());
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
			        PageFormat pf = job.pageDialog(pc.getPageFormat()); // job.pageDialog(attributes);
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
				final JComboBox<ColorController.ShownID> cColorNumber = new JComboBox<ColorController.ShownID>(ColorController.ShownID.values());
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
				final JComboBox<String> cColorName = new JComboBox<String>();
				for(final ColorController.ShownName shownName : ColorController.ShownName.values()) {
					if(shownName.toString() == null)
						continue;
					cColorName.addItem(shownName.toString());
				}
				for(final String localizationName : cc.getLocalizedFileNamesNoTXT()) {
					cColorName.addItem(localizationName);
				}
				// Set selected index:
				int i = 0;
				for(final ColorController.ShownName shownName : ColorController.ShownName.values()) {
					if(shownName.toString() == null)
						continue;				
					if(cc.getShownName() == shownName)
						cColorName.setSelectedIndex(i);
					++i;
				}
				for(final String localizationName : cc.getLocalizedFileNamesNoTXT()) {
					if(cc.getShownName() == ShownName.LOCALIZED && cc.getLocalizedFileNameNoTXT().equals(localizationName))
						cColorName.setSelectedIndex(i);
					++i;
				}
				cColorName.addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						int selectedIndex = cColorName.getSelectedIndex();
						if(selectedIndex < ColorController.ShownName.values().length-1)
							cc.setShownName(ColorController.ShownName.values()[selectedIndex]);
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
		JPanel midPanel = new JPanel(new GridLayout(2,1));
		
		JPanel midTopPanel = new JPanel(new BorderLayout());
		midTopPanel.setBorder(BorderFactory.createTitledBorder("Cover page"));
		PrintPreviewPanel previewCover = new PrintPreviewPanel(true, pc, cc, pipeline);
		pc.addChangeListener(previewCover);
		midTopPanel.add(previewCover, BorderLayout.EAST);
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
		midBottomPanel.add(previewPage, BorderLayout.EAST);			
		JPanel midBottomLeftPanel = new JPanel();	
		midBottomLeftPanel.setLayout(new BoxLayout(midBottomLeftPanel, BoxLayout.Y_AXIS));

		// Magnifier size:
		{
			JPanel pMagnifierSize = new JPanel(new FlowLayout());
			pMagnifierSize.setAlignmentX(Component.LEFT_ALIGNMENT);
			pMagnifierSize.add(new JLabel("Size of the magnifier: "));
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
			JPanel pMagnifiersPerPage = new JPanel(new FlowLayout());
			pMagnifiersPerPage.setAlignmentX(Component.LEFT_ALIGNMENT);
			pMagnifiersPerPage.add(new JLabel("Number of magnifiers per page: "));
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
		midBottomLeftPanel.add(cbShowColors);
		
		cbShowLegend = new JCheckBox("Show parts callout for each page");
		cbShowLegend.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbShowLegend.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowLegend(cbShowLegend.isSelected(), PrintDialog.this);
			}
		});
		midBottomLeftPanel.add(cbShowLegend);
		cbShowPageNumber = new JCheckBox("Show page numbers");
		cbShowPageNumber.setAlignmentX(Component.LEFT_ALIGNMENT);
		cbShowPageNumber.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowPageNumber(cbShowPageNumber.isSelected(), PrintDialog.this);
			}
		});
		midBottomLeftPanel.add(cbShowPageNumber);
		
		JPanel bottomButtonGroupPanel = new JPanel();
		bottomButtonGroupPanel.setLayout(new BoxLayout(bottomButtonGroupPanel, BoxLayout.Y_AXIS));
		bottomButtonGroupPanel.setBorder(BorderFactory.createTitledBorder("Position"));
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
		midBottomLeftPanel.add(bottomButtonGroupPanel);
		midBottomPanel.add(midBottomLeftPanel, BorderLayout.WEST);		
		
		midPanel.add(midTopPanel);
		midPanel.add(midBottomPanel);		
		add(midPanel, BorderLayout.CENTER);
		
		// Bottom: OK/Cancel
		JPanel bottomPanel = new JPanel(new FlowLayout());
		JButton bOK = new JButton("OK");
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
		cbShowColors.setSelected(pc.getShowColors());
		cbShowLegend.setSelected(pc.getShowLegend());
		cbShowPageNumber.setSelected(pc.getShowPageNumber());
		rbShowPosition[pc.getShowPosition().ordinal()].setSelected(true);

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
		//if(e != null && e.getSource() != this)
			update();
		//else
			//updateEnabledFields();
	}
	
	@Override
	public void pack() {
		super.pack();
		int middle = getOwner().getX() + (getOwner().getWidth()-getWidth())/2;
		setLocation(Math.max(0, middle), Math.max(0,  getOwner().getY()));
	}
}
