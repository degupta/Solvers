package com.example.exercise101;

import android.content.Context;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter
{
	protected final Context mContext;
	protected static ArrayList<GameSelector> items = new ArrayList<GameSelector>();
	
	public static final int[] LETTER_SCORES = {
		1,
		4,
		4,
		2,
		1,
		4,
		3,
		3,
		1,
		10,
		5,
		2,
		4,
		2,
		1,
		4,
		10,
		1,
		1,
		1,
		2,
		5,
		4,
		8,
		3,
		10,
		0
	};
	
	public MenuAdapter(final Context pContext) {
		this.mContext = pContext;
	}
	
	@Override
	public View getView(final int pPosition, final View pConvertView, final ViewGroup pParent) 
	{
		final View view;
		if (pConvertView != null) {
			view = pConvertView;
		} else {
			view = LayoutInflater.from(this.mContext).inflate(R.layout.game_selector_layout, null);
		}

		GameSelector game = this.getItem(pPosition);

		/* Set the name of the country: */
		String firstLetter = game.friendName.substring(0, 1).toUpperCase();
		final TextView letter = (TextView) view.findViewById(R.id.player_first_letter);
		letter.setText(firstLetter);
		
		final TextView letterScore = (TextView) view.findViewById(R.id.player_first_letter_score);
		letterScore.setText(getScore(firstLetter));
		
		final TextView friend_name = (TextView) view.findViewById(R.id.friend_name);
		friend_name.setText("Example With Friends with " + game.friendName);
		
		final TextView date = (TextView) view.findViewById(R.id.date);
		date.setText(game.date);
		
		final TextView last_move = (TextView) view.findViewById(R.id.last_move);
		last_move.setText("Last move " + game.lastPlay + " days ago");
		
		if(pPosition == 0)
			view.setBackgroundResource(R.drawable.rounded_top);
		else if(pPosition == MenuAdapter.items.size() - 1)
			view.setBackgroundResource(R.drawable.rounded_bottom);
		else
			view.setBackgroundColor(0xFFFFFFFF);

		return view;
	}

	private String getScore(String firstLetter) 
	{
		return LETTER_SCORES[firstLetter.charAt(0) - 'A'] + "";
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public GameSelector getItem(final int pPosition) {
		return items.get(pPosition);
	}

	@Override
	public long getItemId(final int pPosition) {
		return pPosition;
	}
}
