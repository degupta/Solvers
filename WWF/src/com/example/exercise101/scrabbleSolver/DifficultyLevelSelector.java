package com.example.exercise101.scrabbleSolver;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class DifficultyLevelSelector extends NormalMoveSelector
{
	public static final int SORT_ALL = 0;
	public static final int SORT_BY_UNIQUE_WORDS = 1;
	public static final int DISTRIBUTION_OVER_SCORE = 2;
	public static final int HIGHEST_SCORE = 3;
	
	public static final int MIN_LEVEL = 1;
	public static final int MAX_LEVEL = 10;
	public static final double MID_LEVEL = (MAX_LEVEL + MIN_LEVEL) / 2.0;
	
	double sigma = 1.0;
	double invSigma = 0.0;
	double fAlpha = 0.0;
	double fudgeFactor = 0.0;
	int technique;
	public DifficultyLevelSelector(int _difficulty, int _technique)
	{
		this.technique = _technique;
		fAlpha = _difficulty - MID_LEVEL;
		sigma = fAlpha / Math.sqrt(1 + fAlpha * fAlpha);
		invSigma =  Math.sqrt(1 - sigma * sigma);
		fudgeFactor = fAlpha / (MID_LEVEL - 1) * 0.75;
	}
	
	public double[] randNormPolar()
	{
		double[] rands = new double[2]; 
		double x, y, w;
		do
		{
		    x = 2.0 * Math.random() - 1.0; 
		    y = 2.0 * Math.random() - 1.0; 
		    w = x * x + y * y;
		}
		while(w == 0 || w >= 1);
		     
		w = Math.sqrt((-2.0 * Math.log(w)) / w); 
		rands[0] = x * w; 
		rands[1] = y * w;
		
		return rands;
	}
	
	public int getRandomPosition(int size)
	{
		double[] randNormPolar = this.randNormPolar();
		double mean = (size - 1.0d) / 2.0d;
		double scale = mean * 0.34134 * (1 - Math.abs(fudgeFactor));
		double u1 = sigma * randNormPolar[0]  + invSigma * randNormPolar[1]; 
		return (int) Math.round( (randNormPolar[0] >= 0 ? u1 : -u1) * scale + mean * (1 + fudgeFactor) );  
	}
	
	public void sortByUniqueWords(ArrayList<Move> moves)
	{
		HashMap<String, Move> uniqueMoves = new HashMap<String, Move>();
		for(Move move : moves)
		{
			String key = move.word.replace("/", "");
			if(uniqueMoves.containsKey(key))
			{
				Move m = uniqueMoves.get(key);
				if(m.score > move.score)
					uniqueMoves.put(key, m);
			}
			else
				uniqueMoves.put(key, move);
		}
		moves.clear();
		moves.addAll(uniqueMoves.values());
		super.sort(moves);
	}
	
	public Move select(ArrayList<Move> moves) 
	{
		if(moves.size() == 0)
			return null;
		switch(this.technique)
		{
			case DISTRIBUTION_OVER_SCORE :
				Move highest = findHighestMove(moves);
				int randScore = getRandomPosition(highest.score);
				Log.d("MOVE_SELECTOR", "Selecting score : " + randScore + " from " + highest.score);
				if(randScore <= 0 || randScore > highest.score)
					return null;
				else
					return getClosestMoveToScore(moves, randScore);
			case HIGHEST_SCORE :
				return findHighestMove(moves);
			case SORT_ALL:
			case SORT_BY_UNIQUE_WORDS :
			default:
				return super.select(moves);
		}
	}
	
	private Move findHighestMove(ArrayList<Move> moves) 
	{
		int size = moves.size();
		Move maxMove = moves.get(0);
		for(int i = 1; i < size; i++)
		{
			if(moves.get(i).score >= maxMove.score)
				maxMove = moves.get(i);
		}
		
		return maxMove;
	}

	private Move getClosestMoveToScore(ArrayList<Move> moves, int randScore) 
	{
		int size = moves.size();
		Move closestMove = moves.get(0);
		int closestScore = Math.abs(closestMove.score - randScore);
		Move curMove = null;
		for(int i = 1; i < size; i++)
		{
			curMove = moves.get(i);
			if(Math.abs(randScore - curMove.score) <= closestScore && (this.fAlpha <= 0 ? curMove.score < closestMove.score : curMove.score >= closestMove.score))
			{
				closestMove = curMove;
				closestScore = Math.abs(closestMove.score - randScore);
			}
		}
		return closestMove;
	}

	public void sort(ArrayList<Move> moves)
	{
		switch(this.technique)
		{
			case SORT_BY_UNIQUE_WORDS :
				this.sortByUniqueWords(moves);
				break;
			case DISTRIBUTION_OVER_SCORE :
			case HIGHEST_SCORE :	
			case SORT_ALL:
			default:
				super.sort(moves);
		}
	}
}
