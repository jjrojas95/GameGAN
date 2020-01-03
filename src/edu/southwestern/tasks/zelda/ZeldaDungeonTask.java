package edu.southwestern.tasks.zelda;

import java.util.ArrayList;
import java.util.HashSet;

import edu.southwestern.MMNEAT.MMNEAT;
import edu.southwestern.evolution.genotypes.Genotype;
import edu.southwestern.parameters.CommonConstants;
import edu.southwestern.tasks.NoisyLonerTask;
import edu.southwestern.tasks.gvgai.zelda.dungeon.Dungeon;
import edu.southwestern.tasks.gvgai.zelda.dungeon.DungeonUtil;
import edu.southwestern.tasks.gvgai.zelda.level.ZeldaState;
import edu.southwestern.tasks.gvgai.zelda.level.ZeldaState.GridAction;
import edu.southwestern.util.MiscUtil;
import edu.southwestern.util.datastructures.Pair;
import me.jakerg.rougelike.RougelikeApp;

public abstract class ZeldaDungeonTask<T> extends NoisyLonerTask<T> {

	public ZeldaDungeonTask() {
		// Objective functions
		MMNEAT.registerFitnessFunction("DistanceToTriforce");
		// Additional information tracked about each dungeon
		MMNEAT.registerFitnessFunction("NumRooms",false);
		MMNEAT.registerFitnessFunction("NumRoomsTraversed",false);
		// More?
	}
	
	@Override
	public int numObjectives() {
		return 1;  
	}
	
	public int numOtherScores() {
		return 2;
	}

	@Override
	public double getTimeStamp() {
		return 0; // Not used
	}
	
	public abstract Dungeon getZeldaDungeonFromGenotype(Genotype<T> individual);
		
	@Override
	public Pair<double[], double[]> oneEval(Genotype<T> individual, int num) {
		Dungeon dungeon = getZeldaDungeonFromGenotype(individual);
		if(dungeon == null) {
			// The A* fix could not make the level beat-able. This probably means that the state space was too big to search,
			// so this level should receive minimal fitness.
			return new Pair<double[], double[]>(new double[]{-100}, new double[] {0, 0});
		}
		// A* should already have been run during creation to assure beat-ability, but it is run again here to get the action sequence.
		ArrayList<GridAction> actionSequence;
		try {
			actionSequence = DungeonUtil.makeDungeonPlayable(dungeon);
		}catch(IllegalStateException e) {
			// But sometimes this exception occurs anyway. Not sure why, but we can take this to mean the level has a problem and deserves bad fitness
			return new Pair<double[], double[]>(new double[]{-100}, new double[] {0, 0});
		}
		
		int distanceToTriforce = actionSequence.size();
		int numRooms = dungeon.getLevels().size();
		
		HashSet<Pair<Integer,Integer>> visitedRoomCoordinates = new HashSet<>();
		for(ZeldaState zs: DungeonUtil.mostRecentVisited) {
			// Set does not allow duplicates: one Pair per room
			visitedRoomCoordinates.add(new Pair<>(zs.dX,zs.dY));
		}
		
		int numRoomsTraversed = visitedRoomCoordinates.size();
				
		if(CommonConstants.watch) {
			System.out.println("Distance to Triforce: "+distanceToTriforce);
			System.out.println("Number of rooms: "+numRooms);
			System.out.println("Number of rooms traversed: "+numRoomsTraversed);
			// View whole dungeon layout
			DungeonUtil.viewDungeon(dungeon, DungeonUtil.mostRecentVisited);			
			System.out.println("Enter 'P' to play, or just press Enter to continue");
			String input = MiscUtil.waitForReadStringAndEnterKeyPress();
			System.out.println("Entered \""+input+"\"");
			if(input.toLowerCase().equals("p")) {
				new Thread() {
					@Override
					public void run() {
						// Repeat dungeon generation to remove visited marks
						Dungeon dungeon = getZeldaDungeonFromGenotype(individual);
						RougelikeApp.startDungeon(dungeon);
					}
				}.start();
				System.out.println("Press enter");
				MiscUtil.waitForReadStringAndEnterKeyPress();
			}
		}
		
		return new Pair<double[], double[]>(new double[]{distanceToTriforce}, new double[] {numRooms, numRoomsTraversed});
	}
}