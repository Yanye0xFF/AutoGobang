package ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import javax.swing.JWindow;

import support.Const;
import support.Tools;


public class ScreenShootWindow extends JWindow{

	private static final long serialVersionUID = 1L;
	
	private int screenWidth=0,screenHeight=0;
	
	private BufferedImage imageFull=null,imageDarker=null,imageSelect=null,imageMove=null;
	
	private int moveX=0,moveY=0,pressX=0,pressY=0;
	
	private ToolWindow toolWindow=null;
	
	private volatile int repaintType=0;
	
	private Dimension topLeft=null,bottomEnd=null;
	private int gridSize=0,boardSize=0;
	
	private ShootCallback callback=null;
	private ToolWindow.ItemCallback itemCallback=null;
	
	private boolean captured=false;
	
	private volatile boolean unfiltered=true; 
	private boolean dispCursor=true;
	
	private volatile int subX=0,subY=0,subWidth=0,subHeight=0;
	
	private volatile boolean parseState=false;
	
	public ScreenShootWindow() throws AWTException {
		//获取屏幕尺寸,设置窗口大小全屏
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		screenWidth=dim.width;
		screenHeight=dim.height;
		dim=null;
		
		this.setBounds(0, 0, screenWidth, screenWidth);
		
		imageDarker = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		imageMove=new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
		
		//创建当前屏幕全尺寸截图
		init();
		
		//创建一个空图片(尺寸(0,0)无描述文字)的鼠标指针,用于在截屏时隐藏鼠标指针
		Cursor cursor=Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""),
				new Point(0,0),"");
		
		this.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if(captured) {
					return;
				}
				
				moveX = e.getX();
				moveY = e.getY();
				
				Graphics2D g2d=imageMove.createGraphics();
				g2d.drawImage(imageDarker, 0, 0, null);
				
				subX = pressX<moveX ? pressX : moveX;
				subY = pressY<moveY ? pressY : moveY;
				subWidth = Math.abs(moveX - pressX)+1;
				subHeight = Math.abs(moveY - pressY)+1;
				
				g2d.setColor(Color.BLUE);
				g2d.drawRect(subX-1, subY-1, subWidth+1, subHeight+1);
				
				g2d.drawImage(imageFull.getSubimage(subX, subY, subWidth, subHeight), subX, subY, null);
				
				g2d.setColor(Color.RED);
		        g2d.drawLine(moveX-10, moveY+1, moveX+10, moveY+1);
		        g2d.drawLine(moveX+1, moveY-10, moveX+1, moveY+10);
				
				g2d.dispose();
				g2d=null;
				
				ScreenShootWindow.this.getGraphics().drawImage(imageMove,0,0,ScreenShootWindow.this);
			}
						
			@Override
			public void mouseMoved(MouseEvent e) {
				if(captured) {
					return;
				}
				
				moveX = e.getX();
				moveY = e.getY();
				
				if(dispCursor) {
					dispCursor=false;
					setCursor(cursor);
				}
				
				Graphics2D g2d = imageMove.createGraphics();
				g2d.drawImage(imageDarker, 0, 0, null);
				
				g2d.setColor(Color.RED);
		        g2d.drawLine(moveX-10, moveY, moveX+10, moveY);
		        g2d.drawLine(moveX, moveY-10, moveX, moveY+10);
		      
		    	g2d.setColor(Color.BLUE);
		        g2d.drawLine(0,moveY,(moveX-10),moveY);
		        g2d.drawLine((moveX+10),moveY,screenWidth,moveY);
		        g2d.drawLine(moveX,0,moveX,(moveY-10));
		        g2d.drawLine(moveX,(moveY+10),moveX,screenHeight);
		        
		        g2d.dispose();
		        g2d=null;
		        
				ScreenShootWindow.this.getGraphics().drawImage(imageMove,0,0,ScreenShootWindow.this);
			}
		});
		
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON3) {
					//鼠标右键按下关闭窗口
					if(toolWindow!=null && toolWindow.isVisible()) {
						toolWindow.dispose();
					}
					ScreenShootWindow.this.dispose();
				}else if(e.getButton()==MouseEvent.BUTTON1) {
					if(!captured) {
						//鼠标左键按下记录初始点
						pressX=e.getX();
						pressY=e.getY();
					}
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1) {
					if(!captured) {
						
						captured=true;
						
						if(!dispCursor) {
							//恢复鼠标指针
							dispCursor=true;
							setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						}
						
						subX = pressX<moveX ? pressX : moveX;
						subY = pressY<moveY ? pressY : moveY;
						subWidth = Math.abs(moveX - pressX)+1;
						subHeight = Math.abs(moveY - pressY)+1;
						
						if(itemCallback==null) {
							itemCallback=new ToolWindow.ItemCallback() {
								@Override
								public void onClicked(int id) {
									switch (id) {
									case 0:
										if(unfiltered) {
											imageSelect=null;
											
											imageSelect=binaryImage(imageFull.getSubimage(subX, subY, subWidth, subHeight));
											repaintType=1;
											repaint();
											
											parseState=parseImage(imageSelect);
											repaint();

											unfiltered=false;
										}else {
											if(callback!=null) {
												if(parseState) {
													callback.onScreenShotted(boardSize, gridSize,
															new Point(pressX, pressY),
															new Point(moveX, moveY), 
															topLeft, bottomEnd);
												}else {
													callback.onParseError();
												}
											}
											toolWindow.dispose();
											ScreenShootWindow.this.dispose();
										}
										break;
									case 1:
										unfiltered=true;
										captured=false;
										dispCursor=true;
										toolWindow.init();
										toolWindow.setVisible(false);
										break;
									default:
										//标记变量初始化在init()中完成
										toolWindow.dispose();
										ScreenShootWindow.this.dispose();
										break;
									}
								}
							};
						}
						
						if(toolWindow==null) {
							toolWindow=new ToolWindow();
							toolWindow.setItemCallback(itemCallback);
						}
						
						toolWindow.setVisible(true);
						toolWindow.init();
						
						if((e.getX()+1+ToolWindow.WINDOW_WIDTH)>screenWidth) {
							toolWindow.setPosition(e.getX()-ToolWindow.WINDOW_WIDTH+1, e.getY()+2);
						}else {
							toolWindow.setPosition(e.getX()+1, e.getY()+1);
						}
					}
				}
			}
		});
	}
	
	private BufferedImage binaryImage(BufferedImage raw) {
		
		int width=0,height=0,length=0;
		int i=-1,r=0,g=0,b=0;
		
		width=raw.getWidth();
		height=raw.getHeight();
		length=width*height;
		
		int[] rgbArray=new int[length];
		
		BufferedImage image=new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		raw.getRGB(0, 0, width, height, rgbArray, 0, width);
		
		while(++i<length) {
			r=(rgbArray[i] >> 16) & 0xFF;
			g=(rgbArray[i] >> 8) & 0xFF;
			b=rgbArray[i] & 0xFF;
		  	if(r < Const.LIMIT_RED &&  g < Const.LIMIT_GREEN && b < Const.LIMIT_BLUE) {
		  		rgbArray[i]=0xFF000000;
		  	}else {
		  		rgbArray[i]=0xFFFFFFFF;
		  	}
		}
		
		image.setRGB(0, 0, width,height,rgbArray,0,width);
		rgbArray=null;
		return image;
	}
	
	private boolean parseImage(BufferedImage raw) {
		int width=raw.getWidth();
		int height=raw.getHeight();
		
		int startX=0,startY=0;
		int endX=0,endY=0;
		
		//offset 测算网格数量时的垂直偏移量
		//limit 测算左上及右下定点位置时要求x轴黑线连续长度(当前为1/4图片宽度)
		int yOffset=25,limit=(width/4);
		int linearCount=0;		
		
		//查找左上顶点位置
		top_outer:
		for(int i=0;i<height;++i) {
			for(int j=0;j<width;++j) {
				if(raw.getRGB(j, i)==0xFF000000) {
					if(linearCount==0) {
						startX=j;
						startY=i;
					}
					if(linearCount>limit) {
						linearCount=0;
						break top_outer;
					}
					linearCount++;
				}
			}
			linearCount=0;
		}
		//查找右下底点位置
		bottom_outer:
		for(int i=height-1;i>0;--i) {
			for(int j=width-1;j>0;--j) {
				if(raw.getRGB(j, i)==0xFF000000) {
					if(linearCount==0) {
						endX=j;
						endY=i;
					}
					if(linearCount>limit) {
						break bottom_outer;
					}
					linearCount++;
				}
			}
			linearCount=0;
		}
		
		Graphics2D g2d = raw.createGraphics();
		g2d.setColor(Color.BLUE);
		//测量网格数量
		boolean space=true;
		boardSize=0;
		try {
			for(int i=endX;i>0;--i) {
				if(raw.getRGB(i, (endY-yOffset))==0xFF000000) {
					if(space) {
						boardSize++;
						g2d.fillOval(i-5, (endY-yOffset), 10, 10);
						space=false;
					}
				}else{
					space=true;
					if(boardSize==1) {
						gridSize++;
					}
				}
			}
		}catch (ArrayIndexOutOfBoundsException e) {
			dispError(g2d,width,height);
			return false;
		}
		
		//棋盘大小小于限制值时认为图片源异常
		if(boardSize<Const.BOARD_LIMIT) {
			dispError(g2d,width,height);
			return false;
		}
		
		//based on pressX and pressY
		if(topLeft==null) {
			topLeft=new Dimension(startX, startY);
		}else {
			topLeft.width=startX;
			topLeft.height=startY;
		}
		
		if(bottomEnd==null) {
			bottomEnd=new Dimension(endX, endY);
		}else {
			bottomEnd.width=endX;
			bottomEnd.height=endY;
		}
		
		g2d.setColor(Color.RED);
		//绘制左上标记
		g2d.fillRect(startX-5, startY-5, 10, 10);
		//绘制右下标记
		g2d.fillRect(endX-5, endY-5, 10, 10);
		
		g2d.setColor(Color.BLUE);
		
		int centerX=width/2,centerY=height/2;
		int current=0;
		
		//绘制边框线
		g2d.drawRect(centerX-71, centerY-51, 141, 101);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(centerX-70, centerY-50, 140, 100);
		
		
		g2d.setColor(Color.BLUE);
		Dimension dim=Tools.getStringSize("Analytical Results");
		current=(centerY-50+dim.height);
		g2d.drawString("Analytical Results",(centerX-(dim.width/2)),current);
		current+=5;
		g2d.drawLine(centerX-70, (centerY-50+dim.height+5), centerX+70, current);
		dim=null;
		
		g2d.setColor(Color.BLACK );
		centerX-=65;
		current=dispText(g2d, "sel_size:("+width+"px*"+height+"px)", centerX, current);
		current=dispText(g2d, "board:("+boardSize+"*"+boardSize+")", centerX, current);
		current=dispText(g2d, "grid:("+gridSize+"px*"+gridSize+"px)", centerX, current);
		current=dispText(g2d, "top_left:("+(startX+pressX)+","+(startY+pressY)+")", centerX, current);
		current=dispText(g2d, "bottom_end:("+(pressX+endX)+","+(pressY+endY)+")", centerX, current);
			
		g2d.dispose();
		g2d=null;
		
		return true;
	}
	
	public void init() {
		imageFull=null;
		repaintType=0;
		captured=false;
		unfiltered=true;
		dispCursor=true;
		try {
			//创建当前屏幕全尺寸截图
			Robot robot = new Robot();
			imageFull = robot.createScreenCapture(new Rectangle(0, 0, screenWidth,screenHeight));
		} catch (AWTException e) {
			e.printStackTrace();
		}
		repaint();
	}
	
	@Override
	public void paint(Graphics graphics) {
		if(repaintType==0) {
			RescaleOp ro = new RescaleOp(0.8f, 0, null);
			ro.filter(imageFull, imageDarker);
			graphics.drawImage(imageDarker, 0, 0, this);	
		}else if(repaintType==1) {
			graphics.drawImage(imageSelect, pressX, pressY, this);	
		}
	}
	
	private int dispText(Graphics2D g2d,String text,int x,int y) {
		Dimension dim=Tools.getStringSize(text);
		y+=dim.height;
		g2d.drawString(text,x,y);
		dim=null;
		text=null;
		return y;
	}
	
	private void dispError(Graphics2D g2d,int parentWidth,int parentHeight) {
		
		int centerX=parentWidth/2,centerY=parentHeight/2;
		int[] xPoints=new int[] {centerX,centerX-18,centerX+18};
		int[] yPoints=new int[] {centerY-35,centerY-6,centerY-6};
		
		g2d.setColor(Color.RED);
		//绘制边框线
		g2d.drawRect(centerX-71, centerY-51, 141, 101);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(centerX-70, centerY-50, 140, 100);
		g2d.setColor(Color.BLACK );
		g2d.setColor(Color.RED);
		//绘制警示符号
		g2d.fillPolygon(xPoints, yPoints, 3);
		g2d.setColor(Color.WHITE);
		g2d.fillOval(centerX-3, centerY-30, 5, 15);
		g2d.fillOval(centerX-3, centerY-14, 5, 5);
		g2d.setColor(Color.RED);
		g2d.drawString("PARSEING ERROR",(centerX-52),centerY+15);
		g2d.dispose();
		g2d=null;
	}
	

	public interface ShootCallback{
		//棋盘尺寸,网格大小,初始截图点,结束截图点,左上边距(基于pressed),右下边距(基于pressed)
		void onScreenShotted(int boardSize,int gridSize,Point pressed,Point moved,Dimension start,Dimension end);
		void onParseError();
	}
	
	public void setShotCallback(ShootCallback callback) {
		this.callback=callback;
	}
	
	public void close() {
		itemCallback=null;
		callback=null;
		topLeft=null;
		bottomEnd=null;
		
		toolWindow.close();
		toolWindow=null;
		
		imageFull=null;
		imageDarker=null;
		imageSelect=null;
		imageMove=null;
	}
}
