#ifndef WORD_FINDER_H
#define WORD_FINDER_H

#include "common.h"
#include "BoardTypes.h"
#include "DawgArray.h"

#define MAX_BOARD_SIZE 15
#define BLANK_TILE_RACK ('z' + 1)
#define NUM_TILE_TYPES 27
#define BLANK_FUDGER_ENABLED TRUE


/* word, row, col, score, down */
typedef void (*LegalMove_t)(char*, int, int, int, boolean, void*);

typedef void (*DebugPrint_t)(void*, char*, ...);

typedef struct {
	boolean isDirty;
	int across[2];
	int down[2];
} CrossCheck, *CrossCheck_t;

typedef struct
{
	char* currentBoard;
	int* currentRack;
	LegalMove_t LegalMoveFunction;
	DebugPrint_t DebugPrintFunction;
	MyStringBuffer partialWord;
	DawgArray dawgArray;
	CrossCheck crossChecks[MAX_BOARD_SIZE * MAX_BOARD_SIZE];
	int anchorRow;
	int anchorCol;
	boolean down;
	MyStringBuffer tempString;
	int lettersPlaced;
	int currentScore;
	int currentCrossScore;
	int currentWordMultiplier;
	boolean hasBlanks;

	int boardSize;
	int paddedBoardSize;
	int bingoScore;
	int lettersForBingo;
	int wordMultipliers[(MAX_BOARD_SIZE + 2) * (MAX_BOARD_SIZE + 2)];
	int letterMultipliers[(MAX_BOARD_SIZE + 2) * (MAX_BOARD_SIZE + 2)];

	void* customData;

} WordFinderContext, *WordFinderContext_t;

void getNextMove(DawgArray _dawgArray, char* currentBoard, int* currentRack, LegalMove_t LegalMoveFunc, DebugPrint_t DebugPrintFunction, BoardType boardType, void* customData);
void getFirstMove(DawgArray _dawgArray, char* currentBoard, int* currentRack, LegalMove_t LegalMoveFunc, DebugPrint_t DebugPrintFunction, BoardType boardType, void* customData);

#endif
