package mosaic.ui.dialogs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.OptionsController;
import transforms.ScaleTransform.ScaleQuality;

public class OptionsDialog extends JDialog implements ChangeListener {
	private OptionsController oc;
	private JCheckBox cbAllowFilterReordering, cbScale, cbOptimize;
	private JRadioButton[] rbScaleQuality;
	private static final String DIALOG_TITLE = "Settings";

	public OptionsDialog(JFrame parent, OptionsController oc) {
		super(parent, DIALOG_TITLE, true);
		this.oc = oc;
		oc.addChangeListener(this);
		
		setupUI();
	}
	
	private void setupUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Performance", createPerformancePanel());
		tabs.addTab("Export", createExportPanel());

		cp.add(tabs, BorderLayout.CENTER);

		{
			// Close
			JPanel panel = new JPanel(new FlowLayout());
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			panel.add(closeButton);
			cp.add(panel, BorderLayout.SOUTH);
		}

		stateChanged(null);
	}

	private JPanel createPerformancePanel() {
		JPanel performancePanel = new JPanel();
		performancePanel.setLayout(new BoxLayout(performancePanel, BoxLayout.Y_AXIS));
		
		// Filter options:
		{
			JPanel filterOptionsPanel = new JPanel();
			filterOptionsPanel.setLayout(new BoxLayout(filterOptionsPanel, BoxLayout.Y_AXIS));
			filterOptionsPanel.setBorder(BorderFactory.createTitledBorder("Filter options"));
			
			{
				// Scale before prepare:
				JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				cbScale = new JCheckBox("Attempt to improve performance by scaling the image down before applying the filters.");
				ActionListener a = new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						oc.setScaleBeforePreparing(cbScale.isSelected(), OptionsDialog.this);
					}
				};
				cbScale.addActionListener(a);
				flowPanel.add(cbScale);
				filterOptionsPanel.add(flowPanel);
			}
			{
				// Reorder filters:
				JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				cbAllowFilterReordering = new JCheckBox("Improve rendering speed by reordering filters. Warning: The colors used when constructing the mosaic will not be consistent.");
				ActionListener a = new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						oc.setAllowFilterReordering(cbAllowFilterReordering.isSelected(), OptionsDialog.this);
					}
				};
				cbAllowFilterReordering.addActionListener(a);
				flowPanel.add(cbAllowFilterReordering);
				filterOptionsPanel.add(flowPanel);
			}
			performancePanel.add(filterOptionsPanel);
		}
		{
			// Scale Quality:
			JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			flowPanel.setBorder(BorderFactory.createTitledBorder("Quality when scaling"));
			
			JPanel bottomButtonGroupPanel = new JPanel();
			bottomButtonGroupPanel.setLayout(new BoxLayout(bottomButtonGroupPanel, BoxLayout.Y_AXIS));
			ButtonGroup bgScaleQuality = new ButtonGroup();
			rbScaleQuality = new JRadioButton[ScaleQuality.values().length];
			int i = 0;
			for(final ScaleQuality s : ScaleQuality.values()) {
				rbScaleQuality[i] = new JRadioButton(s.title);
				rbScaleQuality[i].addActionListener(new ActionListener() {				
					@Override
					public void actionPerformed(ActionEvent e) {
						oc.setScaleQuality(s, OptionsDialog.this);
					}
				});
				bgScaleQuality.add(rbScaleQuality[i]);
				bottomButtonGroupPanel.add(rbScaleQuality[i]);
				++i;
			}
			flowPanel.add(bottomButtonGroupPanel);
			performancePanel.add(flowPanel);
		}
		
		return performancePanel;
	}
		
	private JPanel createExportPanel() {
		JPanel exportPanel = new JPanel();
		exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
		
		// Optimize parts options:
		{
			JPanel optimizePartsOptionsPanel = new JPanel();
			optimizePartsOptionsPanel.setLayout(new BoxLayout(optimizePartsOptionsPanel, BoxLayout.Y_AXIS));
			optimizePartsOptionsPanel.setBorder(BorderFactory.createTitledBorder("Optimize parts when exporting"));
			
			{
				// Scale before prepare:
				JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				cbOptimize = new JCheckBox("Substituting parts from the file 'part_types.txt' when exporting. Notice: LDD, SNOT and tiles are not yet supported!");
				ActionListener a = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						oc.setOptimizeUseOfBricksBeforeExporting(cbOptimize.isSelected(), OptionsDialog.this);
					}
				};
				cbOptimize.addActionListener(a);
				flowPanel.add(cbOptimize);
				optimizePartsOptionsPanel.add(flowPanel);
			}
			exportPanel.add(optimizePartsOptionsPanel);
		}
		
		return exportPanel;
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && e.getSource() == this)
			return;
		
		rbScaleQuality[oc.getScaleQuality().ordinal()].setSelected(true);
		cbAllowFilterReordering.setSelected(oc.getAllowFilterReordering());
		cbScale.setSelected(oc.getScaleBeforePreparing());
		cbOptimize.setSelected(oc.getOptimizeUseOfBricksBeforeExporting());
	}
	
	@Override
	public void pack() {
		super.pack();
		int middle = getOwner().getX() + (getOwner().getWidth()-getWidth())/2;
		setLocation(Math.max(0, middle), Math.max(0,  getOwner().getY()));
	}
}
