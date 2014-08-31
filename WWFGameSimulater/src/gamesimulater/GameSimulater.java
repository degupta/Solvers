package gamesimulater;

import java.util.ArrayList;
import java.util.Random;

import wwfsolver.Constants;
import wwfsolver.DawgArray;
import wwfsolver.IMoveSelector;
import wwfsolver.WWFSolver;
import wwfsolver.WWFSolver.Game;

public class GameSimulater {

	// Max number of moves per game is ~310
	public static final int[] NUM_MOVES_PER_TURN = new int[350];

	public static class TileContainer {
		public Random rand;
		public final int[] tiles = new int[Constants.NUM_LETTERS];
		public int numTiles = 0;

		public TileContainer() {
		}

		public TileContainer(int[] _tiles) {
			rand = new Random();
			for (int i = 0; i < tiles.length; i++) {
				tiles[i] = _tiles[i];
				numTiles += tiles[i];
			}
		}

		public void drawRandomlyFrom(TileContainer tileBag) {
			while (tileBag.numTiles > 0 && this.numTiles < 7) {
				int index = findIndexInDistro(tileBag.tiles, tileBag.rand.nextInt(tileBag.numTiles));
				tileBag.tiles[index]--;
				tileBag.numTiles--;
				tiles[index]++;
				numTiles++;
			}
		}

		public int findIndexInDistro(int[] distro, int number) {
			int count = 0;
			int i = 0;
			while (count <= number) {
				count += distro[i];
				i++;
			}
			return Math.min(i - 1, distro.length - 1);
		}
	}

	public static class Word {
		public String word;
		public int row;
		public int col;
		public int score;
		public boolean down;
	}

	public enum MoveSelectionStrategy {
		BestMove(BestMoveSelector.class),
		AverageMove(AverageMoveSelector.class);

		public Class<? extends BaseMoveSelector> clazz;

		private MoveSelectionStrategy(Class<? extends BaseMoveSelector> _clazz) {
			clazz = _clazz;
		}
	}

	public static class BaseMoveSelector implements IMoveSelector {
		public Word selectedWord = null;
		public int numMoves;

		public void move(String word, int row, int col, int score, boolean down) {
			numMoves++;
		}

		public void selectWord() {
			selectedWord = null;
		}

		public void reset() {
			selectedWord = null;
			numMoves = 0;
		}
	}

	public static class BestMoveSelector extends BaseMoveSelector {

		public void move(String word, int row, int col, int score, boolean down) {
			super.move(word, row, col, score, down);
			if (selectedWord == null || score > selectedWord.score || (score == selectedWord.score && word.length() < selectedWord.word.length())) {
				if (selectedWord == null) {
					selectedWord = new Word();
				}
				selectedWord.word = word;
				selectedWord.row = row;
				selectedWord.col = col;
				selectedWord.score = score;
				selectedWord.down = down;
			}
		}
	}

	public static class AverageMoveSelector extends BaseMoveSelector {
		ArrayList<Word> words = new ArrayList<Word>();
		public int maxScore = -1;

		public void move(String word, int row, int col, int score, boolean down) {
			super.move(word, row, col, score, down);
			Word newWord = new Word();
			newWord.word = word;
			newWord.row = row;
			newWord.col = col;
			newWord.score = score;
			newWord.down = down;
			words.add(newWord);
			if (score > maxScore) {
				maxScore = score;
			}
		}

		public void selectWord() {
			int numWords = words.size();
			if (numWords == 0) {
				return;
			}
			float[] randNorm = randNormPolar();
			float mean = maxScore / 2.0f;
			float scale = maxScore * 0.1075f;
			float u1 = randNorm[1];
			int score = (int) (Math.round(((randNorm[0] >= 0 ? u1 : -u1) * scale + mean)));
			score = Math.max(0, Math.min(score, maxScore));

			selectedWord = words.get(0);
			int bestDiff = Math.abs(selectedWord.score - score);
			for (int i = 1; i < numWords; i++) {
				Word word = words.get(i);
				int diffScore = Math.abs(word.score - score);
				if (diffScore < bestDiff || (diffScore == bestDiff && word.word.length() < selectedWord.word.length())) {
					bestDiff = diffScore;
					selectedWord = word;
				}
			}
		}

		public float[] randNormPolar() {
			float[] rands = new float[2];
			float w = 0.0f, x = 0.0f, y = 0.0f;
			do {
				x = 2.0f * (float) Math.random() - 1.0f;
				y = 2.0f * (float) Math.random() - 1.0f;
				w = x * x + y * y;
			} while (w == 0.0f || w >= 1.0f);
			rands[0] = x * w;
			rands[1] = y * w;
			return rands;
		}

		public void reset() {
			super.reset();
			words = new ArrayList<Word>();
			maxScore = -1;
		}
	}

	public final TileContainer tileBag;
	public final TileContainer player1Rack;
	public final TileContainer player2Rack;

	public boolean isFirstMove = true;
	public boolean firstPlayersTurn = true;
	public int player1Score = 0;
	public int player1Moves = 0;
	public int player1Bingos = 0;
	public int player2Score = 0;
	public int player2Moves = 0;
	public int player2Bingos = 0;
	public final char[][] currentBoard = new char[Constants.BOARD_SIZE + 2][Constants.BOARD_SIZE + 2];

	public final Game game;

	public BaseMoveSelector moveSelector;

	public GameSimulater(Game _game, MoveSelectionStrategy strategy) {
		game = _game;
		try {
			moveSelector = strategy.clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tileBag = new TileContainer(game.letterDistro);
		player1Rack = new TileContainer();
		player1Rack.drawRandomlyFrom(tileBag);
		player2Rack = new TileContainer();
		player2Rack.drawRandomlyFrom(tileBag);

		for (int i = 0; i < Constants.BOARD_SIZE + 2; i++) {
			for (int j = 0; j < Constants.BOARD_SIZE + 2; j++) {
				currentBoard[i][j] = Constants.NULL_CHAR;
			}
		}
	}

	public void simulateGame() {
		int passes = 0;
		int move = 0;
		int[] numMovesPerGame = new int[NUM_MOVES_PER_TURN.length];
		while (passes < 3 && player1Rack.numTiles != 0 && player2Rack.numTiles != 0) {
			moveSelector.reset();
			TileContainer rack = firstPlayersTurn ? player1Rack : player2Rack;
			int numTilesInRack = rack.numTiles;
			if (isFirstMove) {
				WWFSolver.getInstance().getFirstMove(DawgArray.getInstance(), currentBoard, rack.tiles, moveSelector, game);
			} else {
				WWFSolver.getInstance().getNextMove(DawgArray.getInstance(), currentBoard, rack.tiles, moveSelector, game);
			}
			moveSelector.selectWord();
			int numTiles = 0;
			if (moveSelector.selectedWord != null) {
				isFirstMove = false;
				passes = 0;
				if (firstPlayersTurn) {
					player1Score += moveSelector.selectedWord.score;
				} else {
					player2Score += moveSelector.selectedWord.score;
				}
				numTiles = playMoveOnBoard(rack);
				rack.drawRandomlyFrom(tileBag);
			} else {
				passes++;
			}

			if (firstPlayersTurn) {
				firstPlayersTurn = false;
				player1Moves++;
				if (numTiles == Constants.LETTERS_FOR_BINGO) {
					player1Bingos++;
				}
			} else {
				firstPlayersTurn = true;
				player2Moves++;
				if (numTiles == Constants.LETTERS_FOR_BINGO) {
					player2Bingos++;
				}
			}

			// 2 ^ numTilesInRack is the number of Swaps and passes possible
			numMovesPerGame[move] = moveSelector.numMoves + (1 << numTilesInRack);
			move++;
		}

		int len = NUM_MOVES_PER_TURN.length;
		synchronized (NUM_MOVES_PER_TURN) {
			for (int i = 0; i < len; i++) {
				NUM_MOVES_PER_TURN[i] += numMovesPerGame[i];
			}
		}
		tallyFinalScores();
	}

	public void printBoard() {
		System.out.println(moveSelector.selectedWord.word);
		for (int i = 1; i <= Constants.BOARD_SIZE; i++) {
			for (int j = 1; j <= Constants.BOARD_SIZE; j++) {
				if (currentBoard[i][j] == Constants.NULL_CHAR) {
					System.out.print(". ");
				} else {
					System.out.print(currentBoard[i][j] + " ");
				}
			}
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	public void tallyFinalScores() {
		for (int i = 0; i < Constants.NUM_LETTERS; i++) {
			if (player1Rack.tiles[i] > 0) {
				int diff = game.letterScores[i] * player1Rack.tiles[i];
				player1Score -= diff;
				player2Score += diff;
			}
			if (player2Rack.tiles[i] > 0) {
				int diff = game.letterScores[i] * player2Rack.tiles[i];
				player1Score += diff;
				player2Score -= diff;
			}
		}
	}

	public int playMoveOnBoard(TileContainer rack) {
		int numTiles = 0;
		int dR = moveSelector.selectedWord.down ? 1 : 0;
		int dC = moveSelector.selectedWord.down ? 0 : 1;
		int len = moveSelector.selectedWord.word.length();
		for (int i = 0; i < len; i++) {
			int curR = moveSelector.selectedWord.row + i * dR;
			int curC = moveSelector.selectedWord.col + i * dC;
			if (currentBoard[curR][curC] == Constants.NULL_CHAR) {
				numTiles++;
				char c = moveSelector.selectedWord.word.charAt(i);
				currentBoard[curR][curC] = c;
				if (c < 'a') {
					rack.tiles[26]--;
					rack.numTiles--;
				} else {
					rack.tiles[c - 'a']--;
					rack.numTiles--;
				}
			}
		}
		return numTiles;
	}
}
