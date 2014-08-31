#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "WordFinder.h"
#include "DawgArray.h"

void toMyStringBuffer(MyStringBuffer_t buffer,const char * word)
{
	int i = 0;
	i = 0;
	while(word[i] != '\0' && word[i] != '\n')
	{
		buffer->stringBuf[i] = word[i];
		i++;
	}
	buffer->stringBuf[i] = '\0';
	buffer->position = i;
}

void testDawg(const char * fileName, DawgArray dawgArray)
{
	MyStringBuffer buffer;
	char word[50];
	FILE *fr = fopen (fileName, "rt");
	int numBad = 0;
	while(fgets(word, 50, fr) != 0)
	{
		if(strlen(word) > MAX_WORD_SIZE)
			continue;
		toMyStringBuffer(&buffer, word);
		if(wordExists(dawgArray, &buffer) == FALSE)
		{
			numBad++;
			printf("%s, %d", buffer.stringBuf, buffer.position);
		}
	}
	fclose(fr);
	printf("Num bad : %d\n", numBad);
}

void LegalMove(char* word, int row, int col, int score, boolean down)
{
	printf("Making word %s from (%d, %d) going %s for %d points\n", word, row, col, (down == TRUE ? "down" : "across"), score);
}

int main(int argc, char *argv[])
{
	DawgArray dawgArray = createDawgFromFile("dict");

	char* board[BOARD_SIZE + 2];
	for(int i = 0; i < BOARD_SIZE + 2; i++)
	{
		board[i] = (char *)(malloc(BOARD_SIZE + 2));
		for(int j = 0; j < BOARD_SIZE + 2; j++)
		{
			board[i][j] = (char)NULL_CHAR;
		}
	}

	int rack[NUM_TILE_TYPES];
	for(int i = 0; i < NUM_TILE_TYPES; i++)
		rack[i] = 0;

	board[6][10] = 'p';

	board[7][10] = 'l';

	board[8][8] = 'g';
	board[8][9] = 'l';
	board[8][10] = 'a';
	board[8][11] = 'd';
	board[8][12] = 's';

	board[9][10] = 'n';
	board[9][11] = 'e';
	board[9][12] = 'o';
	board[9][13] = 'n';

	board[10][9] = 'l';
	board[10][10] = 'e';
	board[10][11] = 'x';

	board[11][9] = 'i';
	board[11][10] = 't';

	board[12][7] = 'j';
	board[12][8] = 'E';
	board[12][9] = 'e';

	rack['o' - 'a']++;
	rack['w' - 'a']++;
	rack['g' - 'a']++;
	rack['e' - 'a']++;
	rack['l' - 'a']++;
	rack['o' - 'a']++;
	rack['e' - 'a']++;

	getNextMove(dawgArray, board, rack, &LegalMove);
	free(dawgArray);
	for(int i = 0; i < BOARD_SIZE + 2; i++)
		free(board[i]);
	return 0;
}
