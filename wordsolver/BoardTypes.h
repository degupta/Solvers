#ifndef BOARD_TYPES_H
#define BOARD_TYPES_H

typedef enum {
	CLASSIC_BOARD,
	TOURNAMENT_BOARD
} BoardType;

int getBoardSize(BoardType boardType);
void setDataForBoard(BoardType boardType, int* boardSize, int* bingoScore, int* lettersForBingo, int* wordMultipliers, int* letterMultipliers);

#endif /* BOARD_TYPES_H */