#include "BoardTypes.h"
#include <string.h>

//--------------------------------------------------------------------
// CLASSIC BOARD CONSTANTS
//--------------------------------------------------------------------

#define CLASSIC_BOARD_SIZE 15
#define CLASSIC_BINGO_SCORE 35
#define CLASSIC_LETTERS_FOR_BINGO 7

const int CLASSIC_WORD_MULTIPLIERS[CLASSIC_BOARD_SIZE + 2][CLASSIC_BOARD_SIZE + 2] =
{
	//  1  2  3  4  5  6  7  8  9  10 11 12 13 14 15
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0},// 1
	{0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0},// 2
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 3
	{0, 3, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 0},// 4
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 5
	{0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0},// 6
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 7
	{0, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 0},// 8
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 9
	{0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0},// 10
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 11
	{0, 3, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 3, 0},// 12
	{0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},// 13
	{0, 1, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 1, 0},// 14
	{0, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 0},// 15
	{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
};

const int CLASSIC_LETTER_MULTIPLIERS[CLASSIC_BOARD_SIZE + 2][CLASSIC_BOARD_SIZE + 2] =
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



//--------------------------------------------------------------------
// TOURNAMENT BOARD CONSTANTS
//--------------------------------------------------------------------
#define TOURNAMENT_BOARD_SIZE 11
#define TOURNAMENT_BINGO_SCORE 35
#define TOURNAMENT_LETTERS_FOR_BINGO 7

const int TOURNAMENT_WORD_MULTIPLIERS[TOURNAMENT_BOARD_SIZE + 2][TOURNAMENT_BOARD_SIZE + 2] =
{
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
	{ 0, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 0 }, // 1
	{ 0, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0 }, // 2
	{ 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0 }, // 3
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 4
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 5
	{ 0, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 0 }, // 6
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 7
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 8
	{ 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0 }, // 9
	{ 0, 1, 2, 1, 1, 1, 2, 1, 1, 1, 2, 1, 0 }, // 10
	{ 0, 1, 1, 3, 1, 1, 1, 1, 1, 3, 1, 1, 0 }, // 11
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
};


const int TOURNAMENT_LETTER_MULTIPLIERS[TOURNAMENT_BOARD_SIZE + 2][TOURNAMENT_BOARD_SIZE + 2] =
{
	//  1  2  3  4  5  6  7  8  9  10 11
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
	{ 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0 }, // 1
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 2
	{ 0, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 0 }, // 3
	{ 0, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 0 }, // 4
	{ 0, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 0 }, // 5
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 6
	{ 0, 1, 1, 2, 1, 1, 1, 1, 1, 2, 1, 1, 0 }, // 7
	{ 0, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 0 }, // 8
	{ 0, 1, 1, 2, 1, 2, 1, 2, 1, 2, 1, 1, 0 }, // 9
	{ 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0 }, // 10
	{ 0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0 }, // 11
	{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
};

int getBoardSize(BoardType boardType) {
	switch(boardType) {
		case TOURNAMENT_BOARD:
			return TOURNAMENT_BOARD_SIZE;
		case CLASSIC_BOARD:
		default:
			return CLASSIC_BOARD_SIZE;
	}
}

void setDataForBoard(BoardType boardType, int* boardSize, int* bingoScore, int* lettersForBingo, int* wordMultipliers, int* letterMultipliers) {
	int size = 0;
	switch(boardType) {
		case TOURNAMENT_BOARD:
			*boardSize = TOURNAMENT_BOARD_SIZE;
			*bingoScore = TOURNAMENT_BINGO_SCORE;
			*lettersForBingo = TOURNAMENT_LETTERS_FOR_BINGO;
			size = sizeof(int) * (TOURNAMENT_BOARD_SIZE + 2) * (TOURNAMENT_BOARD_SIZE + 2);
			memcpy(wordMultipliers, &(TOURNAMENT_WORD_MULTIPLIERS[0][0]), size);
			memcpy(letterMultipliers, &(TOURNAMENT_LETTER_MULTIPLIERS[0][0]), size);
			break;
		case CLASSIC_BOARD:
		default:
			*boardSize = CLASSIC_BOARD_SIZE;
			*bingoScore = CLASSIC_BINGO_SCORE;
			*lettersForBingo = CLASSIC_LETTERS_FOR_BINGO;
			size = sizeof(int) * (CLASSIC_BOARD_SIZE + 2) * (CLASSIC_BOARD_SIZE + 2);
			memcpy(wordMultipliers, &(CLASSIC_WORD_MULTIPLIERS[0][0]), size);
			memcpy(letterMultipliers, &(CLASSIC_LETTER_MULTIPLIERS[0][0]), size);
			break;
	}
}