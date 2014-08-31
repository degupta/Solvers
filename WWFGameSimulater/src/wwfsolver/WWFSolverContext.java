package wwfsolver;

import wwfsolver.WWFSolver.Game;

public class WWFSolverContext {
	char[][] currentBoard;
	int[] currentRack;
	IMoveSelector moveSelector;
	MyStringBuilder partialWord = new MyStringBuilder();
	DawgArray dawgArray;
	CrossCheck[][] crossChecks;
	int anchorRow = 0;
	int anchorCol = 0;
	boolean down = false;
	MyStringBuilder tempString = new MyStringBuilder();
	int lettersPlaced = 0;
	int currentScore = 0;
	int currentCrossScore = 0;
	int currentWordMultiplier = 1;
	boolean hasBlanks = false;

	Game game;
}
