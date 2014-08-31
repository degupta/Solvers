#define RUBY_ENV

#ifdef RUBY_ENV
	#include <ruby.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "common.h"


typedef struct {
	char board[BOARD_SIZE][BOARD_SIZE];
	int bonus[BOARD_SIZE][BOARD_SIZE];
	char tiles[NUM_TILES][TILE_SIZE];

	char playerColor;
	int lowerBound;
	int upperBound;
	int midPoint;
	
	// currentMove[2 * i] = position of ith tile currentMove[2 * i + 1] = dir of ith tile
	int currentMove[NUM_TILES * 2];
	int bestScoreDiff;
	int bestMove[NUM_TILES * 2];
	int bestScore;
} MatchingContext, *MatchingContext_t;

typedef struct {
	MatchingContext_t context;
	boolean visited[BOARD_SIZE][BOARD_SIZE];
	int scoreMultiplier;
	int tilesFound;
	int starsFound;

	#ifdef RUBY_ENV
		VALUE matches;
	#endif
} MatchingScoreContext, *MatchingScoreContext_t;

boolean inBoard(int pos) {
	return pos >= 0 && pos < BOARD_SIZE ? TRUE : FALSE;
}

void findScoreStartingAtFor(MatchingScoreContext_t scoreContext, int row, int col, char myColor) {
	int dR = 0;
	int dC = 1;
	int r, c, i;
	for(i = 0; i < 4; i++) {
		r = row + dR;
		c = col + dC;
		int temp = dR;
		dR = dC;
		dC = -temp;
		if(!inBoard(r) || !inBoard(c)) {
			continue;
		}
		if(scoreContext->visited[r][c] == TRUE) {
			continue;
		}
		char otherColor = scoreContext->context->board[r][c];
		if(otherColor == EMPTY_SPACE) {
			scoreContext->visited[r][c] = TRUE;
			continue;
		}
		if(otherColor == STAR_CHAR) {
			scoreContext->starsFound += 1;
		}
		else {
			if(myColor != otherColor) {
				continue;
			}
			scoreContext->tilesFound += 1;
		}

		#ifdef RUBY_ENV
			VALUE hash = rb_hash_new();
			rb_hash_aset(hash, rb_str_new2("r"), INT2NUM(r));
			rb_hash_aset(hash, rb_str_new2("c"), INT2NUM(c));
			rb_ary_push(scoreContext->matches, hash);
		#endif

		scoreContext->scoreMultiplier *= scoreContext->context->bonus[r][c];
		scoreContext->visited[r][c] = TRUE;
		findScoreStartingAtFor(scoreContext, r, c, myColor);
	}
}

int calculateScore(MatchingContext_t context) {
	MatchingScoreContext scoreContext;
	scoreContext.context = context;

	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			scoreContext.visited[row][col] = FALSE;
		}
	}

	int totalScore = 0;

	int i, j, temp;

	for(i = 0; i < NUM_TILES; i++) {
		int dir = context->currentMove[2 * i + 1];
		int pos = context->currentMove[2 * i];

		if(dir == -1 || pos == -1) {
			continue;
		}

		int dR = 0;
		int dC = 1;
		for(j = 0; j < dir; j++) {
			temp = dR;
			dR = dC;
			dC = -temp;
		}

		int startRow = pos / BOARD_SIZE;
		int startCol = pos % BOARD_SIZE;

		for(j = 0; j < TILE_SIZE; j++) {
			row = startRow + dR * j;
			col = startCol + dC * j;

			if(scoreContext.visited[row][col] == TRUE) {
				continue;
			}

			char myColor = context->board[row][col];

			scoreContext.visited[row][col] = TRUE;

			if(myColor == STAR_CHAR) {
				int k;
				for(k = 0; k < ITEM_COUNT; k++) {
					scoreContext.scoreMultiplier = context->bonus[row][col];
					scoreContext.tilesFound = 0;
					scoreContext.starsFound = 1;
					myColor = (char)(START_ITEM + k);

					#ifdef RUBY_ENV
						scoreContext.matches = rb_ary_new();
					#endif

					findScoreStartingAtFor(&scoreContext, row, col, myColor);
					if(scoreContext.tilesFound >= 1 && scoreContext.tilesFound + scoreContext.starsFound >= MIN_MATCHED_TILES_REQUIRED) {
						totalScore += (scoreContext.tilesFound + scoreContext.starsFound) * SCORE_PER_TILE * scoreContext.scoreMultiplier * (myColor == context->playerColor ? 2 : 1);
					}

					#ifdef RUBY_ENV
						int toClear = RARRAY_LEN(scoreContext.matches);
						int l;
						for(l = 0; l < toClear; l++) {
							VALUE posHash = rb_ary_entry(scoreContext.matches, l);
							int clearRow = NUM2INT(rb_hash_aref(posHash, rb_str_new2("r")));
							int clearCol = NUM2INT(rb_hash_aref(posHash, rb_str_new2("c")));
							if(context->board[clearRow][clearCol] == STAR_CHAR) {
								scoreContext.visited[clearRow][clearCol] = FALSE;
							}
						}
					#endif


					#ifndef RUBY_ENV
						int clearRow, clearCol;
						for(clearRow = 0; clearRow < BOARD_SIZE; clearRow++) {
							for(clearCol = 0; clearCol < BOARD_SIZE; clearCol++) {
								if(clearCol != col && clearRow != row && context->board[clearRow][clearCol] == STAR_CHAR) {
									scoreContext.visited[clearRow][clearCol] = FALSE;
								}
							}
						}
					#endif
				}
			}
			else {
				scoreContext.scoreMultiplier = context->bonus[row][col];
				scoreContext.starsFound = 0;
				scoreContext.tilesFound = 1;

				#ifdef RUBY_ENV
					scoreContext.matches = rb_ary_new();
				#endif

				findScoreStartingAtFor(&scoreContext, row, col, myColor);
				if(scoreContext.tilesFound >= 1 && scoreContext.tilesFound + scoreContext.starsFound >= MIN_MATCHED_TILES_REQUIRED) {
					totalScore += (scoreContext.tilesFound + scoreContext.starsFound) * SCORE_PER_TILE * scoreContext.scoreMultiplier * (myColor == context->playerColor ? 2 : 1);
				}

				#ifdef RUBY_ENV
					int toClear = RARRAY_LEN(scoreContext.matches);
					int l;
					for(l = 0; l < toClear; l++) {
						VALUE posHash = rb_ary_entry(scoreContext.matches, l);
						int clearRow = NUM2INT(rb_hash_aref(posHash, rb_str_new2("r")));
						int clearCol = NUM2INT(rb_hash_aref(posHash, rb_str_new2("c")));
						if(context->board[clearRow][clearCol] == STAR_CHAR) {
							scoreContext.visited[clearRow][clearCol] = FALSE;
						}
					}
				#endif

				#ifndef RUBY_ENV
					int clearRow, clearCol;
					for(clearRow = 0; clearRow < BOARD_SIZE; clearRow++) {
						for(clearCol = 0; clearCol < BOARD_SIZE; clearCol++) {
							if(context->board[clearRow][clearCol] == STAR_CHAR) {
								scoreContext.visited[clearRow][clearCol] = FALSE;
							}
						}
					}
				#endif
			}
		}
	}

	return totalScore;
}

void removeTile(MatchingContext_t context, char *currentTile, int row, int col, int dir) {
	int dR = 0;
	int dC = 1;
	if(dir == 1) {
		dC = 0;
		dR = 1;
	}
	else if(dir == 2) {
		dC = -1;
		dR = 0;
	}
	else if(dir == 3) {
		dC = 0;
		dR = -1;
	}

	int i;
	for(i = 0; i < TILE_SIZE; i++) {
		context->board[row + dR * i][col + dC * i] = EMPTY_SPACE;
	}
}

boolean checkAndPlaceTile(MatchingContext_t context, char *currentTile, int row, int col, int dir) {
	int i, j;
	if(dir  % 2 == 0) {
		int d = dir < 2 ? 1 : -1;

		if(!inBoard(col + d * (TILE_SIZE - 1))) {
			return FALSE;
		}

		for(i = 0; i < TILE_SIZE; i++) {
			if(context->board[row][col + d * i] != EMPTY_SPACE) {
				return FALSE;
			}
		}

		if( (inBoard(col - d) && context->board[row][col - d] != EMPTY_SPACE) ||
			(inBoard(col + d * TILE_SIZE) && context->board[row][col + d * TILE_SIZE] != EMPTY_SPACE)) {
			for(i = 0; i < TILE_SIZE; i++) {
				context->board[row][col + d * i] = currentTile[i];
			}
			return TRUE;
		}

		for(i = 0; i < TILE_SIZE; i++) {
			if( (row > 0 && context->board[row - 1][col + d * i] != EMPTY_SPACE) ||
				(row < BOARD_SIZE - 1 && context->board[row + 1][col + d * i] != EMPTY_SPACE)) {
				for(j = 0; j < TILE_SIZE; j++) {
					context->board[row][col + d * j] = currentTile[j];
				}
				return TRUE;
			}
		}

		return FALSE;
	}
	else {
		int d = dir < 2 ? 1 : -1;
		
		if(!inBoard(row + d * (TILE_SIZE - 1))) {
			return FALSE;
		}

		for(i = 0; i < TILE_SIZE; i++) {
			if(context->board[row + d * i][col] != EMPTY_SPACE) {
				return FALSE;
			}
		}

		if( (inBoard(row - d) && context->board[row - d][col] != EMPTY_SPACE) ||
			(inBoard(row + d * TILE_SIZE) && context->board[row + d * TILE_SIZE][col] != EMPTY_SPACE)) {
			for(i = 0; i < TILE_SIZE; i++) {
				context->board[row + d * i][col] = currentTile[i];
			}
			return TRUE;
		}

		for(i = 0; i < TILE_SIZE; i++) {
			if( (col > 0 && context->board[row + d * i][col - 1] != EMPTY_SPACE) ||
				(col < BOARD_SIZE - 1 && context->board[row + d * i][col + 1] != EMPTY_SPACE)) {
				for(j = 0; j < TILE_SIZE; j++) {
					context->board[row + d * j][col] = currentTile[j];
				}
				return TRUE;
			}
		}

		return FALSE;	
	}
}

boolean findMoves(MatchingContext_t context, int currentTileIndex) {
	if(currentTileIndex >= NUM_TILES) {
		int moveScore = calculateScore(context);
		int scoreDiff = context->midPoint - moveScore;
		if(scoreDiff < 0) {
			scoreDiff *= -1;
		}
		boolean inRange = moveScore >= context->lowerBound && moveScore <= context->upperBound;
		if(inRange || scoreDiff < context->bestScoreDiff) {
			context->bestScoreDiff = scoreDiff;
			int i;
			int len = NUM_TILES * 2;
			for(i = 0; i < len; i++) {
				context->bestMove[i] = context->currentMove[i];
			}
			context->bestScore = moveScore;
		}
		return inRange;
	}
	int row, col, dir;
	char *currentTile = context->tiles[currentTileIndex];
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			for(dir = 0; dir < 4; dir++) {
				if(checkAndPlaceTile(context, currentTile, row, col, dir)) {
					int index = 2 * currentTileIndex;
					context->currentMove[index] = row * BOARD_SIZE + col;
					context->currentMove[index + 1] = dir;

					if(findMoves(context, currentTileIndex + 1) == TRUE) {
						return TRUE;
					}

					removeTile(context, currentTile, row, col, dir);
					context->currentMove[index] = -1;
					context->currentMove[index + 1] = -1;
				}
			}
		}
	}

	return FALSE;
}

int atoi_single_digit(char digit) {
	return (int)(digit - '0');
}

#ifdef RUBY_ENV

static VALUE
matching_find_moves_native(VALUE self, VALUE board, VALUE bonus, VALUE tiles, VALUE playerColor, VALUE lowerBound, VALUE upperBound) {
	MatchingContext context;

	char* boardArr = StringValuePtr(board);
	char* bonusArr = StringValuePtr(bonus);

	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			int index = row * BOARD_SIZE + col;
			context.board[row][col] = boardArr[index];
			context.bonus[row][col] = atoi_single_digit(bonusArr[index]);
		}
	}

	char* tilesArr = StringValuePtr(tiles);

	int i, j;
	for(i = 0; i < NUM_TILES; i++) {
		for(j = 0; j < TILE_SIZE; j++) {
			context.tiles[i][j] = tilesArr[i * TILE_SIZE + j];
		}
	}
	
	context.playerColor = StringValuePtr(playerColor)[0];
	context.lowerBound = NUM2INT(lowerBound);
	context.upperBound = NUM2INT(upperBound);
	context.midPoint = (context.upperBound + context.lowerBound) / 2;

	int len = NUM_TILES * 2;
	for(i = 0; i < len; i++) {
		context.bestMove[i] = -1;
	}
	context.bestScore = -1;
	context.bestScoreDiff = 2488320 * 3;

	findMoves(&context, 0);

	VALUE arrToReturn = rb_ary_new();
	for(i = 0; i < len; i++) {
		rb_ary_push(arrToReturn, INT2NUM(context.bestMove[i]));
	}
	rb_ary_push(arrToReturn, INT2NUM(context.bestScore));

	return arrToReturn;
}

void Init_matchingsolver(void) {
	VALUE klass = rb_define_class("MatchingSolver", rb_cObject);
	rb_define_method(klass, "find_moves_native", matching_find_moves_native, 6);
}
#endif

#ifndef RUBY_ENV
int parseInt(char* num) {
	int len = strlen(num);
	int i;
	int val = 0;
	for(i = 0; i < len; i++) {
		val = val * 10 + atoi_single_digit(num[i]);
	}
	return val;
}

char* dirToStr(int dir) {
	if(dir == 0) {
		return "RIGHT";
	}
	else if(dir == 1) {
		return "DOWN";
	}
	else if(dir == 2) {
		return "LEFT";
	}
	else if(dir == 3) {
		return "UP";
	}
	else {
		return "UNKNOWN";
	}
}

int main(int argc, char** argv) {
	MatchingContext context;
	char* boardArr = argv[1];
	char* bonusArr = argv[2];

	int row, col;
	for(row = 0; row < BOARD_SIZE; row++) {
		for(col = 0; col < BOARD_SIZE; col++) {
			int index = row * BOARD_SIZE + col;
			context.board[row][col] = boardArr[index];
			context.bonus[row][col] = atoi_single_digit(bonusArr[index]);
		}
	}

	char* tilesArr = argv[3];

	int i, j;
	for(i = 0; i < NUM_TILES; i++) {
		for(j = 0; j < TILE_SIZE; j++) {
			context.tiles[i][j] = tilesArr[i * TILE_SIZE + j];
		}
	}

	context.playerColor = argv[4][0];
	context.lowerBound = parseInt(argv[5]);
	context.upperBound = parseInt(argv[6]);
	context.midPoint = (context.upperBound + context.lowerBound) / 2;

	int len = NUM_TILES * 2;
	for(i = 0; i < len; i++) {
		context.bestMove[i] = -1;
	}
	context.bestScore = -1;
	context.bestScoreDiff = 2488320 * 3;

	findMoves(&context, 0);

	for(i = 0; i < NUM_TILES; i++) {
		printf("%d %d :: %d %d %s\n", context.bestMove[2 * i], context.bestMove[2 * i + 1], context.bestMove[2 * i] / BOARD_SIZE, context.bestMove[2 * i] % BOARD_SIZE, dirToStr(context.bestMove[2 * i + 1]));
	}
	printf("Score : %d\n", context.bestScore);

	return 0;
}
#endif
