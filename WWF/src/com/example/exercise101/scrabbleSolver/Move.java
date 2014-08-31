package com.example.exercise101.scrabbleSolver;
import android.util.SparseArray;
public class Move 
{
	public int row, col;
	public String word;
	public boolean down;
	public int score;
	public SparseArray<String> crossWords = new SparseArray<String>();
	public Move(String _word, int _row, int _col, boolean _down, int _score)
	{
		this.word = _word;
		this.row = _row;
		this.col = _col;
		this.down =_down;
		this.score = _score;
	}
	
	public Move()
	{
		
	}
	
	public Move(Move move)
	{
		this.word = move.word;
		this.row = move.row;
		this.col = move.col;
		this.down = move.down;
		this.score = move.score;
		this.crossWords = move.crossWords;
	}
	
	public String toString()
	{
		String str = word + " row " + row + " column " + col + " and going " + (down ? "down" : "across") + " for score " + score + " crossWords : [";
		int len = this.crossWords.size();
		for(int i = 0; i < len; i++)
		{
			str += crossWords.valueAt(i);
			if(i < len - 1)
				str += ", ";
		}
		str += "]";
		
		return str;
	}
}
