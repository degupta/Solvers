#define RUBY_ENV

#ifdef RUBY_ENV
	#include <ruby.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "DawgArray.h"

typedef struct {
	char board[BOARD_SIZE][BOARD_SIZE];
	int letter_multipliers[BOARD_SIZE][BOARD_SIZE];
	int word_multipliers[BOARD_SIZE][BOARD_SIZE];
	boolean visited[BOARD_SIZE][BOARD_SIZE];

	DawgArray dawgArray;

	MyStringBuffer partialWord;
	MyStringBuffer partialWordLocation;
	int totalScore;
	int currentScore;
	int currentWordMultiplier;
	int targetScore;

	#ifdef RUBY_ENV
		ID solver_callback;
		VALUE thizz;
	#endif
} SWFSolverContext, *SWFSolverContext_t;

const int LETTER_SCORES[NUM_LETTERS] = {
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
	10
};


void appendToMyStringBuffer(MyStringBuffer_t strBuf, char c) {
	strBuf->stringBuf[strBuf->position] = c;
	strBuf->position++;
}

void removeFromMyStringBuffer(MyStringBuffer_t strBuf) {
	strBuf->position--;
	strBuf->stringBuf[strBuf->position] = NULL_CHAR;
}

boolean find_words_helper(SWFSolverContext_t context, int node, int row, int col) {
	char edge = context->board[row][col];
	context->visited[row][col] = TRUE;
	appendToMyStringBuffer(&(context->partialWord), edge);
	appendToMyStringBuffer(&(context->partialWordLocation), (char)((BOARD_SIZE - row - 1) * BOARD_SIZE + col + 'A'));
	int scoreToAdd = LETTER_SCORES[edge - 'a'] * context->letter_multipliers[row][col];
	context->currentScore += scoreToAdd;
	context->currentWordMultiplier *= context->word_multipliers[row][col];

	int children[NUM_LETTERS];

	// q is always followed by u
	if(edge == 'q') {
		getChildren(context->dawgArray, node, children);
		int uChild = children['u' - 'a'];
		// Is there a 'u'?
		if(uChild == 0) {
			// No, no point going further, just return from here
			context->visited[row][col] = FALSE;
			removeFromMyStringBuffer(&(context->partialWord));
			removeFromMyStringBuffer(&(context->partialWordLocation));
			context->currentScore -= scoreToAdd;
			context->currentWordMultiplier /= context->word_multipliers[row][col];
			return FALSE;
		}

		// Now we are on the u Node
		edge = 'u';
		node = uChild;
	}

	if(isEndOFWord(context->dawgArray, node)) {
		context->partialWord.stringBuf[context->partialWord.position] = NULL_CHAR;
		context->partialWordLocation.stringBuf[context->partialWordLocation.position] = NULL_CHAR;
		int wordScore = context->currentScore * context->currentWordMultiplier;
		context->totalScore += wordScore;

		#ifdef RUBY_ENV
			rb_funcall(context->thizz, context->solver_callback, 3, rb_str_new2(context->partialWord.stringBuf), INT2NUM(wordScore), rb_str_new2(context->partialWordLocation.stringBuf));
		#endif

		#ifndef RUBY_ENV
			printf("%s %d\n", context->partialWord.stringBuf, context->currentScore * context->currentWordMultiplier);
		#endif

		if(context->totalScore >= context->targetScore) {
			return TRUE;
		}
	}

	getChildren(context->dawgArray, node, children);

	int startR = row - 1;
	if(startR < 0) {
		startR = 0;
	}
	int startC = col - 1;
	if(startC < 0) {
		startC = 0;
	}

	int endR = row + 1;
	if(endR >= BOARD_SIZE) {
		endR = BOARD_SIZE - 1;
	}
	int endC = col + 1;
	if(endC >= BOARD_SIZE) {
		endC = BOARD_SIZE - 1;
	}

	int r, c;
	for(r = startR; r <= endR; r++) {
		for(c = startC; c <= endC; c++) {
			if(context->visited[r][c] == TRUE) {
				continue;
			}
			int childPointer = children[context->board[r][c] - 'a'];
			if(childPointer == 0) {
				continue;
			}
			if(find_words_helper(context, childPointer, r, c)) {
				return TRUE;
			}
		}
	}

	context->visited[row][col] = FALSE;
	removeFromMyStringBuffer(&(context->partialWord));
	removeFromMyStringBuffer(&(context->partialWordLocation));
	context->currentScore -= scoreToAdd;
	context->currentWordMultiplier /= context->word_multipliers[row][col];

	return FALSE;
}

void reset_context(SWFSolverContext_t context) {
	context->partialWord.position = 0;
	context->partialWordLocation.position = 0;
	context->currentScore = 0;
	context->currentWordMultiplier = 1;
}

void find_words(SWFSolverContext_t context) {
	int rootChildren[NUM_LETTERS];
	getChildren(context->dawgArray, 0, rootChildren);
	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			reset_context(context);
			int childPointer = rootChildren[context->board[row][col] - 'a'];
			if(childPointer == 0) {
				continue;
			}
			if(find_words_helper(context, childPointer, row, col)) {
				return;
			}
		}
	}
}

#ifdef RUBY_ENV

int atoi_single_digit(char digit) {
	return (int)(digit - '0');
}

static VALUE swfsolver_find_all_words(VALUE self, VALUE board, VALUE letter_multipliers, VALUE word_multipliers, VALUE targetScore, VALUE dawgFilePath) {
	SWFSolverContext context;

	char* currentBoardArr = StringValuePtr(board);
	char* letterMultipliersArr = StringValuePtr(letter_multipliers);
	char* wordMultipliersArr = StringValuePtr(word_multipliers);

	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		int oppRow = (BOARD_SIZE - row - 1) * BOARD_SIZE;
		for(col = 0; col < BOARD_SIZE; col++) {
			context.board[row][col] = currentBoardArr[oppRow + col];
			context.letter_multipliers[row][col] = atoi_single_digit(letterMultipliersArr[oppRow + col]);
			context.word_multipliers[row][col] = atoi_single_digit(wordMultipliersArr[oppRow + col]);
			context.visited[row][col] = FALSE;
		}
	}

	context.dawgArray = createDawgFromFile(StringValuePtr(dawgFilePath));
	context.thizz = self;
	context.solver_callback = rb_intern("solver_callback");
	context.totalScore = 0;
	context.targetScore = NUM2INT(targetScore);

	find_words(&context);

	free(context.dawgArray);

	return INT2NUM(0);
}

void Init_swfsolver(void) {
	VALUE klass = rb_define_class("SWFSolver", rb_cObject);
	rb_define_method(klass, "find_all_words", swfsolver_find_all_words, 5);
}
#endif

#ifndef RUBY_ENV
int main(int argc, char** argv) {
	SWFSolverContext_t context = (SWFSolverContext_t) malloc(sizeof(SWFSolverContext_t));
	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			context->board[row][col] = (char)('a' + row * BOARD_SIZE + col);
			context->letter_multipliers[row][col] = 1;
			context->word_multipliers[row][col] = 1;
			context->visited[row][col] = FALSE;
		}
	}

	context->dawgArray = createDawgFromFile("dict");
	context->totalScore = 0;
	context->targetScore = 1000;

	find_words(context);

	free(context->dawgArray);
	free(context);
	return 0;
}
#endif