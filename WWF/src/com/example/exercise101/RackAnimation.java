package com.example.exercise101;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

public class RackAnimation 
{
	public static float RACK_LETTER_OFFSET_X = 15;
	public static float RACK_LETTER_OFFSET_Y = -20;
	public static float RACK_SCORE_OFFSET_X = -30;
	public static float RACK_SCORE_OFFSET_Y = 20;
	
	public boolean isSwitchingPositions = false;
	public float switchStartPosition = 0.0f;
	public int switchPositionTime = 0;
	public final float SWITCH_POSITION_SPEED = 10.0f;
	
	public boolean isStartAnim = true;
	public final float START_ANIM_START_SCALE = 0.5f;
	public final float START_ANIM_END_SCALE = 1.0f;
	public final float START_ANIM_SPEED = 0.1f;
	public int startAnimTimeOffset = 0;
	public int startAnimTime = 0;
	
	RectF modifiedRect = new RectF();
	float modifiedscaleW = 0.0f;
	float modifiedscaleH = 0.0f;
	public void modifyDrawVars(RectF drawRect, float scaleW, float scaleH)
	{
		if(this.isStartAnim)
		{
			this.startAnimTime++;
			if(this.startAnimTime < this.startAnimTimeOffset)
			{
				this.modifiedRect.set(0, 0, 0, 0);
				this.modifiedscaleW = 0;
				this.modifiedscaleH = 0;
			}
			else
			{
				float currentScale = this.START_ANIM_SPEED * (this.startAnimTime - this.startAnimTimeOffset) + this.START_ANIM_START_SCALE;
				if(currentScale >= this.START_ANIM_END_SCALE)
				{
					currentScale = this.START_ANIM_END_SCALE;
					this.isStartAnim = false;
				}
				
				this.modifiedscaleW = currentScale * scaleW;
				this.modifiedscaleH = currentScale * scaleH;
				this.modifiedRect.left = drawRect.left + (1.0f - currentScale) * scaleW / 2.0f;
				this.modifiedRect.right = drawRect.right - (1.0f - currentScale) * scaleW / 2.0f;
				this.modifiedRect.top = drawRect.top + (1.0f - currentScale) * scaleH / 2.0f;
				this.modifiedRect.bottom = drawRect.bottom - (1.0f - currentScale) * scaleH / 2.0f;
			}
			
		}
		else if(this.isSwitchingPositions)
		{
			this.switchPositionTime++;
			float disp = drawRect.left - this.switchStartPosition;
			float dist = Math.abs(disp);
			float maxTime = dist / this.SWITCH_POSITION_SPEED;
			if(this.switchPositionTime >= maxTime)
			{
				this.switchPositionTime = 0;
				this.isSwitchingPositions = false;
				this.modifiedRect.set(drawRect);
				this.modifiedscaleW = scaleW;
				this.modifiedscaleH = scaleH;
			}
			else
			{
				this.modifiedRect.left = this.switchStartPosition + disp * this.switchPositionTime / maxTime;
				this.modifiedRect.right = this.modifiedRect.left + scaleW;
			}
		}
		else
		{
			this.modifiedRect.set(drawRect);
			this.modifiedscaleW = scaleW;
			this.modifiedscaleH = scaleH;
		}
	}
	
	public void startSwitchingPositions(float startX)
	{
		if(this.isStartAnim)
			return;
		this.isSwitchingPositions = true;
		this.switchPositionTime = 0;
		this.switchStartPosition = startX;
	}
	
	public void draw(Canvas canvas, char c, int letterScore, Paint textPaint)
	{
		canvas.drawBitmap(GameBoardManager.BLANK_TILE_BITMAP, null, modifiedRect, null);
		if(c != ScrabbleSolver.BLANK_TILE_RACK)
		{
			textPaint.setTextSize(modifiedscaleH * 0.8f);
			canvas.drawText((c + "").toUpperCase(), modifiedRect.left + RACK_LETTER_OFFSET_X, modifiedRect.top + modifiedscaleH + RACK_LETTER_OFFSET_Y, textPaint);
			textPaint.setTextSize(modifiedscaleH * 0.2f);
			canvas.drawText(letterScore + "", modifiedRect.left + modifiedscaleW + RACK_SCORE_OFFSET_X, modifiedRect.top + RACK_SCORE_OFFSET_Y, textPaint);
		}
	}
}
