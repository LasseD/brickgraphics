package mosaic.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.OptionsController;
import transforms.ScaleTransform.ScaleQuality;

public class OptionsDialog extends JDialog implements ChangeListener {
	private OptionsController oc;
	private JCheckBox cbAllowFilterReordering, cbScaleBeforePreparing;
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
		
		JPanel performancePanel = new JPanel();
		performancePanel.setLayout(new BoxLayout(performancePanel, BoxLayout.Y_AXIS));
		
		// Performance options:
		{
			// Scale before prepare:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Scale the loaded image before filtering"));
			cbScaleBeforePreparing = new JCheckBox("Attempt to improve performance by scaling the image down before applying the filters.");
			ActionListener a = new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					oc.setScaleBeforePreparing(cbScaleBeforePreparing.isSelected(), OptionsDialog.this);
				}
			};
			cbScaleBeforePreparing.addActionListener(a);
			titlePanel.add(cbScaleBeforePreparing);
			performancePanel.add(titlePanel);
		}
		{
			// Reorder filters:
			JPanel titlePanel = new JPanel(new FlowLayout());
			titlePanel.setBorder(BorderFactory.createTitledBorder("Reorder filters"));
			cbAllowFilterReordering = new JCheckBox("Improve rendering speed by reordering filters. Warning: The colors used when constructing the mosaic will not be consistent.");
			ActionListener a = new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					oc.setAllowFilterReordering(cbAllowFilterReordering.isSelected(), OptionsDialog.this);
				}
			};
			cbAllowFilterReordering.addActionListener(a);
			titlePanel.add(cbAllowFilterReordering);
			performancePanel.add(titlePanel);
		}
		{
			// Scale Quality:
			JPanel bottomButtonGroupPanel = new JPanel();
			bottomButtonGroupPanel.setLayout(new BoxLayout(bottomButtonGroupPanel, BoxLayout.Y_AXIS));
			bottomButtonGroupPanel.setBorder(BorderFactory.createTitledBorder("Quality when scaling"));
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
			performancePanel.add(bottomButtonGroupPanel);
		}
		
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
		
		tabs.addTab("Performance options", performancePanel);
		cp.add(tabs, BorderLayout.CENTER);
		
		stateChanged(null);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && e.getSource() == this)
			return;
		
		rbScaleQuality[oc.getScaleQuality().ordinal()].setSelected(true);		
		cbAllowFilterReordering.setSelected(oc.getAllowFilterReordering());
		cbScaleBeforePreparing.setSelected(oc.getScaleBeforePreparing());
	}
	
	@Override
	public void pack() {
		super.pack();
		int middle = getOwner().getX() + (getOwner().getWidth()-getWidth())/2;
		setLocation(Math.max(0, middle), Math.max(0,  getOwner().getY()));
	}
}
