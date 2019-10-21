package widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class CapButton extends JButton{
	private static final long serialVersionUID = 1L;
	private boolean hover=false;
	private Stroke dashLine = null; 
	private Stroke solidLine = null;
	public CapButton() {
        
		setBorderPainted(false);
        setFocusPainted(false);
        setContentAreaFilled(false);
        
        dashLine = new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{8,4},0);
        
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
    protected void paintComponent(Graphics g){
        Graphics2D g2d = (Graphics2D) g.create();
        
        if(solidLine==null) {
        	solidLine=g2d.getStroke();
        }
        
        int height = getHeight();
        int width = getWidth();
        
        g2d.setColor(Color.BLUE);
        g2d.drawRect(0, 0, width-1, height-1);
        
        if(hover) {
        	g2d.setColor(Color.RED);
        }else {
        	g2d.setColor(Color.BLACK);
        }
        
        g2d.drawOval(width/2-10, height/2-10, 20, 20);
        
        g2d.setStroke(dashLine);    
        g2d.drawLine(width/2, 0, width/2, height);
        g2d.drawLine(0, height/2, width, height/2);
        
        g2d.setStroke(solidLine); 
        g2d.dispose();
        g2d=null;
    }
}
