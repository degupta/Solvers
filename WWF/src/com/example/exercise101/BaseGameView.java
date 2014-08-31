package com.example.exercise101;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.util.Log;

public class BaseGameView extends SurfaceView
{
	protected SurfaceHolder holder;
	protected GameLoopThread gameLoopThread;
	
	public BaseGameView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		initView();
	}
	
	public BaseGameView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		initView();
	}
	
	public BaseGameView(Context context) 
	{
		super(context);
		initView();
	}
	
	public void initView()
	{
		holder = getHolder();
		holder.addCallback(new SurfaceHolderCallback(this));
	}
	
	public GameLoopThread getGameLoopThread()
	{
		return this.gameLoopThread;
	}
	
	public void createNewThreadAndStart()
	{
		gameLoopThread = new GameLoopThread(this);
		gameLoopThread.setRunning(true);
		gameLoopThread.start();
	}
	
	protected void onDraw(Canvas canvas) 
	{
		super.onDraw(canvas);
	}
	
	public boolean onTouchEvent(MotionEvent event)
	{
		return super.onTouchEvent(event);
	}
}
