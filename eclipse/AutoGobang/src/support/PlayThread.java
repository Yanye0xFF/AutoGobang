package support;

import java.awt.Dimension;
import java.awt.Point;

import core.BoardParser;
import core.WineLoader;
public class PlayThread extends Thread{
	
	private int boardSize=0,gridSize=0;
	private Point pressed=null,moved=null;;
	private Dimension start=null,end=null;
	
	private ProcessCallBack callBack=null;
	private OperateHandler handler=null;
	
	private BoardCapture boardCapture=null;
	private BoardParser boardParser=null;

	private volatile int handType=BoardParser.TYPE_WHITE;
	private WineLoader coreLoader=null;
	private volatile boolean runing=true;
	
	public PlayThread(int boardSize, int gridSize, Point pressed, Point moved,Dimension start,
			Dimension end) {
		this.boardSize=boardSize;
		this.gridSize=gridSize;
		this.pressed=pressed;
		this.moved=moved;
		this.start=start;
		this.end=end;
	}
	
	
	public void setProcessCore(WineLoader wineLoader) {
		this.coreLoader=wineLoader;
	}
	
	public void setHandType(int type) {
		runing=true;
		if(type==BoardParser.TYPE_WHITE) {
			this.handType=BoardParser.TYPE_BLACK;
		}else if(type==BoardParser.TYPE_BLACK) {
			this.handType=BoardParser.TYPE_WHITE;
		}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
	}

	@Override
	public void run() {
		if(callBack==null) {
			System.out.println("must init process callback");
			return;
		}
		//棋盘数据数组,横向按行读取
		// 1存在 0 空白
		int[][] oldBoard=new int[boardSize][boardSize];
		int[][] newBoard=new int[boardSize][boardSize];
		int[][] myTurn=new int[boardSize][boardSize];
		
		if(handler==null) {
			handler=new OperateHandler(boardSize,gridSize,pressed,start);
		}
		
		if(boardCapture==null) {
			boardCapture=new BoardCapture(pressed.x, pressed.y, (moved.x-pressed.x), (moved.y-pressed.y));
		}
		
		if(boardParser==null) {
			boardParser=new BoardParser(boardSize,gridSize,start,end);
		}
		
		if(coreLoader==null) {
			callBack.onLogAdd("process library is null",true);
			return;
		}
		
		fillArray(oldBoard, 0);
		fillArray(newBoard, 0);
		fillArray(myTurn, 0);
		
		coreLoader.restart();
		coreLoader.init(boardSize);
		
		//稍稍延时
		callBack.onLogAdd("thread will start after 3 seconds",true);
		callBack.onLogAdd("do not move browser window",true);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int bestX=0,bestY=0;
		int turnCount=0;
		//我方先手 调用ai落子
		if(handType==BoardParser.TYPE_WHITE) {
			if(coreLoader.begin()==200) {
				bestX=coreLoader.getBestX();
				bestY=coreLoader.getBestY();
				myTurn[bestY][bestX]=1;
				turnCount++;
				callBack.onLogAdd("robot : ("+bestX+" , "+bestY+")",false);
				handler.mouseClick(coreLoader.getBestX(), coreLoader.getBestY());
			}
		}else {
			callBack.onLogAdd("scaning for human point",true);
		}
		
		//我方后手扫描等待对方落子
		while(runing) {
			
			boardParser.parseImage(boardCapture.creat(), newBoard, handType);

			Point point=minusArray(oldBoard,newBoard);
			if(point!=null) {
				callBack.onThinkState(true);
				if(handType==BoardParser.TYPE_WHITE) {
					callBack.onLogAdd(" -- human : ("+point.x+" , "+point.y+")",true);
				}else {
					callBack.onLogAdd("human : ("+point.x+" , "+point.y+")",false);
				}
				if(coreLoader.turn(point.x, point.y)==200) {
					bestX=coreLoader.getBestX();
					bestY=coreLoader.getBestY();
					myTurn[bestY][bestX]=1;
					turnCount++;
					if(handType==BoardParser.TYPE_WHITE) {
						callBack.onLogAdd("robot : ("+bestX+" , "+bestY+")",false);
					}else {
						callBack.onLogAdd(" -- robot : ("+bestX+" , "+bestY+")",true);
					}
					callBack.onThinkState(false);
					handler.mouseClick(coreLoader.getBestX(), coreLoader.getBestY());
				}
			}
			
			point=null;
			
			if(turnCount>3 && isWin(myTurn)) {
				runing=false;
				callBack.onLogAdd("\nresult : robot win",true);
				break;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		callBack.onFinished("\nthread close");
	}

	@Override
	public synchronized void start() {
		super.start();
	}
	
	public interface ProcessCallBack{
		void onLogAdd(String str,boolean newLine);
		void onThinkState(boolean think);
		void onFinished(String str);
	}
	
	public void setProcessCallBack(ProcessCallBack callBack) {
		this.callBack=callBack;
	}
	
	public void stopRuning() {
		this.runing=false;
	}
	
	private void fillArray(int[][] array,int num) {
		int length=array.length;
		for(int x=0;x<length;++x) {
			for(int y=0;y<length;++y) {
				array[x][y]=0;
			}
		}
	}
	
	private Point minusArray(int[][] src,int[][] target) {
		int pX=0,pY=0;
		for(int x=0;x<boardSize;++x) {
			for(int y=0;y<boardSize;++y) {
				if(src[x][y]!=target[x][y]) {
					pX=y;
					pY=x;
					src[x][y]=target[x][y];
					return new Point(pX, pY);
				}
			}
		}
		return null;
	}
	
	private boolean isWin(int[][] array) {
		int count=0;
		int cursorX=0,cursorY=0;
		for(int x=0;x<boardSize;++x) {
			for(int y=0;y<boardSize;++y) {
				if(array[x][y]==1) {
					//N
					count=1;
					cursorX = (x>0) ? x-1 :0;
					while(array[cursorX][y] == 1 && cursorX > 0) {
						cursorX--;
						count++;
					}
					if(count>4) {
						return true;
					}
					//S
					count=1;	
					cursorX = (x<boardSize-1) ? x+1 :boardSize-1;
					while(array[cursorX][y] == 1 && cursorX < boardSize-1) {
						cursorX++;
						count++;
					}
					if(count>4) {
						return true;
					}
					//W
					count=1;
					cursorY = (y>0) ? y-1 :0;
					while(array[x][cursorY] == 1 && cursorY > 0) {
						cursorY--;
						count++;
					}
					if(count>4) {
						return true;
					}
					//E
					count=1;
					cursorY = (y<boardSize-1) ? y+1 :boardSize-1;
					while(array[x][cursorY] == 1 && cursorY < boardSize-1) {
						cursorY++;
						count++;
					}
					if(count>4) {
						return true;
					}
					//NW
					count=1;
					cursorX = (x>0) ? x-1 :0;
					cursorY = (y>0) ? y-1 :0;
					while(array[cursorX][cursorY] == 1 && cursorY > 0 &&  cursorX > 0) {
						cursorX--;
						cursorY--;
						count++;
					}
					if(count>4) {
						return true;
					}
					//NE
					count=1;
					cursorX = (x>0) ? x-1 :0;
					cursorY = (y<boardSize-1) ? y+1 :boardSize-1;
					while(array[cursorX][cursorY] == 1 && cursorY < boardSize-1 &&  cursorX > 0) {
						cursorX--;
						cursorY++;
						count++;
					}
					if(count>4) {
						return true;
					}
					//SW
					count=1;
					cursorX = (x<boardSize-1) ? x+1 :boardSize-1;
					cursorY = (y>0) ? y-1 :0;
					while(array[cursorX][cursorY] == 1 && cursorY > 0 &&  cursorX < boardSize-1) {
						cursorX++;
						cursorY--;
						count++;
					}
					if(count>4) {
						return true;
					}
					//SE
					count=1;
					cursorX = (x<boardSize-1) ? x+1 :boardSize-1;
					cursorY = (y<boardSize-1) ? y+1 :boardSize-1;
					while(array[cursorX][cursorY] == 1 && cursorY < boardSize-1 &&  cursorX < boardSize-1) {
						cursorX++;
						cursorY++;
						count++;
					}
					if(count>4) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
