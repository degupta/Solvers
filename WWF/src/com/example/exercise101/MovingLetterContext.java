package com.example.exercise101;

import android.app.AlertDialog;
import android.graphics.PointF;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

public class MovingLetterContext 
{
	public char currentlyMovingLetter = ScrabbleSolver.NULL_CHAR;
	public char blankLetter = ScrabbleSolver.NULL_CHAR;
	public int currentlyMovingLetterRackPosition = -1;
	public Point currentlyMovingBoardPosition;
	public PointF currentlyMovingLetterPoint;
	public RectF currentlyMovingLetterRect;
	public AlertDialog currentBlankDialog;
	
	public GameView gameView;
	
	public boolean isMovingFromRack = false;
	
	public Matrix inverseMatrix;
	
	public MovingLetterContext(GameView _gameView)
	{
		this.currentlyMovingLetterPoint = new PointF(-1, -1);
		this.currentlyMovingLetterRect = new RectF();
		this.currentlyMovingBoardPosition = new Point(-1, -1);
		this.gameView = _gameView;
		this.inverseMatrix = new Matrix();
	}
	
	public GameBoard gameBoard()
	{
		return gameView.gameBoardManager.gameBoard;
	}
	
	public GameBoardManager gameBoardManager()
	{
		return this.gameView.gameBoardManager;
	}
	
	public ScrabbleSolver scrabbleSolver()
	{
		return this.gameView.gameBoardManager.gameBoard.scrabbleSolver;
	}
	
	public boolean isMovingLetter()
	{
		return this.currentlyMovingLetter != ScrabbleSolver.NULL_CHAR;
	}
	
	public void setUpRect()
	{
		int size = Math.max(gameView.getWidth(), gameView.getHeight());
		this.currentlyMovingLetterRect.left = this.currentlyMovingLetterPoint.x - gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * size;
		this.currentlyMovingLetterRect.top = this.currentlyMovingLetterPoint.y - gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * size;
		this.currentlyMovingLetterRect.right = this.currentlyMovingLetterPoint.x + gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * size;
		this.currentlyMovingLetterRect.bottom = this.currentlyMovingLetterPoint.y + gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * size;
	}
	
	public boolean isMovingBlankTile()
	{
		return this.currentlyMovingLetter == ScrabbleSolver.BLANK_TILE_RACK;
	}
	
	public String getDrawString()
	{
		return (this.currentlyMovingLetter + "").toUpperCase();
	}
	
	public void drawLetter(Canvas canvas)
	{
		this.setUpRect();
		canvas.drawBitmap(GameBoardManager.BLANK_TILE_BITMAP, null, this.currentlyMovingLetterRect, null);
		float scale = (this.currentlyMovingLetterRect.bottom - this.currentlyMovingLetterRect.top);
		gameBoard().textPaint.setTextSize(scale * 0.8f);
		if(!this.isMovingBlankTile() && this.currentlyMovingLetter >= 'a' && this.currentlyMovingLetter <= 'z')
		{
			canvas.drawText(this.getDrawString(), currentlyMovingLetterRect.left + scale * 0.2f, currentlyMovingLetterRect.top + scale * 0.8f, gameBoard().textPaint);
			gameBoard().textPaint.setTextSize(scale * 0.2f);
			canvas.drawText(gameBoard().getLetterScore(this.currentlyMovingLetter) + "", currentlyMovingLetterRect.left + scale * 0.7f, currentlyMovingLetterRect.top + scale * 0.2f, gameBoard().textPaint);
		}
	}
	
	
	public void startMovingLetterFromRack(float x, float y)
	{
		this.isMovingFromRack = true;
		this.currentlyMovingLetterRackPosition = this.screenPointToRackPoint(x);
		this.currentlyMovingLetter = gameBoardManager().currentLetters[this.currentlyMovingLetterRackPosition];
		gameBoardManager().currentLetters[this.currentlyMovingLetterRackPosition] = ScrabbleSolver.NULL_CHAR;
		this.currentlyMovingLetterPoint.x = x;
		this.currentlyMovingLetterPoint.y = y;
	}
	
	public boolean startMovingLetterFromBoard(float x, float y)
	{
		this.currentlyMovingBoardPosition = this.screenPointToBoardPoint(x, y);
		if(!gameBoard().canMoveLetterAtPosition(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y))
			return false;
		this.isMovingFromRack = false;
		this.currentlyMovingLetter = gameBoard().getTempLetterAt(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y);
		boolean isBlank = gameBoard().removeTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y);
		if(isBlank)
		{
			this.blankLetter = this.currentlyMovingLetter;
			this.currentlyMovingLetter = ScrabbleSolver.BLANK_TILE_RACK;
		}
		this.currentlyMovingLetterPoint.x = x;
		this.currentlyMovingLetterPoint.y = y;
		return true;
	}
	
	public void moveLetter(float x, float y)
	{
		this.currentlyMovingLetterPoint.x = x;
		this.currentlyMovingLetterPoint.x = Math.min(Math.max(this.currentlyMovingLetterPoint.x, gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * gameView.getWidth()), (1.0f - gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE) * gameView.getWidth());
		this.currentlyMovingLetterPoint.y = y;
		this.currentlyMovingLetterPoint.y = Math.min(Math.max(this.currentlyMovingLetterPoint.y, gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE * gameView.getHeight()), (1.0f - gameView.MOVING_TILE_HALF_SIZE_PERCENTAGE) * gameView.getHeight());
	}
	
	public void clearMovingState()
	{
		this.currentlyMovingLetter = ScrabbleSolver.NULL_CHAR;
		this.blankLetter = ScrabbleSolver.NULL_CHAR;
		this.currentlyMovingLetterRackPosition = -1;
		this.currentlyMovingBoardPosition.x = -1;
		this.currentlyMovingBoardPosition.y = -1;
		if(this.currentBlankDialog != null)
			currentBlankDialog.dismiss();
	}
	
	public void cancelMovingLetter()
	{
		if(!this.isMovingLetter())
			return;
		if(this.isMovingFromRack)
		{
			gameBoardManager().currentLetters[this.currentlyMovingLetterRackPosition] = this.currentlyMovingLetter;
		}
		else
		{
			if(this.isMovingBlankTile())
			{
				this.gameBoard().addTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y, this.blankLetter, true);
			}
			else
				gameBoard().addTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y, this.currentlyMovingLetter, false);
		}
		
		this.clearMovingState();
	}
	
	public void setMovingLetterFromBlankDialog(char c)
	{
		this.currentlyMovingLetter = c;
		gameBoard().addTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y, this.currentlyMovingLetter, true);
		this.clearMovingState();
	}
	
	public GameView getGameView()
	{
		return this.gameView;
	}
	
	public void notifyOnThis()
	{
		notify();
	}
	
	public synchronized void showBlankChooser()
	{	
		/*((Activity) this.gameView.getContext()).runOnUiThread(new Runnable() {
			public AlertDialog dialog = null;
			
			public AlertDialog getDialog()
			{
				return this.dialog;
			}
			
			public void run()
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(getGameView().getContext());
				//LayoutInflater inflater = (LayoutInflater) this.gameView.getContext().getSystemService(this.gameView.getContext().LAYOUT_INFLATER_SERVICE);
				//View view = inflater.inflate(R.layout.blank_letter_chooser, (ViewGroup) this.gameView.getContext().findViewById(R.id.root));
				View view = LayoutInflater.from(getGameView().getContext()).inflate(R.layout.blank_letter_chooser, null);
				
				for(int i = 0; i < 26; i++)
				{
					Button button = (Button) view.findViewById(R.id.button1 + i);
					button.setOnTouchListener(new View.OnTouchListener() 
					{
			            public boolean onTouch(View v, MotionEvent evt) 
			            {
			            	if(evt.getAction() == MotionEvent.ACTION_UP)
			            	{
			            		char c = ((Button) v).getText().toString().toLowerCase().charAt(0);
			            		setMovingLetter(c);
			            		getDialog().dismiss();
			            		notifyOnThis();
			            	}
			            	return true;
			            }
			        });
				}
				dialog = builder.setView(view).show();
			}
		});
		
		try 
		{
			wait();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}*/
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getGameView().getContext());
		View view = LayoutInflater.from(getGameView().getContext()).inflate(R.layout.blank_letter_chooser, null);
		
		for(int i = 0; i < 26; i++)
		{
			Button button = (Button) view.findViewById(R.id.button1 + i);
			button.setOnTouchListener(new View.OnTouchListener() 
			{
	            public boolean onTouch(View v, MotionEvent evt) 
	            {
	            	if(evt.getAction() == MotionEvent.ACTION_UP)
	            	{
	            		char c = ((Button) v).getText().toString().toLowerCase().charAt(0);
	            		setMovingLetterFromBlankDialog(c);
	            	}
	            	return true;
	            }
	        });
		}
		
		currentBlankDialog = builder.setView(view).show();
	}
	
	public void placeMovingLetterOnBoard(float x, float y)
	{
		if(!this.isMovingLetter())
			return;
		Point p = this.screenPointToBoardPoint(x, y);
		if(p.x >= 0 && p.x < gameBoardManager().getHeight() && p.y >= 0 && p.y < gameBoardManager().getWidth() && this.findEmptyPositionToPlace(p))
		{
			if(this.isMovingFromRack)
			{
				gameBoardManager().currentLetters[this.currentlyMovingLetterRackPosition] = ScrabbleSolver.NULL_CHAR;
			}
			else
			{
				boolean isBlank = gameBoard().removeTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y);
				if(isBlank)
				{
					this.blankLetter = this.currentlyMovingLetter;
					this.currentlyMovingLetter = ScrabbleSolver.BLANK_TILE_RACK;
				}
			}
			
			if(this.isMovingBlankTile())
			{
				if(this.blankLetter == ScrabbleSolver.NULL_CHAR)
				{
					this.currentlyMovingBoardPosition = p;
					this.showBlankChooser();
					return;
				}
				else
				{
					gameBoard().addTempLetter(p.x, p.y, this.blankLetter, true);
				}
			}
			else
				gameBoard().addTempLetter(p.x, p.y, this.currentlyMovingLetter, false);
			
			if(gameView.closeScale(gameView.scale, 1.0f))
			{
				/*gameView.scale = 2.0f;
				x *= gameView.scale;
				y *= gameView.scale;
				gameView.centerOn(x, y);*/
				gameView.startScaleAnim(1.0f, 2.0f, x, y);
			}
		}
		else
		{
			this.cancelMovingLetter();
		}
		
		this.clearMovingState();
	}
	
	public boolean findEmptyPositionToPlace(Point p)
	{
		if(gameBoard().canPlaceLetterAtPosition(p.x, p.y))
			return true;
		Point dir = new Point(0, 1);
		int numIters = 0;
		while(true)
		{
			if(gameBoard().canPlaceLetterAtPosition(p.x + dir.x, p.y + dir.y))
			{
				p.x += dir.x;
				p.y += dir.y;
				return true;
			}
			else if(numIters > 8)
				return false;
			int temp = dir.x;
			dir.x = dir.y;
			dir.y = -temp;
			numIters++;
			if(numIters % 4 == 0)
				dir.y += 1;
		}
	}
	
	public void placeMovingLetterOnRack(float x, float y, boolean shouldCancelMovingLetter)
	{
		if(!this.isMovingLetter())
			return;
		
		int otherPos = this.screenPointToRackPoint(x);
		if(this.isMovingFromRack)
		{
			if(otherPos != this.currentlyMovingLetterRackPosition)
			{
				char temp = gameBoardManager().currentLetters[otherPos];
				gameBoardManager().currentLetters[otherPos] = ScrabbleSolver.NULL_CHAR;
				gameBoardManager().currentLetters[this.currentlyMovingLetterRackPosition] = temp;
				this.gameBoard().rackAnimationManager.startSwitchAnimation(currentlyMovingLetterRackPosition, otherPos);
				this.currentlyMovingLetterRackPosition = otherPos;
			}
			
			if(shouldCancelMovingLetter)
				this.cancelMovingLetter();
		}
		else
		{
			if(gameBoardManager().currentLetters[otherPos] == ScrabbleSolver.NULL_CHAR)
			{
				gameBoardManager().currentLetters[otherPos] = this.currentlyMovingLetter;
				gameBoard().removeTempLetter(currentlyMovingBoardPosition.x, currentlyMovingBoardPosition.y);
				this.clearMovingState();
			}
			else
				this.cancelMovingLetter();
		}
	}
	
	public int screenPointToRackPoint(float x)
	{
		return (int)(x / gameView.getWidth() * GameBoardManager.NUM_OF_MAX_TILES_IN_RACK);
	}
	
	public Point screenPointToBoardPoint(float x, float y)
	{
		float[] pts = new float[] {x, y};
		this.gameView.latestMatrix.invert(this.inverseMatrix);
		this.inverseMatrix.mapPoints(pts, pts);
		Point p = new Point();
		p.x =  (int)(pts[1] / gameView.latestSize * gameBoardManager().getWidth());
		p.y =  (int)(pts[0] / gameView.latestSize * gameBoardManager().getHeight());
		return p;
	}
}
