package com.example.exercise101;

import java.io.ByteArrayOutputStream;

import com.example.exercise101.scrabbleSolver.Move;
import com.example.exercise101.scrabbleSolver.ScrabbleSolver;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.DialogInterface;
import android.graphics.Color;
import android.app.AlertDialog;
import android.os.AsyncTask;

public class GameActivity extends Activity {
	private String m_userId = "";
	private String m_otherId = "";
	public int difficulty = 0;
	public static final int PLAY_ACTION_ID = 0;
	public static final int PASS_ACTION_ID = 1;
	public SensorHelper sensorHelper; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		this.m_userId = getIntent().getExtras().getString(LoginActivity.USER_ID);
		this.m_otherId = getIntent().getExtras().getString(MenuActivity.OTHER_PLAYER);
		this.difficulty = getIntent().getExtras().getInt(MenuActivity.DIFFICULTY);
		
		ImageButton chatbutton = (ImageButton) findViewById(R.id.chat_button);
        chatbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_chat_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_chat));
            		Intent intent = new Intent(GameActivity.this, ChatActivity.class);
            		intent.putExtra(LoginActivity.USER_ID, m_userId);
            		intent.putExtra(MenuActivity.OTHER_PLAYER, m_otherId);
            		startActivity(intent);
            	}
            	return true;
            }
        });
        
        ImageButton playbutton = (ImageButton) findViewById(R.id.play_button);
		playbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_play_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_play));
            		showYesNoDialog(PLAY_ACTION_ID);
            	}
            	return true;
            }
        });
        
		ImageButton pass_button = (ImageButton) findViewById(R.id.pass_button);
        pass_button.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_pass_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.button_pass));
            		showYesNoDialog(PASS_ACTION_ID);
            	}
            	return true;
            }
        });
        
        
        ImageButton swapbutton = (ImageButton) findViewById(R.id.swap_button);
        swapbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.wwf_swap_button_highlight));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((ImageButton) v).setImageDrawable(getResources().getDrawable(R.drawable.wwf_swap_button));
            		GameActivity.this.swapLetters();
            	}
            	return true;
            }
        });
        
        this.sensorHelper = new SensorHelper(this, SensorHelper.ACCELEROMETER, new ICallback()
        {

			@Override
			public void callback(Object param) 
			{
				GameView gv = (GameView) findViewById(R.id.game_view);
				gv.gameBoardManager.bringBackLettersFromBoard();
				gv.gameBoardManager.shuffleCurrentLetters();
			}
        	
        });
        
        readFile();
        updateUI();
	}
	
	
	public void showYesNoDialog(int actionID)
	{
		
		new YesNoDialog(this, actionID, new ICallback()
			{

				@Override
				public void callback(Object param) 
				{
					GameView gv = (GameView) findViewById(R.id.game_view);
					switch(((Integer)param))
					{
						case GameActivity.PLAY_ACTION_ID:
							if(gv.gameBoardManager.gameManager.currentPlayerIsComputer())
								setupComputerPlay();
							else
								gv.gameBoardManager.playHuman();
							break;
						case GameActivity.PASS_ACTION_ID:
							gv.gameBoardManager.pass();
							break;
					}					
				}
			
			});
	}
	
	public void setupComputerPlay()
	{
		final AsyncTask<Void, Integer, Boolean> asynctask = new AsyncTask<Void, Integer, Boolean>() 
		{
			private ProgressDialog mProgressDialog;
			@Override protected void onPreExecute() {
				super.onPreExecute(); // TODO Show some ProgressDialog…
				mProgressDialog = new ProgressDialog(GameActivity.this);
				mProgressDialog.setTitle("Computer is thinking");
				mProgressDialog.setMessage("Even I got to think man....");
				mProgressDialog.show();
			}
				
			@Override 
			protected Boolean doInBackground(Void ... params) 
			{
				try
				{
					GameView gv = (GameView) findViewById(R.id.game_view);
					gv.gameBoardManager.playComputer();
					publishProgress(100);
				}
				catch(Exception e)
				{
					Log.e("SCRABBLE_SOLVER", "ERROR", e);
				}
				return true;
			}

			protected void onProgressUpdate(Integer ... values) {
				mProgressDialog.setProgress(values[0]);
			}

			protected void onPostExecute(Boolean result) 
			{
				super.onPostExecute(result);
				mProgressDialog.dismiss();
			}
		};
		
		asynctask.execute((Void[]) null);
	}
	
	
	public void setupAysncTask()
	{
		final AsyncTask<Void, Integer, Boolean> asynctask = new AsyncTask<Void, Integer, Boolean>() 
		{
			private ProgressDialog mProgressDialog;
			@Override protected void onPreExecute() {
				super.onPreExecute(); // TODO Show some ProgressDialog…
				mProgressDialog = new ProgressDialog(GameActivity.this);
				mProgressDialog.setTitle("AsyncTask");
				mProgressDialog.setMessage("Working really hard");
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				mProgressDialog.show();
			}
				
			@Override 
			protected Boolean doInBackground(Void ... params) 
			{
				long startTime = System.currentTimeMillis();;
				long length = 3000;
				int progress = 0;
				
				while(progress < 100)
				{
					progress = (int)((System.currentTimeMillis() - (double)startTime) * 100.0 / length);
					publishProgress(progress);
				}
				return true;
			}

			protected void onProgressUpdate(Integer ... values) {
				mProgressDialog.setProgress(values[0]);
			}

			@Override protected void onPostExecute(Boolean result) 
			{
				super.onPostExecute(result);
				mProgressDialog.dismiss();
				finish();
			}
		};
		
		asynctask.execute((Void[]) null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	public void swapLetters()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Swap Letters");
		alert.setMessage("Type the letters you want to swap (space for blank)");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Swap", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				char[] letters = input.getText().toString().toLowerCase().replace(' ', ScrabbleSolver.BLANK_TILE_RACK).toCharArray();
				GameView gv = (GameView) GameActivity.this.findViewById(R.id.game_view);
				gv.gameBoardManager.verifyAndSwap(letters);
				dialog.dismiss();
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton) 
			{
				dialog.dismiss();
			}
		});

		alert.show();
	}
	
	public void updateUI()
	{
		GameView gv = (GameView) findViewById(R.id.game_view);
		ImageButton pass = (ImageButton) (this.findViewById(R.id.pass_button));
		ImageButton swap = (ImageButton) (this.findViewById(R.id.swap_button));
		if(gv.gameBoardManager.gameManager.currentPlayerIsComputer())
		{
			pass.setImageDrawable(this.getResources().getDrawable(R.drawable.button_pass_disabled));
			pass.setEnabled(false);
			swap.setImageDrawable(this.getResources().getDrawable(R.drawable.wwf_swap_button_disabled));
			swap.setEnabled(false);
		}
		else
		{
			pass.setImageDrawable(this.getResources().getDrawable(R.drawable.button_pass));
			pass.setEnabled(true);
			swap.setImageDrawable(this.getResources().getDrawable(R.drawable.wwf_swap_button));
			swap.setEnabled(true);
		}
		
		TextView your_name = (TextView) findViewById(R.id.your_score_name);
		TextView other_name = (TextView) findViewById(R.id.other_score_name);
		TextView your = (TextView) findViewById(R.id.your_score);
		TextView other = (TextView) findViewById(R.id.other_score);
		if(gv.gameBoardManager.gameManager.currentPlayerNo == 0)
		{
			your_name.setText(Html.fromHtml("<b><font color = '#FA5928'>" + this.m_userId.toUpperCase() + "</font></b>"));
			your.setText(Html.fromHtml("<b><font color = '#FA5928'>" + gv.gameBoardManager.gameManager.players[0].score + "</font></b>"));
			other_name.setText(Html.fromHtml("<b><font color = '#FFFFFF'>" + this.m_otherId.toUpperCase() + "</font></b>"));
			other.setText(Html.fromHtml("<b><font color = '#FFFFFF'>" + gv.gameBoardManager.gameManager.players[1].score + "</font></b>"));
			findViewById(R.id.your_chance).setVisibility(View.VISIBLE);
			findViewById(R.id.other_chance).setVisibility(View.INVISIBLE);
		}
		else
		{
			your_name.setText(Html.fromHtml("<b><font color = '#FFFFFF'>" + this.m_userId.toUpperCase() + "</font></b>"));
			your.setText(Html.fromHtml("<b><font color = '#FFFFFF'>" + gv.gameBoardManager.gameManager.players[0].score + "</font></b>"));
			other_name.setText(Html.fromHtml("<b><font color = '#FA5928'>" + this.m_otherId.toUpperCase() + "</font></b>"));
			other.setText(Html.fromHtml("<b><font color = '#FA5928'>" + gv.gameBoardManager.gameManager.players[1].score + "</font></b>"));
			findViewById(R.id.your_chance).setVisibility(View.INVISIBLE);
			findViewById(R.id.other_chance).setVisibility(View.VISIBLE);
		}
		
		TextView letters_in_bag = (TextView) findViewById(R.id.letters_in_bag);
		letters_in_bag.setText(Html.fromHtml("<b><font color = '#C8BDF0'>" + gv.gameBoardManager.gameBoard.scrabbleSolver.currentLettersInBag + "</b> letters remain</font>"));
		
		Move lastMove = gv.gameBoardManager.lastMove;
		TextView last_played = (TextView) findViewById(R.id.last_played);
		if(lastMove != null)
		{
			String line1 = "<b><font color = '#FFFFFF'>" + (gv.gameBoardManager.gameManager.currentPlayerIsComputer() ? "You" : this.m_otherId) + " played</font>";
			String line2 = "<font color = '#FA5928'>" + lastMove.word.replace("/", "").toUpperCase() + "</font><font color = '#FFFFFF'> for " + lastMove.score + " points</font></b>";
			last_played.setText(Html.fromHtml(line1 + "<br/>" + line2));
		}
		else if(!gv.gameBoardManager.justStarted)
		{
			String line1 = "<b><font color = '#FFFFFF'>" + (gv.gameBoardManager.gameManager.currentPlayerIsComputer() ? "You" : this.m_otherId) + "</font> <font color = '#FA5928'>passed</font></b>";
			last_played.setText(Html.fromHtml(line1));
		}
		else
		{
			last_played.setText("");
		}
	}
	
	protected void onResume() 
	{
		super.onResume();
	    this.sensorHelper.onResume();
	}
	 
	protected void onPause() 
	{
		super.onPause();
		this.sensorHelper.onPause();
	}
	
	protected void onDestroy()
	{
		super.onDestroy();
		//writeFile();
	}
	
	// Things to save => board (225 bytes), current racks and scores (22 bytes), letters in bag (27 bytes), blank positions (2 bytes)
	// Flags byte 1 => 
	//    Bit 0 => 0 for player, 1 for other
	//    Bit 1 => isFirstMove
	public void writeFile()
	{
		byte[] saveFile = new byte[225 + 22 + 27 + 2 + 1];
		int currentByte = 0;
		GameView gv = (GameView) findViewById(R.id.game_view);
		for(int row = 1; row <= gv.gameBoardManager.getHeight(); row++)
		{
			for(int col = 1; col <= gv.gameBoardManager.getHeight(); col++)
			{
				saveFile[currentByte++] = (byte) gv.gameBoardManager.gameBoard.scrabbleSolver.getLetterAt(row, col);
			}
		}
		
		for(int i = 0; i < gv.gameBoardManager.gameManager.players.length; i++)
		{
			Player p = gv.gameBoardManager.gameManager.players[i];
			int added = 0;
			for(int j = 0; j < 26; j++)
			{
				for(int k = 0; k < p.currentRack[j] && added < 7; k++)
				{
					added++;
					saveFile[currentByte++] = (byte) ('a' + j);
				}	
			}
			while(added < 7)
			{
				added++;
				saveFile[currentByte++] = 0;
			}
			byte[] score = intToByte(p.score);
			for(int j = 0; j < 4; j++)
				saveFile[currentByte++] = score[j];
		}
		
		for(int i = 0; i < 27; i++)
			saveFile[currentByte++] = (byte) (gv.gameBoardManager.gameBoard.scrabbleSolver.currentBag[i]);
		
		byte[] blankPos = gv.gameBoardManager.gameBoard.scrabbleSolver.getBlankPositions();
		for(int i = 0; i < blankPos.length; i++)
			saveFile[currentByte++] = blankPos[i];
		
		
		byte flags = (byte)(gv.gameBoardManager.gameManager.currentPlayerNo == 1 ? 1 : 0);
		flags |= (byte)(gv.gameBoardManager.isFirstMove ? 2 : 0);
				
		saveFile[currentByte++] = flags;
		
		currentByte = 0;
		for(int i = 0; i < 15; i++)
		{
			String s = "";
			for(int j = 0; j < 15; j++)
			{
				s += (char)(saveFile[currentByte] == 0 ? '0' : saveFile[currentByte]) + " ";
				currentByte++;
			}
			Log.d(ScrabbleSolver.TAG, s);
		}
		
		for(int i = 0; i < 2; i++)
		{
			String s = "";
			for(int j = 0; j < 7; j++)
			{
				s += (char)(saveFile[currentByte++]) + " ";
			}
			
			s += "::";
			for(int j = 0; j < 4; j++)
				s += saveFile[currentByte++] + " ";
			Log.d(ScrabbleSolver.TAG, s);
		}
		
		String s = "";
		for(int i = 0; i < 27; i++)
			s += saveFile[currentByte++] + " ";
		Log.d(ScrabbleSolver.TAG, s);
		
		s = "";
		for(int i = 0; i < 2; i++)
			s += saveFile[currentByte++] + " ";
		Log.d(ScrabbleSolver.TAG, s);
		
		Log.d(ScrabbleSolver.TAG, saveFile[currentByte++] + " ");
		
		FileHelper.write(FileHelper.GAMES, this.m_otherId, saveFile, this, false);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		for(String word : gv.gameBoardManager.gameBoard.scrabbleSolver.playedWords)
		{
			for(int i = 0; i < word.length(); i++)
				baos.write(word.charAt(i));
			baos.write(0);
		}
		
		FileHelper.write(FileHelper.GAMES, this.m_otherId, baos.toByteArray(), this, true);
	}
	
	// Things to save => board (225 bytes), current racks and scores (22 bytes), letters in bag (27 bytes), blank positions (2 bytes)
	// Flags byte 1 => 
	//    Bit 0 => 0 for player, 1 for other
	//    Bit 1 => isFirstMove
	public void readFile()
	{
		if(!FileHelper.fileExists(FileHelper.GAMES, this.m_otherId, this))
			return;
		byte[] data = FileHelper.read(FileHelper.GAMES, this.m_otherId, this);
		GameView gv = (GameView) findViewById(R.id.game_view);
		int currentByte = 0;
		for(int i = 0; i < 225; i++)
		{
			gv.gameBoardManager.gameBoard.scrabbleSolver.addToBoard(i / 15 + 1, i % 15 + 1, (char)data[currentByte++]);
		}
		
		for(int i = 0; i < gv.gameBoardManager.gameManager.players.length; i++)
		{
			Player p = gv.gameBoardManager.gameManager.players[i];
			for(int j = 0; j < 27; j++)
				p.currentRack[j] = 0;
			for(int j = 0; j < 7; j++)
			{
				if(data[currentByte] != 0)
					p.currentRack[(int)(data[currentByte] - 'a')]++;
				currentByte++;
			}
			int score = (data[currentByte++] << 24) | (data[currentByte++] << 16) | (data[currentByte++] << 8) | data[currentByte++];
			p.score = score;
		}
		
		gv.gameBoardManager.gameBoard.scrabbleSolver.cleanBag();
		for(int i = 0; i < 27; i++)
		{
			gv.gameBoardManager.gameBoard.scrabbleSolver.addLetterToBag((char) ('a' + i), (char)data[currentByte++]);
		}
		
		for(int i = 0; i < 2; i++)
		{
			int pos = (data[currentByte] & 0xFF) - 1;
			if(pos >= 0)
				gv.gameBoardManager.gameBoard.scrabbleSolver.addBlank(pos / 15 + 1, pos % 15 + 1);
			currentByte++;
		}
		
		int flags = data[currentByte++];
		gv.gameBoardManager.gameManager.currentPlayerNo = flags & 1;
		gv.gameBoardManager.isFirstMove = (flags & 2) == 2;
		
		String playedWord = "";
		for(int i = currentByte; i < data.length; i++)
		{
			if(data[i] == 0)
			{
				gv.gameBoardManager.gameBoard.scrabbleSolver.playedWords.add(playedWord);
				playedWord = "";
			}
			else
				playedWord += (char)(data[i]);
		}
		
		gv.gameBoardManager.updateCurrentLetters(gv.gameBoardManager.gameManager.getCurrentPlayer().currentRack);
		gv.gameBoardManager.gameBoard.tempCharacters.clear();
		gv.gameBoardManager.gameBoard.tempBlankPosition.clear();
	}
	
	public static byte[] intToByte(int num)
	{
		byte[] bytes = new byte[4];
		for(int i = 0; i < 4; i++)
		{
			bytes[i] = (byte) (num >> (8 * (3 - i)));
		}
		
		return bytes;
	}
}
