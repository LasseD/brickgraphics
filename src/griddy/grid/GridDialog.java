package griddy.grid;

import griddy.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

import ui.LividTextField;

public class GridDialog extends JDialog {
	private static final long serialVersionUID = -4153803597071144909L;
	private JRadioButton[] gridTypeButtons;
	private JButton addLevelButton;
	private JCheckBox contrastButton;
	private ColorButton colorButton;
	private JPanel gridsPanel;
	
	public GridDialog(final Griddy parent, final Grid grid) {
		super(parent, "Grid Settings", true);
		//setIconImage(Icons.get(Icons.SIZE_LARGE, "preferences.png").getImage()); TODO: pic
		
		// Main type:
		gridTypeButtons = new JRadioButton[SizeType.values().length];
		ButtonGroup gridTypeButtonGroup = new ButtonGroup();
		int i = 0;
		for(final SizeType sizeType : SizeType.values()) {
			JRadioButton b = new JRadioButton(sizeType.icon(16));
			b.setToolTipText("Set grid type: " + sizeType.name());
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					grid.setSizeType(sizeType);
					parent.repaint();
				}
			});
			gridTypeButtons[i] = b;		
			gridTypeButtonGroup.add(b);
			i++;
		}

		// Main colour:
		colorButton = new ColorButton(this, Color.BLACK);
		colorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.getGridLevels().get(0).setColor(colorButton.getColor());
				parent.repaint();
			}
		});
		// Main contrast:
		contrastButton = new JCheckBox("Use contrast color");
		contrastButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.getGridLevels().get(0).setDrawContrasting(contrastButton.isSelected());
				parent.repaint();
			}
		});
		// Secondary grids:
		addLevelButton = new JButton("Add grid level");
		addLevelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				grid.addGridLevel();
				rebuildGridLevels(parent, grid);
				parent.repaint();
			}
		});
		
		
		// all components:	
		JPanel mainGridPanel = new JPanel(new GridLayout(2,1));
		// top (size):
		JPanel topPanel = new JPanel(new FlowLayout());
		for(JRadioButton b : gridTypeButtons) {
			topPanel.add(b);
		}
		mainGridPanel.add(topPanel);
		// bottom (colour, contrast)
		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.add(new JLabel("Color"));
		bottomPanel.add(colorButton);
		bottomPanel.add(contrastButton);
		mainGridPanel.add(bottomPanel);

		// All other grids:
		JPanel midPanel = new JPanel(new BorderLayout());
		JPanel addLevelPanel = new JPanel(new FlowLayout());
		addLevelPanel.add(addLevelButton);
		midPanel.add(addLevelPanel, BorderLayout.EAST);
		
		gridsPanel = new JPanel();
		gridsPanel.setLayout(new BoxLayout(gridsPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(gridsPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollPane.add(gridsPanel);
		midPanel.add(scrollPane, BorderLayout.CENTER);

        // all:
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(mainGridPanel, BorderLayout.NORTH);
		contentPane.add(midPanel, BorderLayout.CENTER);
		contentPane.add(createOkPanel(), BorderLayout.SOUTH);

		pack();
	}
	
	private void rebuildGridLevels(final Griddy parent, final Grid grid) {
		List<Grid.GridLevel> gridLevels = grid.getGridLevels();
		gridsPanel.removeAll();
		int i = 1;
		for(final Grid.GridLevel gl : gridLevels) {
			if(i == 1) {
				i++;
				continue;
			}
						
			JPanel glPanel = new JPanel(new GridLayout(2, 1));
			glPanel.setBorder(new TitledBorder("Grid level " + i++));
			// top (size):
			JPanel topPanel = new JPanel(new FlowLayout());
			topPanel.add(new JLabel("Grid size"));
			final LividTextField tx = new LividTextField(gl.getXSize() + "", 4);
			topPanel.add(tx);
			topPanel.add(new JLabel("x"));
			final LividTextField ty = new LividTextField(gl.getYSize() + "", 4);
			topPanel.add(ty);
			glPanel.add(topPanel, BorderLayout.NORTH);
			// bottom (colour, contrast, remove)
			JPanel bottomPanel = new JPanel(new FlowLayout());
			final ColorButton colorButton = new ColorButton(this, gl.getColor());
			bottomPanel.add(colorButton);
			final JCheckBox contrastButton = new JCheckBox("Draw constrasting", gl.getDrawContrasting());
			bottomPanel.add(contrastButton);
			JButton removeButton = new JButton("Remove");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					grid.removeGridLevel(gl);
					rebuildGridLevels(parent, grid);
					parent.repaint();
				}
			});
			bottomPanel.add(removeButton);
			glPanel.add(bottomPanel, BorderLayout.SOUTH);
			
			ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					gl.setColor(colorButton.getColor());
					try {
						gl.setXSize(Integer.parseInt(tx.getText().trim()));
						gl.setYSize(Integer.parseInt(ty.getText().trim()));						
						gl.setDrawContrasting(contrastButton.isSelected());
						parent.repaint();
					}
					catch(Exception e2) {
						// NOP!
					}
				}				
			};
			colorButton.addActionListener(actionListener);
			tx.addActionListener(actionListener);
			ty.addActionListener(actionListener);
			contrastButton.addActionListener(actionListener);
			
			gridsPanel.add(glPanel);
		}
		gridsPanel.revalidate();
		repaint();
	}
	
	public Action makeShowOptionsDialogAction(final Griddy parent, final Grid grid) {
		Action a = new AbstractAction() {
			private static final long serialVersionUID = 37932469441L;

			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(parent, grid);
			}
		};

		a.putValue(Action.SHORT_DESCRIPTION, "Grid Options");
		a.putValue(Action.LONG_DESCRIPTION, "Show grid options dialog.");
		//a.putValue(Action.SMALL_ICON, Icons.get(Icons.SIZE_SMALL, "preferences"));
		//a.putValue(Action.LARGE_ICON_KEY, Icons.get(Icons.SIZE_LARGE, "preferences"));
		a.putValue(Action.NAME, "GridOptions");
		a.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
		a.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Grid Options".indexOf('G'));
		a.putValue(Action.ACTION_COMMAND_KEY, "Grid Options");
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));	
		
		return a;
	}
	
	public void showDialog(final Griddy parent, final Grid grid) {
		Grid.GridLevel topLevel = grid.getGridLevels().get(0);
		int typeIndex = grid.getSizeType().ordinal();
		gridTypeButtons[typeIndex].setSelected(true);
		
		contrastButton.setSelected(topLevel.getDrawContrasting());
		
		colorButton.setColor(topLevel.getColor());
		
		rebuildGridLevels(parent, grid);

		setSize(640, 480);
		setVisible(true);
	}
	
	private JPanel createOkPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JButton ok = new JButton("OK");
		
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		panel.add(ok);	
		return panel;
	}
}
