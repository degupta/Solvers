package com.example.exercise101.scrabbleSolver;
import java.util.ArrayList;
public class PlayContext 
{
	public static final int SUCCESS = 0;
	public static final int NOT_IN_A_STRAIGHT_LINE_ERROR = 1;
	public static final int NOT_LEGAL_WORDS_ERROR = 2;
	public static final int WORD_ALREADY_MADE_ERROR = 3;
	public static final int NOT_JOINED = 4;
	public static final int NOT_ENOUGH_LETTERS = 5;
	public ArrayList<String> badWords = new ArrayList<String>();
	public Move move;
	public int result;
}
