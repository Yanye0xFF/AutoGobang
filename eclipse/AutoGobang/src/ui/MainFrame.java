package ui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.Thread.State;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import core.WineLoader;
import support.Const;
import support.PlayThread;
import widget.CapButton;
import widget.CleanButton;
import widget.HintView;
import widget.QImageView;
import widget.StartButton;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int WINDOW_WIDTH=320;
	private static final int WINDOW_HEIGHT=480;
	private static final String APP_NAME="AutoGobang";
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private CapButton captureBtn=null;
	private StartButton startBtn=null;
	private HintView hintView=null;
	private CleanButton cleanButton=null;
	private QImageView display=null;
	private JTextArea textArea=null;
	
	private ButtonListener listener=null;
	
	private ui.ScreenShootWindow shootWindow=null;
	private ui.ScreenShootWindow.ShootCallback shootCallback=null;
	
	private ui.HandWindow handWindow=null;
	private ui.HandWindow.HandCallback handCallback=null;
	
	private BufferedImage imageDisplay=null;

	private int boardSize=0,gridSize=0;
	private Point pressed=null,moved=null;
	private Dimension start=null,end=null;
	
	//信息显示区域状态,局部刷新
	private static final int TYPE_ERROR=0;
	private static final int TYPE_RESULT=1;
	private static final int TYPE_FIRST=2;
	private int displayType=TYPE_FIRST;
	
	//应用运行状态
	private static final int STATE_IDLE=0;
	private static final int STATE_RUN=1;
	private int runingState=STATE_IDLE;
	
	private WineLoader coreLoader=null;
	
	private PlayThread playThread=null;
	private PlayThread.ProcessCallBack processCallBack=null;
	
	private ExecutorService cacheThreadPool = null;
	
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(APP_NAME);
		
		setResizable(false);
		setAlwaysOnTop(true);
		
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setWindowLocation();
		
		listener=new ButtonListener();
		imageDisplay=new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);
		cacheThreadPool = Executors.newCachedThreadPool();
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		display = new QImageView();
		display.setBounds(10, 10, 250, 250);
		contentPane.add(display);
		
		captureBtn = new CapButton();
		captureBtn.setToolTipText("截取区域");
		captureBtn.setBounds(270, 10, 32, 32);
		contentPane.add(captureBtn);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 270, 292, 171);
		contentPane.add(scrollPane);
		
		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		startBtn = new StartButton();
		startBtn.setBounds(270, 52, 32, 32);
		contentPane.add(startBtn);
		
		hintView = new HintView();
		hintView.setBounds(270, 136, 32, 32);
		contentPane.add(hintView);
		
		cleanButton = new CleanButton();
		cleanButton.setBounds(270, 94, 32, 32);
		contentPane.add(cleanButton);
		
		captureBtn.addActionListener(listener);
		startBtn.addActionListener(listener);
		cleanButton.addActionListener(listener);
		
		//初始化,绘制信息提示板初始信息
		init();
		
		//读取c++库配置文件
		readConfig();
		
		shootCallback=new ScreenShootWindow.ShootCallback() {
			@Override
			public void onScreenShotted(int boardSize, int gridSize, Point pressed, Point moved, Dimension start,
					Dimension end) {
				MainFrame.this.boardSize=boardSize;
				MainFrame.this.gridSize=gridSize;
				MainFrame.this.pressed=pressed;
				MainFrame.this.moved=moved;
				MainFrame.this.start=start;
				MainFrame.this.end=end;
				dispShootData(false);
			}
			
			@Override
			public void onParseError() {
				dispShootData(true);
			}
		};
		
		handCallback=new HandWindow.HandCallback() {
			@Override
			public void onClicked(int id) {
				
				//初始化c++库
				if(coreLoader==null) {
					coreLoader=WineLoader.instance;
					addLog("library load : "+Const.LIBRARY_NAME,true);
				}
				//释放窗口选区工具资源
				if(shootWindow!=null) {
					shootWindow.close();
					shootWindow=null;
				}
				
				if(runingState==STATE_IDLE) {
					runingState=STATE_RUN;
					startBtn.setState(true);
					if(playThread==null || playThread.getState()==State.TERMINATED) {
						playThread=new PlayThread(boardSize, gridSize, pressed,moved, start, end);
						playThread.setProcessCallBack(processCallBack);
						playThread.setProcessCore(coreLoader);
					}
					playThread.setHandType(id);
					cacheThreadPool.execute(playThread);
				}
			}
		};
		
		processCallBack=new PlayThread.ProcessCallBack() {
			@Override
			public void onFinished(String str) {
				runingState=STATE_IDLE;
				startBtn.setState(false);
				addLog(str,true);
			}
			@Override
			public void onLogAdd(String str, boolean newLine) {
				addLog(str,newLine);				
			}
			@Override
			public void onThinkState(boolean think) {
				hintView.setState(think);
			}
		};
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				if(coreLoader!=null) {
					coreLoader.close();
				}
				if(cacheThreadPool!=null) {
					cacheThreadPool.shutdown();
				}
			}
		});
	}
	
	private class ButtonListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if(e.getSource().equals(captureBtn)) {
				
				if(runingState==STATE_RUN) {
					return;
				}
				
				if(handWindow!=null && handWindow.isVisible()) {
					return;
				}
				
				EventQueue.invokeLater(new Runnable() { 
					@Override
					public void run() {
						try {
							if(shootWindow==null) {
								shootWindow=new ui.ScreenShootWindow();
								shootWindow.setShotCallback(shootCallback);
							}
							if(!shootWindow.isVisible()) {
								shootWindow.init();
								shootWindow.setVisible(true);
							}
						} catch (AWTException e) {
							e.printStackTrace();
						}
					}
				});
			}else if(e.getSource().equals(startBtn)) {
				
				//初始化c++库
				if(coreLoader==null) {
					coreLoader=WineLoader.instance;
					addLog("library load : "+Const.LIBRARY_NAME,true);
				}
				
				if(shootWindow!=null && shootWindow.isVisible()) {
					return;
				}
				
				if(runingState==STATE_RUN) {
					runingState=STATE_IDLE;
					startBtn.setState(false);
					playThread.stopRuning();
					return;
				}

				if(displayType==TYPE_RESULT) {
					
					if(handWindow==null) {
						handWindow=new HandWindow();
						handWindow.setHandCallback(handCallback);
					}
					
					if(!handWindow.isVisible()) {
						handWindow.setVisible(true);
						handWindow.init();
						int centerX= MainFrame.this.getX()+WINDOW_WIDTH/2;
						int centerY= MainFrame.this.getY()+WINDOW_HEIGHT/2;
						//handWindow size(200,80),floating the bottom-right of screen
						handWindow.setLocation(centerX-100, centerY-40);
					}
				}else {
					addLog("please select valid board area",true);
				}
			}else if(e.getSource().equals(cleanButton)) {
				textArea.setText("");
			}
		}
	}
	
	private void setWindowLocation() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		setLocation(width-WINDOW_WIDTH, height-WINDOW_HEIGHT-40);
	}
	
	private void init() {		

		Graphics graphics=imageDisplay.getGraphics();
		int width=display.getWidth();
		int height=display.getHeight();
		
		//清除上次信息
		graphics.setColor(Color.WHITE);
		graphics.fillRect(1, 1, width-2, height-2);
		graphics.setColor(Color.BLACK);
		graphics.drawRect(0, 0, width-1, height-1);	
		
		width/=2;
		height/=2;
		graphics.setColor(Color.RED);
		graphics.drawRect(width-100, height-15, 200, 30);	
		graphics.drawString("click cursor to select board area", width-90, height+5);
		
		graphics.dispose();
		graphics=null;
		
		display.setImage(imageDisplay);
	}
		
	private void readConfig() {
		File file=new File(System.getProperty("user.dir")+"\\library\\using.ini");
		if(file.exists()) {
			BufferedReader buffer = null;  
			try {
				buffer = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf-8"));
				String line=buffer.readLine();
				if(line!=null && !line.isEmpty()) {
					Const.LIBRARY_NAME=line.substring(line.indexOf("<")+1, line.indexOf(">"));
				}
				line=null;
				buffer.close();
				buffer=null;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				try {
					if(buffer!=null) {
						buffer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(Const.LIBRARY_NAME!=null && !Const.LIBRARY_NAME.isEmpty()) {
					this.setTitle(APP_NAME+"--"+Const.LIBRARY_NAME);
					addLog("use dll : "+Const.LIBRARY_NAME, true);
					return ;
				}
			}
		}
		this.setTitle(APP_NAME+"--"+Const.DEFAULT_LIBRARY_NAME);
		Const.LIBRARY_NAME=Const.DEFAULT_LIBRARY_NAME;
		addLog("can not find config file \"using.ini\" in library folder", true);
		addLog("use default dll : "+Const.DEFAULT_LIBRARY_NAME, true);
	}
	
	
	private void dispShootData(boolean isError) {
		
		//解析结果是错误状态且当前显示也为错误状态则不再重绘
		if(isError && displayType==TYPE_ERROR) {
			return;
		}
		
		Graphics graphics=imageDisplay.getGraphics();
		int width=display.getWidth();
		int height=display.getHeight();
		final int centerX=250/2,centerY=250/2;
		
		if(isError) {
			displayType=TYPE_ERROR;
			
			final int[] xPoints=new int[] {centerX,centerX-18,centerX+18};
			final int[] yPoints=new int[] {centerY-35,centerY-6,centerY-6};
			
			//清除上次信息
			graphics.setColor(Color.WHITE);
			graphics.fillRect(1, 1, width-2, height-2);

			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, width-1, height-1);	
			
			graphics.setColor(Color.RED);
			//绘制边框线
			graphics.drawRect(centerX-71, centerY-51, 141, 101);

			graphics.setColor(Color.RED);
			//绘制警示符号
			graphics.fillPolygon(xPoints, yPoints, 3);
			graphics.setColor(Color.WHITE);
			graphics.fillOval(centerX-3, centerY-30, 5, 15);
			graphics.fillOval(centerX-3, centerY-14, 5, 5);
			graphics.setColor(Color.RED);
			graphics.drawString("PARSEING ERROR",(centerX-52),centerY+15);
			
		}else {
			if(displayType==TYPE_ERROR || displayType==TYPE_FIRST) {
				displayType=TYPE_RESULT;
				
				graphics.setColor(Color.WHITE);
				graphics.fillRect(1, 1, width-2, height-2);
				graphics.setColor(Color.BLACK);
				graphics.drawRect(0, 0, width-1, height-1);
				graphics.setColor(Color.BLUE);
				graphics.drawRect(centerX-101, centerY-61, 201, 121);
				graphics.drawLine(centerX-100, centerY-30, centerX+100, centerY-30);
				graphics.drawString("Analytical Results", centerX-50, centerY-40);
			}else {
				graphics.setColor(Color.WHITE);
				graphics.fillRect(centerX-99, centerY-29, 198, 89);
			}
			
			graphics.setColor(Color.BLACK);
			graphics.drawString("board:("+boardSize+" * "+boardSize+") grid:("+gridSize+"px * "+gridSize+"px)", centerX-90, centerY-10);
			graphics.drawString("top_left:("+pressed.x+" , "+pressed.y+")", centerX-90, centerY+5);
			graphics.drawString("bottom_end:("+moved.x+" , "+moved.y+")", centerX-90, centerY+20);
			graphics.drawString("padding_start:("+start.width+"px , "+start.height+"px)", centerX-90, centerY+35);
			graphics.drawString("padding_end:("+end.width+"px , "+end.height+"px)", centerX-90, centerY+50);
		}
		display.setImage(imageDisplay);
		graphics.dispose();
		graphics=null;
	}
	
	private void addLog(String log,boolean newLine) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(newLine) {
					textArea.append(log+"\n");	
				}else {
					textArea.append(log);	
				}
										
			}
		});	
	}
}
