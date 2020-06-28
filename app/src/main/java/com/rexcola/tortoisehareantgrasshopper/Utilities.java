package com.rexcola.tortoisehareantgrasshopper;


import java.util.Random;

import android.util.Log;
import android.view.MotionEvent;

public class Utilities {

	public static void printSamples(MotionEvent ev) {
	     final int historySize = ev.getHistorySize();
	     final int pointerCount = ev.getPointerCount();
	     for (int h = 0; h < historySize; h++) {
	    	 RexLog(String.format("At time %d:", ev.getHistoricalEventTime(h)));
	         for (int p = 0; p < pointerCount; p++) {
	        	 RexLog(String.format("  pointer %d: (%f,%f)",
	                 ev.getPointerId(p), ev.getHistoricalX(p, h), ev.getHistoricalY(p, h)));
	         }
	     }
	     RexLog(String.format("At time %d:", ev.getEventTime()));
	     for (int p = 0; p < pointerCount; p++) {
	    	 RexLog(String.format("  pointer %d: (%f,%f)",
	             ev.getPointerId(p), ev.getX(p), ev.getY(p)));
	     }
	 }
	
	public static void RexLog(String s)
	{
		Log.d("RexCola",s);
	}
	
	public static int distance (Coords c1, Coords c2)
	{
		return Math.abs(c1.x-c2.x) + Math.abs(c1.y - c2.y);
	}
	
	public static class Coords
	{
		public Coords(int inX, int inY)
		{
			x = inX;
			y = inY;
		};
		public int x;
		public int y;
	}
	
	public static float randomFloat(float low, float high)
	{
		return low + (new Random()).nextFloat() * (high-low);
	}
	
	public static boolean randomBoolean()
	{
		return (new Random()).nextBoolean();
	}
}
