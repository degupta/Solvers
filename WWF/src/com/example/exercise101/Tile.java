package com.example.exercise101;

import com.example.exercise101.scrabbleSolver.ScrabbleSolver;


public class Tile 
{
	public enum TileType
	{
		BLANK(R.drawable.tile_blank, 'b'),
		DOUBLE_LETTER(R.drawable.tile_double_letter, 'd'),
		TRIPLE_LETTER(R.drawable.tile_triple_letter, 't'),
		DOUBLE_WORD(R.drawable.tile_double_word, 'D'),
		TRIPLE_WORD(R.drawable.tile_triple_word, 'T'),
		CENTER(R.drawable.tile_center, 'c');
		
		private int resId;
		private char identifier;
		private TileType(int _resType, char _identifier)
		{
			resId = _resType;
			identifier = _identifier;
		}
		
		public int getResId()
		{
			return resId;
		}
		
		public char getIdentifier()
		{
			return identifier;
		}
	}
	
	private TileType tileType;
	private char letter;
	private byte player;
	
	public Tile(TileType _tileType)
	{
		this.tileType = _tileType;
		this.letter = ScrabbleSolver.NULL_CHAR;
	}
	
	public TileType getTileType()
	{
		return this.tileType;
	}
	
	public char getLetter()
	{
		return letter;
	}
	
	public void setLetter(char _letter)
	{
		this.letter = _letter;
	}
	
	public byte getPlayer()
	{
		return player;
	}
	
	public void setPlayer(byte _player)
	{
		this.player = _player;
	}
	
	public static TileType getTileType(char c)
	{
		
		for(TileType tile : TileType.values())
		{
			if(tile.getIdentifier() == c)
				return tile;
		}
		
		return null;
	}	
}
