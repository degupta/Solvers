package com.example.exercise101;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

public class RackAnimationManager 
{
	RackAnimation rackAnims[];
	GameBoard gameboard;
	
	public float RACK_LETTER_PAINT_SIZE = 74;
	public float RACK_SCORE_TEXT_SIZE = 25;
	public float latestSize = 0.0f;
	
	public RackAnimationManager(GameBoard _gameboard)
	{
		rackAnims = new RackAnimation[GameBoardManager.NUM_OF_MAX_TILES_IN_RACK];
		gameboard = _gameboard;
		for(int i = 0; i < rackAnims.length; i++)
		{
			rackAnims[i] = new RackAnimation();
			rackAnims[i].startAnimTimeOffset = i * 5;
		}
	}
	
	public void startSwitchAnimation(int curIndex, int fromIndex)
	{
		this.rackAnims[curIndex].startSwitchingPositions(0.0125f * latestSize + latestSize / GameBoardManager.NUM_OF_MAX_TILES_IN_RACK * fromIndex);
	}
	
	public void drawRack(Canvas canvas, int height, char[] currentRack, RectF drawRect, Paint textPaint)
	{
		int w = GameBoardManager.NUM_OF_MAX_TILES_IN_RACK;
		float size = canvas.getWidth();
		this.latestSize = size;
		drawRect.top = 0;
		drawRect.bottom = height;
		drawRect.left = 0;
		drawRect.right = size;
		canvas.drawBitmap(GameBoardManager.TILE_RACK_BITMAP, null, drawRect, null);
		drawRect.top = 0.0814f * height;
		drawRect.left = 0.0125f * size;
		drawRect.bottom = 0.8721f * height;
		size *= 0.975f;
		float scaleW = size / w;
		float scaleH = drawRect.bottom - drawRect.top;
		drawRect.right = drawRect.left + scaleW;
		for(int col = 0; col < w; col++)
		{
			char c = currentRack[col];
			this.rackAnims[col].modifyDrawVars(drawRect, scaleW, scaleH);
			if(c != ScrabbleSolver.NULL_CHAR)
			{
				this.rackAnims[col].draw(canvas, c, gameboard.getLetterScore(c), textPaint);
			}
			drawRect.left += scaleW;
			drawRect.right += scaleW;
		}
	}
}
