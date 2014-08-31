package com.example.exercise101;

import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;


public class RackView  extends BaseGameView 
{
	protected SurfaceHolder holder;
	protected GameLoopThread gameLoopThread;
	public Paint textPaint;
	Rect drawRect = new Rect(0, 0, 0, 0);

	public RackView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		initView();
	}
	
	public RackView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		initView();
	}
	
	public RackView(Context context) 
	{
		super(context);
		initView();
	}
	
	public void initView()
	{
		super.initView();
		textPaint = new Paint();
		textPaint.setColor(0xFF000000);
		textPaint.setTextSize(50);
	}
	
	protected void onDraw(Canvas canvas) 
	{
		if(!this.getGameLoopThread().isRunning())
			return;
		int w = GameBoardManager.NUM_OF_MAX_TILES_IN_RACK;
		int size = canvas.getWidth();
		int scaleW = (int)(size / w);
		int scaleH = (int)(canvas.getHeight());
		drawRect.top = 0;
		drawRect.bottom = scaleH;
		drawRect.left = 0;
		drawRect.right = scaleW;
		for(int col = 0; col < w; col++)
		{
			char c = (char) (col  + 'a');
			if(c != ScrabbleSolver.NULL_CHAR)
			{
				canvas.drawBitmap(GameBoardManager.BLANK_TILE_BITMAP, null, drawRect, null);
				//canvas.drawText((c + "").toUpperCase(), drawRect.left + LETTER_OFFSET_X, drawRect.top + scaleH + LETTER_OFFSET_Y, textPaint);
			}
			drawRect.left += scaleW;
			drawRect.right += scaleW;
		}
	}
}
