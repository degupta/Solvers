package com.example.exercise101;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import com.example.exercise101.Tile.TileType;

import java.util.ArrayList;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.exercise101.scrabbleSolver.IMoveSelector;
import com.example.exercise101.scrabbleSolver.Move;
import com.example.exercise101.scrabbleSolver.PlayContext;
import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

public class GameBoardManager 
{
	public static final boolean IS_DEBUG = false;
	
	public static final int NUM_OF_MAX_TILES_IN_RACK = 7;
	public static HashMap<TileType, Bitmap> TILE_TYPE_TO_IMAGE = new HashMap<TileType, Bitmap>();
	public static Bitmap BLANK_TILE_BITMAP;
	public static Bitmap TILE_RACK_BITMAP;
	public static Bitmap SCORE_BADGE;
	public static Bitmap WORD_PLAYED_ANIM_ACROSS;
	public static Bitmap WORD_PLAYED_ANIM_DOWN;
	public GameBoard gameBoard;
	private Context mContext;
	private int mResource;
	public GameManager gameManager;
	
	public boolean isFirstMove = true;
	public boolean justStarted = true;
	
	public char[] currentLetters;
	
	public Move lastMove = null;
	
	public GameBoardManager(Context context, int rawResource, int dictionaryRawResource, IMoveSelector _moveSelector)
	{
		mContext = context;
		mResource = rawResource;
		InputStream inputStream = mContext.getResources().openRawResource(mResource);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		try 
		{
			String line = reader.readLine();
			int width = Integer.parseInt(line.substring(0, line.indexOf(',')));
			int height = Integer.parseInt(line.substring(line.indexOf(',') + 1));
			
			gameBoard = new GameBoard(width, height, mContext.getResources().openRawResource(dictionaryRawResource), _moveSelector);
			int row = 0;
			line = reader.readLine();
			while (row < height) 
			{
				int len = line.length();
				for(int col = 0; col < len; col++)
				{
					gameBoard.addTile(row, col, Tile.getTileType(line.charAt(col)));
				}
				row++;
				line = reader.readLine();
			}
			
			this.gameBoard.scrabbleSolver.setCurrentBag(new int[27]);
			for(int i = 0; i < 27; i++)
			{
				int score = Integer.parseInt(line.substring(0, line.indexOf(',')));
				int num = Integer.parseInt(line.substring(line.indexOf(',') + 1));
				this.gameBoard.scrabbleSolver.addLetterToBag((char) ('a' + i), num);
				this.gameBoard.letterScores[i] = score;
				line = reader.readLine();
			}
		} 
		catch (Exception e) 
		{
			Log.e("GAMEBORAD", "Can't Read Game Board File");
		}
		
		if(IS_DEBUG)
		{
			gameManager = new GameManager(0);
			for(int i = 0; i < 27; i++)
			{
				this.gameManager.players[0].currentRack[i] = 0;
				this.gameManager.players[1].currentRack[i] = 0;
			}
			
			this.gameManager.players[0].currentRack['s' - 'a']++;
			this.gameManager.players[0].currentRack['o' - 'a']++;
			this.gameManager.players[0].currentRack['o' - 'a']++;
			this.gameManager.players[0].currentRack['n' - 'a']++;
			this.gameManager.players[0].currentRack['e' - 'a']++;
			this.gameManager.players[0].currentRack['r' - 'a']++;
			this.gameManager.players[0].currentRack['z' - 'a' + 1]++;
			this.gameManager.players[0].tilesInRack = 7;
			this.gameManager.players[1].currentRack['t' - 'a']++;
			this.gameManager.players[1].tilesInRack = 1;

			this.gameBoard.scrabbleSolver.setRack(this.gameManager.players[0].currentRack);
			Move m = new Move("sooner", 8, 8, true, 10);
			this.gameBoard.scrabbleSolver.play(m);
			this.lastMove = m;
			this.gameBoard.startWordPlayedAnim(m);
		}
		else
		{
			gameManager = new GameManager(0);
			for(int i = 0; i < this.gameManager.players.length; i++)
			{
				int numDrawn = this.gameBoard.scrabbleSolver.drawLettersFromBagAndAddToRack(this.gameManager.players[i].currentRack, NUM_OF_MAX_TILES_IN_RACK);
				this.gameManager.players[i].tilesInRack += numDrawn;
			}
		}	
		
		this.currentLetters = new char[NUM_OF_MAX_TILES_IN_RACK];		
		this.updateCurrentLetters(this.gameManager.getCurrentPlayer().currentRack);
		
		initImages();
	}
	
	public void initImages()
	{
		for(TileType tile : TileType.values())
		{
			TILE_TYPE_TO_IMAGE.put(tile, BitmapFactory.decodeResource(mContext.getResources(), tile.getResId()));
		}
		
		BLANK_TILE_BITMAP = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tile_blank_letter);
		TILE_RACK_BITMAP = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.tile_rack);
		SCORE_BADGE = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wwf_score_badge);
		WORD_PLAYED_ANIM_ACROSS = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wwf_word_made_effect_vertical);
		WORD_PLAYED_ANIM_DOWN = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.wwf_word_made_effect_horizontal);
	}
	
	public void drawGameBoard(Canvas canvas, float size)
	{
		this.gameBoard.draw(canvas, size, lastMove);
	}
	
	public void updateCurrentLetters(int[] tileFreq)
	{
		for(int i = 0; i < currentLetters.length; i++)
			this.currentLetters[i] = ScrabbleSolver.NULL_CHAR;
		
		int currentLetter = 0;
		for(int i = 0; i < tileFreq.length; i++)
		{
			for(int j = 0; j < tileFreq[i]; j++)
			{
				if(currentLetter == this.currentLetters.length)
					break;
				this.currentLetters[currentLetter] = (char) (i + 'a');
				currentLetter++;
			}
		}
		
		shuffleCurrentLetters();
	}
	
	public void bringBackLettersFromBoard()
	{
		int j = 0;
		for(int i = 0; i < this.gameBoard.tempCharacters.size(); i++)
		{
			char c = this.gameBoard.tempBlankPosition.contains(this.gameBoard.tempCharacters.keyAt(i)) ? ScrabbleSolver.NULL_CHAR : this.gameBoard.tempCharacters.valueAt(i);
			while(this.currentLetters[j] != ScrabbleSolver.NULL_CHAR)
				j++;
			this.currentLetters[j] = c;
		}
		
		this.gameBoard.tempCharacters.clear();
		this.gameBoard.tempBlankPosition.clear();
				
	}
	
	public void shuffleCurrentLetters()
	{
		int numChars = 0;
		for(numChars = 0; numChars < currentLetters.length; numChars++)
			if(this.currentLetters[numChars] == ScrabbleSolver.NULL_CHAR)
				break;
		
		for(int i = 0; i < numChars; i++)
		{
			int rand = (int)(Math.random() * numChars);
			char temp = this.currentLetters[i];
			this.currentLetters[i] = this.currentLetters[rand];
			this.currentLetters[rand] = temp;
		}
	}
	
	public void drawRack(Canvas canvas, int height)
	{
		this.gameBoard.drawRack(canvas, height, this.currentLetters);
	}
	
	public int getWidth()
	{
		return this.gameBoard.getWidth();
	}
	
	public int getHeight()
	{
		return this.gameBoard.getHeight();
	}
	
	
	public void playComputer()
	{
		Move move;
		Player p = this.gameManager.getCurrentPlayer();
		Log.d(ScrabbleSolver.TAG, "Setting Rack for player " + this.gameManager.currentPlayerNo);
		this.gameBoard.scrabbleSolver.setRack(p.currentRack);
		
		Log.d(ScrabbleSolver.TAG, "Getting Move...");
		if(this.isFirstMove)
			move = this.gameBoard.scrabbleSolver.getFirstMove();
		else
			move = this.gameBoard.scrabbleSolver.getNextMove();
		
		if(move != null)
		{
			//move.score *= isFirstMove ? 2 : 1;
			p.score += move.score;
			
			int num = this.gameBoard.scrabbleSolver.play(move);
			Log.d(ScrabbleSolver.TAG, "Playing Move .... : " + move.toString());
			p.tilesInRack -= num;
			
			Log.d(ScrabbleSolver.TAG, "Drawing tiles...");
			int numDrawn = this.gameBoard.scrabbleSolver.drawLettersFromBagAndAddToRack(p.currentRack, NUM_OF_MAX_TILES_IN_RACK - p.tilesInRack);
			p.tilesInRack += numDrawn;
			
			final Move mMove = new Move(move);
			this.lastMove = move;
			this.gameBoard.startWordPlayedAnim(lastMove);
			((GameActivity) this.mContext).runOnUiThread(new Runnable()
			{
				public void run()
				{
					showMsg("Computer just played " + mMove.word.replace("/", "") + " for " + mMove.score + " points", "Word Played");
				}
			});
			
			this.isFirstMove = false;
			
			this.switchPlayer();
		}
		else
		{
			((GameActivity) this.mContext).runOnUiThread(new Runnable()
			{
				public void run()
				{
					showMsg("Computer just passed!", "Computer passed");
				}
			});
			Log.d(ScrabbleSolver.TAG, "PASSING...");
			
			this.pass();
		}
	}
	
	public void playHuman()
	{
		Player p = this.gameManager.getCurrentPlayer();
		Log.d(ScrabbleSolver.TAG, "Setting Rack for player " + this.gameManager.currentPlayerNo);
		this.gameBoard.scrabbleSolver.setRack(p.currentRack);
		
		PlayContext pc = this.gameBoard.scrabbleSolver.playHuman(this.gameBoard.tempCharacters, this.gameBoard.tempBlankPosition, this.isFirstMove);
		
		if(pc.result == PlayContext.SUCCESS)
		{
			//p.score += isFirstMove ? pc.move.score * 2 : pc.move.score;
			p.score += pc.move.score;
			
			int num = this.gameBoard.scrabbleSolver.play(pc.move);
			Log.d(ScrabbleSolver.TAG, "Playing Move .... : " + pc.move.toString());
			p.tilesInRack -= num;
			
			Log.d(ScrabbleSolver.TAG, "Drawing tiles...");
			int numDrawn = this.gameBoard.scrabbleSolver.drawLettersFromBagAndAddToRack(p.currentRack, NUM_OF_MAX_TILES_IN_RACK - p.tilesInRack);
			p.tilesInRack += numDrawn;
			
			this.showMsg("You just played " + pc.move.word.replace("/", "") + " for " + pc.move.score + " points", "Congratulations");
			
			this.isFirstMove = false;
			this.lastMove = pc.move;
			this.gameBoard.startWordPlayedAnim(lastMove);
			switchPlayer();
		}
		else
			this.showErrorDialog(pc);
	}
	
	public String stringWords(ArrayList<String> words)
	{
		String str = "";
		int len = words.size();
		for(int i = 0; i < len; i++)
		{
			if(i == len - 1)
				str += words.get(i).replace("/", "");
			else if(i == len - 2)
				str += words.get(i).replace("/", "") + " and ";
			else
				str += words.get(i).replace("/", "") + ", ";
		}
		
		return str;
	}
	
	public void showErrorDialog(PlayContext pc)
	{
		String msg = "";
		switch(pc.result)
		{
			case PlayContext.NOT_IN_A_STRAIGHT_LINE_ERROR :
				msg = "The letters are not placed in a straight line. Please try again.";
			break;
			case PlayContext.NOT_JOINED :
				msg = "The letters are not joined to any other word. Please try again.";
			break;
			case PlayContext.NOT_LEGAL_WORDS_ERROR :
				msg = "The words " + stringWords(pc.badWords) + (pc.badWords.size() > 1 ? " are not legal words." : " is not a legal word.") + " Please try again.";
			break;
			case PlayContext.WORD_ALREADY_MADE_ERROR :
				msg = "The words " + stringWords(pc.badWords) + " are already played before. Please try again.";
			break;
			case PlayContext.NOT_ENOUGH_LETTERS :
				msg = "You have not played enough letters. Please try again.";
			break;
			default:
				msg = "There was an error playing your word. Please try again.";
				break;
		}
		this.showMsg(msg, "Error");
	}
	
	public void showMsg(String msg, String title)
	{
		/*DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
		{
		    public void onClick(DialogInterface dialog, int which) 
		    {
			            dialog.dismiss();
		    }
		};
		AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
		builder.setTitle(title).setMessage(msg).setPositiveButton("Ok", dialogClickListener).show();*/
		Toast toast = Toast.makeText(this.mContext, title + " : " + msg, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	public void switchPlayer()
	{
		this.gameManager.switchPlayer();
		this.updateCurrentLetters(this.gameManager.getCurrentPlayer().currentRack);
		this.gameBoard.tempCharacters.clear();
		this.gameBoard.tempBlankPosition.clear();
		this.justStarted = false;
		((GameActivity) this.mContext).runOnUiThread(new Runnable()
		{
			public void run()
			{
				((GameActivity)GameBoardManager.this.mContext).updateUI();
			}
		});
	}
	
	public void pass()
	{
		/*Player p = this.gameManager.getCurrentPlayer();
		
		for(int i = 0; i < p.currentRack.length; i++)
			if(p.currentRack[i] > 0)
				this.gameBoard.scrabbleSolver.addLetterToBag((char) ('a' + i), p.currentRack[i]);
		
		
		clearRack(p);
		
		int numDrawn = this.gameBoard.scrabbleSolver.drawLettersFromBagAndAddToRack(p.currentRack, NUM_OF_MAX_TILES_IN_RACK - p.tilesInRack);
		p.tilesInRack += numDrawn;*/
		lastMove = null;
		this.switchPlayer();
	}
	
	public void verifyAndSwap(char[] letters)
	{
		if(letters.length <= 0)
		{
			this.showMsg("Error Swapping Letters", "You didnt select any letters");
			return;
		}
		Player p = this.gameManager.getCurrentPlayer();
		
		int[] alreadySeen = new int[27];
		for(int i = 0; i < letters.length; i++)
		{
			if(p.currentRack[letters[i] - 'a'] - alreadySeen[letters[i] - 'a'] <= 0)
			{
				this.showMsg("Error Swapping Letters", "You are trying to swap letters you don't have");
				return;
			}
			alreadySeen[letters[i] - 'a']++;
		}
		
		for(int i = 0; i < letters.length; i++)
		{
			this.gameBoard.scrabbleSolver.addLetterToBag(letters[i], 1);
			p.currentRack[letters[i] - 'a']--;
			p.tilesInRack--;
		}
		
		int numDrawn = this.gameBoard.scrabbleSolver.drawLettersFromBagAndAddToRack(p.currentRack, NUM_OF_MAX_TILES_IN_RACK - p.tilesInRack);
		p.tilesInRack += numDrawn;
		
		lastMove = null;
		this.switchPlayer();
	}
	
	public void clearRack(Player p)
	{
		for(int i = 0; i < p.currentRack.length; i++)
		{
			p.currentRack[i] = 0;
		}
		p.tilesInRack = 0;
	}
}
