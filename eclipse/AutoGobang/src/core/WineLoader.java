package core;

import com.sun.jna.Library;
import com.sun.jna.Native;

import support.Const;

public interface WineLoader extends Library{
	
	WineLoader instance=(WineLoader)Native.loadLibrary(System.getProperty("user.dir")+"\\library\\"+Const.LIBRARY_NAME, WineLoader.class);

	public void SomeFunction(String args);
	public void init(int mapSize);
	public void restart();
	public void takeBack();
	public int begin();
	public int getBestX();
	public int getBestY();
	public int turn(int x,int y);
	public int setTimeoutTurn(int value);
	public int setTimeoutMatch(int value);
	public void close();
}
