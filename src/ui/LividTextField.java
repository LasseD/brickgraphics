package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;

public class LividTextField extends JTextField {
	public LividTextField(String s, int c) {
		super(s, c);
	}
	
	public LividTextField(String s) {
		super(s);
	}
	
	public LividTextField(int c) {
		super(c);
	}
	
	@Override
	public synchronized void addActionListener(final ActionListener l) {
		super.addActionListener(l);
		super.addKeyListener(new KeyListener() {			
			@Override
			public void keyTyped(KeyEvent e) {
				// NOP: Perform on release
			}			
			@Override
			public void keyReleased(KeyEvent e) {
				l.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.getKeyChar()+""));
			}			
			@Override
			public void keyPressed(KeyEvent e) {
				// NOP: Perform on release
			}
		});
	}
}
