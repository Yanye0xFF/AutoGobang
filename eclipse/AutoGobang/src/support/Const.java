package support;

public class Const {
	//网格线颜色过滤范围(小于)
	public static final int LIMIT_RED=220;
	public static final int LIMIT_GREEN=200;
	public static final int LIMIT_BLUE=120;
	
	//白色棋子过滤范围(大于)
	public static final int INVERSE_RED=210;
	public static final int INVERSE_GREEN=200;
	public static final int INVERSE_BLUE=180;
	
	//最小棋盘尺寸
	public static final int BOARD_LIMIT=15;
	
	//边框线宽px
	public static final int BORDER_WIDTH=3;
	//内部网格线宽px
	public static final int LINE_WIDTH=2;
	
	//c++支持库名称,在软件启动时从using.ini配置文件加载
	//格式<sample.dll>
	public static String LIBRARY_NAME=null;
	
	//默认配置文件未找到时启用默认dll
	public static final String DEFAULT_LIBRARY_NAME="ai_psv.dll";
}
