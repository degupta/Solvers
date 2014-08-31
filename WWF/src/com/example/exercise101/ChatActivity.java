package com.example.exercise101;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.view.inputmethod.InputMethodManager;

public class ChatActivity extends Activity {
	private String m_userId = "";
	private String m_otherId = "";
	private ListView chatList;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		this.m_userId = getIntent().getExtras().getString(LoginActivity.USER_ID);
		this.m_otherId = getIntent().getExtras().getString(MenuActivity.OTHER_PLAYER);
		
		TextView tv = (TextView) findViewById(R.id.chat_title);
		tv.setText(tv.getText() + this.m_otherId);
		
		chatList = (ListView) this.findViewById(R.id.chat_list);
		chatList.setAdapter(new ChatAdapter(this));
		
		this.setUpChat();
		
		Button chatbutton = (Button) findViewById(R.id.chat_button);
		chatbutton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent evt) {
            	if(evt.getAction() == MotionEvent.ACTION_DOWN)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok_down));
            	}
            	else if(evt.getAction() == MotionEvent.ACTION_UP)
            	{
            		((Button) v).setBackground(getResources().getDrawable(R.drawable.button_ok));
            		EditText msgIdText = (EditText) findViewById(R.id.chat_message);
            		msgIdText.clearFocus();
            		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            		imm.hideSoftInputFromWindow(msgIdText.getWindowToken(), 0);
            		addChatToDataBase(msgIdText.getText().toString(), true);
            		addChat(msgIdText.getText().toString(), "");
            		addChatToDataBase("Message Received", false);
            		addChat("", "Message Received");
            		msgIdText.setText("");
            	}
            	return true;
            }
        });
	}
	
	public void setUpChat()
	{
		ChatAdapter.items.clear();
		String selection = "name =?";
		String[] selectionArgs = new String[] {this.m_otherId};
		WWFOpenHelper.query(this, WWFOpenHelper.CHAT_TABLE_NAME, WWFOpenHelper.CHAT_TABLE_COLUMN_DEF, WWFOpenHelper.CHAT_TABLE_COLUMNS,
				selection, selectionArgs, null, null, "timestamp", new ICallback() {
				@SuppressWarnings("unchecked")
				@Override
				public void callback(Object param) 
				{
					if(param == null)
						return;
					ArrayList<Object[]> results = (ArrayList<Object[]>) param;
					for(int i = 0; i < results.size(); i++)
					{
						if(((Integer)results.get(i)[1]) == 1)
						{
							addChat((String)results.get(i)[0], "");
						}
						else
						{
							addChat("", (String)results.get(i)[0]);
						}
					}
				}
		});
	}
	
	public void addChatToDataBase(String msg, boolean byMe)
	{
		Object[][] insertValues = new Object[][] 
		{
			{"name", this.m_otherId},
			{"byMe", byMe ? 1 : 0},
			{"timeStamp", System.currentTimeMillis()},
			{"message", msg}
		};
		WWFOpenHelper.insert(this, insertValues, WWFOpenHelper.CHAT_TABLE_NAME, WWFOpenHelper.CHAT_TABLE_COLUMN_DEF);
	}
	
	public void addChat(String msg, String otherMsg)
	{
		ChatAdapter.items.add(new String[] {msg, otherMsg});
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

}
