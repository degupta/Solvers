#include <ruby.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include "WordFinder.h"
#include "DawgArray.h"

typedef struct {
	VALUE thizz;
} WordSolverCustomData, *WordSolverCustomData_t;

static DawgArray dawgArray = NULL;

void LegalMove(char* word, int row, int col, int score, boolean down, void* customData) {
	rb_funcall(((WordSolverCustomData_t) customData)->thizz, rb_intern("solver_callback"), 5, rb_str_new2(word), INT2FIX(row), INT2FIX(col), INT2FIX(score), down == TRUE ? Qtrue : Qfalse);
}

void debug_print(void* customData, char* fmt, ...) {
	char buffer[255];
	va_list args;
	va_start(args, fmt);
	vsprintf(buffer, fmt, args);
	rb_funcall(((WordSolverCustomData_t) customData)->thizz, rb_intern("debug_print"), 1, rb_str_new2(buffer));
	va_end(args);
}

static VALUE wordsolver_solve_board(VALUE self, VALUE currentBoard, VALUE currentRack, VALUE dawgFilePath, VALUE _boardType) {
	if (dawgArray == NULL) {
		dawgArray = createDawgFromFile(StringValuePtr(dawgFilePath));
	}

	BoardType boardType = (BoardType) NUM2INT(_boardType);

	int boardSize = getBoardSize(boardType);

	int paddedBoardSize = boardSize + 2;
	char board[(MAX_BOARD_SIZE + 2) * (MAX_BOARD_SIZE + 2)];
	int currentIndex = 0;
	char* currentBoardArr = StringValuePtr(currentBoard);
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
	char* currentRackArr =  StringValuePtr(currentRack);
	int len = strlen(currentRackArr);
	for(int i = 0; i < NUM_TILE_TYPES; i++) {
		rack[i] = 0;
	}
	for(int i = 0; i < len; i++) {
		rack[currentRackArr[i] - 'a']++;
	}

	WordSolverCustomData customData;
	customData.thizz = self;

	if(firstMove == TRUE) {
		getFirstMove(dawgArray, board, rack, &LegalMove, &debug_print, boardType, (void*) &customData);
	}
	else {
		getNextMove(dawgArray, board, rack, &LegalMove, &debug_print, boardType, (void*) &customData);
	}

	return INT2FIX(0);
}

static VALUE wordsolver_test_method(VALUE self) {
  	return rb_str_new2("bonjour!");
}

/* ruby calls this to load the extension */
void Init_wordsolver(void) {
	VALUE klass = rb_define_class("WordSolver", rb_cObject);
	rb_define_method(klass, "solve_board", wordsolver_solve_board, 4);
	rb_define_method(klass, "test_method", wordsolver_test_method, 0);
}