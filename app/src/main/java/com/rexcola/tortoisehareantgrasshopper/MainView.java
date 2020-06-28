package com.rexcola.tortoisehareantgrasshopper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
class MainView extends SurfaceView implements SurfaceHolder.Callback {
    class SortingChaosThread extends Thread {

    	private Paint backgroundPaint;
    	private Paint foregroundPaint;
        public SortingChaosThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;

        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
        	doReset();
        	backgroundPaint = new Paint();
        	foregroundPaint = new Paint();
        		
        }
		private Integer[] winCount = new Integer[4];
        private int ties = 0;
		private Integer[] alg = new Integer[2];
		private Double[] score = new Double[2];

        public void doReset()
        {
        	synchronized (syncObject)
        	{
        		for (int i = 0; i < 4; i++)
				{
					winCount[i] = 0;
				}
        		resetRace();
        	}
        	
        }


        private double stepScore(int alg, int player, Integer[] die)
		{
			double jump = 0;

			switch(alg)
			{
				case 0:
					jump = die[player];
					break;
				case 1:
					if (die[0] == die[1]) {
						jump = 21;
					}
					break;
				case 2:
					jump = (die[0] + die[1]) / 2.0;
					break;
				case 3:
					if (Math.abs(die[0]-die[1]) < 2){
						jump = 7.925;
					}
					break;
			}
			return jump;
		}
        private void doStep(Canvas c)
		{
			if ((score[0] > 500) || (score[1] > 500))
			{
				if (Math.abs(score[0] - score[1]) > 10.0) {
					if (score[0] > score[1]) {
						winCount[alg[0]]++;
					} else {
						winCount[alg[1]]++;
					}
				}else {
					ties++;
				}
				resetRace();
			}
			Integer[] die = new Integer[2];
			die[0] = myRandomizer.nextInt(6) + 1;
			die[1] = myRandomizer.nextInt(6) + 1;
			score[0] += stepScore(alg[0], 0, die);
			score[1] += stepScore(alg[1], 1, die);

		}
        private void resetRace()
		{
			myRandomizer = new Random();

			alg[0] = myRandomizer.nextInt(4);
			alg[1] = myRandomizer.nextInt(4);
			score[0] = 0.0;
			score[1] = 0.0;
		}
        private void randomizeArray()
        {
            values = new Integer[mCanvasWidth];
            for (int i= 0; i < mCanvasWidth; i++)
            {
            	values[i] = myRandomizer.nextInt(mCanvasHeight);
            }     
            linearThreshold = myRandomizer.nextInt(30)+5;
        }
        private void randomizeColors()
        {
        	backgroundPaint = new Paint();
        	foregroundPaint = new Paint();
        	backgroundPaint.setColor(Color.rgb(myRandomizer.nextInt(256),myRandomizer.nextInt(256),myRandomizer.nextInt(256)));
        	foregroundPaint.setColor(Color.rgb(myRandomizer.nextInt(256),myRandomizer.nextInt(256),myRandomizer.nextInt(256)));
        }
        @Override
        public void run() {
            int count = 0;
        	doStart();

            while (mRun) {
                Canvas c = null;
                try {
                    //c = mSurfaceHolder.lockCanvas();
                    synchronized (syncObject) {

                    	if (justReset)
                    	{
                    		// Don't understand why, but on my phone it is crucial to do a bit of drawing
                    		// first or else the first background doesn't work
                    		if (firstTime)
                    		{
                    			c = null;
                    			drawValue(0,c);
                    			firstTime = false;
                    		}

                    		c = mSurfaceHolder.lockCanvas();
                    		Rect littleRect = new Rect(0,0,mCanvasWidth,mCanvasHeight);
                    		Paint tmpPaint = new Paint();
                    		tmpPaint.setColor(Color.BLACK);
                    		c.drawRect(littleRect,tmpPaint);


                        	for (int i = 0; i < mCanvasWidth; i++)
                        	{
                        		drawValue(i,c);
                        	}
                    		justReset = false;
                    		if (c!= null)
                    		{
                    			mSurfaceHolder.unlockCanvasAndPost(c);
                    			c = null;
                    		}
                    	}                    	
                    	doStep(c);
                    	if (count++ > 1000000)
                        {
                            count = 0;
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }



        public void setRunning(boolean b) {
            mRun = b;
        }


        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (syncObject) {
                mCanvasWidth = width;
//                if (mCanvasWidth > 400)
//                {
//                	mCanvasWidth = 400;
//                }
                mCanvasHeight = height;
                resetRace();
            }
        }

//        void doTouch(MotionEvent motion)
//        {
//            synchronized (syncObject) {
//                	mX = motion.getX(motion.getPointerCount()-1);
//                	mY = motion.getY(motion.getPointerCount()-1);
//            }
//        	
//        }


        private void swapValues(int pos1, int pos2, Canvas c)
        {
        	Integer temp;
        	temp = values[pos1];
        	values[pos1] = values[pos2];
        	values[pos2] = temp;
        	
        	drawValue(pos1,c);
        	drawValue(pos2, c);

        }
        private void drawValue(int pos1, Canvas c)
        {
        	boolean madeOwnCanvas = (c == null);
        	Canvas useCanvas;
        	
        	if (madeOwnCanvas)
        	{
        		Rect littleRect = new Rect(pos1,0,pos1+1,mCanvasHeight);
        		useCanvas = mSurfaceHolder.lockCanvas(littleRect);
        		if (useCanvas != null)
        		{
        			useCanvas.drawRect(littleRect,backgroundPaint);
        		}
        	}
        	else
        	{
        		useCanvas = c;
        	}


        	if (useCanvas != null)
        	{
        		int i1 = mCanvasHeight/2;
        		int i2 = values[pos1]/2;

        		useCanvas.drawLine(pos1,  values[pos1], pos1, 0,foregroundPaint);
        		useCanvas.drawLine(pos1,  mCanvasHeight, pos1, values[pos1],backgroundPaint);
//        		useCanvas.drawLine(pos1,  i1, pos1, 0,foregroundPaint);
//        		useCanvas.drawLine(pos1,  i1, pos1, i2,backgroundPaint);
        	}

        	if (madeOwnCanvas && useCanvas != null)
        	{
        		mSurfaceHolder.unlockCanvasAndPost(useCanvas);
        	}
        }
        
        private void singleSortStep(Canvas c)
        {
        	if (highwater > 1)
        	{
        		if (curPos == highwater)
        		{
        			curPos = 0;
        			highwater--;
        		}
        		else
        		{
        			if (values[curPos] > values [curPos+1])
        			{
        				swapValues(curPos,curPos+1, c);        				
        			}
        			curPos++;
        		}
        	}
        }
      
        
        public class SortRangeSizeComparator implements Comparator<sortRange>
        {
        	public int compare(sortRange range1, sortRange range2)
        	{
        		if ( range1.size > range2.size)
        		{
        			return -1;
        		}
        		else
        			
        		{
        			return 1;
        		}
        	}
        }
        public class SortRangePositionComparator implements Comparator<sortRange>
        {
        	public int compare(sortRange range1, sortRange range2)
        	{
        		if ( range1.start < range2.start)
        		{
        			return -1;
        		}
        		else
        			
        		{
        			return 1;
        		}
        	}
        }
        private class sortRange{
        	public int start;
        	public int end;
        	public int size;
        	sortRange(int inStart, int inEnd)
        	{
        		start = inStart;
        		end = inEnd;
        		size = end - start;
        	}
        }
        private List<sortRange> sortList ;
        private PriorityQueue<sortRange> sortQueue;
        private PriorityQueue<sortRange> smallSortQueue;
        private boolean doingPartition;
        private int pivot;
        private int low;
        private int partitionLow;
        private int partitionHigh;
        private int high;
        int linearThreshold;
        private void resetQuickSort()
        {
        	Comparator<sortRange> rangeComparator = new SortRangeSizeComparator();
        	Comparator<sortRange> positionComparator = new SortRangePositionComparator();
            sortQueue = new PriorityQueue<sortRange>(10, rangeComparator);
            smallSortQueue = new PriorityQueue<sortRange>(10, positionComparator);
            sortList = new ArrayList<sortRange>();
                
            sortQueue.add(new sortRange(0,mCanvasWidth-1));
            doingPartition = false;
        	
        }
        private void quickSortPriority(Canvas c)
        {
        	if (doingPartition)
        	{
                low++;
                while ( low< partitionHigh && values[low] < pivot)
                    low++;
                high--;
                while (high>partitionLow && values[high] > pivot)
                	high--;

                if (low < high)
                {
                    swapValues(low, high, c);
                }
                else
                {
            		addToQueue(partitionLow,high);
            		addToQueue(high + 1,partitionHigh);
            		doingPartition = false;
                }

        	}
        	else
        	{
        		if (sortQueue.isEmpty() && smallSortQueue.isEmpty())
        		{
        			resetRace();
        		}
        		else
        		{
        			sortRange thisRange;
        			if (!sortQueue.isEmpty())
        			{
        				thisRange = sortQueue.remove();
        			}
        			else
        			{
        				thisRange = smallSortQueue.remove();
        			}
        			startPartition(thisRange.start,thisRange.end);

        		}
        	}

            
        }
        private void startPartition(int p, int r)
        {
        	pivot = values[p];
        	partitionLow = p;
        	partitionHigh = r;
        	low = p-1;
        	high = r+1;
        	doingPartition = true;
        }
        private void quickSortQueue(int p, int r, Canvas c)
        {
        	if (p<r)
        	{
        		int q = partition(p,r,c);
        		addToQueue(p,q);
        		addToQueue(q+1,r);
        	}
        }
        
        private void addToQueue(int i, int j)
        {
        	if (i < j)
        	{
        		if (j-i < linearThreshold)
        		{
        			smallSortQueue.add(new sortRange(i,j));
        		}
        		else
        		{
        			sortQueue.add(new sortRange(i,j));
        		}
        	}
        }

        private void quickSortRandom(int p, int r, Canvas c)
        {
        	sortList = new ArrayList<sortRange>();
        	
        	sortList.add(new sortRange(p,r));
        	while (!sortList.isEmpty())
        	{
        		int item = myRandomizer.nextInt(sortList.size());
        		sortRange thisRange = sortList.get(item);
        		sortList.remove(item);
        		quickSortRange(thisRange.start,thisRange.end, c);
        		
        	}
        	
        }
        private void quickSortRange(int p, int r, Canvas c)
        {
        	if (p<r)
        	{
        		int q = partition(p,r,c);
        		sortList.add(new sortRange(p,q));
        		sortList.add(new sortRange(q+1,r));
        		
        	}
        }
        private void quickSort(int p, int r, Canvas c)
        {
            if(p<r)
            {
                int q=partition(p,r, c);
                //if ( (q-p) > (r - q))
                if (myRandomizer.nextInt(1000) > 500)
                //if (p > (mCanvasWidth -r))
                //if (q > mCanvasWidth/2)
                {
                	quickSort(p,q, c);
                	quickSort(q+1,r, c);
                }
                else
                {
                	quickSort(q+1,r, c);
                	quickSort(p,q, c);
                }
            }
        }
        
        private void quickSort2(int startPos, int endPos, Canvas c)
        {
        	if (startPos >= endPos)
        	{
        		return;
        	}
        	
        	int pos1 = startPos;
        	int pos2 = endPos;
        	
        	while (pos1 != pos2)
        	{
        		if (values[pos1] >= values[pos2])
        		{
        			swapValues(pos1,pos2,c);
        			pos1++;
        		}
        		else
        		{
        			pos2--;
        		}
        		
        	}
        	quickSort2(startPos, pos1 - 1,c);
        	quickSort2(pos1 + 1, endPos, c);
        }
        
        private int partition(int p, int r, Canvas c) {

            int x = values[p];
            int i = p-1 ;
            int j = r+1 ;

            while (true) {
                i++;
                while ( i< r && values[i] < x)
                    i++;
                j--;
                while (j>p && values[j] > x)
                    j--;

                if (i < j)
                    swapValues(i, j, c);
                else
                    return j;
            }
        }
    }

            
    private Random myRandomizer;
    private Integer[] values;
    private int highwater;
    private int curPos;
    boolean justReset;
    boolean firstTime = true;

    /** The thread that actually draws the animation */
    private SortingChaosThread thread;

    private Context context;
    
    public MainView(Context inContext, AttributeSet attrs) {
        super(inContext, attrs);
    	context = inContext;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public SortingChaosThread getThread() {
        return thread;
    }

    public boolean onTouchEvent(MotionEvent motion)
    {
    	return true;
    }


//    /**
//     * Installs a pointer to the text view used for messages.
//     */
//    public void setTextView(TextView textView) {
//        statusText = textView;
//    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {

        thread = new SortingChaosThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
            }
        });
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    


    /*
     * Member (state) fields
     */

    private int mCanvasHeight = 1;
    private int mCanvasWidth = 1;
    private static Handler mHandler;
    private boolean mRun = false;
    private SurfaceHolder mSurfaceHolder;
    
    private Integer syncObject = 1;


    

}
