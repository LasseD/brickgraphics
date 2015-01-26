package mosaic.ui;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.print.*;
import javax.swing.*;
import javax.swing.event.*;
import mosaic.controllers.PrintController;

/**
 * @author ld
 */
public class PrintPreviewPanel extends JPanel implements ChangeListener {
	public static final int PADDING_PIXELS = 20;
	
	private boolean isCoverPage;
	private PrintController pc;
	
	public PrintPreviewPanel(boolean isCoverPage, PrintController pc) {
		this.isCoverPage = isCoverPage;
		this.pc = pc;
		pc.addListener(this);
		setBorder(BorderFactory.createTitledBorder("Preview"));
		setPreferredSize(new Dimension(200, 200));
	}
	
	private void drawCoverPage(Graphics2D g2, PageFormat pf) {
		if(!pc.getCoverPageShow())
			return;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)pf.getWidth(), (int)pf.getHeight());
		try {
			pc.print(g2, pf, 0);
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
	
	private void drawInstructionsPage(Graphics2D g2, PageFormat pf) {
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, (int)pf.getWidth(), (int)pf.getHeight());
		try {
			pc.print(g2, pf, 1);
		} catch (PrinterException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		
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
		g2.setColor(Color.BLACK);
		g2.drawRect(pageRect.x, pageRect.y, pageRect.width, pageRect.height);
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		repaint();
	}
}
