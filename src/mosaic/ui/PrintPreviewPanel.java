package mosaic.ui;

import io.Log;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;

import ui.LividTextField;
import mosaic.controllers.ColorController;
import mosaic.controllers.PrintController;
import mosaic.rendering.Pipeline;
import mosaic.rendering.PipelineMosaicListener;

/**
 * @author LD
 */
public class PrintPreviewPanel extends JPanel implements ChangeListener, PipelineMosaicListener {
	public static final int PADDING_PIXELS = 8;
	
	private boolean isCoverPage;
	private PrintController pc;
	private LividTextField tf;
	private int shownPage;
	
	public PrintPreviewPanel(boolean isCoverPage, final PrintController pc, final ColorController cc, Pipeline pipeline) {
		this.isCoverPage = isCoverPage;
		this.pc = pc;
		pc.addChangeListener(this);
		cc.addChangeListener(this);
		pipeline.addMosaicListener(this);
		setBorder(BorderFactory.createTitledBorder("Preview"));
		setLayout(new BorderLayout());
		PicturePanel picturePanel = new PicturePanel();
		picturePanel.setPreferredSize(new Dimension(250, 250));
		add(picturePanel, BorderLayout.CENTER);
		if(!isCoverPage) {
			shownPage = 1;
			// Add navigation panel:
			JPanel navPanel = new JPanel(new BorderLayout());

			// Text field:
			tf = new LividTextField("1");
			tf.setHorizontalAlignment(SwingConstants.CENTER);
			navPanel.add(tf, BorderLayout.CENTER);
			tf.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						shownPage = Integer.parseInt(tf.getText());
						update();
					}
					catch(NumberFormatException e2) {
						// Nop
					}
				}
			});
			navPanel.add(tf, BorderLayout.CENTER);
						
			// left button:
			JButton leftButton = new JButton("<");
			leftButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					--shownPage;
					update();
				}
			});
			navPanel.add(leftButton, BorderLayout.WEST);
			
			// right panel:
			JButton rightButton = new JButton(">");
			rightButton.addActionListener(new ActionListener() {				
				@Override
				public void actionPerformed(ActionEvent e) {
					++shownPage;
					update();
				}
			});
			navPanel.add(rightButton, BorderLayout.EAST);
			
			add(navPanel, BorderLayout.SOUTH);
		}
	}
	
	private void sanitizePage() {
		if(isCoverPage)
			return;
		int maxPage = pc.getNumberOfPages();
		if(shownPage < 1)
			shownPage = 1;
		else if(shownPage > maxPage)
			shownPage = maxPage;		
	}
	
	private void update() {
		sanitizePage();
		String sp = "" + shownPage;
		if(tf != null && !tf.getText().trim().equals(sp)) {
			tf.setText(sp);
		}
		repaint();
	}
	
	private void drawCoverPage(Graphics2D g2, PageFormat pf) {
		if(!pc.getCoverPageShow())
			return;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)pf.getWidth(), (int)pf.getHeight());
		try {
			pc.print(g2, pf, 0);
		} catch (PrinterException e) {
			Log.log(e);
		}
	}
	
	private void drawInstructionsPage(Graphics2D g2, PageFormat pf) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)pf.getWidth(), (int)pf.getHeight());
		try {
			pc.print(g2, pf, pc.getCoverPageShow() ? shownPage : shownPage-1);
		} catch (PrinterException e) {
			Log.log(e);
		}
	}
	
	private class PicturePanel extends JPanel {
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);			
			Graphics2D g2 = (Graphics2D)g;
			if(!isVisible())
				return;
			
			sanitizePage();
			// Compute where to draw page:
			final Rectangle pageRect = new Rectangle(PADDING_PIXELS, PADDING_PIXELS, getWidth()-2*PADDING_PIXELS, getHeight()-2*PADDING_PIXELS);

			// Fit pageRect:
			double pageRect_w2h = pageRect.width/(double)pageRect.height;
			PageFormat pf = pc.getPageFormat();
			double pf_w2h = pf.getWidth()/pf.getHeight();
			if(pf_w2h < pageRect_w2h) { // Fit width:
				pageRect.width = (int)(pageRect.height*pf_w2h);
				pageRect.x = (getWidth()-pageRect.width)/2;
			}
			else { // fit height:
				pageRect.height = (int)(pageRect.width/pf_w2h);			
				pageRect.y = (getHeight()-pageRect.height)/2;
			}
			
			AffineTransform originalTransform = g2.getTransform();
			
			double scale = pageRect.height / pf.getHeight();
			g2.translate(pageRect.x, pageRect.y);		
			g2.scale(scale, scale);
			
			if(isCoverPage) {
				drawCoverPage(g2, pf);
			}
			else {
				drawInstructionsPage(g2, pf);
			}
			
			g2.setTransform(originalTransform);
			
			// Draw border:
			g2.setColor(getBackground());
			g2.fillRect(pageRect.x+pageRect.width, pageRect.y, getWidth(), pageRect.height); // OK to paint out of border.
			g2.setColor(Color.BLACK);
			g2.drawRect(pageRect.x, pageRect.y, pageRect.width, pageRect.height);
		}		
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		update();
	}

	@Override
	public void mosaicChanged(Dimension ignore) {
		update();
	}
}
