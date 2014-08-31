package com.example.exercise101;

public class Player 
{
	public int[] currentRack = new int[27];
	public int score = 0;
	public int tilesInRack = 0;
	public boolean isComputer = false;
	
	public Player(boolean _isComputer)
	{
		this.isComputer  = _isComputer;
	}
}
