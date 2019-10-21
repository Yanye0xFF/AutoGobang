package support;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

public class Tools {
	
	public static Dimension getStringSize(String text) {
		FontRenderContext renderContext = new FontRenderContext(null,
				false, false);
		Font font=new Font("SimSun", Font.PLAIN, 12);
		Rectangle2D bounds = font.getStringBounds(text, renderContext);
		int width = (int) bounds.getWidth();
		//padding 2px
		int height = (int) bounds.getHeight()+2;
		font=null;
		return new Dimension(width, height);
	}
}
