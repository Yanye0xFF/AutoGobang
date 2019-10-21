package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class ToolWindow extends JWindow {

	private static final long serialVersionUID = 1L;
	public static final int WINDOW_WIDTH=126;
	public static final int WINDOW_HEIGHT=34;
	private LabelMouseListener listener=null;
	private ItemCallback callback=null;
	private JLabel[] lbItems=new JLabel[3];
	private int lastClicked=-1;
	
	public ToolWindow() {
		
		setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		Font font=new Font("宋体", Font.PLAIN, 12);
		
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		listener=new LabelMouseListener();
		
		lbItems[0] = new JLabel("识别");
		lbItems[0].setBounds(10, 10, 29, 15);
		lbItems[0].setFont(font);
		contentPane.add(lbItems[0]);
		
		lbItems[1] = new JLabel("重试");
		lbItems[1].setFont(font);
		lbItems[1].setBounds(49, 10, 29, 15);
		contentPane.add(lbItems[1]);
		
		lbItems[2] = new JLabel("退出");
		lbItems[2].setFont(font);
		lbItems[2].setBounds(88, 10, 29, 15);
		contentPane.add(lbItems[2]);
		
		lbItems[0].addMouseListener(listener);
		lbItems[1].addMouseListener(listener);
		lbItems[2].addMouseListener(listener);	
	}
	
	public void setPosition(int x,int y) {
		this.setLocation(x, y);
	}
	
	class LabelMouseListener extends MouseAdapter{
		@Override
		public void mouseEntered(MouseEvent e) {
			JLabel label=(JLabel)e.getSource();
			label.setForeground(Color.RED);
			label=null;
		}
		@Override
		public void mouseExited(MouseEvent e) {
			JLabel label=(JLabel)e.getSource();
			label.setForeground(Color.BLACK);
			label=null;
		}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			JLabel label=(JLabel)arg0.getSource();
			for(int i=0;i<lbItems.length;++i) {
				if(label.equals(lbItems[i])) {
					if(i==0) {
						lbItems[i].setText("完成");
					}
					if(callback!=null) {
						lastClicked=i;
						callback.onClicked(i);
						label=null;
						break;
					}
					//未执行回调接口,且点击退出时关闭窗口
					if(i==2) {
						label=null;
						dispose();
					}
				}
			}
		}
	}
	
	public void init() {
		lbItems[0].setText("识别");
		if(lastClicked!=-1) {
			lbItems[lastClicked].setForeground(Color.BLACK);
		}
	}
	
	public interface ItemCallback{
		void onClicked(int id);
	}
	
	public void setItemCallback(ItemCallback callback) {
		this.callback=callback;
	}
	
	public void close() {
		for(int i=0;i<3;++i) {
			lbItems[i].removeMouseListener(listener);
			lbItems[i]=null;
		}
		listener=null;
		callback=null;
	}
}
