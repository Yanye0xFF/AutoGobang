package widget;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class QImageView extends JLabel {

	private static final long serialVersionUID = 1L;
	private Image image;
	
    private int xDistance = 0;
    private int yDistance = 0;

    int x = -1;
    int y = -1;
    int minX;
    int maxX;
    int minY;
    int maxY;

    private boolean firstDraw = true;    
    
    private String imagePath;
    
    public QImageView() {
    }

    @Override
    protected void paintComponent(Graphics g) {
    	if(image==null) {
    		return;
    	}    	
        Graphics2D g2d = (Graphics2D) g.create();
        
        int currentWidth = image.getWidth(null);
        int currentHeight = image.getHeight(null);

        if (firstDraw) {
            // 图片于容器垂直居中
            x = (getWidth() - currentWidth) / 2;
            y = (getHeight() - currentHeight) / 2;
            firstDraw = false;
        }else {
            x += xDistance;
            y += yDistance;
        }

        minX = (int) (-getWidth() * 0.7);
        maxX = (int) (getWidth() * 0.7);
        minY = (int) (-getHeight() * 0.7);
        maxY = (int) (getHeight() * 0.7);

        x = x < minX ? minX:x;
        x = x > maxX ? maxX:x;
        
        y = y < minY ? minY:y;
        y = y > maxY ? maxY:y;
        
        g2d.drawImage(image, x, y, null);
        g2d.dispose();
        g2d=null;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        firstDraw = true;
        this.repaint();
    }
    
    public void setImage(String imagePath) {
    	this.imagePath=imagePath;
    	try {
			setImage(ImageIO.read(new File(imagePath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String getImagePath() {
    	return imagePath;
    }

    public void moveImage(int xDistance, int yDistance) {
        this.xDistance = xDistance;
        this.yDistance = yDistance;
        this.repaint();
    }
}
