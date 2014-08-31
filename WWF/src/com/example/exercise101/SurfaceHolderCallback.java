package com.example.exercise101;

import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;

public class SurfaceHolderCallback implements Callback
{
	protected BaseGameView gameView;
	
	public SurfaceHolderCallback(BaseGameView _gameView)
	{
		this.gameView = _gameView;
	}
	
	 @Override
     public void surfaceDestroyed(SurfaceHolder holder) 
     {
            boolean retry = true;
            this.gameView.getGameLoopThread().setRunning(false);
            while (retry) 
            {
                   try 
                   {
                	   this.gameView.getGameLoopThread().join();
                       retry = false;
                   }
                   catch (InterruptedException e)
                   {
                   }
            }
     }

     @Override
     public void surfaceCreated(SurfaceHolder holder) 
     {
    	 this.gameView.createNewThreadAndStart();
     }

     @Override
     public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
     {
     }
}
