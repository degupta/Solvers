package wwfsolver;

import java.util.HashMap;

public class WWFSolver {
	public enum Game {
		WordsWithFriends(Constants.WWF_WORD_MULTIPLIERS, Constants.WWF_LETTER_MULTIPLIERS, Constants.WWF_LETTER_SCORES, Constants.WWF_BINGO_SCORE, Constants.WWF_LETTER_DISTRIBUTION, Constants.BOARD_SIZE),
		WordsWithFriends11x11(Constants.WWF_WORD_MULTIPLIERS_11_11, Constants.WWF_WORD_MULTIPLIERS_11_11, Constants.WWF_LETTER_SCORES, Constants.WWF_BINGO_SCORE, Constants.WWF_LETTER_DISTRIBUTION_11_11, Constants.BOARD_SIZE_11_11),
		Scrabble(Constants.SCRABBLE_WORD_MULTIPLIERS, Constants.SCRABBLE_LETTER_MULTIPLIERS, Constants.SCRABBLE_LETTER_SCORES, Constants.SCRABBLE_BINGO_SCORE, Constants.SCRABBLE_LETTER_DISTRIBUTION, Constants.BOARD_SIZE);

		public final int[][] wordMultipliers;
		public final int[][] letterMultipliers;
		public final int[] letterScores;
		public final int bingoScore;
		public final int[] letterDistro;
		public final int boardSize;

		private Game(int[][] _wordMultipliers, int[][] _letterMultipliers, int[] _letterScores, int _bingoScore, int[] _letterDistro, int _boardSize) {
			wordMultipliers = _wordMultipliers;
			letterMultipliers = _letterMultipliers;
			letterScores = _letterScores;
			bingoScore = _bingoScore;
			letterDistro = _letterDistro;
			boardSize = _boardSize;
		}
	}

	/** Singleton instance */
	private static final WWFSolver sSingleton = new WWFSolver();

	/** Singleton accessor
	 * 
	 * @return singleton instance */
	public static WWFSolver getInstance() {
		return WWFSolver.sSingleton;
	}

	public WWFSolver() {

	}

	public void resetContext(final WWFSolverContext context, final int row, final int col, final boolean down) {
		context.anchorRow = row;
		context.anchorCol = col;
		context.down = down;
		context.partialWord.position = 0;
		context.partialWord.stringBuf[0] = Constants.NULL_CHAR;
		context.currentScore = 0;
		context.currentWordMultiplier = 1;
		context.currentCrossScore = 0;
		context.lettersPlaced = 0;
	}

	public WWFSolverContext getContext(final DawgArray dawgArray, final char[][] currentBoard, final int[] currentRack, final IMoveSelector moveSelector, final Game game) {
		final WWFSolverContext context = new WWFSolverContext();
		context.dawgArray = dawgArray;
		context.currentBoard = currentBoard;
		context.currentRack = currentRack;
		context.moveSelector = moveSelector;
		context.hasBlanks = context.currentRack[Constants.BLANK_TILE_RACK - 'a'] > 0;
		context.game = game;
		context.crossChecks = new CrossCheck[context.game.boardSize][context.game.boardSize];
		return context;
	}

	public void getNextMove(final DawgArray dawgArray, final char[][] currentBoard, final int[] currentRack, final IMoveSelector moveSelector, final Game game) {
		this.getAllNextMoves(this.getContext(dawgArray, currentBoard, currentRack, moveSelector, game));
	}

	public void getFirstMove(final DawgArray dawgArray, final char[][] currentBoard, final int[] currentRack, final IMoveSelector moveSelector, final Game game) {
		final WWFSolverContext context = this.getContext(dawgArray, currentBoard, currentRack, moveSelector, game);
		context.anchorRow = (context.game.boardSize / 2) + 1;
		context.anchorCol = (context.game.boardSize / 2) + 1;
		this.LeftPart(context, 0, context.game.boardSize / 2);
	}

	public void getAllNextMoves(final WWFSolverContext context) {
		int lastAnchor = 0;
		for (int row = 1; row <= context.game.boardSize; row++) {
			lastAnchor = 0;
			for (int col = 1; col <= context.game.boardSize; col++) {
				if ((context.currentBoard[row][col] != Constants.NULL_CHAR) || !this.anchorSquare(context, row, col)) {
					continue;
				}
				this.resetContext(context, row, col, false);
				if (context.currentBoard[row][col - 1] == Constants.NULL_CHAR) {
					this.LeftPart(context, 0, col - lastAnchor - 1);
				} else {
					context.anchorCol = lastAnchor + 1;
					this.ExtendRight(context, 0, row, lastAnchor + 1, false);
				}
				lastAnchor = col;
			}
		}

		lastAnchor = 0;
		for (int col = 1; col <= context.game.boardSize; col++) {
			lastAnchor = 0;
			for (int row = 1; row <= context.game.boardSize; row++) {
				if ((context.currentBoard[row][col] != Constants.NULL_CHAR) || !this.anchorSquare(context, row, col)) {
					continue;
				}
				this.resetContext(context, row, col, true);
				if (context.currentBoard[row - 1][col] == Constants.NULL_CHAR) {
					this.LeftPart(context, 0, row - lastAnchor - 1);
				} else {
					context.anchorRow = lastAnchor + 1;
					this.ExtendRight(context, 0, lastAnchor + 1, col, false);
				}
				lastAnchor = row;
			}
		}
	}

	private boolean anchorSquare(final WWFSolverContext context, final int row, final int col) {
		if ((col < 1) || (col > context.game.boardSize) || (row < 1) || (row > context.game.boardSize)) {
			return false;
		}
		if (context.currentBoard[row][col - 1] != Constants.NULL_CHAR) {
			return true;
		}
		if (context.currentBoard[row][col + 1] != Constants.NULL_CHAR) {
			return true;
		}
		if (context.currentBoard[row - 1][col] != Constants.NULL_CHAR) {
			return true;
		}
		if (context.currentBoard[row + 1][col] != Constants.NULL_CHAR) {
			return true;
		}
		return false;
	}

	boolean inRack(final WWFSolverContext context, final char c) {
		return (context.currentRack[c - 'a'] > 0) || (context.currentRack[Constants.BLANK_TILE_RACK - 'a'] > 0);
	}

	void addToRack(final WWFSolverContext context, char letter, final boolean removedBlank) {
		if (removedBlank == true) {
			letter = Constants.BLANK_TILE_RACK;
		}
		context.currentRack[letter - 'a'] += 1;
	}

	boolean removeFromRack(final WWFSolverContext context, char letter) {
		boolean removedBlank = false;
		if (context.currentRack[letter - 'a'] < 1) {
			letter = Constants.BLANK_TILE_RACK;
			removedBlank = true;
		}
		context.currentRack[letter - 'a'] -= 1;
		return removedBlank;
	}

	void appendToMyStringBuffer(final MyStringBuilder strBuf, final char c) {
		strBuf.stringBuf[strBuf.position] = c;
		strBuf.position++;
	}

	void removeFromMyStringBuffer(final MyStringBuilder strBuf) {
		strBuf.position--;
		strBuf.stringBuf[strBuf.position] = Constants.NULL_CHAR;
	}

	private void LeftPart(final WWFSolverContext context, final int node, final int limit) {
		this.ExtendRight(context, node, context.anchorRow, context.anchorCol, true);
		if (limit <= 0) {
			return;
		}

		final HashMap<Character, Integer> edges = context.dawgArray.getChildren(node);
		for (final Character edgeC : edges.keySet()) {
			char edge = edgeC.charValue();
			if (this.inRack(context, edge)) {
				final boolean blank = this.removeFromRack(context, edge);
				if (blank) {
					edge -= 32;
				}
				this.appendToMyStringBuffer(context.partialWord, edge);
				context.lettersPlaced++;

				this.LeftPart(context, edges.get(edgeC), limit - 1);

				context.lettersPlaced--;
				this.addToRack(context, edgeC, blank);
				this.removeFromMyStringBuffer(context.partialWord);
			}
		}
	}

	private void ExtendRight(final WWFSolverContext context, final int node, final int row, final int col, final boolean calculateLeftScore) {
		if ((row > (context.game.boardSize + 1)) || (col > (context.game.boardSize + 1))) {
			return;
		}

		int leftScoreMultiplier = 1;
		int leftScoreTotal = 0;
		if (calculateLeftScore) {

			final int dR = context.down ? 1 : 0;
			final int dC = context.down ? 0 : 1;
			int curR = row - (context.partialWord.position * dR);
			int curC = col - (context.partialWord.position * dC);
			for (int i = 0; i < context.partialWord.position; i++, curR += dR, curC += dC) {
				if (context.partialWord.stringBuf[i] >= 'a') {
					leftScoreTotal += context.game.letterMultipliers[curR][curC] * context.game.letterScores[context.partialWord.stringBuf[i] - 'a'];
				}
				leftScoreMultiplier *= context.game.wordMultipliers[curR][curC];
			}

			context.currentScore += leftScoreTotal;
			context.currentWordMultiplier *= leftScoreMultiplier;
		}

		final HashMap<Character, Integer> edges = context.dawgArray.getChildren(node);
		if (context.currentBoard[row][col] == Constants.NULL_CHAR) {
			if ((context.lettersPlaced > 0) && ((row > context.anchorRow) || (col > context.anchorCol)) && context.dawgArray.isEndOFWord(node)) {
				context.partialWord.stringBuf[context.partialWord.position] = Constants.NULL_CHAR;
				this.sendMoves(context, row - (context.down ? context.partialWord.position : 0), col - (context.down ? 0 : context.partialWord.position), (context.currentScore * context.currentWordMultiplier) + context.currentCrossScore + (context.lettersPlaced == Constants.LETTERS_FOR_BINGO ? context.game.bingoScore : 0), context.down);
			}

			if ((row <= context.game.boardSize) && (col <= context.game.boardSize)) {
				final int currentWordMultiplier = context.game.wordMultipliers[row][col];
				final int currentLetterMultiplier = context.game.letterMultipliers[row][col];

				final int[] crossCheckArr = this.crossCheck(context, row, col, !context.down);
				final int crossScore = crossCheckArr[0] * currentWordMultiplier;
				final int crossCheckLetters = crossCheckArr[1];
				final boolean doubleUp = (crossCheckLetters & 0x80000000) == 0;

				for (final Character edgeC : edges.keySet()) {
					char edge = edgeC.charValue();
					if (this.inRack(context, edge) && ((crossCheckLetters & (1 << (edge - 'a'))) > 0)) {
						final boolean blank = this.removeFromRack(context, edge);
						int currentLetterScore = context.game.letterScores[edge - 'a'] * currentLetterMultiplier;
						if (blank) {
							edge -= 32;
							currentLetterScore = 0;
						}
						this.appendToMyStringBuffer(context.partialWord, edge);
						context.currentScore += currentLetterScore;
						context.currentWordMultiplier *= currentWordMultiplier;
						final int letterCrossScore = crossScore + (doubleUp ? currentLetterScore * currentWordMultiplier : 0);
						context.currentCrossScore += letterCrossScore;
						context.lettersPlaced++;

						this.ExtendRight(context, edges.get(edgeC), row + (context.down ? 1 : 0), col + (context.down ? 0 : 1), false);

						this.addToRack(context, edgeC, blank);
						this.removeFromMyStringBuffer(context.partialWord);
						context.currentScore -= currentLetterScore;
						context.currentWordMultiplier /= currentWordMultiplier;
						context.currentCrossScore -= letterCrossScore;
						context.lettersPlaced--;
					}
				}
			}
		} else {
			char edge = context.currentBoard[row][col];
			final boolean isBlank = edge < 'a';
			if (isBlank) {
				edge += 32;
			}
			if (edges.containsKey(edge)) {
				final int nextNode = edges.get(edge);
				final int currentLetterScore = isBlank ? 0 : context.game.letterScores[edge - 'a'];
				if (nextNode != 0) {
					context.currentScore += currentLetterScore;
					this.appendToMyStringBuffer(context.partialWord, edge);

					this.ExtendRight(context, nextNode, row + (context.down ? 1 : 0), col + (context.down ? 0 : 1), false);

					this.removeFromMyStringBuffer(context.partialWord);
					context.currentScore -= currentLetterScore;
				}
			}
		}

		if (calculateLeftScore) {
			context.currentScore -= leftScoreTotal;
			context.currentWordMultiplier /= leftScoreMultiplier;
		}
	}

	void sendMoves(final WWFSolverContext context, final int row, final int col, final int score, final boolean down) {
		context.moveSelector.move(new String(context.partialWord.stringBuf, 0, context.partialWord.position), row, col, score, down);

		if (!Constants.BLANK_FUDGER_ENABLED || !context.hasBlanks) {
			return;
		}

		for (int i = 0; i < context.partialWord.position; i++) {
			context.tempString.stringBuf[i] = context.partialWord.stringBuf[i];
		}
		context.tempString.position = context.partialWord.position;

		final int dR = down ? 1 : 0;
		final int dC = down ? 0 : 1;
		final int crossDR = down ? 0 : 1;
		final int crossDC = down ? 1 : 0;
		int curR = row + ((context.tempString.position - 1) * dR);
		int curC = col + ((context.tempString.position - 1) * dC);

		for (int i = context.tempString.position - 1; i >= 0; i--, curR -= dR, curC -= dC) {
			if (context.currentBoard[curR][curC] != Constants.NULL_CHAR) {
				continue;
			}
			final char c = context.tempString.stringBuf[i];
			if (c < 'a') {
				for (int j = i - 1; j >= 0; j--) {
					if (context.tempString.stringBuf[j] == (c + 32)) {
						final int jRow = curR - ((i - j) * dR);
						final int jCol = curC - ((i - j) * dC);
						if (context.currentBoard[jRow][jCol] != Constants.NULL_CHAR) {
							continue;
						}
						final int letterScore = context.game.letterScores[(c + 32) - 'a'];
						int diffScore = letterScore * (context.game.letterMultipliers[curR][curC] - context.game.letterMultipliers[jRow][jCol]) * context.currentWordMultiplier;
						if ((context.currentBoard[curR + crossDR][curC + crossDC] != Constants.NULL_CHAR) || (context.currentBoard[curR - crossDR][curC - crossDC] != Constants.NULL_CHAR)) {
							diffScore += letterScore * context.game.letterMultipliers[curR][curC] * context.game.wordMultipliers[curR][curC];
						}
						if ((context.currentBoard[jRow + crossDR][jCol + crossDC] != Constants.NULL_CHAR) || (context.currentBoard[jRow - crossDR][jCol - crossDC] != Constants.NULL_CHAR)) {
							diffScore -= letterScore * context.game.letterMultipliers[jRow][jCol] * context.game.wordMultipliers[jRow][jCol];
						}

						context.tempString.stringBuf[i] = context.partialWord.stringBuf[j];
						context.tempString.stringBuf[j] = context.partialWord.stringBuf[i];
						context.moveSelector.move(new String(context.tempString.stringBuf, 0, context.tempString.position), row, col, score + diffScore, down);
						context.tempString.stringBuf[i] = context.partialWord.stringBuf[j];
						context.tempString.stringBuf[j] = context.partialWord.stringBuf[i];
					}
				}
			}
		}
	}

	int[] crossCheck(final WWFSolverContext context, final int row, final int col, final boolean checkDown) {
		final int otherR = row - 1;
		final int otherC = col - 1;
		if (context.crossChecks[otherR][otherC] == null) {
			context.crossChecks[otherR][otherC] = new CrossCheck();
		}
		if (context.crossChecks[otherR][otherC].isDirty) {
			this.computeCrossCheck(context, row, col);
		}
		return checkDown ? context.crossChecks[otherR][otherC].down : context.crossChecks[otherR][otherC].across;
	}

	void computeCrossCheck(final WWFSolverContext context, final int row, final int col) {
		if (context.currentBoard[row][col] != Constants.NULL_CHAR) {
			context.crossChecks[row - 1][col - 1].resetClean();
			return;
		}

		this.getCrossCheck(context, row, col, true);
		this.getCrossCheck(context, row, col, false);
		context.crossChecks[row - 1][col - 1].isDirty = false;
	}

	void getCrossCheck(final WWFSolverContext context, final int row, final int col, final boolean down) {
		final MyStringBuilder stringBuf = context.tempString;

		final int dR = down ? 1 : 0;
		final int dC = down ? 0 : 1;

		stringBuf.position = 0;
		int thisPosition = 0;

		int currentPosR = row - dR;
		int currentPosC = col - dC;
		char current;
		int crossCheckScore = 0;

		while ((currentPosR > 0) && (currentPosC > 0)) {
			current = context.currentBoard[currentPosR][currentPosC];
			if (current == Constants.NULL_CHAR) {
				break;
			}
			final boolean isBlank = current < 'a';
			if (isBlank) {
				current += 32;
			}
			crossCheckScore += isBlank ? 0 : context.game.letterScores[current - 'a'];
			currentPosR -= dR;
			currentPosC -= dC;
		}

		for (int i = currentPosR + dR, j = currentPosC + dC; ((i + dR) <= row) && ((j + dC) <= col); i += dR, j += dC) {
			this.appendToMyStringBuffer(stringBuf, context.currentBoard[i][j] < 'a' ? (char) (context.currentBoard[i][j] + 32) : context.currentBoard[i][j]);
		}

		thisPosition = stringBuf.position;
		// Space for the current letter
		stringBuf.position++;

		currentPosR = row + dR;
		currentPosC = col + dC;
		while ((currentPosR <= context.game.boardSize) && (currentPosC <= context.game.boardSize)) {
			current = context.currentBoard[currentPosR][currentPosC];
			if (current == Constants.NULL_CHAR) {
				break;
			}
			final boolean isBlank = current < 'a';
			if (isBlank) {
				current += 32;
			}
			this.appendToMyStringBuffer(stringBuf, current);
			crossCheckScore += isBlank ? 0 : context.game.letterScores[current - 'a'];
			currentPosR += dR;
			currentPosC += dC;
		}

		final int[] crossCheckArr = (down ? context.crossChecks[row - 1][col - 1].down : context.crossChecks[row - 1][col - 1].across);

		if ((thisPosition == 0) && (stringBuf.position == 1)) {
			crossCheckArr[0] = 0;
			crossCheckArr[1] = 0xFFFFFFFF;
			return;
		}

		int crossCheck = 0;

		for (int i = 0; i < 26; i++) {
			context.tempString.stringBuf[thisPosition] = (char) (i + 'a');
			if (context.dawgArray.wordExists(context.tempString)) {
				crossCheck |= (1 << i);
			}
		}

		if (crossCheck == 0) {
			crossCheckArr[0] = 0;
			crossCheckArr[1] = 0;
			return;
		}

		crossCheckArr[0] = crossCheckScore;
		crossCheckArr[1] = crossCheck;
	}
}
