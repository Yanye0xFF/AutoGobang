package support;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class BoardCapture {
	
	private int x,y,width,height;
	private Robot robot = null;
	public BoardCapture() {
	}
	
	public BoardCapture(int x,int y,int width,int height) {
		this.x=x;
		this.y=y;
		this.width=width;
		this.height=height;
	}
	
	public BufferedImage creat() {
		try {
			if(robot ==null) {
				robot = new Robot();
			}
			return robot.createScreenCapture(new Rectangle(x, y, width,height));
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void close() {
		robot=null;
	}
}
