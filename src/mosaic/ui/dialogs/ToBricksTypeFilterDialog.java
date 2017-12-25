package mosaic.ui.dialogs;

import icon.Icons;
import icon.ToBricksIcon.ToBricksIconType;
import javax.swing.*;
import bricks.ToBricksType;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import mosaic.controllers.*;
import mosaic.ui.MainWindow;

public class ToBricksTypeFilterDialog extends JDialog implements ChangeListener, ActionListener {
	private JCheckBox[] boxes;
	private ToBricksController controller;
	
	public ToBricksTypeFilterDialog(final ToBricksController controller, MainWindow mw) {
		super(mw, "Available construction techniques", true);
		this.controller = controller;
		controller.addChangeListener(this);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		ToBricksType[] tbtValue = ToBricksType.values();
		boxes = new JCheckBox[tbtValue.length];
		for(int i = 0; i < tbtValue.length; ++i) {
			ToBricksType type = tbtValue[i];
			JCheckBox cb = new JCheckBox(type.getDescription(), false);
			cb.addActionListener(this);
			boxes[i] = cb;

			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel(type.getIcon().get(ToBricksIconType.Enabled, Icons.SIZE_LARGE)));
			panel.add(cb);
			
			mainPanel.add(panel);
		}
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);

		setModal(true);
		
		stateChanged(null);
	}
		
	public void tellController() {
		boolean[] selected = new boolean[boxes.length];
		
		for(int i = 0; i < boxes.length; ++i) {
			if(boxes[i].isSelected())
				selected[i] = true;
		}
		controller.setAvailableToBricksTypes(selected);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e != null && e.getSource() == this)
			return;
		boolean[] selectedIndices = controller.getAvailableToBricksTypes();
		int selectedIndex = controller.getToBricksType().ordinal();
		for(int i = 0; i < selectedIndices.length; ++i) {
			boxes[i].setEnabled(i != selectedIndex);
			boxes[i].setSelected(selectedIndices[i]);				
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		tellController();
	}
}
