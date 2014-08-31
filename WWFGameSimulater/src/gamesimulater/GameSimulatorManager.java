package gamesimulater;

import gamesimulater.GameSimulater.MoveSelectionStrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wwfsolver.WWFSolver.Game;

public class GameSimulatorManager {
	public static final int NUM_THREADS = 8;
	public static BufferedWriter resultWriter;
	public static int gamesSimulated = 0;
	public static int totalGamesSimulated = 0;
	public static int gamesToSimulate = 0;

	public static void simulateGames(Game game, int numGames, String fileName, MoveSelectionStrategy strategy) {
		File file = new File("bin/" + fileName);
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		resultWriter = new BufferedWriter(fw);
		ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
		gamesSimulated = 0;
		gamesToSimulate = numGames;
		for (int i = 0; i < numGames; i++) {
			GameRunner gr = new GameRunner(game, strategy);
			executor.execute(gr);
		}
		executor.shutdown();
		while (!executor.isTerminated())
			;
		try {
			resultWriter.close();
			fw.close();
			System.out.println(numGames + " " + game.toString() + " games simulated!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized void finishGameSimulation() {
		gamesSimulated++;
		totalGamesSimulated++;
		if (gamesSimulated % 10 == 0) {
			System.out.println(gamesSimulated + "/" + gamesToSimulate);
		}
	}

	public synchronized static void writeResult(int p1Score, int p1Moves, int p1Bingos, int p2Score, int p2Moves, int p2Bingos) {
		String comma = ",";
		String s = new StringBuilder().append(p1Score).append(comma).append(p1Moves).append(comma).append(p1Bingos).append(comma).append(p2Score).append(comma).append(p2Moves).append(comma).append(p2Bingos).toString();
		try {
			resultWriter.write(s + "\n");
			finishGameSimulation();
		} catch (IOException e) {
			System.out.println(s);
		}
	}

	public synchronized static void writeAggregateResults(String fileName) {
		File file = new File("bin/" + fileName);
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter aggResultWriter = new BufferedWriter(fw);
			aggResultWriter.write("NUM_GAMES:" + totalGamesSimulated + "\n");
			aggResultWriter.write("NUM_POSSIBLE_MOVES_PER_TURN" + "\n");
			for (int i = 0; i < GameSimulater.NUM_MOVES_PER_TURN.length; i++) {
				aggResultWriter.write((i + 1) + ":" + ((float) GameSimulater.NUM_MOVES_PER_TURN[i] / totalGamesSimulated) + "\n");
			}
			aggResultWriter.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		totalGamesSimulated = 0;
		for (int i = 0; i < GameSimulater.NUM_MOVES_PER_TURN.length; i++) {
			GameSimulater.NUM_MOVES_PER_TURN[i] = 0;
		}
	}

	public static class GameRunner implements Runnable {
		Game game;
		MoveSelectionStrategy strategy;

		public GameRunner(Game game, MoveSelectionStrategy strategy) {
			this.game = game;
			this.strategy = strategy;
		}

		@Override
		public void run() {
			GameSimulater gs = new GameSimulater(game, strategy);
			gs.simulateGame();
			writeResult(gs.player1Score, gs.player1Moves, gs.player1Bingos, gs.player2Score, gs.player2Moves, gs.player2Bingos);
		}
	}
}
