#include "DawgArray.h"
#include <stdio.h>
#include <stdlib.h>

DawgArray createDawgFromFile(const char * fileName)
{
	char byte[4];
	FILE *fr = fopen (fileName, "rb");
	int size = 0;

	// The first four bytes will specify how many bytes are there in the file (minus the first four bytes)
	fread(byte, 1, 4, fr);
	int i = 0;
	for(i = 0; i < 4; i++) {
		int b = (int)((byte[i])) & 0x000000FF;
		size = size | b << (8 * (3 - i));
	}

	// Create an array of that size
	DawgArray dawgArray = (DawgArray)(malloc(size));
	fread(dawgArray, 1, size, fr);
	fclose(fr);
	return dawgArray;
}

boolean wordExists(DawgArray dawgArray, MyStringBuffer_t word)
{
	int length = word->position;
	if(length == 0)
		return FALSE;

	char currentChar = NULL_CHAR;
	int currentNode = 0;
	int currentIndex = 0;
	int childArrPos = 0;
	while(1)
	{
		// Get the index of the list of children
		int childPos = getChildPointer(dawgArray, currentNode);
		// If the index was 0 we have no children and we haven't reached the end of the word => word doesn't exist
		if(childPos == 0)
			return FALSE;
		// Go through the list of children and find the one with the character we are looking for
		while(1)
		{
			childArrPos = childPos * 4;
			currentChar = (char)(dawgArray[childArrPos + 3]);
			if(currentChar == word->stringBuf[currentIndex])
			{
				// We found the character move on
				currentIndex++;
				break;
			}
			// If we have reached end of list but still haven't found the character this word doesn't exist
			if((dawgArray[childArrPos + 2] & 2) > 0)
				return FALSE; // Is End Of List
			childPos++;
		}

		// Have we reached the last letter in the word. If yes check
		// whether the current node has its end of word flag set!
		if(currentIndex == length)
			return (dawgArray[childArrPos + 2] & 1) > 0; // Is Final Node

		// Recurse on child
		currentNode = childPos;
	}

	// Child not found return false
	return FALSE;
}

/**
 * Checks whether the list of children has 'letter' and
 * whether it is at an end-of-word
 */
boolean isChildEndOFWord(DawgArray dawgArray, int currentNode, char letter)
{
	int childPos = getChildPointer(dawgArray, currentNode);
	// This thing has no children
	if(childPos == 0)
		return FALSE;
	int childArrPos = 0;
	while(1)
	{
		childArrPos = childPos * 4;
		// Found the letter, check whether it is end of word
		if((char)(dawgArray[childArrPos + 3]) == letter)
			return (dawgArray[childArrPos + 2] & 1) > 0;
		// We haven't found the letter but are at the end of the list of children, break out.
		if((dawgArray[childArrPos + 2] & 2) > 0)
			break;
		childPos++;
	}

	// Child not found return false
	return FALSE;
}

/**
 * Checks whether the node is end of a word
 */
boolean isEndOFWord(DawgArray dawgArray, int currentNode)
{
	return (dawgArray[currentNode * 4 + 2] & 1) > 0;
}

/**
 * Returns a hashmap with the child's letter as the key
 * and the position its index as the value
 */
void getChildren(DawgArray dawgArray, int currentNode, int* children)
{
	int i = 0;
	for(i = 0; i < NUM_LETTERS; i++)
		children[i] = 0;
	int childPos = getChildPointer(dawgArray, currentNode);
	// It has no children, return empty hashmap
	if(childPos == 0)
		return;
	int childArrPos = 0;
	// Go through list of children
	while(1)
	{
		childArrPos = childPos * 4;
		// Put the child and its index in the hashmap
		children[dawgArray[childArrPos + 3] - 'a'] = childPos;
		// End of list of children
		if((dawgArray[childArrPos + 2] & 2) > 0)
			break;
		childPos++;
	}
}

/**
 * Gets the start position of where the list of children of
 * currentNode is
 */
int getChildPointer(DawgArray dawgArray, int currentNode)
{
	int pos = currentNode * 4;
	int childPos = ((int)(dawgArray[pos]) << 16) | ((int)(dawgArray[pos + 1]) << 8) | ((int)(dawgArray[pos + 2]));
	childPos = childPos >> 2;
	return childPos;
}

/**
 * Gets the next child in the list. Stores the letter of the next child letter in
 * nextChildLetter. Returns whether the next child was the last child in the list of
 * children
 */
boolean getNextChild(DawgArray dawgArray, int currentChild, char* nextChildLetter)
{
	int childArrPos = currentChild * 4;
	*nextChildLetter = dawgArray[childArrPos + 3];
	return (dawgArray[childArrPos + 2] & 2) > 0;
}

/**
 * Finds the child pointer of currentNode with a particular letter.
 * Returns 0 if no child has that letter
 */
int findChildPointer(DawgArray dawgArray, int currentNode, char letter)
{
	int childPos = getChildPointer(dawgArray, currentNode);
	// It has no children, return empty hashmap
	if(childPos == 0)
		return 0;
	int childArrPos = 0;
	// Go through list of children
	while(1)
	{
		childArrPos = childPos * 4;
		// Put the child and its index in the hashmap
		if(dawgArray[childArrPos + 3] == letter)
			return childPos;
		// End of list of children
		if((dawgArray[childArrPos + 2] & 2) > 0)
			break;
		childPos++;
	}

	return 0;
}
