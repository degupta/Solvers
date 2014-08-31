package gamesimulater;

import java.util.ArrayList;

import gamesimulater.GameSimulater.MoveSelectionStrategy;
import wwfsolver.WWFSolver.Game;

public class Simulation {

	Game game;
	int numLoops;
	int numGamesPerLoop;
	String filePrefix;
	String aggregatePrefix;
	MoveSelectionStrategy moveSelectionStrategy;

	public Simulation(Game _game, int _numLoops, int _numGamesPerLoop, String _filePrefix, String _aggregatePrefix, MoveSelectionStrategy _strategy) {
		game = _game;
		numLoops = _numLoops;
		numGamesPerLoop = _numGamesPerLoop;
		filePrefix = _filePrefix;
		moveSelectionStrategy = _strategy;
		aggregatePrefix = _aggregatePrefix;
	}

	public static ArrayList<Simulation> getSimulations() {
		ArrayList<Simulation> simulations = new ArrayList<Simulation>();
		simulations.add(new Simulation(Game.WordsWithFriends, 1, 100000, "wwf_avg_", "wwf_agg_", MoveSelectionStrategy.AverageMove));
		return simulations;
	}
}
