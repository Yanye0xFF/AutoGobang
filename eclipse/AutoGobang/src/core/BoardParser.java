package core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import support.Const;

public class BoardParser {
	
	private int boardSize=0,gridSize=0;
	private Dimension start=null,end=null;
	
	public static final int TYPE_WHITE=0;
	public static final int TYPE_BLACK=1;
	
	public static final int NODE_CORNER=0;
	public static final int NODE_NORMAL=1;
	public static final int NODE_SIDE_TOP=2;
	public static final int NODE_SIDE_BOTTOM=3;
	public static final int NODE_SIDE_LEFT=4;
	public static final int NODE_SIDE_RIGHT=5;
	
	private BufferedImage boardCache=null;
	
	public BoardParser() {
	}
	
	public BoardParser(int boardSize, int gridSize,Dimension start,Dimension end) {
		this.boardSize=boardSize;
		this.gridSize=gridSize;
		this.start=start;
		this.end=end;
	}
	
	public boolean parseImage(BufferedImage img,int[][] array,int type) {
				
		int cacheWidth=end.width-start.width+1;
		int cacheHeight=end.height-start.height+1;
		
		if(boardCache==null) {
			boardCache=new BufferedImage(cacheWidth, cacheHeight, BufferedImage.TYPE_INT_RGB);
		}
		
		int color=0,r=0,g=0,b=0;
		switch (type) {
			case TYPE_BLACK:
				//识别黑色棋子
				for(int y=0;y<cacheHeight;++y) {
					for(int x=0;x<cacheWidth;++x) {
						color=img.getRGB(x+start.width, y+start.height);
						r=(color >> 16) & 0xFF;
						g=(color >> 8) & 0xFF;
						b=color & 0xFF;
						if(r < Const.LIMIT_RED &&  g < Const.LIMIT_GREEN && b < Const.LIMIT_BLUE) {
							boardCache.setRGB(x, y, 0xFF000000);
					  	}else {
					  		boardCache.setRGB(x, y, 0xFFFFFFFF);

					  	}
					}
				}
				break;
			case TYPE_WHITE:
				//识别白色棋子
				for(int y=0;y<cacheHeight;++y) {
					for(int x=0;x<cacheWidth;++x) {
						color=img.getRGB(x+start.width, y+start.height);
						r=(color >> 16) & 0xFF;
						g=(color >> 8) & 0xFF;
						b=color & 0xFF;
						if(r > Const.INVERSE_RED &&  g > Const.INVERSE_GREEN && b > Const.INVERSE_BLUE) {
							boardCache.setRGB(x, y, 0xFF000000);
					  	}else {
					  		boardCache.setRGB(x, y, 0xFFFFFFFF);
					  	}
					}
				}
				break;
			default:
				return false;
		}
		
		img=null;

		//按行扫描判断节点类型
		int xOffset=0,yOffset=0;
		int nodeType=-1;
		for(int y=0;y<boardSize;++y) {
			for(int x=0;x<boardSize;++x) {

				//已存在的点不必再次从原图中识别
				if(array[y][x]==1) {
					continue;
				}
				
				if(y%(boardSize-1)==0 && x%(boardSize-1)==0) {
					nodeType=NODE_CORNER;
				}else {
					if(y==0) {
						nodeType=NODE_SIDE_TOP;
					}else if(y==(boardSize-1)) {
						nodeType=NODE_SIDE_BOTTOM;
					}else if(x==0) {
						nodeType=NODE_SIDE_LEFT;
					}else if(x==(boardSize-1)) {
						nodeType=NODE_SIDE_RIGHT;
					}else {
						nodeType=NODE_NORMAL;
					}
				}
				
				if(isNode(x,y,x*gridSize+xOffset,y*gridSize+yOffset,nodeType)) {
					array[y][x]=1;
				}	
				
				if(x%(boardSize-1)==0) {
					xOffset+=Const.BORDER_WIDTH+2;
				}else {
					xOffset+=Const.LINE_WIDTH;
				}
				
			}
			
			xOffset=0;
			if(y%(boardSize-1)==0) {
				yOffset+=Const.BORDER_WIDTH+2;
			}else {
				yOffset+=Const.LINE_WIDTH;
			}
		}
		return true;
		
	}
	
	private boolean isNode(int gridX,int gridY,int posX,int posY,int type) {
		
		//棋子中心参照格点中心面积分布(上:下)4:6
		int startX=0,startY=0,width=0,height=0;
		
		switch (type) {
		//left offset 0
		//right offset 0
			case NODE_NORMAL:
				//top offset 0.4
				//bottom offset 0.6
				startX=posX-(gridSize/2);
				startY=posY-(int)(gridSize*0.4);
				width=gridSize;
				height=gridSize;
				break;
			case NODE_CORNER:
				if(gridY==0) {
					if(gridX==0) {
						startX=posX;
					}else {
						startX=posX-(gridSize/2);
					}
					startY=posY;
					width=gridSize/2;
					height=(int)(gridSize*0.6);
				}else if(gridY==(boardSize-1)) {
					if(gridX==0) {
						startX=posX;
						startY=posY-(int)(gridSize*0.4);
					}else {
						startX=posX-(gridSize/2);
						startY=posY-(int)(gridSize*0.4);
					}
					width=gridSize/2;
					height=(int)(gridSize*0.4);
				}
				break;
			case NODE_SIDE_TOP:
				//top offset 0
				//bottom offset 0.6
				startX=posX-(gridSize/2);
				startY=posY;
				width=gridSize;
				height=(int)(gridSize*0.6);
				break;
			case NODE_SIDE_BOTTOM:
				//top offset 0.4
				//bottom offset 0
				startX=posX-(gridSize/2);
				startY=posY-(int)(gridSize*0.4);
				width=gridSize;
				height=(int)(gridSize*0.4);
				break;
			case NODE_SIDE_LEFT:
				//top offset 0.4
				//bottom offset 0.6
				startX=posX;
				startY=posY-(int)(gridSize*0.4);
				width=gridSize/2;
				height=gridSize;
				break;
			case NODE_SIDE_RIGHT:
				//top offset 0.4
				//bottom offset 0.6
				startX=posX-(gridSize/2);
				startY=posY-(int)(gridSize*0.4);
				width=gridSize/2;
				height=gridSize;
				break;
			default:
				break;
		}
		
		int counter=0;
		for(int y=startY,yLimit=(startY+height-1);y<yLimit;++y) {
			for(int x=startX,xLimit=(startX+width-1);x<xLimit;++x) {
				if(boardCache.getRGB(x, y)==0xFF000000) {
					counter++;
				}
			}		
		}		
		
		double result=counter/(width*height*1.0D);
		//限制占比55%
		return result>0.55D ? true : false;
	}
	
}
