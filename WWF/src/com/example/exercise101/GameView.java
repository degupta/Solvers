package com.example.exercise101;

import com.example.exercise101.scrabbleSolver.DifficultyLevelSelector;
import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.graphics.Matrix;
import android.util.Log;

public class GameView extends BaseGameView 
{
	public final float MIN_DOUBLE_TAP_DIST_SQ = 2500;
	public final long MIN_DOUBLE_TAP_TIME = 10;
	public final long MAX_DOUBLE_TAP_TIME = 300;
	public final float MIN_SCALE = 1.0f;
	public final float MAX_SCALE = 5.0f;
	public final float SCALE_EPS = 0.01f;
	public float TILE_RACK_HEIGHT_PERCENTAGE = 0.0f;
	public final float MOVING_TILE_HALF_SIZE_PERCENTAGE = 0.12f;
	
	public float scale = 1.0f;
	private float posX = 0.0f, posY = 0.0f;
	private float lastDownX = -1.0f, lastDownY = -1.0f;
	private long lastDownT = 0;
	
	public Matrix latestMatrix;
	public float latestSize = 0.0f;
	private Paint rackPaint;
	
	public GameBoardManager gameBoardManager;
	
	public MovingLetterContext movingLetterContext;
	
	public float scaleTime = 0.0f;
	public float scaleStart = 0.0f;
	public float scaleEnd = 0.0f;
	public float positionToCenterOnX = 0.0f;
	public float positionToCenterOnY = 0.0f;
	public boolean isScaleAnim = false;
	public final float SCALE_SPEED = 0.1f;

	public GameView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
	}
	
	public GameView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
	
	public GameView(Context context) 
	{
		super(context);
	}
	
	public void initView()
	{
		super.initView();
		int difficulty = ((GameActivity)this.getContext()).getIntent().getExtras().getInt(MenuActivity.DIFFICULTY);
		DifficultyLevelSelector dls = new DifficultyLevelSelector(difficulty, difficulty > DifficultyLevelSelector.MAX_LEVEL ? DifficultyLevelSelector.HIGHEST_SCORE : DifficultyLevelSelector.DISTRIBUTION_OVER_SCORE);
		if(GameBoardManager.IS_DEBUG)
			gameBoardManager = new GameBoardManager(this.getContext(), R.raw.board, R.raw.dict, dls);
		else
			gameBoardManager = new GameBoardManager(this.getContext(), R.raw.board, R.raw.dict, dls);
		rackPaint = new Paint();
		rackPaint.setColor(0xff1c2ce0);
		this.latestMatrix = new Matrix();
		this.movingLetterContext = new MovingLetterContext(this);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
		float y = event.getY();
		long t = event.getEventTime();
		final int pointerCount = event.getPointerCount();
		
		boolean inTileRack = y >= (1 - this.TILE_RACK_HEIGHT_PERCENTAGE) * this.getHeight();
		
		if(this.movingLetterContext.isMovingLetter())
		{
			if(event.getAction() == MotionEvent.ACTION_UP)
			{
				if(inTileRack)
				{
					this.movingLetterContext.placeMovingLetterOnRack(x, y, true);
				}
				else
				{
					this.movingLetterContext.placeMovingLetterOnBoard(x, y);
				}
			}
			else if(pointerCount <= 1  && event.getAction() == MotionEvent.ACTION_MOVE  && event.getHistorySize() > 0)
			{
				this.movingLetterContext.moveLetter(x, y);
				if(inTileRack)
				{
					this.movingLetterContext.placeMovingLetterOnRack(x, y, false);
				}
			}
			return true;
		}
		
		if(inTileRack && !gameBoardManager.gameManager.currentPlayerIsComputer())
		{	
			if(pointerCount <= 1 && event.getAction() == MotionEvent.ACTION_DOWN)
			{
				this.movingLetterContext.startMovingLetterFromRack(x, y);
			}
			return true;
		}
		
		
		if(pointerCount <= 1 && event.getAction() == MotionEvent.ACTION_DOWN)
		{
			
			if(!gameBoardManager.gameManager.currentPlayerIsComputer() && this.movingLetterContext.startMovingLetterFromBoard(x, y))
			{
				return true;
			}
			
			float dX = x - lastDownX;
			float dY = y - lastDownY;
			long dT = t - lastDownT;
			if(lastDownT > 0 && Math.abs(dX * dX + dY * dY) < MIN_DOUBLE_TAP_DIST_SQ && dT >= MIN_DOUBLE_TAP_TIME && dT <= MAX_DOUBLE_TAP_TIME)
			{
				if(closeScale(scale, 1.0f))
				{
					startScaleAnim(1.0f, 2.0f, x, y);
				}
				else
					startScaleAnim(this.scale, 1.0f, x / this.scale - posX, y / this.scale - posY);
			}
			
			lastDownX = x;
			lastDownY = y;
			lastDownT = t;
		}
		else if(pointerCount <= 1  && event.getHistorySize() > 0)
		{
			float dX = x - event.getHistoricalAxisValue(MotionEvent.AXIS_X, 0);
			float dY = y - event.getHistoricalAxisValue(MotionEvent.AXIS_Y, 0);
			posX += dX;
			posX = Math.min(posX, 0);
			posY += dY;
			posY = Math.min(posY, 0);
		}
		else if(pointerCount == 2 && event.getAction() == MotionEvent.ACTION_MOVE && event.getHistorySize() > 0)
		{
			float px0 = event.getX(0);
			float py0 = event.getY(0);
			float hpx0 = event.getHistoricalAxisValue(MotionEvent.AXIS_X, 0, 0);
			float hpy0 = event.getHistoricalAxisValue(MotionEvent.AXIS_Y, 0, 0); 
			
			float px1 = event.getX(1);
			float py1 = event.getY(1);
			float hpx1 = event.getHistoricalAxisValue(MotionEvent.AXIS_X, 1, 0);
			float hpy1 = event.getHistoricalAxisValue(MotionEvent.AXIS_Y, 1, 0);
			
			double distsq = Math.pow(px0 - px1, 2) + Math.pow(py0 - py1, 2);
			double hdistsq = Math.pow(hpx0 - hpx1, 2) + Math.pow(hpy0 - hpy1, 2);
			
			scale *= (float)(Math.sqrt(distsq/hdistsq));
			
			scale = Math.max(Math.min(scale, MAX_SCALE), MIN_SCALE);
		}
		else
		{
			super.onTouchEvent(event);
		}
		
		return true;
	}
	
	public void startScaleAnim(float oldScale, float newScale, float x, float y)
	{
		this.scaleStart = oldScale;
		this.scaleEnd = newScale;
		this.scaleTime = 0;
		this.positionToCenterOnX = x;
		this.positionToCenterOnY = y;
		this.isScaleAnim = true;
	}
	
	
	public void centerOn(float x, float y)
	{
		posX = this.getWidth() / 2.0f / this.scale - x;
		posY = (1.0f - this.TILE_RACK_HEIGHT_PERCENTAGE) * this.getHeight() / 2.0f / this.scale - y;
		posX = Math.min(posX, 0);
		posY = Math.min(posY, 0);
	}
	
	public boolean closeScale(float _scale, float num)
	{
		return Math.abs(_scale - num) < SCALE_EPS; 
	}
	
	@Override
    protected void onDraw(Canvas canvas) 
	{
		if(!this.getGameLoopThread().isRunning())
			return;
		
		float dir = this.scaleStart <= this.scaleEnd ? 1.0f : -1.0f;
		if(this.isScaleAnim)
		{
			this.scale = this.scaleStart + this.SCALE_SPEED * this.scaleTime * dir;
			if(dir > 0)
				this.scale = Math.min(this.scaleEnd, scale);
			else
				this.scale = Math.max(this.scaleEnd, this.scale);
			if(this.scaleTime >= (this.scaleEnd - this.scaleStart) / this.SCALE_SPEED * dir)
			{
				this.scale = this.scaleEnd;
				this.isScaleAnim = false;
			}
			this.posX = 0;
			this.posY = 0;
			this.centerOn(this.positionToCenterOnX, this.positionToCenterOnY);
			this.scaleTime += 1.0f;
		}
		
		canvas.drawColor(0xffa7bccb);
		
		canvas.save();
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		TILE_RACK_HEIGHT_PERCENTAGE = 86.0f / 68.0f * (0.975f / 7.0f * w) / h; 
		h = (1.0f - TILE_RACK_HEIGHT_PERCENTAGE) * h;
		
		float minScale = w < h ? h / w : w / h;
		float actualScale = scale;
		
		if(closeScale(actualScale, 1.0f))
			actualScale = 1.0f;
		else if(actualScale > 1.0f && actualScale < minScale )
			actualScale = minScale;
		
		canvas.scale(actualScale, actualScale);
		
		float size = 0.0f, dx = 0.0f, dy = 0.0f;
		float minX = 0.0f, minY = 0.0f;
		if(w < h)
		{
			if(closeScale(actualScale, 1.0f))
			{
				dy = (h - w) / 2.0f;
				size = w;
				minY = dy;
			}
			else
			{
				size = h;
				minX = w / actualScale - size;
				minY = h / actualScale - size;
			}
		}
		else if(w > h)
		{
			if(closeScale(actualScale, 1.0f))
			{
				dx = (w - h) / 2.0f;
				size = h;
				minX = dx;
			}
			else
			{
				size = w;
				minX = w / actualScale - size;
				minY = h / actualScale - size;
			}
		}
		
		if(dx + posX < minX)
			posX = minX - dx;
		if(dy + posY < minY)
			posY = minY - dy;
		
		canvas.translate(dx + posX, dy + posY);
		
		gameBoardManager.drawGameBoard(canvas, size);
		
		this.latestSize = size;
		this.latestMatrix.set(canvas.getMatrix());
		
		canvas.restore();
		
		canvas.save();
		canvas.translate(0, h);
		int rackHeight = (int)(canvas.getHeight() - h);
		canvas.drawRect(0, 0, canvas.getWidth(), rackHeight, rackPaint);
		gameBoardManager.drawRack(canvas, rackHeight);
		canvas.restore();
		
		if(this.movingLetterContext.isMovingLetter())
		{
			this.movingLetterContext.drawLetter(canvas);
		}
    }
}
