#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "WordFinder.h"
#include "DawgArray.h"
#include <stdarg.h>

static DawgArray dawgArray = NULL;

void LegalMoveMain(char* word, int row, int col, int score, boolean down, void* customData) {
	printf("%s at row %d and col %d going %s, for %d points\n", word, row, col, down ? "down" : "across", score);
}

void debug_print_main(void* customData, char* fmt, ...) {
	va_list args;
	va_start(args, fmt);
	vprintf(fmt, args);
	va_end(args);
}

void solve_board(char* currentBoard, char* currentRack, char* dawgFilePath, BoardType boardType) {
	if (dawgArray == NULL) {
		dawgArray = createDawgFromFile(dawgFilePath);
	}

	int boardSize = getBoardSize(boardType);

	int paddedBoardSize = boardSize + 2;
	
	char board[(MAX_BOARD_SIZE + 2) * (MAX_BOARD_SIZE + 2)];
	int currentIndex = 0;
	char* currentBoardArr = currentBoard;
	boolean firstMove = TRUE;
	int currentPosition = 0;
	for(int row = 0; row < paddedBoardSize; row++) {
		for(int col = 0; col < paddedBoardSize; col++) {
			if(row == 0 || row == boardSize + 1 || col == 0 || col == boardSize + 1) {
				board[currentPosition] = (char) NULL_CHAR;
			}
			else {
				char c = currentBoardArr[currentIndex];
				board[currentPosition] = c;
				if(c != NULL_CHAR) {
					firstMove = FALSE;
				}
				currentIndex++;
			}
			currentPosition++;
		}
	}

	int rack[NUM_TILE_TYPES];
	char* currentRackArr =  currentRack;
	int len = strlen(currentRackArr);
	for(int i = 0; i < NUM_TILE_TYPES; i++) {
		rack[i] = 0;
	}
	for(int i = 0; i < len; i++) {
		rack[currentRackArr[i] - 'a']++;
	}

	if(firstMove == TRUE) {
		getFirstMove(dawgArray, board, rack, &LegalMoveMain, &debug_print_main, boardType, NULL);
	}
	else {
		getNextMove(dawgArray, board, rack, &LegalMoveMain, &debug_print_main, boardType, NULL);
	}
}

int main(int argc, char**argv) {
	char board[225];
	for (int i = 0; i < 225; i++) {
		board[i] = NULL_CHAR;
	}
	solve_board(board, "abcedfg", "dict", 0);
}