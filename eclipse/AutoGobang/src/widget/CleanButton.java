package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class CleanButton extends JButton{
	private static final long serialVersionUID = 1L;
	private boolean hover=false;
	
	public CleanButton() {
		setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		Graphics2D g2d=(Graphics2D) g.create();
		int width=getWidth();
		int height=getHeight();
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, width-1, height-1);
		
		if(hover) {
			g2d.setColor(Color.RED);
		}
		g2d.drawLine(width/2-10, 6, width/2+10, 6);
		g2d.drawLine(width/2-10, 12, width/2+10, 12);
		g2d.drawLine(width/2-10, 18, width/2+10, 18);
		g2d.drawLine(width/2-10, 24, width/2, 24);
		
		g2d.drawLine(width/2+3, 22, width/2+10, 29);
		g2d.drawLine(width/2+10, 22, width/2+3, 29);
		
		g2d.dispose();
		g2d=null;
	}
}
