package widget;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class StartButton extends JButton{
	private static final long serialVersionUID = 1L;
	private boolean hover=false;
	private boolean state=false;
	public StartButton() {
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
	
	public void setState(boolean state) {
		this.state=state;
		repaint();
	}
	
	
	@Override
    protected void paintComponent(Graphics g){
		
        Graphics2D g2d = (Graphics2D) g.create();
        int centerX = getWidth()/2;
        int centerY = getHeight()/2;
        if(hover) {
        	g2d.setColor(Color.RED);
        }else {
        	g2d.setColor(Color.BLACK);
        }
        g2d.drawOval(0, 0, 31, 31);
        
        
        if(state) {
        	g2d.fillRect(centerX-7, centerX-7, 15, 15);
        }else {
        	int[] xPoints=new int[] {centerX-5,centerX-5,centerX+10};
            int[] yPoints=new int[] {centerY-10,centerY+10,centerY};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
        
        
        g2d.dispose();
        g2d=null;
    }
	
}
