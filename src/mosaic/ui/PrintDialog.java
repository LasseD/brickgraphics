package mosaic.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.*;
import mosaic.controllers.PrintController.CoverPagePictureType;
import mosaic.controllers.PrintController.ShowPosition;

/**
 * Construction:
 * 
 * Top: Button for page setup.
 * Upper middle: Settings for cover page on left, preview on right.
 * Lower Middle: Settings for normal pages: COntrols on left, preview on right. 
 * Bottom: OK/Cancel buttons. OK => Call to controller.
 * 
 * @author ld
 */
public class PrintDialog extends JDialog implements ChangeListener {
	private static final long serialVersionUID = -1834122366997009709L;
	private PrintController pc;
	// Input boxes:
	private JTextField tfMagnifiersPerPageWidth, tfMagnifiersPerPageHeight, tfFontSize;
	private JCheckBox cbCoverPageShow, cbCoverPageShowFileName, cbCoverPageShowLegend, cbShowLegend, cbShowPageNumber;
	private JRadioButton[] rbCoverPagePictureType, rbShowPosition;
	
	public PrintDialog(MainWindow mw, PrintController pc) {
		super(mw, "Print Setup");
		this.pc = pc;
		pc.addListener(this);
		
		// Set up:
		setModal(true);
		buildUI();
		
		update();
    }
	
	private void buildUI() {
		setLayout(new BorderLayout());
		// Top: Page setup
		JPanel topPanel = new JPanel(new FlowLayout());
		JButton bPageFormat = new JButton("Page layout");
		bPageFormat.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				PrinterJob job = pc.getPrinterJob();
		        PageFormat pf = job.pageDialog(pc.getPageFormat()); // job.pageDialog(attributes);
		        pc.setPageFormat(pf, PrintDialog.this);
			}
		});
		topPanel.add(bPageFormat);
		add(topPanel, BorderLayout.NORTH);
		
		// Middle: Main components + preview.
		JPanel midPanel = new JPanel(new GridLayout(2,1));
		
		JPanel midTopPanel = new JPanel(new BorderLayout());
		midTopPanel.setBorder(BorderFactory.createTitledBorder("Cover page"));
		PrintPreviewPanel previewCover = new PrintPreviewPanel(true, pc);
		midTopPanel.add(previewCover, BorderLayout.EAST);
		JPanel midTopLeftPanel = new JPanel(new GridLayout(7, 1));
		// Settings for cover page:
		cbCoverPageShow = new JCheckBox("Include cover page");
		cbCoverPageShow.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShow(cbCoverPageShow.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShow);
		cbCoverPageShowFileName = new JCheckBox("Show file name");
		cbCoverPageShowFileName.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShowFileName(cbCoverPageShowFileName.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShowFileName);
		cbCoverPageShowLegend = new JCheckBox("Show complete parts callout");
		cbCoverPageShowLegend.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setCoverPageShowLegend(cbCoverPageShowLegend.isSelected(), PrintDialog.this);
			}
		});
		midTopLeftPanel.add(cbCoverPageShowLegend);

		ButtonGroup bgCoverPagePictureType = new ButtonGroup();
		rbCoverPagePictureType = new JRadioButton[CoverPagePictureType.values().length];
		int i = 0;
		for(final CoverPagePictureType t : CoverPagePictureType.values()) {
			rbCoverPagePictureType[i] = new JRadioButton("Cover picture: " + t.name());
			rbCoverPagePictureType[i].addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					pc.setCoverPagePictureType(t, PrintDialog.this);
				}
			});
			bgCoverPagePictureType.add(rbCoverPagePictureType[i]);
			midTopLeftPanel.add(rbCoverPagePictureType[i]);
			++i;
		}
		midTopPanel.add(midTopLeftPanel, BorderLayout.WEST);
		
		JPanel midBottomPanel = new JPanel(new BorderLayout());
		midBottomPanel.setBorder(BorderFactory.createTitledBorder("Page setup"));
		PrintPreviewPanel previewPage = new PrintPreviewPanel(false, pc);
		midBottomPanel.add(previewPage, BorderLayout.EAST);			
		JPanel midBottomLeftPanel = new JPanel(new GridLayout(8, 1));	

		JPanel pMagnifiersPerPage = new JPanel(new FlowLayout());
		pMagnifiersPerPage.add(new JLabel("Number of magnifiers per page: "));
		tfMagnifiersPerPageWidth = new JTextField(3);		
		pMagnifiersPerPage.add(tfMagnifiersPerPageWidth);
		pMagnifiersPerPage.add(new JLabel("X"));
		tfMagnifiersPerPageHeight = new JTextField(3);		
		pMagnifiersPerPage.add(tfMagnifiersPerPageHeight);	
		ActionListener aMagnifiersPerPage = new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					int w = Integer.parseInt(tfMagnifiersPerPageWidth.getText());
					int h = Integer.parseInt(tfMagnifiersPerPageHeight.getText());
					if(w > 0 && h > 0)
						pc.setMagnifiersPerPage(new Dimension(w, h), PrintDialog.this);
				}
				catch(NumberFormatException ignore) {
				}
				update();
			}
		};
		tfMagnifiersPerPageWidth.addActionListener(aMagnifiersPerPage);
		tfMagnifiersPerPageHeight.addActionListener(aMagnifiersPerPage);
		midBottomLeftPanel.add(pMagnifiersPerPage);
		
		cbShowLegend = new JCheckBox("Show parts callout for each page");
		cbShowLegend.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowLegend(cbShowLegend.isSelected(), PrintDialog.this);
			}
		});
		midBottomLeftPanel.add(cbShowLegend);
		cbShowPageNumber = new JCheckBox("Show page numbers");
		cbShowPageNumber.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				pc.setShowPageNumber(cbShowPageNumber.isSelected(), PrintDialog.this);
			}
		});
		midBottomLeftPanel.add(cbShowPageNumber);
		tfFontSize = new JTextField(3);
		tfFontSize.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					float s = Float.parseFloat(tfFontSize.getText());
					if(s >= 1)
						pc.setFontSize(s, PrintDialog.this);
				}
				catch(NumberFormatException ignore) {
				}
				update();
			}
		});
		JPanel pFontSize = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pFontSize.add(new JLabel("Text size"));
		pFontSize.add(tfFontSize);
		midBottomLeftPanel.add(pFontSize);
		
		ButtonGroup bgShowPosition = new ButtonGroup();
		rbShowPosition = new JRadioButton[ShowPosition.values().length];
		i = 0;
		for(final ShowPosition p : ShowPosition.values()) {
			rbShowPosition[i] = new JRadioButton("Position: " + p.title);
			rbShowPosition[i].addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					pc.setShowPosition(p, PrintDialog.this);
				}
			});
			bgShowPosition.add(rbShowPosition[i]);
			midBottomLeftPanel.add(rbShowPosition[i]);
			++i;
		}
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
		tfFontSize.setText("" + pc.getFontSize());
		tfMagnifiersPerPageWidth.setText(""+pc.getMagnifiersPerPage().width);
		tfMagnifiersPerPageHeight.setText(""+pc.getMagnifiersPerPage().height);
		cbShowLegend.setSelected(pc.getShowLegend());
		cbShowPageNumber.setSelected(pc.getShowPageNumber());
		int i = 0;
		for(ShowPosition p : ShowPosition.values()) {
			if(p == pc.getShowPosition())
				rbShowPosition[i].setSelected(true);
			++i;
		}

		boolean cps = pc.getCoverPageShow();
		cbCoverPageShow.setSelected(cps);

		cbCoverPageShowFileName.setSelected(pc.getCoverPageShowFileName());
		cbCoverPageShowLegend.setSelected(pc.getCoverPageShowLegend());
		i = 0;
		for(CoverPagePictureType t : CoverPagePictureType.values()) {
			if(t == pc.getCoverPagePictureType()) {
				rbCoverPagePictureType[i].setSelected(true);				
			}
			++i;
		}
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
		if(e != null && e.getSource() != this)
			update();
		else 
			updateEnabledFields();
	}
	
	@Override
	public void pack() {
		super.pack();
		int middle = getOwner().getX() + (getOwner().getWidth()-getWidth())/2;
		setLocation(Math.max(0, middle), Math.max(0,  getOwner().getY()));
	}
}
