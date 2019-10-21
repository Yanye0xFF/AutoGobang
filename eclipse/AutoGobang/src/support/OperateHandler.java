package support;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class OperateHandler {
	
	private int boardSize=0,gridSize=0;
	private Point pressed=null;
	private Dimension start=null;
	private Robot robot=null;;
	public OperateHandler() {
	}
	
	public OperateHandler(int boardSize, int gridSize, Point pressed, Dimension start) {
		this.boardSize=boardSize;
		this.gridSize=gridSize;
		this.pressed=pressed;
		this.start=start;
	}
	
	public boolean mouseClick(int boardX,int boardY) {
		//中心点(及其)左侧使用top left padding
		//中心点右侧使用 bottom end padding
		if(boardX<0 || boardY>boardSize-1) {
			return false;
		}
		
		if(boardX<0 || boardY>boardSize-1) {
			return false;
		}
		
		int clickX=0,clickY=0;
		int xOffset=0,yOffset=0;
		
		for(int y=0;y<boardY;++y) {
			if(y%(boardSize-1)==0) {
				yOffset+=Const.BORDER_WIDTH+1;
			}else {
				yOffset+=Const.LINE_WIDTH;
			}
		}
		
		for(int x=0;x<boardX;++x) {
			if(x%(boardSize-1)==0) {
				xOffset+=Const.BORDER_WIDTH+1;
			}else {
				xOffset+=Const.LINE_WIDTH;
			}
		}

		clickX=pressed.x+start.width+boardX*gridSize+xOffset;
		clickY=pressed.y+start.height+boardY*gridSize+yOffset;
		
		try {	
			if(robot==null) {
				robot=new Robot();
			}
			robot.delay(200);
			robot.mouseMove(clickX,clickY);
			robot.delay(150);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.delay(100);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);
			robot.delay(200);
			robot.mouseMove(880, 1000);
		} catch (AWTException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
