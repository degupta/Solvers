package com.example.exercise101.scrabbleSolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.util.Log;
public class NormalMoveSelector implements IMoveSelector 
{
	public class MoveComparator implements Comparator<Move>
	{
	    public int compare(Move move1, Move move2) 
	    {
	        return move1.score - move2.score;
	    }
	}
	
	public double randNorm()
	{
		double U1 = Math.random();
		double U2 = Math.random();
		return Math.sqrt(-2 * Math.log(U1)) * Math.cos(2 *  Math.PI * U2);
	}
	
	public int getRandomPosition(int size)
	{
		double rand = randNorm();
		double half = size % 2 == 0? size / 2.0 - 1 : (size - 1) / 2.0;
		int randPos = (int)(rand * (half * 0.34134) + half);
		return randPos;
	}
	
	public void sort(ArrayList<Move> moves)
	{
		Collections.sort(moves, new MoveComparator());
	}
	
	public Move select(ArrayList<Move> moves) 
	{
		if(moves.size() == 0)
			return null;
		sort(moves);
		int pos = getRandomPosition(moves.size());
		Log.d("MOVE_SELECTOR", "Selecting " + (pos + 1) + " from " + moves.size());
		if(pos < 0 || pos > moves.size() - 1)
			return null;
		else
			return moves.get(pos);
	}
	
	public static void main(String args[])
	{
		int size = 30, num = 100000;
		int arr[] = new int[size];
		double half = size % 2 == 0? size / 2.0 - 1 : (size - 1) / 2.0;
		int lowerBound = (int)Math.round(half - half * 0.34134);
		int upperBound = (int)Math.round(half + half * 0.34134);
		int inSD1 = 0;
		int passes = 0;
		NormalMoveSelector sel = new NormalMoveSelector();
		for(int i = 0; i < num; i++)
		{
			int pos = sel.getRandomPosition(size);
			if(pos < 0 || pos > size - 1)
				passes++;
			else
			{
				arr[pos]++;
				if(pos >= lowerBound && pos <= upperBound)
					inSD1++;
			}
		}
		
		System.out.println("Numbers within 1 SD of mean : " +  (double)inSD1 / num);
		System.out.println("Passes : " +  (double)passes / num);
		System.out.println("PDF : ");
		for(int i = 0; i < size; i++)
		{
			System.out.println(arr[i] / (double) num);
		}
	}

}
