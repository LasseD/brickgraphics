package mosaic.ui.actions;

import io.Model;
import java.awt.*;
import java.awt.print.*;
import java.awt.event.*;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import colors.LEGOColor;
import mosaic.io.BrickGraphicsState;
import mosaic.ui.MainWindow;
import transforms.ToBricksTransform;
import ui.Icons;

/**
 * This class takes care of the printing mechanism
 * @author ld
 */
public class PrintAction implements Printable {
	private ToBricksTransform tbt;
	
	private PrintAction(ToBricksTransform tbt) {
		this.tbt = tbt;
	}
	
	public static Action createPrintAction(final Model<BrickGraphicsState> currentModel, final MainWindow parent) {
		Action printAction = new AbstractAction() {
			private static final long serialVersionUID = 5508303534964025455L;

			public void actionPerformed(ActionEvent e) {
				final PrinterJob job = PrinterJob.getPrinterJob();
				job.setPrintable(new PrintAction(parent.getBrickedView().getToBricksTransform()));
				
				if (job.printDialog()) {
				    try {
				        job.print();
				    } 
				    catch (PrinterException e2) {
						String message = "An error ocurred while printing: " + e2.getMessage();
						JOptionPane.showMessageDialog(parent, message, "Error when printing", JOptionPane.ERROR_MESSAGE);
						e2.printStackTrace();
				    }
				}
			}
		};

		printAction.putValue(Action.SHORT_DESCRIPTION, "Print the mosaic.");
		printAction.putValue(Action.SMALL_ICON, Icons.get(16, "printer"));
		printAction.putValue(Action.LARGE_ICON_KEY, Icons.get(32, "printer"));
		printAction.putValue(Action.NAME, "Print");
		printAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		printAction.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, "Print".indexOf('P'));
		printAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));

		return printAction;
	}

	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
		Graphics2D g2 = (Graphics2D)g;		
		
		final int blockW = 8;
		final int blockH = 4;
		
		LEGOColor[][] instructions = tbt.getMainTransform().lastInstructions();
		
		final int w = instructions.length;
		final int h = instructions[0].length;
		final int numBlocksWidth = (w+blockW-1) / blockW;
		final int numBlocksHeight = (h+blockH-1) / blockH;		
	    if (page >= numBlocksWidth*numBlocksHeight) {
	    	return NO_SUCH_PAGE;
	    }
		final int blockX = page % numBlocksWidth;
		final int blockY = page / numBlocksWidth;
	    
	    // Now we perform our rendering
	    final int margin = (int)(pf.getImageableWidth()/10);
	    g2.drawString("8x16 flise til placering " + (blockX+1) + " mod højre, " + (blockY+1) + " ned.", margin+10, margin+20);
	    g2.drawString("Side " + (page+1) + " af " + (numBlocksWidth*numBlocksHeight), (int)pf.getImageableWidth()/2-50, (int)pf.getImageableHeight()-50);
	    
	    double startImageY = pf.getImageableHeight()-margin-(int)(pf.getImageableWidth()*0.4);
	    g2.translate(margin, startImageY-50);

	    // draw magnified:
		Rectangle bounds = new Rectangle(blockX*blockW, blockY*blockH, blockW, blockH);
		Dimension dim = new Dimension((int)(pf.getImageableWidth()*0.8), (int)(pf.getImageableWidth()*0.4));
		//tbt.getMainTransform().drawLastInstructions(g2, bounds, 1, 1, dim);
		Set<LEGOColor> used = tbt.getMainTransform().drawLastColors(g2, bounds, 1, 1, dim, 2);
		LEGOColor[] usedc = used.toArray(new LEGOColor[]{});
		
		String[] dkNames = new String[500];
		for(int i = 0; i < 500; ++i)
			dkNames[i] = "";
		dkNames[1] = "HVID";
		dkNames[5] = "SANDFARVET";
		dkNames[21] = "RØD";
		dkNames[23] = "BLÅ";
		dkNames[24] = "GUL";
		dkNames[26] = "SORT";
		dkNames[28] = "GRØN";
		dkNames[106] = "ORANGE";
		dkNames[119] = "LIME";
		dkNames[154] = "MØRKERØD";
		dkNames[192] = "BRUN";
		dkNames[194] = "LYSEGRÅ";
		dkNames[199] = "MØRKEGRÅ";
		if(page == 0) {
			int[] sums = new int[500];
			for(int x = 0; x < instructions.length; ++x) {
				for(int y = 0; y < instructions[x].length; ++y) {
					sums[instructions[x][y].getID()]++;
				}
			}
			for(int i = 0; i < 500; ++i) {
				if(dkNames[i].length() == 0)
					continue;
				//System.out.println(dkNames[i] + ": " + sums[i]);
			}
 		}
		
		g2.translate(0, -startImageY+200);
		for(int i = 0; i < usedc.length/2; ++i) {
			g2.setColor(usedc[i].getRGB());
			g2.fillRect(0,  i*60, 50, 50);
			g2.setColor(Color.BLACK);
			g2.drawRect(0,  i*60, 50, 50);
			g2.drawString("" + dkNames[usedc[i].getID()], 60, i*60+30);
		}
		for(int j = 0, i = usedc.length/2; i < usedc.length; ++i, ++j) {
			g2.setColor(usedc[i].getRGB());
			g2.fillRect(300,  j*60, 50, 50);
			g2.setColor(Color.BLACK);
			g2.drawRect(300,  j*60, 50, 50);
			g2.drawString("" + dkNames[usedc[i].getID()], 360, j*60+30);			
		}
		
	    return PAGE_EXISTS;
	}
}
