package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

import core.BoardParser;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

public class HandWindow extends JWindow {

	private static final long serialVersionUID = 1L;
	private JLabel lbClose;
	private JRadioButton rdBlack,rdWhite;
	private TagMouseListener listener=null;
	private HandCallback callback=null;
	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		graphics.setColor(Color.BLACK);
		
		graphics.drawRect(0, 0, 199, 79);
		graphics.drawLine(0, 35, 199, 35);
		graphics.dispose();
		graphics=null;
	}

	public HandWindow() {
		
		setSize(200, 80);
		setAlwaysOnTop(true);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);
		
		listener=new TagMouseListener();
		Font font=new Font("宋体", Font.PLAIN, 12);
		
		JLabel lbType = new JLabel("我方棋子类型选择");
		lbType.setBounds(5, 10, 100, 15);
		lbType.setFont(font);
		contentPane.add(lbType);
		
		rdBlack = new JRadioButton("黑子(先手)");
		rdBlack.setBounds(5, 45, 85, 23);
		rdBlack.setFont(font);
		contentPane.add(rdBlack);
		
		rdWhite = new JRadioButton("白子(后手)");
		rdWhite.setBounds(109, 45, 85, 23);
		rdWhite.setFont(font);
		contentPane.add(rdWhite);
		
		ButtonGroup buttonGroup=new ButtonGroup();
		buttonGroup.add(rdBlack);
		buttonGroup.add(rdWhite);
		
		lbClose = new JLabel("关闭");
		lbClose.setHorizontalAlignment(SwingConstants.RIGHT);
		lbClose.setBounds(163, 10, 32, 15);
		lbClose.setFont(font);
		contentPane.add(lbClose);
		
		lbClose.addMouseListener(listener);
		rdBlack.addMouseListener(listener);
		rdWhite.addMouseListener(listener);
	}
	
	class TagMouseListener extends MouseAdapter{
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getSource().equals(rdBlack)) {
				callback.onClicked(BoardParser.TYPE_BLACK);
			}else if(e.getSource().equals(rdWhite)) {
				callback.onClicked(BoardParser.TYPE_WHITE);
			}
			HandWindow.this.dispose();
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			if(e.getSource().equals(lbClose)) {
				lbClose.setForeground(Color.RED);
			}
		}
		@Override
		public void mouseExited(MouseEvent e) {
			if(e.getSource().equals(lbClose)) {
				lbClose.setForeground(Color.BLACK);
			}
		}
	}
	
	public interface HandCallback{
		void onClicked(int id);
	}
	
	public void setHandCallback(HandCallback callback) {
		this.callback=callback;
	}
	
	public void init() {
		lbClose.setForeground(Color.BLACK);
	}
	
	public void close() {
		lbClose.removeMouseListener(listener);
		rdBlack.removeMouseListener(listener);
		rdWhite.removeMouseListener(listener);
		listener=null;
		callback=null;
		lbClose=null;
		rdBlack=null;
		rdWhite=null;
	}
}
