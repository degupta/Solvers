package com.example.exercise101.scrabbleSolver;
import java.util.HashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import android.util.Log;
import java.io.InputStream;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.SparseArray;
public class ScrabbleSolver
{
	public static final String TAG = "SCRABBLE_SOLVER";
	
	public static final char NULL_CHAR = '\0';
	public static final char BLANK_TILE_RACK = 'z' + 1;
	public static final char BLANK_PREFIX = '/';
	
	
	private CrossCheck[][] crossChecks; 
	private char[][] currentBoard;
	private int[] currentRack = new int[27];
	private int[] actualRack;
	public int[] currentBag;
	public int currentLettersInBag = 0;
	private DawgArray dawgArray;
	private ArrayList<Move> legalMoves;
	private IMoveSelector moveSelector = null;
	private IScoreSupplier scoreSupplier = null;
	private HashSet<Integer> blankPositions = new HashSet<Integer>();
	public HashSet<String> playedWords = new HashSet<String>();
	private InputStream trieInputStream;
	public int BOARD_SIZE;
	
	public ScrabbleSolver(InputStream inputStream)
	{
		this(inputStream, null, null);
	}
	
	public ScrabbleSolver(InputStream inputStream, IMoveSelector _moveSelector, IScoreSupplier _scoreSupplier)
	{
		this.moveSelector = _moveSelector;
		this.scoreSupplier = _scoreSupplier;
		BOARD_SIZE = this.scoreSupplier != null ? this.scoreSupplier.getBoardSize() : 5; 
		crossChecks = new CrossCheck[BOARD_SIZE][BOARD_SIZE]; 
		currentBoard = new char[BOARD_SIZE + 2][BOARD_SIZE + 2];
		
		trieInputStream = inputStream;
		final AsyncTask<Void, Void, Void> asynctask = new AsyncTask<Void, Void, Void>() 
		{	
			@Override 
			protected Void doInBackground(Void ... params) 
			{
				long start = SystemClock.currentThreadTimeMillis();
				dawgArray = new DawgArray(trieInputStream);
				Log.d(TAG, "Trie created in " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0 + " seconds");
				return null;
			}
		};
		asynctask.execute((Void[]) null);
		
		for(int i = 0; i < BOARD_SIZE + 2; i++)
		{
			for(int j = 0; j < BOARD_SIZE + 2; j++)
			{
				currentBoard[j][i] = NULL_CHAR; 
			}
		}
	}
	
	public void addToBoard(int row, int col, char c)
	{
		this.currentBoard[row][col] = c;
	}
	
	public char getLetterAt(int row, int col)
	{
		return this.currentBoard[row][col];
	}
	
	public void setRack(int[] _rack)
	{
		this.actualRack = _rack;
		for(int i = 0; i < actualRack.length; i++)
			this.currentRack[i] = this.actualRack[i];
	}
	
	public void addToRack(char letter, int num)
	{
		addToRack(letter, num, false);
	}
	
	private void addToRack(char letter, int num, boolean removedBlank)
	{
		if(removedBlank)
			letter = BLANK_TILE_RACK;
		this.currentRack[letter - 'a'] += num;
	}
	
	private boolean removeFromRack(char letter, int num)
	{
		boolean removedBlank = false;
		if(this.currentRack[letter - 'a'] - num < 0)
		{
			letter = BLANK_TILE_RACK;
			removedBlank = true;
		}
		this.currentRack[letter - 'a'] -= num;
		return removedBlank;
	}
	
	private void removeFromActualRack(char letter, int num)
	{
		this.actualRack[letter - 'a'] -= num;
	}
	
	private boolean inRack(char c)
	{
		if(c < 'a' || c > BLANK_TILE_RACK )
		{
			Log.d(TAG, "--------TRYING TO GET FOR " + (int)c);
		}
		return currentRack[c - 'a'] > 0 || currentRack[BLANK_TILE_RACK - 'a'] > 0;
	}
	
	public void clearRack()
	{
		for(int i = 0; i < this.currentRack.length; i++)
			this.currentRack[i] = 0;
	}
	
	public int to1D(int row, int col)
	{
		return (row - 1) * BOARD_SIZE + col;
	}
	
	public void addBlank(int row, int col)
	{
		this.blankPositions.add(to1D(row, col));
	}
	
	public byte[] getBlankPositions()
	{
		byte[] blankPos = new byte[2];
		int i = 0;
		for(Integer pos : this.blankPositions)
		{
			blankPos[i] = (byte)(int)pos;
			i++;
		}
		for(int j = i + 1; j < 2; j++)
			blankPos[j] = 0;
		return blankPos;
	}
	
	public boolean isBlank(int row, int col)
	{
		return this.blankPositions.contains(to1D(row, col));
	}
	
	public MoveStoreObject getAndRemoveBlanks(String word)
	{
		MoveStoreObject mso = new MoveStoreObject();
		int len = word.length();
		int blankOffset = 0;
		char c;
		for(int i = 0; i < len; i++)
		{
			c = word.charAt(i);
			if(c == BLANK_PREFIX)
			{
				mso.blankPositions.add(i - blankOffset);
				blankOffset++;
			}
			else
				mso.word += c;
		}
		
		return mso;
	}
	
	public int getEndPosition(Move move)
	{
		int len = move.word.length();
		int blankOffset = 0;
		for(int i = 0; i < len; i++)
		{
			if(move.word.charAt(i) == BLANK_PREFIX)
				blankOffset++;
		}
		
		return this.to1D(move.row + (move.down ? len - blankOffset - 1 : 0), move.col + (move.down ? 0 : len - blankOffset - 1));
	}
	
	public void getMove(SparseArray<Character> tempChars, HashSet<Integer> tempBlanks, PlayContext pc, boolean isFirstMove)
	{
		Move move = new Move();
		
		int len = tempChars.size();
		int lowestRow = BOARD_SIZE + 1, lowestCol = BOARD_SIZE + 1;
		for(int i = 0; i < len; i++)
		{
			int pos = tempChars.keyAt(i);
			int row = pos / BOARD_SIZE + 1;
			int col = pos % BOARD_SIZE + 1;
			if(i == 1)
			{
				move.down = lowestRow != row;
			}
			else if(i > 1)
			{
				if( (!move.down && row != lowestRow) || (move.down && col != lowestCol) )
				{
					pc.result = PlayContext.NOT_IN_A_STRAIGHT_LINE_ERROR;
				}
			}
			
			if(row < lowestRow)
				lowestRow = row;
			if(col < lowestCol)
				lowestCol = col;
		}
		
		if(len == 1)
		{
			if(this.currentBoard[lowestRow - 1][lowestCol] != NULL_CHAR || this.currentBoard[lowestRow + 1][lowestCol] != NULL_CHAR)
				move.down = true;
		}
		
		int dr = move.down ? 1 : 0, dc = move.down ? 0 : 1; 
		
		while(lowestRow > 0 && lowestCol > 0 && this.currentBoard[lowestRow - dr][lowestCol - dc] != NULL_CHAR)
		{
			lowestRow -= dr;
			lowestCol -= dc;
		}
		
		int row = lowestRow, col = lowestCol;
		move.word = "";
		int lettersPlaced = 0;
		boolean foundAnchorSquare = false;
		while(row <= BOARD_SIZE && col <= BOARD_SIZE)
		{
			int oneD = this.to1D(row, col) - 1;
			if(this.currentBoard[row][col] != NULL_CHAR)
			{
				move.word += this.currentBoard[row][col];
				row += dr;
				col += dc;
			}
			else if(tempChars.get(oneD) != null)
			{
				if(tempBlanks.contains(oneD))
					move.word += "/";
				move.word += tempChars.get(oneD);
				lettersPlaced += 1;
				if(!foundAnchorSquare)
				{
					foundAnchorSquare = (!isFirstMove && this.anchorSquare(row, col)) || (isFirstMove && oneD == BOARD_SIZE * BOARD_SIZE / 2); 
				}
				row += dr;
				col += dc;
			}
			else
				break;
		}
		
		if(lettersPlaced != len || !foundAnchorSquare)
			pc.result = PlayContext.NOT_JOINED;
		
		if(pc.result != PlayContext.SUCCESS)
			return;
		
		move.row = lowestRow;
		move.col = lowestCol;
		pc.move = move;
	}
	
	public void calculatePlayedWords()
	{
		for(int row = 1; row <= BOARD_SIZE; row++)
		{
			String acrossWord = "";
			String downWord = "";
			for(int col = 1; col <= 15; col++)
			{
				if(this.currentBoard[row][col] == NULL_CHAR)
				{
					if(acrossWord.length() > 1)
						this.playedWords.add(acrossWord);
					acrossWord = "";
				}
				else
					acrossWord += this.currentBoard[row][col];
				
				if(this.currentBoard[col][row] == NULL_CHAR)
				{
					if(downWord.length() > 1)
						this.playedWords.add(downWord);
					downWord = "";
				}
				else
					downWord += this.currentBoard[col][row];
			}
		}
	}
	
	public PlayContext playHuman(SparseArray<Character> tempChars, HashSet<Integer> tempBlanks, boolean isFirstMove)
	{
		PlayContext pc = new PlayContext();
		pc.result = PlayContext.SUCCESS;
		
		if(tempChars.size() == 0)
		{
			pc.result = PlayContext.NOT_ENOUGH_LETTERS;
			return pc;
		}
		
		getMove(tempChars, tempBlanks, pc, isFirstMove);
		
		if(pc.result != PlayContext.SUCCESS)
		{
			return pc;
		}
		
		MoveStoreObject mso = this.getAndRemoveBlanks(pc.move.word);
		
		if(!this.dawgArray.wordExists(mso.word))
		{
			pc.result = PlayContext.NOT_LEGAL_WORDS_ERROR;
			pc.badWords.add(mso.word);
		}
		
		calculateScore(pc.move);
		int crossLen = pc.move.crossWords.size();
		for(int i = 0; i < crossLen; i++)
		{
			if(!this.dawgArray.wordExists(pc.move.crossWords.valueAt(i)))
			{
				pc.result = PlayContext.NOT_LEGAL_WORDS_ERROR;
				pc.badWords.add(pc.move.crossWords.valueAt(i));
			}
		}

		if(pc.result != PlayContext.SUCCESS)
		{
			return pc;
		}
		
		
		if(this.playedWords.contains(mso.word) && (tempChars.size() != 1 || this.playedWords.contains(pc.move.crossWords.valueAt(0))))
		{
			pc.badWords.add(mso.word);
			pc.result = PlayContext.WORD_ALREADY_MADE_ERROR;
			return pc;
		}
		
		return pc;
	}
	
	public int play(Move move)
	{
		MoveStoreObject mso = getAndRemoveBlanks(move.word);
		int len = mso.word.length();
		int nextR, nextC;
		char c;
		int tilesPlayed = 0;
		for(int i = 0; i < len; i++)
		{
			nextR = move.row + (move.down ? i : 0);
			nextC = move.col + (move.down ? 0 : i);
			c = mso.word.charAt(i);
			if(this.currentBoard[nextR][nextC] != c && this.currentBoard[nextR][nextC] == NULL_CHAR)
			{
				if(mso.blankPositions.contains(i))
				{
					this.addBlank(nextR, nextC);
					this.removeFromActualRack(BLANK_TILE_RACK, 1);
				}
				else
					this.removeFromActualRack(c, 1);
				this.currentBoard[nextR][nextC] = c;
				this.setCrossCheckDirty(nextR, nextC);
				tilesPlayed++;
			}
		}
		
		this.playedWords.add(move.word);
		len = move.crossWords.size();
		for(int i = 0; i < len; i++)
		{
			this.playedWords.add(move.crossWords.valueAt(i));;
		}
		
		return tilesPlayed;
	}
	
	public ArrayList<Move> firstMove()
	{
		legalMoves = new ArrayList<Move>();
		LeftPart(new StringBuffer(), 0, BOARD_SIZE / 2, BOARD_SIZE / 2 + 1, BOARD_SIZE / 2 + 1, false);
		return legalMoves;
	}
	
	public Move getFirstMove()
	{
		ArrayList<Move> moves = firstMove();
		if(this.moveSelector != null)
			return this.moveSelector.select(moves);
		else
			return moves.get((int)(Math.random() * moves.size()));
	}
	
	public Move getNextMove()
	{
		Log.d(TAG, "Generating Moves...");
		ArrayList<Move> moves = getAllNextMoves();
		Log.d(TAG, "Selecting Move...");
		if(this.moveSelector != null)
			return this.moveSelector.select(moves);
		else
			return moves.get((int)(Math.random() * moves.size()));
	}
	
	public ArrayList<Move> getAllNextMoves()
	{
		legalMoves = new ArrayList<Move>();
		int lastAnchor = 0;
		StringBuffer word = new StringBuffer();
		for(int row = 1; row <= BOARD_SIZE; row++)
		{
			lastAnchor = 0;
			for(int col = 1; col <= BOARD_SIZE; col++)
			{
				if(currentBoard[row][col] != NULL_CHAR || !anchorSquare(row, col))
				{
					continue;
				}
				if(word.length() > 0)
					word.delete(0, word.length() - 1);
				if(currentBoard[row][col - 1] == NULL_CHAR)
					LeftPart(word, 0, col - lastAnchor - 1, row, col, false);
				else
					ExtendRight(word, 0, row, lastAnchor + 1, row, lastAnchor + 1, false);
				lastAnchor = col;
			}
		}
		
		lastAnchor = 0;
		for(int col = 1; col <= BOARD_SIZE; col++)
		{
			lastAnchor = 0;
			for(int row = 1; row <= BOARD_SIZE; row++)
			{
				if(currentBoard[row][col] != NULL_CHAR || !anchorSquare(row, col))
				{
					continue;
				}
				if(word.length() > 0)
					word.delete(0, word.length());
				if(currentBoard[row - 1][col] == NULL_CHAR)
					LeftPart(word, 0, row - lastAnchor - 1, row, col, true);
				else
					ExtendRight(word, 0, lastAnchor + 1, col, lastAnchor + 1, col, true);
				lastAnchor = row;
			}
		}
		
		return this.legalMoves;
	}
	
	private boolean anchorSquare(int row, int col)
	{
		if(col < 1 || col > BOARD_SIZE || row < 1 || row > BOARD_SIZE)
			return false;
		if(currentBoard[row][col - 1] != NULL_CHAR)
			return true;
		if(currentBoard[row][col + 1] != NULL_CHAR)
			return true;
		if(currentBoard[row - 1][col] != NULL_CHAR)
			return true;
		if(currentBoard[row + 1][col] != NULL_CHAR)
			return true;
		return false;
	}
	
	private void LeftPart(StringBuffer partialWord, int node, int limit, int row, int col, boolean down)
	{
		ExtendRight(partialWord, node, row, col, row, col, down);
		if(limit <= 0)
			return;
		HashMap<Character, Integer> edges = dawgArray.getChildren(node);
		for(Character edge : edges.keySet())
		{
			if(inRack(edge) && crossCheck(row, col, edge, !down))
			{
				boolean blank = this.removeFromRack(edge, 1);
				if(blank)
					partialWord.append(BLANK_PREFIX);
				LeftPart(partialWord.append(edge), edges.get(edge), limit - 1, row, col, down);
				this.addToRack(edge, 1, blank);
				partialWord.deleteCharAt(partialWord.length() - 1);
				if(blank)
					partialWord.deleteCharAt(partialWord.length() - 1);
			}
		}
	}
	
	private void ExtendRight(StringBuffer partialWord, int node, int row, int col, int anchorRow, int anchorCol, boolean down)
	{
		if(row > BOARD_SIZE + 1 || col > BOARD_SIZE + 1)
			return;
		HashMap<Character, Integer> edges = dawgArray.getChildren(node);
		if(currentBoard[row][col] == NULL_CHAR )
		{
			if(dawgArray.isEndOFWord(node))
			{
				CheckMove(StringBufferToString(partialWord), row, col, anchorRow, anchorCol, down);
			}
			for(Character edge : edges.keySet())
			{ 
				if(inRack(edge) && crossCheck(row, col, edge, !down))
				{
					boolean blank = this.removeFromRack(edge, 1);
					if(blank)
						partialWord.append(BLANK_PREFIX);
					ExtendRight(partialWord.append(edge), edges.get(edge), row + (down ? 1 : 0), col + (down ? 0 : 1), anchorRow, anchorCol, down);
					this.addToRack(edge, 1, blank);
					partialWord.deleteCharAt(partialWord.length() - 1);
					if(blank)
						partialWord.deleteCharAt(partialWord.length() - 1);
				}
			}
		}
		else
		{
			char c = currentBoard[row][col];
			if(!edges.containsKey(c))
				return;
			int nextNode = edges.get(c);
			if(nextNode != 0)
			{
				ExtendRight(partialWord.append(c), nextNode, row + (down ? 1 : 0), col + (down ? 0 : 1), anchorRow, anchorCol, down);
				partialWord.deleteCharAt(partialWord.length() - 1);
			}
		}
	}
	
	private int numBlanks(StringBuffer partialWord)
	{
		int len = partialWord.length();
		int num = 0;
		for(int i = 0; i < len; i++)
			if(partialWord.charAt(i) == BLANK_PREFIX)
				num++;
		return num;
	}
	
	private void setCrossCheckDirty(int row, int col)
	{
		row -= 1;
		col -= 1;
		for(int i = 0; i < BOARD_SIZE; i++)
		{
			if(this.crossChecks[row][i] == null)
				this.crossChecks[row][i] = new CrossCheck();
			this.crossChecks[row][i].isDirty = true;
			if(this.crossChecks[i][col] == null)
				this.crossChecks[i][col] = new CrossCheck();
			this.crossChecks[i][col].isDirty = true;
		}
	}
	
	/*
	 * [downPrefix, downPostfix]
	 */
	private String[] getDownPrefixAndSuffix(int row, int col)
	{
		row -= 1;
		col -= 1;
		String downPrefix = "", downPostfix = "";
		
		int currentPos = row;
		char current;
		while(currentPos > 0)
		{
			current = this.currentBoard[currentPos][col + 1];
			if(current == NULL_CHAR)
				break;
			downPrefix = current + downPrefix;
			currentPos--;
		}
		
		currentPos = row + 2;
		while(currentPos <= BOARD_SIZE)
		{
			current = this.currentBoard[currentPos][col + 1];
			if(current == NULL_CHAR)
				break;
			downPostfix += current;
			currentPos++;
		}		
		return new String[] {downPrefix, downPostfix};
	}
	
	
	/*
	 * [acrossPrefix, acrossPostfix]
	 */
	private String[] getAcrossPrefixAndSuffix(int row, int col)
	{
		row -= 1;
		col -= 1;
		String acrossPrefix = "", acrossPostfix = "";
		
		int currentPos;
		char current;	
		currentPos = col;
		while(currentPos > 0)
		{
			current = this.currentBoard[row + 1][currentPos];
			if(current == NULL_CHAR)
				break;
			acrossPrefix = current + acrossPrefix;
			currentPos--;
		}
		
		currentPos = col + 2;
		while(currentPos <= BOARD_SIZE)
		{
			current = this.currentBoard[row + 1][currentPos];
			if(current == NULL_CHAR)
				break;
			acrossPostfix += current;
			currentPos++;
		}
		
		return new String[] {acrossPrefix, acrossPostfix};
	}
	
	private void computeCrossCheck(int row, int col)
	{
		row -= 1;
		col -= 1;
		
		if(this.crossChecks[row ][col] == null)
			this.crossChecks[row][col] = new CrossCheck();
		
		this.crossChecks[row][col].acrossChecks.clear();
		this.crossChecks[row][col].downChecks.clear();
		
		if(this.currentBoard[row + 1][col + 1] != NULL_CHAR)
		{
			this.crossChecks[row][col].isDirty = false;
			return;
		}
		
		String[] parts = this.getDownPrefixAndSuffix(row + 1, col + 1);
		
		boolean addAll = parts[0].length() == 0 && parts[1].length() == 0;
		for(char i = 'a'; i <= 'z'; i++)
		{
			if(addAll || dawgArray.wordExists(parts[0] + i + parts[1]))
				this.crossChecks[row][col].downChecks.add(i);
		}
		
		parts = this.getAcrossPrefixAndSuffix(row + 1, col + 1);
		
		addAll = parts[0].length() == 0 && parts[1].length() == 0;
		for(char i = 'a'; i <= 'z'; i++)
		{
			if(addAll || dawgArray.wordExists(parts[0] + i + parts[1]))
				this.crossChecks[row][col].acrossChecks.add(i);
		}
		
		this.crossChecks[row][col].isDirty = false;
	}
	
	private boolean crossCheck(int row, int col, char c, boolean checkDown)
	{
		if(row > BOARD_SIZE || col > BOARD_SIZE || row < 1 || col < 1)
			return true;
		row -= 1;
		col -= 1;
		if(this.crossChecks[row][col] == null || this.crossChecks[row][col].isDirty)
			computeCrossCheck(row + 1, col + 1);
		return this.crossChecks[row][col].contains(c, checkDown);
	}
	
	private String StringBufferToString(StringBuffer partialWord)
	{
		String word = "";
		int len = partialWord.length();
		for(int i = 0; i < len; i++)
			word += partialWord.charAt(i);
		
		return word;
	}
	
	private void CheckMove(String word, int row, int col, int anchorRow, int anchorCol, boolean down)
	{
		if(row <= anchorRow && col <= anchorCol)
			return;
		
		MoveStoreObject mso = this.getAndRemoveBlanks(word);
		
		if(this.playedWords.contains(mso.word))
			return;
		
		row = row - (down ? mso.word.length()  : 0);
		col = col - (down ? 0 : mso.word.length());
		int len = word.length();
		for(int i = 0; i < len; i++)
		{
			if(this.currentBoard[row + (down ? i : 0)][col + (down ? 0 : i)] != mso.word.charAt(i))
			{
				Move m = new Move(word, row, col, down, 0);
				calculateScore(m);
				this.legalMoves.add(m);
				return;
			}
		}
	}
	
	public int calculateScore(Move move)
	{
		MoveStoreObject mso = getAndRemoveBlanks(move.word);
		int len = mso.word.length();
		int wordMultiplier = 1;
		int score = 0, totalCrossScore = 0, nextR, nextC;
		char c;
		int numLettersPlayed = 0;
		for(int i = 0; i < len; i++)
		{
			c = mso.word.charAt(i);
			nextR = move.row + (move.down ? i : 0);
			nextC = move.col + (move.down ? 0 : i);
			int currentLetterScore = this.getLetterScore(c);
			boolean isBlank = mso.blankPositions.contains(i) || this.isBlank(nextR, nextC);
			if(this.currentBoard[nextR][nextC] != c && this.currentBoard[nextR][nextC] == NULL_CHAR)
			{
				int currentWordMultiplier = this.getWordMultiplier(nextR, nextC); 
				int currentLetterMultiplier = this.getLetterMultiplier(nextR, nextC);
				
				if(!isBlank)
					score += currentLetterMultiplier * currentLetterScore;
				
				String[] crossWordArr = move.down ? this.getAcrossPrefixAndSuffix(nextR, nextC) : this.getDownPrefixAndSuffix(nextR, nextC);
				int crossScore = 0;
				String crossWord = crossWordArr[0];
				int lenC = crossWord.length();
				for(int j = 0; j < lenC; j++)
				{
					if(!this.isBlank(move.down ? nextR - lenC + j : nextR, move.down ? nextC : nextC - lenC + j))
						crossScore += this.getLetterScore(crossWord.charAt(j));
				}
				
				crossWord = crossWordArr[1];
				lenC = crossWord.length();
				for(int j = 0; j < lenC; j++)
				{
					if(!this.isBlank(move.down ? nextR + j + 1 : nextR, move.down ? nextC : nextC + j + 1))
						crossScore += this.getLetterScore(crossWord.charAt(j));
				}
				
				if((lenC > 0 || crossWordArr[0].length() > 0) && !isBlank)
				{
					move.crossWords.append(i, crossWordArr[0] + c + crossWordArr[1]);
					crossScore += currentLetterMultiplier * currentLetterScore;
				}
				
				totalCrossScore += crossScore * currentWordMultiplier;
				
				wordMultiplier *= currentWordMultiplier;

				numLettersPlayed++;
			}
			else if(!isBlank)
				score += currentLetterScore;
		}
		
		move.score = wordMultiplier * score + totalCrossScore  + (numLettersPlayed == 7 ? 35 : 0); 
		return move.score;
	}
	
	public int getWordMultiplier(int row, int col)
	{
		if(scoreSupplier != null)
			return scoreSupplier.getWordMultiplier(row, col);
		else
			return 1;
	}
	
	public int getLetterMultiplier(int row, int col)
	{
		if(scoreSupplier != null)
			return scoreSupplier.getLetterMultiplier(row, col);
		else
			return 1;
	}
	
	public int getLetterScore(char c)
	{
		if(scoreSupplier != null)
			return scoreSupplier.getLetterScore(c);
		else
			return 1;
	}
	
	public void setCurrentBag(int[] _currentBag)
	{
		this.currentBag = _currentBag;
		currentLettersInBag = 0;
		for(int i = 0; i < currentBag.length; i++)
			currentLettersInBag += currentBag[i];
	}
	
	public void removeLetterFromBag(char c, int num)
	{
		this.currentBag[c - 'a'] -= num;
		currentLettersInBag -= num;
	}
	
	public void addLetterToBag(char c, int num)
	{
		this.currentBag[c - 'a'] += num;
		currentLettersInBag += num;
	}
	
	public void cleanBag()
	{
		for(int i = 0; i < this.currentBag.length; i++)
			this.currentBag[i] = 0;
		currentLettersInBag += 0;
	}
	
	public char[] drawRandomLettersFromBag(int num)
	{
		char[] letters = new char[num];
		
		for(int i = 0; i < num; i++)
			letters[i] = NULL_CHAR;
		
		for(int i = 0; i < num; i++)
		{
			if(currentLettersInBag == 0)
				break;
			int sum = 0;
			int rand = (int)(Math.random() * currentLettersInBag);
			int j = 0;
			while(sum <= rand && j < currentBag.length)
			{
				sum += currentBag[j];
				j++;
			}
			
			if(sum == 0)
				break;
			
			j--;
			
			char c = (char) (j + 'a');
			letters[i] = c;
			this.removeLetterFromBag(c, 1);
		}
		
		return letters;
	}
	
	public String getIntArrLettersString(int[] arr)
	{
		String str = "[";
		for(int i = 0; i < arr.length; i++)
		{
			str += (char)(i + 'a') + ":" + arr[i];
			if(i < arr.length - 1)
				str += ", ";
		}
		str += "]";
		return str;
	}
	
	public int drawLettersFromBagAndAddToRack(int[] rack, int num)
	{
		int noDrawn = 0;
		char[] letters = this.drawRandomLettersFromBag(num);
		for(int i = 0; i < letters.length; i++)
		{
			if(letters[i] == NULL_CHAR)
				return noDrawn;
			
			rack[letters[i] - 'a']++;
			noDrawn++;
		}
		return noDrawn;
	}
}
