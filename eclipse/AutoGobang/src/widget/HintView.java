package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class HintView extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private boolean state=false;
	
	public HintView() {
	}
	
	public void setState(boolean state) {
		this.state=state;
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics arg0) {
		Graphics2D g2d = (Graphics2D) arg0.create();
		int height = getHeight();
        int width = getWidth();
        g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, width-1, height-1);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(1, 1, width-2, height-2);
		if(state) {
			 g2d.setColor(Color.RED);
		}else {
			 g2d.setColor(Color.BLACK);
		}
		g2d.drawOval(width/2-7, height/2-7, 15, 15);
		g2d.drawLine(width/2-7-5, height/2, width/2-7, height/2);
		g2d.drawLine(width/2+8, height/2, width/2+8+5, height/2);
		g2d.dispose();
		g2d=null;
	}
}
