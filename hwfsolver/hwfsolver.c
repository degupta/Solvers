#define RUBY_ENV

#ifdef RUBY_ENV
	#include <ruby.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "DawgArray.h"

typedef struct {
	DawgArray dawgArray;

	MyStringBuffer partialWord;
	
	char *currentWord;
	int wordLength;

	int letterCounts[NUM_LETTERS];
	boolean strikedLetters[NUM_LETTERS];

	int lastVowelPosition;
} HWFSolverContext, *HWFSolverContext_t;

typedef struct {
	DawgArray dawgArray;
	MyStringBuffer partialWord;
	int letterCounts[NUM_LETTERS];
	VALUE wordsArr;
	int minLen;
	int maxLen;
	int currentScore;
	int currentWordMultiplier;
	int bonusPosition;
	int letterMultiplier;
	int wordMultiplier;
} HWFFindWordsContext, *HWFFindWordsContext_t;

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

boolean isVowel(char c) {
	return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ? TRUE : FALSE;
}

void get_words_native_helper(HWFFindWordsContext_t context, int node) {
	if(context->partialWord.position >= context->minLen && isEndOFWord(context->dawgArray, node) && node > 0) {
		context->partialWord.stringBuf[context->partialWord.position] = NULL_CHAR;
		#ifdef RUBY_ENV
			VALUE hash = rb_hash_new();
			rb_hash_aset(hash, rb_str_new2("word"), rb_str_new2(context->partialWord.stringBuf));
			rb_hash_aset(hash, rb_str_new2("score"), INT2NUM(context->currentScore * context->currentWordMultiplier));
			rb_ary_push(context->wordsArr, hash);
		#endif

		#ifndef RUBY_ENV
			printf("%s\n", context->partialWord.stringBuf);
		#endif
	}

	if(context->partialWord.position >= context->maxLen) {
		return;
	}

	int children[NUM_LETTERS];
	getChildren(context->dawgArray, node, children);
	int i;

	for(i = 0; i < NUM_LETTERS; i++) {
		if(children[i] == 0 || context->letterCounts[i] <= 0) {
			continue;
		}
		appendToMyStringBuffer(&(context->partialWord), 'a' + i);
		context->letterCounts[i]--;
		if(context->partialWord.position == context->bonusPosition) {
			context->currentScore += LETTER_SCORES[i] * context->letterMultiplier;
			context->currentWordMultiplier *= context->wordMultiplier;
		}
		else {
			context->currentScore += LETTER_SCORES[i];
		}

		get_words_native_helper(context, children[i]);

		if(context->partialWord.position == context->bonusPosition) {
			context->currentScore -= LETTER_SCORES[i] * context->letterMultiplier;
			context->currentWordMultiplier /= context->wordMultiplier;
		}
		else {
			context->currentScore -= LETTER_SCORES[i];
		}
		context->letterCounts[i]++;
		removeFromMyStringBuffer(&(context->partialWord));
	}
}

void get_letter_probs_native_helper(HWFSolverContext_t context, int node, int currentPosition) {
	if(currentPosition > context->wordLength) {
		return;
	}
	int i;
	if(currentPosition == context->wordLength) {
		if(isEndOFWord(context->dawgArray, node)) {
			boolean addedLetters[NUM_LETTERS];
			for(i = 0; i < NUM_LETTERS; i++) {
				addedLetters[i] = 0;
			}
			for(i = 0; i < context->wordLength; i++) {
				int pos = context->partialWord.stringBuf[i] - 'a';
				if(!addedLetters[pos] && !context->strikedLetters[pos]) {
					context->letterCounts[pos]++;
					addedLetters[pos] = TRUE;
				}
			}
		}
		return;
	}

	int children[NUM_LETTERS];
	getChildren(context->dawgArray, node, children);

	char currentChar = context->currentWord[currentPosition];
	if(currentChar == UNKNOWN_CHAR) {
		for(i = 0; i < NUM_LETTERS; i++) {
			if(children[i] == 0 || context->strikedLetters[i]) {
				continue;
			}
			char c = 'a' + i;
			if(isVowel(c) && currentPosition > context->lastVowelPosition) {
				continue;
			}
			appendToMyStringBuffer(&(context->partialWord), c);
			get_letter_probs_native_helper(context, children[i], currentPosition + 1);
			removeFromMyStringBuffer(&(context->partialWord));
		}
	}
	else {
		if(children[currentChar - 'a'] == 0) {
			return;
		}
		appendToMyStringBuffer(&(context->partialWord), currentChar);
		get_letter_probs_native_helper(context, children[currentChar - 'a'], currentPosition + 1);
		removeFromMyStringBuffer(&(context->partialWord));
	}
}

#ifdef RUBY_ENV
static VALUE hwfsolver_get_letter_probs_native(VALUE self, VALUE currentWord, VALUE strikedLetters, VALUE dawgFilePath) {
	HWFSolverContext context;
	context.dawgArray = createDawgFromFile(StringValuePtr(dawgFilePath));

	context.partialWord.position = 0;
	int i = 0;
	for(i = 0; i < NUM_LETTERS; i++) {
		context.letterCounts[i] = 0;
		context.strikedLetters[i] = FALSE;
	}

	context.currentWord = StringValuePtr(currentWord);
	context.wordLength = strlen(context.currentWord);

	char* strikedLettersArr = StringValuePtr(strikedLetters);
	int len = strlen(strikedLettersArr);

	for(i = 0; i < len; i++) {
		context.strikedLetters[strikedLettersArr[i] - 'a'] = TRUE;
	}

	context.lastVowelPosition = context.wordLength;
	for(i = 0; i < context.wordLength; i++) {
		char c = context.currentWord[i];
		if(c != UNKNOWN_CHAR) {
			context.strikedLetters[context.currentWord[i] - 'a'] = TRUE;
		}

		if(isVowel(c)) {
			context.lastVowelPosition = i;
		}
	}

	get_letter_probs_native_helper(&context, 0, 0);

	free(context.dawgArray);

	VALUE returnArr = rb_ary_new2(NUM_LETTERS);
	for(i = 0; i < NUM_LETTERS; i++) {
		rb_ary_push(returnArr, INT2NUM(context.letterCounts[i]));
	}

	return returnArr;
}

static VALUE hwfsolver_get_words_native(VALUE self, VALUE currentLetters, VALUE minLen, VALUE maxLen, VALUE bonusPosition, VALUE letterMultiplier, VALUE wordMultiplier, VALUE dawgFilePath) {
	HWFFindWordsContext context;
	context.dawgArray = createDawgFromFile(StringValuePtr(dawgFilePath));

	context.currentScore = 0;
	context.currentWordMultiplier = 1;
	context.bonusPosition = NUM2INT(bonusPosition);
	context.letterMultiplier = NUM2INT(letterMultiplier);
	context.wordMultiplier = NUM2INT(wordMultiplier);
	context.partialWord.position = 0;
	int i = 0;
	for(i = 0; i < NUM_LETTERS; i++) {
		context.letterCounts[i] = 0;
	}

	char *currentLettersArr = StringValuePtr(currentLetters);
	int len = strlen(currentLettersArr);

	for(i = 0; i < len; i++) {
		context.letterCounts[currentLettersArr[i] - 'a']++;
	}

	context.minLen = NUM2INT(minLen);
	context.maxLen = NUM2INT(maxLen);

	VALUE arrToReturn = rb_ary_new();
	context.wordsArr = arrToReturn;

	get_words_native_helper(&context, 0);

	free(context.dawgArray);

	return arrToReturn;
}

void Init_hwfsolver(void) {
	VALUE klass = rb_define_class("HWFSolver", rb_cObject);
	rb_define_method(klass, "get_letter_probs_native", hwfsolver_get_letter_probs_native, 3);
	rb_define_method(klass, "get_words_native", hwfsolver_get_words_native, 7);
}
#endif

#ifndef RUBY_ENV
int main(int argc, char** argv) {

}
#endif