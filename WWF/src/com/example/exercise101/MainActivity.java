package com.example.exercise101;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.os.Handler;

public class MainActivity extends Activity {
	private Handler mHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mHandler = new Handler();
		mHandler.postDelayed(mUpdateTimeTask, 3000);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private Runnable mUpdateTimeTask = new Runnable() {
	   public void run() {
		   Intent intent = new Intent(MainActivity.this, LoginActivity.class);
	       startActivity(intent);
	       finish();
	   }
	};

}
