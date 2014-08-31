package com.example.exercise101;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import java.io.InputStream;

import com.example.exercise101.scrabbleSolver.IMoveSelector;
import com.example.exercise101.scrabbleSolver.Move;
import com.example.exercise101.scrabbleSolver.ScrabbleSolver;
import com.example.exercise101.scrabbleSolver.NormalMoveSelector;
import com.example.exercise101.scrabbleSolver.IScoreSupplier;
import android.graphics.Paint;
import android.util.SparseArray;
import java.util.HashSet;
public class GameBoard implements IScoreSupplier
{
	public float LETTER_OFFSET_X = 5;
	public float LETTER_OFFSET_Y = -10;
	public float LETTER_PAINT_SIZE = 37;
	
	
	public float BOARD_SCORE_TEXT_SIZE = 13;
	public float BOARD_SCORE_OFFSET_X = -15;
	public float BOARD_SCORE_OFFSET_Y = 20;
	
	private Tile[][] gameBoard;
	public ScrabbleSolver scrabbleSolver;
	public int[] letterScores = new int[27];
	public Paint textPaint = new Paint();
	RectF drawRect = new RectF(0, 0, 0, 0);
	RectF lastMoveDrawRect = new RectF(0, 0, 0, 0);
	
	public SparseArray<Character> tempCharacters;
	public HashSet<Integer> tempBlankPosition;
	
	public RackAnimationManager rackAnimationManager;
	
	public boolean isWordPlayedAnim = false;
	public int wordPlayedAnimPosition = -1;
	public int wordPlayedAnimTime = 0;
	public RectF wordPlayedAnimRect = new RectF();
	public final float WORD_PLAYED_ANIM_SPEED = 0.1f;
	public final float WORD_PLAYED_ANIM_SIZE = 0.2f;
	public final float WORD_PLAYED_ANIM_MAX_POS = 1.0f - this.WORD_PLAYED_ANIM_SIZE;
	
	public GameBoard(int width, int height, InputStream inputStream, IMoveSelector _moveSelector)
	{
		gameBoard = new Tile[height][width];
		scrabbleSolver = new ScrabbleSolver(inputStream, _moveSelector, this);
		textPaint.setColor(0xFF000000);
		textPaint.setTextSize(this.LETTER_PAINT_SIZE);
		textPaint.setFakeBoldText(true);
		this.tempCharacters = new SparseArray<Character>();
		this.tempBlankPosition = new HashSet<Integer>();
		rackAnimationManager = new RackAnimationManager(this);
	}
	
	public int getWidth()
	{
		return gameBoard[0].length;
	}
	
	public int getHeight()
	{
		return gameBoard.length;
	}
	
	public void addTile(int row, int col, Tile.TileType tile)
	{
		if(gameBoard[row][col] == null)
		{
			gameBoard[row][col] = new Tile(tile);
		}
	}
	
	public int to1D(int row, int col)
	{
		return row * this.getWidth() + col;
	}
	
	public void addTempLetter(int row, int col, char c, boolean isBlank)
	{
		int oneD = to1D(row, col);
		this.tempCharacters.append(oneD, c);
		if(isBlank)
			this.tempBlankPosition.add(oneD);
	}
	
	public boolean removeTempLetter(int row, int col)
	{
		int oneD = to1D(row, col);
		this.tempCharacters.remove(oneD);
		if(this.tempBlankPosition.contains(oneD))
		{
			this.tempBlankPosition.remove(oneD);
			return true;
		}
		else
			return false;
	}
	
	public boolean canMoveLetterAtPosition(int row, int col)
	{
		return this.tempCharacters.get(to1D(row, col)) != null;
	}
	
	public char getTempLetterAt(int row, int col)
	{
		return this.tempCharacters.get(to1D(row, col));
	}
	
	public boolean canPlaceLetterAtPosition(int row, int col)
	{
		return row >= 0 && row < this.scrabbleSolver.BOARD_SIZE && col >= 0 && col < this.scrabbleSolver.BOARD_SIZE && this.scrabbleSolver.getLetterAt(row + 1, col + 1) == ScrabbleSolver.NULL_CHAR && this.tempCharacters.get(to1D(row, col)) == null;
	}
	
	public Tile getTile(int row, int col)
	{
		return gameBoard[row][col];
	}
	
	public void startWordPlayedAnim(Move lastMove)
	{
		this.isWordPlayedAnim = true;
		this.wordPlayedAnimTime = 0;
		this.wordPlayedAnimPosition = this.scrabbleSolver.to1D(lastMove.row, lastMove.col) - 1;
		wordPlayedAnimRect.set(0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public void stopWordPlayedAnim()
	{
		this.isWordPlayedAnim = false;
		this.wordPlayedAnimTime = 0;
		this.wordPlayedAnimPosition = -1;
		wordPlayedAnimRect.set(0.0f, 0.0f, 0.0f, 0.0f);
	}
	
	public void draw(Canvas canvas, float size, Move lastMove)
	{
		int lastMovePos = lastMove == null ? -1 : this.scrabbleSolver.getEndPosition(lastMove) - 1;
		int lastMoveStart = lastMove == null ? -1 : this.scrabbleSolver.to1D(lastMove.row, lastMove.col) - 1;
		float lastMoveW = 0.0f, lastMoveH = 0.0f;
		int w = getWidth();
		int h = getHeight();
		
		float scaleW = size / w;
		float scaleH = size / h;
		
		drawRect.top = 0;
		drawRect.bottom = scaleH;
		drawRect.left = 0;
		drawRect.right = scaleW;
		char c;
		for(int col = 0; col < w; col++)
		{
			for(int row = 0; row < h; row++)
			{
				int oneD = this.to1D(row, col);
				if(this.tempCharacters.get(oneD) != null)
					c = this.tempCharacters.get(oneD);
				else
					c = this.scrabbleSolver.getLetterAt(row + 1, col + 1);
				if(c != ScrabbleSolver.NULL_CHAR)
				{
					canvas.drawBitmap(GameBoardManager.BLANK_TILE_BITMAP, null, drawRect, null);
					textPaint.setTextSize(scaleH * 0.8f);
					if(lastMovePos >= 0 && inBetween(lastMoveStart, lastMovePos, lastMove.down, oneD))
						textPaint.setColor(0xFFFFFFFF);
					else
						textPaint.setColor(0xFF000000);
					canvas.drawText((c + "").toUpperCase(), drawRect.left + LETTER_OFFSET_X, drawRect.top + scaleH + LETTER_OFFSET_Y, textPaint);
					if(!(scrabbleSolver.isBlank(row + 1, col + 1) || this.tempBlankPosition.contains(oneD)))
					{
						textPaint.setTextSize(scaleH * 0.2f);
						canvas.drawText(this.getLetterScore(c) + "", drawRect.left + scaleW + BOARD_SCORE_OFFSET_X, drawRect.top + BOARD_SCORE_OFFSET_Y, textPaint);
					}
				}
				else
					canvas.drawBitmap(GameBoardManager.TILE_TYPE_TO_IMAGE.get(getTile(row, col).getTileType()), null, drawRect, null);
				
				if(oneD == lastMovePos)
				{
					lastMoveW = scaleW * 0.80f;
					lastMoveH = scaleH * 0.80f;
					lastMoveDrawRect.left = drawRect.right - lastMoveW / 2.0f;
					lastMoveDrawRect.right = drawRect.right + lastMoveW / 2.0f;
					lastMoveDrawRect.top = drawRect.bottom - lastMoveH / 2.0f;
					lastMoveDrawRect.bottom = drawRect.bottom + lastMoveH / 2.0f;
				}
				
				if(lastMove != null && this.isWordPlayedAnim && this.wordPlayedAnimPosition == oneD)
				{
					float offset = Math.min(this.WORD_PLAYED_ANIM_SPEED * this.wordPlayedAnimTime, WORD_PLAYED_ANIM_MAX_POS) - 1;
					if(lastMove.down)
					{
						this.wordPlayedAnimRect.left = drawRect.left;
						this.wordPlayedAnimRect.right = drawRect.right;
						this.wordPlayedAnimRect.top = drawRect.top + offset * scaleH;
						this.wordPlayedAnimRect.bottom = drawRect.bottom + (offset + this.WORD_PLAYED_ANIM_SIZE) * scaleH;
						canvas.drawBitmap(GameBoardManager.WORD_PLAYED_ANIM_DOWN, null, this.wordPlayedAnimRect, null);
					}
					else
					{
						this.wordPlayedAnimRect.left = drawRect.left + offset * scaleW;
						this.wordPlayedAnimRect.right = drawRect.right + (offset + this.WORD_PLAYED_ANIM_SIZE) * scaleW;
						this.wordPlayedAnimRect.top = drawRect.top;
						this.wordPlayedAnimRect.bottom = drawRect.bottom;
						canvas.drawBitmap(GameBoardManager.WORD_PLAYED_ANIM_ACROSS, null, this.wordPlayedAnimRect, null);
					}
					
					if(offset >= this.WORD_PLAYED_ANIM_MAX_POS - 1)
					{
						this.wordPlayedAnimTime = 0;
						this.wordPlayedAnimPosition += lastMove.down ? this.scrabbleSolver.BOARD_SIZE : 1;
						if(this.wordPlayedAnimPosition > lastMovePos)
							this.stopWordPlayedAnim();
					}
					else
						this.wordPlayedAnimTime++;
				}
				
				drawRect.top += scaleH;
				drawRect.bottom += scaleH;
			}
			
			drawRect.left += scaleW;
			drawRect.right += scaleW;
			drawRect.top = 0;
			drawRect.bottom = scaleH;
		}
		
		if(lastMovePos >= 0)
		{
			textPaint.setColor(0xFFFFFFFF);
			canvas.drawBitmap(GameBoardManager.SCORE_BADGE, null, lastMoveDrawRect, null);
			textPaint.setTextSize(lastMoveH * 0.3f);
			canvas.drawText(lastMove.score + "", lastMoveDrawRect.left + lastMoveW * 0.3f, lastMoveDrawRect.top + lastMoveH * 0.65f, textPaint);
		}
		
		textPaint.setColor(0xFF000000);
	}
	
	private boolean inBetween(int lastMoveStart, int lastMovePos, boolean down, int oneD) 
	{
		if(!down)
		{
			return oneD >= lastMoveStart && oneD <= lastMovePos;
		}
		else
		{
			return oneD / this.scrabbleSolver.BOARD_SIZE >= lastMoveStart / this.scrabbleSolver.BOARD_SIZE
				&& oneD / this.scrabbleSolver.BOARD_SIZE <= lastMovePos / this.scrabbleSolver.BOARD_SIZE
				&& oneD % this.scrabbleSolver.BOARD_SIZE == lastMoveStart % this.scrabbleSolver.BOARD_SIZE;
		}
	}

	public void drawRack(Canvas canvas, int height, char[] currentRack) 
	{
		this.rackAnimationManager.drawRack(canvas, height, currentRack, drawRect, textPaint);
	}

	@Override
	public int getWordMultiplier(int row, int col) 
	{
		Tile.TileType t = getTile(row - 1, col - 1).getTileType();
		if(t == Tile.TileType.DOUBLE_WORD)
			return 2;
		if(t == Tile.TileType.TRIPLE_WORD)
			return 3;
		return 1;
	}

	@Override
	public int getLetterMultiplier(int row, int col) 
	{
		Tile.TileType t = getTile(row - 1, col - 1).getTileType();
		if(t == Tile.TileType.DOUBLE_LETTER)
			return 2;
		if(t == Tile.TileType.TRIPLE_LETTER)
			return 3;
		return 1;
	}

	@Override
	public int getLetterScore(char c) 
	{
		return this.letterScores[c - 'a'];
	}

	@Override
	public int getBoardSize() 
	{
		return this.getHeight();
	}
}
