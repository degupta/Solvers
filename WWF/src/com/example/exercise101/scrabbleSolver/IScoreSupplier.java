package com.example.exercise101.scrabbleSolver;

public interface IScoreSupplier 
{
	public int getWordMultiplier(int row, int col);
	public int getLetterMultiplier(int row, int col);
	public int getLetterScore(char c);
	public int getBoardSize();
}
