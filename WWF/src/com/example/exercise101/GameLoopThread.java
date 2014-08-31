package com.example.exercise101;

import android.graphics.Canvas;

public class GameLoopThread extends Thread 
{
       private BaseGameView view;
       private boolean running = false;
      
       public GameLoopThread(BaseGameView view) 
       {
    	   this.view = view;
       }
 
       public void setRunning(boolean run)
       {
    	   running = run;
       }
       
       public boolean isRunning()
       {
    	   return running;
       }
 
       @Override
       public void run() 
       {
             while (running) 
             {
                    Canvas c = null;
                    try
                    {
                           c = view.getHolder().lockCanvas();
                           synchronized (view.getHolder()) 
                           {
                                  view.onDraw(c);
                           }
                    } 
                    finally
                    {
                           if (c != null)
                           {
                                  view.getHolder().unlockCanvasAndPost(c);
                           }
                    }
             }
       }
}  