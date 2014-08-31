package com.example.exercise101.scrabbleSolver;
import java.util.HashSet;
public class CrossCheck
{
	public boolean isDirty = false;
	HashSet<Character> acrossChecks = new HashSet<Character>();
	HashSet<Character> downChecks = new HashSet<Character>();
	
	public boolean contains(char c, boolean checkDown)
	{
		return checkDown ? downChecks.contains(c) : acrossChecks.contains(c);
	}
	
	public void addCrossCheck(char c, boolean checkDown)
	{
		if(checkDown)
			downChecks.add(c);
		else
			acrossChecks.add(c);
	}
}
