package gamesimulater;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import wwfsolver.DawgArray;

public class MainClass {

	public static void main(String args[]) {
		try {
			DawgArray.getInstance().init(new BufferedInputStream(new FileInputStream(args.length > 0 && args[0] != null ? args[0] : "dawg_dict")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int index = 1;
		for (Simulation simulation : Simulation.getSimulations()) {
			for (int i = 1; i <= simulation.numLoops; i++) {
				GameSimulatorManager.simulateGames(simulation.game, simulation.numGamesPerLoop, simulation.filePrefix + i, simulation.moveSelectionStrategy);
			}

			GameSimulatorManager.writeAggregateResults(simulation.aggregatePrefix + index);
			index++;
		}
	}
}
