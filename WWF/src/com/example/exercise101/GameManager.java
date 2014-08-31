package com.example.exercise101;

public class GameManager 
{
	public Player[] players = new Player[2];
	public int currentPlayerNo;
	public GameManager(int _currentPlayer)
	{
		for(int i = 0; i < players.length; i++)
			players[i] = new Player(i == 1);
		currentPlayerNo = _currentPlayer;
	}
	
	public Player getCurrentPlayer()
	{
		return this.players[this.currentPlayerNo];
	}
	
	public boolean currentPlayerIsComputer()
	{
		return this.getCurrentPlayer().isComputer;
	}
	
	public void switchPlayer()
	{
		this.currentPlayerNo = (this.currentPlayerNo + 1) % this.players.length;
	}
}
