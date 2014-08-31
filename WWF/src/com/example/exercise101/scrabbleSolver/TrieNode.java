package com.example.exercise101.scrabbleSolver;
import java.util.HashMap;
import java.io.*;
import java.util.Set;
public class TrieNode 
{	
	char c;
	HashMap<Character, TrieNode> children;
	boolean isFinal = false;
	public TrieNode(char _c)
	{
		c = _c;
		children = new HashMap<Character, TrieNode>();
	}
	
	public void addChild(TrieNode child)
	{
		children.put(child.c, child);
	}
	
	public boolean hasLetter(char _c)
	{
		return children.get(_c) != null;
	}
	
	public TrieNode getChildNode(char _c)
	{
		return children.get(_c);
	}
	
	public boolean isFinal()
	{
		return this.isFinal;
	}
	
	public void addWord(String word)
	{
		char _c = word.charAt(0);
		TrieNode node = getChildNode(_c);
		if(node == null)
		{
			node = new TrieNode(_c);
			this.addChild(node);
		}
		
		if(word.length() > 1)
			node.addWord(word.substring(1));
		else
			node.isFinal = true;
	}
	
	public Set<Character> getEdges()
	{
		return this.children.keySet();
	}
	
	public boolean wordExists(String word)
	{
		if(word.length() == 0)
			return this.isFinal();
		char _c = word.charAt(0);
		TrieNode node = getChildNode(_c);
		return node == null ? false : node.wordExists(word.substring(1));
	}
	
	public void printTrie(int tabs)
	{
		for(int i = 0; i < tabs; i++)
			System.out.print("\t");
		System.out.print(this.c);
		System.out.println();
		Set<Character> edges = this.getEdges();
		for(Character edge : edges)
			this.children.get(edge).printTrie(tabs + 1);
	}
	
	public static TrieNode createTrie(InputStream inputStream)
	{
		try 
		{
			//reader = new BufferedReader(new FileReader(wordListPath));
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			TrieNode root = new TrieNode(ScrabbleSolver.NULL_CHAR);
			String word = reader.readLine();
			while(word != null) 
			{
				if (word.length() <= 15)
					root.addWord(word.toLowerCase());
				word = reader.readLine();
			}
		
			return root;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
