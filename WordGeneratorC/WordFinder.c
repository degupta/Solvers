#include "WordFinder.h"
#include <stdio.h>
#include <stdlib.h>

const int WORD_MULTIPLIERS[BOARD_SIZE + 2][BOARD_SIZE + 2] =
{
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0},
	{0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 3, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 3, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 0},
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
	{0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0},
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0},
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
};

const int LETTER_MULTIPLIERS[BOARD_SIZE + 2][BOARD_SIZE + 2] =
{
	//  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
	{0, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 0}, // 1
	{0, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 0}, // 2
	{0, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0}, // 3
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0}, // 4
	{0, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 0}, // 5
	{0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 1, 0}, // 6
	{0, 3, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 3, 0}, // 7
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0}, // 8
	{0, 3, 1, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1, 3, 0}, // 9
	{0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 1, 0}, // 10
	{0, 1, 1, 2, 1, 1, 1, 2, 1, 2, 1, 1, 1, 2, 1, 1, 0}, // 11
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0}, // 12
	{0, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 2, 1, 0}, // 13
	{0, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 0}, // 14
	{0, 1, 1, 1, 1, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 0}, // 15
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
};

const int LETTER_SCORES[NUM_TILE_TYPES] =
{
	1,
	4,
	4,
	2,
	1,
	4,
	3,
	3,
	1,
	10,
	5,
	2,
	4,
	2,
	1,
	4,
	10,
	1,
	1,
	1,
	2,
	5,
	4,
	8,
	3,
	10,
	0
};

boolean anchorSquare(WordFinderContext_t context, int row, int col);
void getAllNextMoves(WordFinderContext_t context);
void LeftPart(WordFinderContext_t context, int node, int limit);
void ExtendRight(WordFinderContext_t context, int node, int row, int col, boolean calculateLeftScore);
void sendMoves(WordFinderContext_t context, int row, int col, int score, boolean down);
void computeCrossCheck(WordFinderContext_t context, int row, int col);
void getCrossCheck(WordFinderContext_t context, int row, int col, boolean down);
int* crossCheck(WordFinderContext_t context, int row, int col, boolean checkDown);

/**
 * Reset the context so that it can used in a new search from a new position and direction.
 */
void resetContext(WordFinderContext_t context, int row, int col, boolean down)
{
	context->anchorRow = row;
	context->anchorCol = col;
	context->down = down;
	context->partialWord.position = 0;
	context->partialWord.stringBuf[0] = NULL_CHAR;
	context->currentScore = 0;
	context->currentWordMultiplier = 1;
	context->currentCrossScore = 0;
	context->lettersPlaced = 0;
}

/**
 * Create the initial context. Initialize all its members.
 */
WordFinderContext_t getContext(DawgArray _dawgArray, char** currentBoard, int* currentRack, LegalMove_t LegalMoveFunc)
{
	WordFinderContext_t context = (WordFinderContext_t) malloc(sizeof(WordFinderContext));
	resetContext(context, 0, 0, FALSE);
	context->LegalMoveFunction = LegalMoveFunc;
	context->dawgArray = _dawgArray;
	context->currentBoard = currentBoard;
	context->currentRack = currentRack;
	for(int row = 0; row < BOARD_SIZE; row++)
	{
		for(int col = 0; col < BOARD_SIZE; col++)
		{
			context->crossChecks[row][col].across[0] = 0;
			context->crossChecks[row][col].across[1] = 0;
			context->crossChecks[row][col].down[0] = 0;
			context->crossChecks[row][col].down[1] = 0;
			context->crossChecks[row][col].isDirty = TRUE;
		}
	}
	context->hasBlanks = context->currentRack[BLANK_TILE_RACK - 'a'] > 0;
	return context;
}

/**
 * CALL THIS ONLY WHEN THERE IS AT LEAST ONE TILE ON THE BOARD.
 *
 *
 * LegalMoveFunc is called with words (and associated information) as they are being found and not just right at the end
 * and in no particular order.
 * So if you want to store all the words found or sort them by score, you need to write a data structure to do it.
 *
 * ----------------------------
 * VERY IMPORTANT -- DAWG ARRAY
 * ----------------------------
 * The DawgArray should be read in as specified. Please look at the function createDawgFromFile() in main.c
 * to see how this done.
 *
 * ------------------------------------
 * VERY IMPORTANT -- COORDINATE SYSTEM
 * ------------------------------------
 * The 0,0 coordinate is the top left hand corner.
 * I don't use x and y. I use row and columns for clarity, I suggest you do the same (as in please use board[row][col]).
 * Row increases downwards and columns increases rightwards. *
 *
 * -------------------------------
 * VERY IMPORTANT -- BOARD LAYOUT
 * -------------------------------
 * The currentBoard SHOULD BE A 17x17 2D array not 15x15 (this is not on a whim - it is required).
 * currentBoard[0][0:16] = currentBoard[16][0:16] = currentBoard[0:16][16] = currentBoard[0:16][0] should be NULL_CHAR.
 *
 * The rest of the 15x15 sub array should have the current characters on the board (currentBoard[1][1] should be the
 * letter on the top left corner of the board).
 *
 * -- So when I call LegalMoveFunction with row, col they start with 1 and not 0 --
 *
 * All the letters should be in lower case, EXCEPT blank tiles whose letters should be in upper case (necessary for
 * correct score calculation).
 *
 * -------------------------------
 * VERY IMPORTANT -- TILE LAYOUT
 * -------------------------------
 * The currentRack should be 27 int array with the current number of A's in the tile rack in currentRack[0],
 * current number of B's in the tile rack in currentRack[1] and so on.
 * The current number of blanks in the tile rack should be in currentRack[26]
 *
 */
void getNextMove(DawgArray _dawgArray, char** currentBoard, int* currentRack, LegalMove_t LegalMoveFunc)
{
	// Create a new search context
	WordFinderContext_t context = getContext(_dawgArray, currentBoard, currentRack, LegalMoveFunc);
	// Find all next moves
	getAllNextMoves(context);
	// Release the context
	free(context);
}

/*
 * CALL THIS ONLY WHEN THERE ARE NO TILES ON THE BOARD
 *
 * LegalMoveFunc is called with words (and associated information) as they are being found and not just right at the end
 * and in no particular order.
 * So if you want to store all the words found or sort them by score, you need to write a data structure to do it.
 *
 * PLEASE SEE COMMENTS IN getNextMove()
 */
void getFirstMove(DawgArray _dawgArray, char** currentBoard, int* currentRack, LegalMove_t LegalMoveFunc)
{
	// Create a new search context
	WordFinderContext_t context = getContext(_dawgArray, currentBoard, currentRack, LegalMoveFunc);
	// Start building from the center of the board
	int middle = BOARD_SIZE / 2 + 1;
	// Since the board is symmetric just search for across words
	resetContext(context, middle, middle, FALSE);
	// Limit to extend left if half the board size
	LeftPart(context, 0, BOARD_SIZE / 2);
	// Release the context
	free(context);
}

/**
 * Finds all the words starting from each position from where we can build words.
 * Goes across and down.
 */
void getAllNextMoves(WordFinderContext_t context)
{
	// Anchor : A position that has a letter placed next to it.
	// This is where we start building a new word from (since words need
	// to placed next to existing tiles).

	int lastAnchor = 0; // last found anchor
	// Search for all words going across.
	for(int row = 1; row <= BOARD_SIZE; row++)
	{
		lastAnchor = 0;
		for(int col = 1; col <= BOARD_SIZE; col++)
		{
			// If there is already a tile at this position, or it is not an anchor sqaure
			// can't start building a new word from here.
			if(context->currentBoard[row][col] != NULL_CHAR || anchorSquare(context, row, col) == FALSE)
			{
				continue;
			}

			// resue the context to start from row, col and going across.
			resetContext(context, row, col, FALSE);
			if(context->currentBoard[row][col - 1] == NULL_CHAR)
			{
				// If there is no tile in the column before us, we first start expanding
				// towards the left and then later we can start expanding to the right
				LeftPart(context, 0, col - lastAnchor - 1);
			}
			else
			{
				// If there is a tile in the column before us, find the position which is
				// next to the last empty space there was (which will always be lastAnchor + 1)
				// and then start expanding right from there.
				context->anchorCol = lastAnchor + 1;
				ExtendRight(context, 0, row, lastAnchor + 1, FALSE);
			}

			// Record that this is the last time we encountered an anchor position.
			lastAnchor = col;
		}
	}

	// Search for words going down.
	// Exactly the same as searching for words across, just switch row and col.
	lastAnchor = 0;
	for(int col = 1; col <= BOARD_SIZE; col++)
	{
		lastAnchor = 0;
		for(int row = 1; row <= BOARD_SIZE; row++)
		{
			if(context->currentBoard[row][col] != NULL_CHAR || anchorSquare(context, row, col) == FALSE)
			{
				continue;
			}

			resetContext(context, row, col, TRUE);
			if(context->currentBoard[row - 1][col] == NULL_CHAR)
			{
				LeftPart(context, 0, row - lastAnchor - 1);
			}
			else
			{
				context->anchorRow = lastAnchor + 1;
				ExtendRight(context, 0, lastAnchor + 1, col, FALSE);
			}

			lastAnchor = row;
		}
	}
}

/**
 * Is the current position an anchor square?
 */
boolean anchorSquare(WordFinderContext_t context, int row, int col)
{
	if(col < 1 || col > BOARD_SIZE || row < 1 || row > BOARD_SIZE)
		return FALSE;
	if(context->currentBoard[row][col - 1] != NULL_CHAR)
		return TRUE;
	if(context->currentBoard[row][col + 1] != NULL_CHAR)
		return TRUE;
	if(context->currentBoard[row - 1][col] != NULL_CHAR)
		return TRUE;
	if(context->currentBoard[row + 1][col] != NULL_CHAR)
		return TRUE;
	return FALSE;
}

/**
 * Does the player have the particular letter (or a blank).
 */
boolean inRack(WordFinderContext_t context, char c)
{
	return context->currentRack[c - 'a'] > 0 || context->currentRack[BLANK_TILE_RACK - 'a'] > 0;
}

/**
 * Add a letter to the rack. ONLY CALL AFTER removeFromRack()
 * removedBlank = was blank removed when removing from rack
 */
void addToRack(WordFinderContext_t context, char letter, boolean removedBlank)
{
	if(removedBlank == TRUE)
		letter = BLANK_TILE_RACK;
	context->currentRack[letter - 'a'] += 1;
}

/**
 * Remove a letter from the rack. Only call after making sure user has inRack.
 * Otherwise will remove blank.
 */
boolean removeFromRack(WordFinderContext_t context, char letter)
{
	boolean removedBlank = FALSE;
	if(context->currentRack[letter - 'a'] < 1)
	{
		// User doesn't have the letter, remove a blank.
		letter = BLANK_TILE_RACK;
		removedBlank = TRUE;
	}
	context->currentRack[letter - 'a'] -= 1;
	return removedBlank;
}

/**
 * Append a character to the end of MyStringBuffer.
 */
void appendToMyStringBuffer(MyStringBuffer_t strBuf, char c)
{
	strBuf->stringBuf[strBuf->position] = c;
	strBuf->position++;
}

/**
 * Remove the character at the end of MyStringBuffer.
 */
void removeFromMyStringBuffer(MyStringBuffer_t strBuf)
{
	strBuf->position--;
	strBuf->stringBuf[strBuf->position] = NULL_CHAR;
}

/**
 * Extend the current word towards the left (or up).
 * Will only extend to completely empty spaces (no anchor squares)
 * So don't need to do any cross checks.
 */
void LeftPart(WordFinderContext_t context, int node, int limit)
{
	// First Extend Right as much as you can
	ExtendRight(context, node, context->anchorRow, context->anchorCol, TRUE);

	if(limit <= 0)
		return; // No place to extend left

	// Get the child pointer of current node
	int childPointer = getChildPointer(context->dawgArray, node);
	if(childPointer == 0)
		return; // No children, return.

	char edge = NULL_CHAR;
	boolean endOfList = FALSE;
	while(TRUE)
	{
		// Get the letter of the current child
		endOfList = getNextChild(context->dawgArray, childPointer, &edge);
		if(inRack(context, edge) == TRUE) // Do we have that letter in the rack?
		{
			boolean blank = removeFromRack(context, edge); // Yes! Remove it (or a blank)
			if(blank)
				edge -= 32; // Blanks are represented as UPPER CASE LETTERS
			 // Add the character to the current word we are making
			appendToMyStringBuffer(&(context->partialWord), edge);
			// Increase the number of letters placed.
			context->lettersPlaced++;

			// Recurse!
			LeftPart(context, childPointer, limit - 1);

			// BACKTRACK! -- Remove letter from word, add to rack and decrease number of letters placed.
			context->lettersPlaced--;
			addToRack(context, edge, blank);
			removeFromMyStringBuffer(&(context->partialWord));
		}
		if(endOfList == TRUE)
			break; // No more children, break;
		childPointer++;
	}
}

/**
 * Extend the current word towards the right (or down).
 */
void ExtendRight(WordFinderContext_t context, int node, int row, int col, boolean calculateLeftScore)
{
	// Are we off the board?
	if(row > BOARD_SIZE + 1 || col > BOARD_SIZE + 1)
		return;

	int leftScoreMultiplier = 1;
	int leftScoreTotal = 0;
	if(calculateLeftScore)
	{
		// We just came from extending left, calculate the score
		// by placing the letters towards the left
		// LeftPart only extends to non anchor squares so there are no
		// cross words or cross score to consider.
		int dR = context->down ? 1: 0;
		int dC = context->down ? 0: 1;
		int curR = row - context->partialWord.position * dR;
		int curC = col - context->partialWord.position * dC;
		for(int i = 0; i < context->partialWord.position; i++, curR += dR, curC += dC)
		{
			// Make sure it isn't a blank
			if(context->partialWord.stringBuf[i] >= 'a')
				leftScoreTotal += LETTER_MULTIPLIERS[curR][curC] * LETTER_SCORES[context->partialWord.stringBuf[i] - 'a'];
			leftScoreMultiplier *= WORD_MULTIPLIERS[curR][curC];
		}

		context->currentScore += leftScoreTotal;
		context->currentWordMultiplier *= leftScoreMultiplier;
	}

	// Is the current position empty?
	if(context->currentBoard[row][col] == NULL_CHAR)
	{
		// Have we formed a word (make sure we have placed a letter at least first!)
		if(context->lettersPlaced > 0 && (row > context->anchorRow || col > context->anchorCol) && isEndOFWord(context->dawgArray, node) == TRUE)
		{
			// We have formed a legitimate word!
			context->partialWord.stringBuf[context->partialWord.position] = NULL_CHAR;

			// Send the move from the starting position and direction and calculate the final score.
			sendMoves(context,
					row - (context->down ? context->partialWord.position : 0),
					col - (context->down ? 0 : context->partialWord.position),
					context->currentScore * context->currentWordMultiplier + context->currentCrossScore + (context->lettersPlaced == LETTERS_FOR_BINGO ? BINGO_SCORE : 0),
					context->down);
		}

		// Get the pointer to the list of children.
		int childPointer = getChildPointer(context->dawgArray, node);

		// Have we gone off the board?
		if(row <= BOARD_SIZE && col <= BOARD_SIZE && childPointer != 0)
		{
			// Get current score multipliers.
			int currentWordMultiplier = WORD_MULTIPLIERS[row][col];
			int currentLetterMultiplier = LETTER_MULTIPLIERS[row][col];

			// Get cross check letters and score
			int* crossCheckArr = crossCheck(context, row, col, !context->down);
			// Multiply the cross check score by currentWordMultiplier
			int crossScore = crossCheckArr[0] * currentWordMultiplier;
			int crossCheckLetters = crossCheckArr[1];
			// Double up only if there were some letters above or below this position.
			// If there were none crossCheck() returns 0xFFFFFFFF as crossCheckLetters
			// hence & with 0x80000000 (MSB set).
			boolean doubleUp = (crossCheckLetters & 0x80000000) == 0;

			char edge = NULL_CHAR;
			boolean endOfList = FALSE;
			while(TRUE)
			{
				// Get the next child
				endOfList = getNextChild(context->dawgArray, childPointer, &edge);
				// Do we have that letter in the rack and does it satisfy cross check requirements?
				if(inRack(context, edge) == TRUE && (crossCheckLetters & (1 << (edge - 'a'))) > 0)
				{
					// Yes! Remove it
					boolean blank = removeFromRack(context, edge);
					int currentLetterScore = LETTER_SCORES[edge - 'a'] * currentLetterMultiplier;
					if(blank)
					{
						// if blank, make it upper case and score as 0
						edge -= 32;
						currentLetterScore = 0;
					}
					// Add it current word
					appendToMyStringBuffer(&(context->partialWord), edge);
					// Add score to currentScore
					context->currentScore += currentLetterScore;
					// Multiply currentWordMultiplier
					context->currentWordMultiplier *= currentWordMultiplier;
					// Add crossScore
					int letterCrossScore = crossScore + (doubleUp ? currentLetterScore * currentWordMultiplier : 0);
					context->currentCrossScore += letterCrossScore;
					// Increase letters placed
					context->lettersPlaced++;

					// Recurse!
					ExtendRight(context, childPointer, row + (context->down ? 1 : 0), col + (context->down ? 0 : 1), FALSE);

					// BACKTRACK! - Remove from current word, add back to rack and deduct currentScore, currentLetterScore,
					// lettersPlaced and currentWordMultiplier
					addToRack(context, edge, blank);
					removeFromMyStringBuffer(&(context->partialWord));
					context->currentScore -= currentLetterScore;
					context->currentWordMultiplier /= currentWordMultiplier;
					context->currentCrossScore -= letterCrossScore;
					context->lettersPlaced--;
				}
				if(endOfList == TRUE)
					break; // No more children left.
				childPointer++;
			}
		}
	}
	else
	{
		// Non empty position. Add letter on board.
		char c = context->currentBoard[row][col];
		boolean isBlank = c < 'a';
		if(isBlank)
			c += 32;
		// Find if current node has a child pointer with the character
		int nextNode = findChildPointer(context->dawgArray, node, c);
		if(nextNode != 0)
		{
			// Yes it does!
			// Calculate Score of adding that letter
			int currentLetterScore = isBlank ? 0 : LETTER_SCORES[c - 'a'];
			context->currentScore += currentLetterScore;
			// Add the letter to current word
			appendToMyStringBuffer(&(context->partialWord), c);

			// Recurse!
			ExtendRight(context, nextNode, row + (context->down ? 1 : 0), col + (context->down ? 0 : 1), FALSE);

			// BACKTRACK! - Remove letter from current word, deduct the score.
			removeFromMyStringBuffer(&(context->partialWord));
			context->currentScore -= currentLetterScore;
		}
	}

	if(calculateLeftScore)
	{
		// BACKTRACK! - if we added the left score in the beginning deduct now.
		context->currentScore -= leftScoreTotal;
		context->currentWordMultiplier /= leftScoreMultiplier;
	}
}

/**
 * Send a legal move to the callback function
 */
void sendMoves(WordFinderContext_t context, int row, int col, int score, boolean down)
{
	context->LegalMoveFunction(context->partialWord.stringBuf, row, col, score, down);

	if(!BLANK_FUDGER_ENABLED || !context->hasBlanks)
		return;

	// Player has blanks, swap the blanks around to get new words.
	// For example if the word is pizZa (second Z is a blank)
	// piZza is also a legitimate move (first Z is blank)
	// Doing this swapping here since faster than actually building into
	// the actual backtracking algorithm (Remember I only use a blank if the user
	// doesn't have the letter in the rack I am looking for).

	// Copy over the current word to prevent thrashing it.
	for(int i = 0; i < context->partialWord.position; i++)
		context->tempString.stringBuf[i] = context->partialWord.stringBuf[i];
	context->tempString.position = context->partialWord.position;
	context->tempString.stringBuf[context->tempString.position] = NULL_CHAR;

	int dR = down ? 1 : 0;
	int dC = down ? 0 : 1;
	int crossDR = down ? 0 : 1;
	int crossDC = down ? 1 : 0;
	// Start position of the word
	int curR = row + (context->tempString.position - 1) * dR;
	int curC = col + (context->tempString.position - 1) * dC;

	for(int i = context->tempString.position - 1; i >= 0; i--, curR -= dR, curC -= dC)
	{
		// Can't swap letters already on the board
		if(context->currentBoard[curR][curC] != NULL_CHAR)
			continue;
		char c = context->tempString.stringBuf[i];
		if(c < 'a')
		{
			// Only swap blank letters!
			for(int j = i - 1; j >= 0; j--)
			{
				if(context->tempString.stringBuf[j] == c + 32)
				{
					// Blank letter same as normal letter.
					int jRow = curR - (i - j) * dR;
					int jCol = curC - (i - j) * dC;

					// Can't swap letters already on the board
					if(context->currentBoard[jRow][jCol] != NULL_CHAR)
						continue;

					// What will be the difference in the score by swapping the two letters?
					// Could be pretty large. For example consider :
					// p   i   z   Z   a
					// N   N   N  TL   N (N = normal, TL = triple letter)
					// Here the blank is on the triple letter, but by swapping we can get 20 more points!
					int letterScore = LETTER_SCORES[c + 32 - 'a'];
					// How much is the difference in the score by swapping the two letters?
					// Difference is the letterScore * difference in letter multipliers * word multiplier of the current word
					int diffScore = letterScore * (LETTER_MULTIPLIERS[curR][curC] - LETTER_MULTIPLIERS[jRow][jCol]) * context->currentWordMultiplier;
					// Add cross score (if there was a cross word) from moving the letter to new position
					if(context->currentBoard[curR + crossDR][curC + crossDC] != NULL_CHAR || context->currentBoard[curR - crossDR][curC - crossDC] != NULL_CHAR)
						diffScore += letterScore * LETTER_MULTIPLIERS[curR][curC] * WORD_MULTIPLIERS[curR][curC];
					// Remove cross score (if there was a cross word) of the old position
					if(context->currentBoard[jRow + crossDR][jCol + crossDC] != NULL_CHAR || context->currentBoard[jRow - crossDR][jCol - crossDC] != NULL_CHAR)
						diffScore -= letterScore * LETTER_MULTIPLIERS[jRow][jCol] * WORD_MULTIPLIERS[jRow][jCol];

					// Swap the letters!
					context->tempString.stringBuf[i] = context->partialWord.stringBuf[j];
					context->tempString.stringBuf[j] = context->partialWord.stringBuf[i];
					// Send it as a legal move!
					context->LegalMoveFunction(context->tempString.stringBuf, row, col, score + diffScore, down);
					// Restore the swapped letters!
					context->tempString.stringBuf[i] = context->partialWord.stringBuf[j];
					context->tempString.stringBuf[j] = context->partialWord.stringBuf[i];
				}
			}
		}
	}
}

/**
 * Mark cross check position as clean
 */
void resetClean(CrossCheck* cross)
{
	cross->across[0] = cross->across[1] = cross->down[0] = cross->down[1] = 0;
	cross->isDirty = FALSE;
}

/**
 * Return the cross check letters and score for the particular position and direction
 */
int* crossCheck(WordFinderContext_t context, int row, int col, boolean checkDown)
{
	int otherR = row - 1;
	int otherC = col - 1;
	// isDirty = not been calculated yet, or that tile was changed
	if(context->crossChecks[otherR][otherC].isDirty)
		computeCrossCheck(context, row, col);
	return checkDown ? context->crossChecks[otherR][otherC].down : context->crossChecks[otherR][otherC].across;
}

/**
 * Compute the cross check letters and score for a particular position (both directions).
 */
void computeCrossCheck(WordFinderContext_t context, int row, int col)
{
	if(context->currentBoard[row][col] != NULL_CHAR)
	{
		// There is already a letter here. Clean it!
		resetClean(&(context->crossChecks[row - 1][col - 1]));
		return;
	}

	// get cross checks for both direction
	getCrossCheck(context, row, col, TRUE);
	getCrossCheck(context, row, col, FALSE);
	context->crossChecks[row - 1][col - 1].isDirty = FALSE;
}

/**
 * Helper to compute cross check for particular direction and position
 */
void getCrossCheck(WordFinderContext_t context, int row, int col, boolean down)
{
	MyStringBuffer_t stringBuf = &context->tempString;

	int dR = down ? 1 : 0;
	int dC = down ? 0 : 1;

	stringBuf->position = 0;
	int thisPosition = 0;

	int currentPosR = row - dR;
	int currentPosC = col - dC;
	char current;
	int crossCheckScore = 0;

	// NOTE :
	// Comments are according to finding cross words going down
	// But same for cross words going across
	// So,
	// When I say up it also means left
	// When I say down it also means right

	// Find all letter directly above current Position
	while(currentPosR > 0 && currentPosC > 0)
	{
		current = context->currentBoard[currentPosR][currentPosC];
		if(current == NULL_CHAR)
			break; // Reached uppermost contiguous word
		// if the letter is blank then it is in upper case
		boolean isBlank = current < 'a';
		// if uppercase, change to lowercase to check in dawg array
		if(isBlank)
			current += 32;
		// Add the score from that letter
		crossCheckScore += isBlank ? 0 : LETTER_SCORES[current - 'a'];
		// Go up
		currentPosR -= dR;
		currentPosC -= dC;
	}

	// Append all letters above current position
	for(int i = currentPosR + dR, j = currentPosC + dC; i + dR <= row && j + dC <= col; i += dR, j += dC)
		appendToMyStringBuffer(stringBuf, context->currentBoard[i][j] < 'a' ? (char)(context->currentBoard[i][j] + 32) : context->currentBoard[i][j]);

	// Record where we are trying to place letter
	thisPosition = stringBuf->position;
	// Space for the current letter
	stringBuf->position++;

	// Append all letters below current position
	currentPosR = row + dR;
	currentPosC = col + dC;
	while(currentPosR <= BOARD_SIZE && currentPosC <= BOARD_SIZE)
	{
		current = context->currentBoard[currentPosR][currentPosC];
		if(current == NULL_CHAR)
			break; // Reached lowermost contiguous word
		// if the letter is blank then it is in upper case
		boolean isBlank = current < 'a';
		// if uppercase, change to lowercase to check in dawg array
		if(isBlank)
			current += 32;
		// Append letter
		appendToMyStringBuffer(stringBuf, current);
		// Add the score from that letter
		crossCheckScore += isBlank ? 0 : LETTER_SCORES[current - 'a'];
		// Go down
		currentPosR += dR;
		currentPosC += dC;
	}

	int* crossCheckArr = (down ? context->crossChecks[row - 1][col - 1].down : context->crossChecks[row - 1][col - 1].across);

	if(thisPosition == 0 && stringBuf->position == 1)
	{
		// There are no letters above or below this position.
		// All letters are acceptable and cross score is 0.
		crossCheckArr[0] = 0;
		crossCheckArr[1] = 0xFFFFFFFF;
		return;
	}

	// Bit mask of acceptable letters
	int crossCheck = 0;

	// Find all letters that form legal words with the cross words
	for(int i = 0; i < 26; i++)
	{
		stringBuf->stringBuf[thisPosition] = (char) (i + 'a');
		if(wordExists(context->dawgArray, stringBuf))
			crossCheck |= (1 << i);
	}

	if(crossCheck == 0)
	{
		// There are no words that form legal words
		// No letters are acceptable and cross score is 0.
		crossCheckArr[0] = 0;
		crossCheckArr[1] = 0;
		return;
	}

	// There are some letters that form a legal word with the cross words.
	// Record that and the score.
	crossCheckArr[0] = crossCheckScore;
	crossCheckArr[1] = crossCheck;
}
