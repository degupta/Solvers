#ifndef DAWG_ARRAY_H
#define DAWG_ARRAY_H

#include "common.h"

typedef unsigned char* DawgArray;

DawgArray createDawgFromFile(const char * fileName);
boolean wordExists(DawgArray dawgArray, MyStringBuffer_t word);
boolean isEndOFWord(DawgArray dawgArray, int currentNode);
boolean isChildEndOFWord(DawgArray dawgArray, int currentNode, char letter);
void getChildren(DawgArray dawgArray, int currentNode, int* children);
int getChildPointer(DawgArray dawgArray, int currentNode);
boolean getNextChild(DawgArray dawgArray, int currentChild, char* nextChildLetter);
int findChildPointer(DawgArray dawgArray, int currentNode, char letter);
#endif
