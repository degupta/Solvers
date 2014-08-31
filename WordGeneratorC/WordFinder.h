#ifndef WORD_FINDER_H
#define WORD_FINDER_H

#include "common.h"
#include "DawgArray.h"

#define BOARD_SIZE 15
#define BLANK_TILE_RACK ('z' + 1)
#define NUM_TILE_TYPES 27
#define BINGO_SCORE 35
#define BLANK_FUDGER_ENABLED TRUE
#define LETTERS_FOR_BINGO 7
#define NUM_BLANKS 2

/* word, row, col, score, down */
typedef void (*LegalMove_t)(char*, int, int, int, boolean);

typedef struct {
	boolean isDirty;
	int across[2];
	int down[2];
} CrossCheck;

typedef struct
{
	char** currentBoard;
	int* currentRack;
	LegalMove_t LegalMoveFunction;
	MyStringBuffer partialWord;
	DawgArray dawgArray;
	CrossCheck crossChecks[BOARD_SIZE][BOARD_SIZE];
	int anchorRow;
	int anchorCol;
	boolean down;
	MyStringBuffer tempString;
	int lettersPlaced;
	int currentScore;
	int currentCrossScore;
	int currentWordMultiplier;
	boolean hasBlanks;

} WordFinderContext, *WordFinderContext_t;

void getNextMove(DawgArray _dawgArray, char** currentBoard, int* currentRack, LegalMove_t LegalMoveFunc);
void getFirstMove(DawgArray _dawgArray, char** currentBoard, int* currentRack, LegalMove_t LegalMoveFunc);

#endif
