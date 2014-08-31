package com.example.exercise101;

import android.content.Context;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter 
{
	protected final Context mContext;
	protected static ArrayList<String[]> items = new ArrayList<String[]>();
	
	public ChatAdapter(final Context pContext) {
		this.mContext = pContext;
	}
	
	@Override
	public View getView(final int pPosition, final View pConvertView, final ViewGroup pParent) 
	{
		final View view;
		if (pConvertView != null) {
			view = pConvertView;
		} else {
			view = LayoutInflater.from(this.mContext).inflate(R.layout.chat_layout, null);
		}

		final String msg[] = this.getItem(pPosition);

		/* Set the name of the country: */
		final TextView msgTextView = (TextView) view.findViewById(R.id.textMe);
		if(msg[0].length() > 0)
		{
			msgTextView.setVisibility(View.VISIBLE);
			msgTextView.setText(msg[0]);
		}
		else
			msgTextView.setVisibility(View.INVISIBLE);
		
		final TextView msgTextView2 = (TextView) view.findViewById(R.id.textOther);
		if(msg[1].length() > 0)
		{
			msgTextView2.setVisibility(View.VISIBLE);
			msgTextView2.setText(msg[1]);
		}
		else
			msgTextView2.setVisibility(View.INVISIBLE);

		return view;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public String[] getItem(final int pPosition) {
		return items.get(pPosition);
	}

	@Override
	public long getItemId(final int pPosition) {
		return pPosition;
	}
}
