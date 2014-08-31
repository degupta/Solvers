package com.example.exercise101.scrabbleSolver;
import java.util.ArrayList;
public interface IMoveSelector 
{
	/*
	 * Selects a move from the arraylist.
	 * If you want to pass return null;
	 */
	public Move select(ArrayList<Move> moves);
}
