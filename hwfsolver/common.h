#ifndef COMMON_H
#define COMMON_H

#define FALSE 0
#define TRUE 1
#define NULL_CHAR '\0'
#define MAX_WORD_SIZE 8
#define NUM_LETTERS 26
#define UNKNOWN_CHAR '.'

typedef char boolean;

typedef struct
{
	int position; // Also represents the length
	char stringBuf[MAX_WORD_SIZE + 1];
} MyStringBuffer, *MyStringBuffer_t;

#endif
