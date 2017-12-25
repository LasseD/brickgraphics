package ui;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;

public class ProgressDialog extends JDialog {
	private JTextField tf;
	private JProgressBar pb;
	
	public ProgressDialog(Frame frame, String title) {
		super(frame, title, true);
		setUI();
		setLocationRelativeTo(frame);
	}
	
	private void setUI() {
		tf = new JTextField();
		tf.setEditable(false);
		pb = new JProgressBar();
		pb.setValue(0);
    	pb.setStringPainted(true);
		
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(pb, BorderLayout.CENTER);
		cp.add(tf, BorderLayout.SOUTH);		
		
		setSize(400, 100);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
	}
	
	public ProgressWorker createWorker(Runnable bg) {
		ProgressWorker ret = new ProgressWorker(bg);
		ret.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				if("progress".equals(e.getPropertyName())) {
					pb.setValue((Integer)e.getNewValue());
	            }			
			}
		});
		return ret;
	}
	
	public class ProgressWorker extends SwingWorker<Object, String>{
		private Runnable bg;
		
		public ProgressWorker(Runnable bg) {
			this.bg = bg;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			bg.run();
			return null;
		}	
		
		@Override
		protected void done() {
			setVisible(false);
		}	
		
		@Override
	    protected void process(java.util.List<String> s) {
			if(!isVisible()) {
				new Thread(new Runnable() {					
					@Override
					public void run() {
						setVisible(true); // Run in separate thread because it blocks :(
					}
				}).start();
			}			
	        tf.setText(s.get(s.size()-1));
	    }
		
		public void setProgressAndText(int progress, String text) {
			setProgress(progress);
			publish(text);			
		}
	}
}
