package com.example.exercise101;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import com.example.exercise101.scrabbleSolver.DawgArray;
import com.example.exercise101.scrabbleSolver.DifficultyLevelSelector;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class MenuActivity extends Activity {
	public static final String OTHER_PLAYER = "otherPlayer";
	public static final String DIFFICULTY = "difficulty";
	
	private String m_userId = "";
	private String xpromoAppId = "";
	private Bitmap xpromoBmp = null;
	private ListView gameList;
	private GameSelector selectedGame = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		this.m_userId = getIntent().getExtras().getString(LoginActivity.USER_ID);
		
		gameList = (ListView) this.findViewById(R.id.game_list);
		gameList.setAdapter(new MenuAdapter(this));
		
		this.setUpXPromo();
		
		MenuAdapter.items.clear();
		MenuAdapter.items.add(new GameSelector("Devansh", "Feb 28th, 2013 12:20PM", 5));
		MenuAdapter.items.add(new GameSelector("Surith", "Feb 21st, 2013 1:20PM", 6));
		MenuAdapter.items.add(new GameSelector("Jason", "Feb 23rd, 2013 2:20PM", 7));
		MenuAdapter.items.add(new GameSelector("Nicolas", "Mar 23rd, 2013 8:20PM", 7));
		
		((MenuAdapter)gameList.getAdapter()).notifyDataSetChanged();
		
		gameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {  
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				GameSelector game = ((MenuAdapter)gameList.getAdapter()).getItem(position);
				MenuActivity.this.selectedGame = game;
				startGame();
			}
	      });
		
		String betweenText = "Unknow Version";
		try 
		{
			betweenText = "@" + this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName + " (build #1)";
		} 
		catch (NameNotFoundException e) 
		{
		}
		
		TextView buildText = (TextView) this.findViewById(R.id.build_text);
		buildText.setText("Example With Friends, " + betweenText + ", @ 2013 Zynga Inc.");
		
		
		Button logoutbutton = (Button) findViewById(R.id.logout_button);
		logoutbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok));
            		logout();
            	}
            	return true;
            }
        });
		
		
		/*
		Log.d("DAWG", "DAWG STARTED");
		long start = System.currentTimeMillis();
		DawgArray dawgArray = new DawgArray(this.getResources().openRawResource(R.raw.dict));
		Log.d("DAWG", "DAWG FINISHED : Took " + (System.currentTimeMillis() - start) / 1000.0 + " seconds");
		
		Log.d("DAWG", "GETTING WORD LIST");
		ArrayList<String> words = this.getWords();
		Log.d("DAWG", "Testing dawg created from file for Array Lookup... ");
		start = System.currentTimeMillis();
		int numBadWords = 0;
		for(String word : words)
		{
			if(!dawgArray.wordExists(word))
				numBadWords++;
		}
		Log.d("DAWG", "Finished testing dawg created from file for Array Lookup. No. of bad words " + numBadWords + " out of " + (words.size()) + ". It only took " + (System.currentTimeMillis() - start) / 1000.0 + " secs.");
		*/
	}
	
	public void startGame()
	{
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.level_selector_dialog, null);

		String array_spinner[] = new String[DifficultyLevelSelector.MAX_LEVEL - DifficultyLevelSelector.MIN_LEVEL + 2];
		
		for(int i = 0; i < array_spinner.length - 1; i++)
			array_spinner[i] = (i + DifficultyLevelSelector.MIN_LEVEL) + "";
		
		array_spinner[array_spinner.length - 1] = "Insane";

		Spinner s = (Spinner) layout.findViewById(R.id.level_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, array_spinner);
		s.setAdapter(adapter);

		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
		{
		    @Override
		    public void onClick(DialogInterface dialog, int which) 
		    {
		        switch (which)
		        {
		        	case DialogInterface.BUTTON_POSITIVE:
		        		Spinner s = (Spinner) ((AlertDialog) dialog).findViewById(R.id.level_spinner);
		        		goToGame(s.getSelectedItemPosition() + 1);
		        		dialog.dismiss();
		        		break;

			        case DialogInterface.BUTTON_NEGATIVE:
			            dialog.dismiss();
			            break;
		        }
		    }
		};
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		builder.setMessage("Please select level difficulty").setPositiveButton("Start", dialogClickListener)
		    .setNegativeButton("Cancel", dialogClickListener).show();
	}
	
	public void goToGame(int difficulty)
	{
		Intent intent = new Intent(MenuActivity.this, GameActivity.class);
		intent.putExtra(LoginActivity.USER_ID, m_userId);
		intent.putExtra(OTHER_PLAYER, this.selectedGame.friendName);
		intent.putExtra(DIFFICULTY, difficulty);
		startActivity(intent);
	}
	
	public ArrayList<String> getWords()
	{
		ArrayList<String> words = new ArrayList<String>();
		try 
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.getResources().openRawResource(R.raw.words)));
			String word = reader.readLine();
			while(word != null) 
			{
				words.add(word.toLowerCase());
				word = reader.readLine();
			}
		}
		catch (Exception e) 
		{
			Log.e("DAWG", "WORD LIST ERROR", e);
		}
		
		return words;
	}
	
	private void setUpXPromo() 
	{
		final AsyncTask<Void, Integer, Boolean> asynctask = new AsyncTask<Void, Integer, Boolean>() 
		{	
			@Override 
			protected Boolean doInBackground(Void ... params) 
			{
				URL url;
				HttpURLConnection urlConnection = null;
				boolean success = false;
				try 
				{
					url = new URL("http://pastebin.com/raw.php?i=Bb5GRpDT");
					urlConnection = (HttpURLConnection) url.openConnection();
				    InputStream in = urlConnection.getInputStream();
				    String line = "";
				    String json = "";
				    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				    while( (line = reader.readLine()) != null )
				    	json += line;
				   
				    urlConnection.disconnect();
				    
				    JSONObject xpromoJson = new JSONObject(json);
				    xpromoAppId = xpromoJson.getString("app");
				    
				    success = true;
				    
				    byte[] imageBytes = null;
				    String cachedFileName = FileHelper.XPROMO_IMAGE_FILE_PREFIX + "_" + xpromoAppId;
				    if(FileHelper.fileExistsInCache(cachedFileName, MenuActivity.this))
				    {
				    	imageBytes = FileHelper.readFromCache(cachedFileName, MenuActivity.this);
				    }
				    else
				    {
					    url = new URL(xpromoJson.getString("image"));
						urlConnection = (HttpURLConnection) url.openConnection();
					    in = urlConnection.getInputStream();
					    reader = new BufferedReader(new InputStreamReader(in));
					    
					    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
					    int c;
					    while ((c = in.read()) != -1)
					    	byteArrayOut.write(c);
					    
					    imageBytes = byteArrayOut.toByteArray();
					    
					    FileHelper.writeToCache(cachedFileName, imageBytes, MenuActivity.this);
				    }
				    
				    if(imageBytes != null)
				    	xpromoBmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
				} 
				catch(Exception e)
				{
					Log.e("XPROMO_ERROR", "XPROMO_ERROR", e);
				}
				finally 
				{
					if(urlConnection != null)
						urlConnection.disconnect();
				}
				
				return success;
			}

			protected void onPostExecute(Boolean result) 
			{
				super.onPostExecute(result);
		    	if(result)
		    	{
				    ImageButton xpromobutton = (ImageButton) findViewById(R.id.xpromo_game_icon);
				    if(xpromoBmp != null)
				    	xpromobutton.setImageBitmap(xpromoBmp);
				    xpromobutton.setOnTouchListener(new View.OnTouchListener() {
			            public boolean onTouch(View v, MotionEvent evt) {
			            	if(evt.getAction() == MotionEvent.ACTION_UP)
			            	{
			            		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+ xpromoAppId)));
			            	}
			            	return true;
			            }
			        });
		    	}
			}
		};
				
		asynctask.execute((Void[]) null);
	}

	public void logout()
	{
		SharedPreferencesCompat.putString(SharedPreferencesCompat.USER_ID_PREFERENCE, "", this);
		Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}

}
