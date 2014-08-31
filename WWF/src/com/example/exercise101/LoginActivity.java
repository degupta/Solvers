package com.example.exercise101;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.widget.Button;
import android.view.*;
import android.widget.EditText;

public class LoginActivity extends Activity {
	
	public static String USER_ID = "user_id";
	private String m_userId = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		m_userId = SharedPreferencesCompat.getPreferenceString(SharedPreferencesCompat.USER_ID_PREFERENCE, this);
		if(this.m_userId != null && this.m_userId.length() > 0)
		{
			openMenu();
			return;
		}
		
		Button connectbutton = (Button) findViewById(R.id.connect_button);
		connectbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok));
            		tryLogin();
            	}
            	return true;
            }
        });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
	    if (keyCode == KeyEvent.KEYCODE_BACK
	            && event.getRepeatCount() == 0) {

	        finish();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	public void tryLogin()
	{
		EditText userIdText = (EditText) findViewById(R.id.user_id);
		if(userIdText.getText().toString().length() == 0)
		{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() 
			{
			    public void onClick(DialogInterface dialog, int which) 
			    {
			    	dialog.dismiss();
			    }
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Login Failed").setMessage("Please re-enter you user id").setPositiveButton("Ok", dialogClickListener).show();
		}
		else
		{
			this.m_userId = userIdText.getText().toString();
			openMenu();
		}
	}
	
	public void openMenu()
	{
		SharedPreferencesCompat.putString(SharedPreferencesCompat.USER_ID_PREFERENCE, this.m_userId, this);
		Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
		intent.putExtra(USER_ID, this.m_userId);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

}
