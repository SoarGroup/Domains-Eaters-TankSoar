package edu.umich.soar.gridmap2d.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.commsen.stopwatch.Report;
import com.commsen.stopwatch.Stopwatch;

import edu.umich.soar.gridmap2d.Gridmap2D;
import edu.umich.soar.gridmap2d.Names;
import edu.umich.soar.gridmap2d.Simulation;

public class EatersMap extends GridMapBase implements GridMap,
		CellObjectObserver {
	private static final Log logger = LogFactory.getLog(EatersMap.class);

	public static EatersMap generateInstance(String mapPath,
			boolean unopenedBoxesTerminal, double lowProbability,
			double highProbability) {
		return new EatersMap(mapPath, unopenedBoxesTerminal, lowProbability,
				highProbability);
	}

	private int foodCount;
	private int scoreCount;
	private Set<CellObject> unopenedBoxes;
	private boolean unopenedBoxesTerminal;
	private double lowProbability;
	private double highProbability;
	private CellObject rewardInfoObject;
	private int positiveRewardID;
	List<CellObject> rewardObjects = new ArrayList<CellObject>();
	private boolean generatePhase = false;

	private EatersMap(String mapPath, boolean unopenedBoxesTerminal,
			double lowProbability, double highProbability) {
		super(mapPath);
		this.lowProbability = lowProbability;
		this.highProbability = highProbability;

		reset();
	}

	public void reset() {
		foodCount = 0;
		scoreCount = 0;
		unopenedBoxes = new HashSet<CellObject>();
		rewardInfoObject = null;
		positiveRewardID = 0;
		rewardObjects.clear();
		generatePhase = true;
		super.reload();

		// override walls if necessary
		if (getData().randomWalls) {
			generateRandomWalls(lowProbability, highProbability);
		}

		// override food if necessary
		if (getData().randomFood) {
			generateRandomFood();
		}

		// pick positive box
		if (rewardInfoObject != null) {
			assert positiveRewardID == 0;
			positiveRewardID = Simulation.random.nextInt(rewardObjects.size());
			positiveRewardID += 1;
			logger.trace("reward-info.positive-id: " + positiveRewardID);
			rewardInfoObject.setProperty("apply.reward-info.positive-id",
					positiveRewardID);

			// assigning colors like this helps us see the task happen
			// colors[0] = info box (already assigned)
			// colors[1] = positive reward box
			// colors[2..n] = negative reward boxes
			int negativeColor = 2;
			for (CellObject aBox : rewardObjects) {
				if (aBox.getProperty("box-id", 0, Integer.class) == positiveRewardID) {
					aBox.setProperty("apply.reward.correct", "ignored");
					aBox.setProperty(Names.kPropertyColor,
							Gridmap2D.simulation.kColors[1]);
				} else {
					aBox.setProperty(Names.kPropertyColor,
							Gridmap2D.simulation.kColors[negativeColor]);
					negativeColor += 1;
					assert negativeColor < Gridmap2D.simulation.kColors.length;
				}
			}
		}

		generatePhase = false;
		for (Report report : Stopwatch.getAllReports()) {
			System.out.println(report);
		}
	}

	/**
	 * @param lowProbability
	 * @param highProbability
	 * 
	 * @throws IllegalStateException
	 *             If no blocking types are available.
	 */
	private void generateRandomWalls(double lowProbability,
			double highProbability) {
		if (!getData().cellObjectManager
				.hasTemplatesWithProperty(Names.kPropertyBlock)) {
			throw new IllegalStateException(
					"tried to generate random walls with no blocking types");
		}

		assert getData().cells != null;
		int size = getData().cells.size();

		logger.trace("Confirming perimeter wall.");
		int[] xy = new int[2];
		for (xy[0] = 0; xy[0] < size; ++xy[0]) {
			for (xy[1] = 0; xy[1] < size; ++xy[1]) {
				if (xy[0] == 0 || xy[0] == size - 1 || xy[1] == 0
						|| xy[1] == size - 1) {
					if (getData().cells.getCell(xy).hasObjectWithProperty(
							Names.kPropertyBlock)) {
						removeFoodAndAddWall(xy);
					}
					continue;
				}
			}
		}

		logger.trace("Generating random walls.");
		for (xy[0] = 2; xy[0] < size - 3; ++xy[0]) {
			for (xy[1] = 2; xy[1] < size - 3; ++xy[1]) {

				if (noWallsOnCorners(xy)) {
					double probability = lowProbability;
					if (wallOnAnySide(xy)) {
						probability = highProbability;
					}
					if (Simulation.random.nextDouble() < probability) {
						removeFoodAndAddWall(xy);
					}
				}
			}
		}
	}

	private void removeFoodAndAddWall(int[] xy) {
		logger.trace(Arrays.toString(xy) + ": Changing to wall.");
		Cell cell = getData().cells.getCell(xy);
		assert cell != null;
		cell.removeAllObjectsByProperty(Names.kPropertyEdible);
		CellObject wall = getData().cellObjectManager
				.createRandomObjectWithProperty(Names.kPropertyBlock);
		cell.addObject(wall);
	}

	private boolean noWallsOnCorners(int[] xy) {
		Cell cell;

		cell = getData().cells.getCell(new int[] { xy[0] + 1, xy[1] + 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return false;
		}
		cell = getData().cells.getCell(new int[] { xy[0] - 1, xy[1] + 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return false;
		}
		cell = getData().cells.getCell(new int[] { xy[0] - 1, xy[1] - 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return false;
		}
		cell = getData().cells.getCell(new int[] { xy[0] + 1, xy[1] - 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return false;
		}
		return true;
	}

	private boolean wallOnAnySide(int[] xy) {
		Cell cell;

		cell = getData().cells.getCell(new int[] { xy[0] + 1, xy[1] });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return true;
		}
		cell = getData().cells.getCell(new int[] { xy[0], xy[1] + 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return true;
		}
		cell = getData().cells.getCell(new int[] { xy[0] - 1, xy[1] });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return true;
		}
		cell = getData().cells.getCell(new int[] { xy[0], xy[1] - 1 });
		if (cell != null && cell.hasObjectWithProperty(Names.kPropertyBlock)) {
			return true;
		}
		return false;
	}

	/**
	 * @throws IllegalStateException
	 *             If no food types available
	 */
	private void generateRandomFood() {
		if (!getData().cellObjectManager
				.hasTemplatesWithProperty(Names.kPropertyEdible)) {
			throw new IllegalStateException(
					"tried to generate random walls with no food types");
		}

		logger.trace("Generating random food.");

		int[] xy = new int[2];
		for (xy[0] = 1; xy[0] < getData().cells.size() - 1; ++xy[0]) {
			for (xy[1] = 1; xy[1] < getData().cells.size() - 1; ++xy[1]) {
				Cell cell = getData().cells.getCell(xy);
				assert cell != null;
				if (!cell.hasObjectWithProperty(Names.kPropertyBlock)) {
					logger.trace(Arrays.toString(xy) + "Adding random food.");
					cell.removeAllObjectsByProperty(Names.kPropertyEdible);
					CellObject wall = getData().cellObjectManager
							.createRandomObjectWithProperty(Names.kPropertyEdible);
					cell.addObject(wall);
				}
			}
		}
	}

	public boolean isAvailable(int[] location) {
		Cell cell = getData().cells.getCell(location);
		boolean enterable = !cell.hasObjectWithProperty(Names.kPropertyBlock);
		boolean noPlayer = !cell.hasPlayers();
		return enterable && noPlayer;
	}

	public void addStateUpdate(CellObject added) {
		if (generatePhase) {
			if (added.getProperty("apply.reward-info", false, Boolean.class)) {
				rewardInfoObject = added;
			}

			if (added.getProperty("apply.reward", false, Boolean.class)) {
				// assign identification number
				added.setProperty("box-id", Integer.toString(rewardObjects
						.size() + 1));

				// keep track of reward objects
				rewardObjects.add(added);

				logger.trace("Reward box: "
						+ added.getProperty("box-id", -1, Integer.class));

			} else if (added.getProperty("apply.reward-info", false,
					Boolean.class)) {
				// assign identification properties
				added.setProperty(Names.kPropertyColor,
						Gridmap2D.simulation.kColors[0]);
				added.setProperty("box-id", "0");

				logger.trace("Info box: "
						+ added.getProperty("box-id", -1, Integer.class));
			}
		}

		if (added.hasProperty(Names.kPropertyEdible)) {
			foodCount += 1;
		}

		if (added.hasProperty("apply.points")) {
			scoreCount += added.getProperty("apply.points", 0, Integer.class);
		}
	}

	public void removalStateUpdate(CellObject removed) {
		if (unopenedBoxesTerminal) {
			if (isUnopenedBox(removed)) {
				unopenedBoxes.remove(removed);
			}
		}

		if (removed.hasProperty(Names.kPropertyEdible)) {
			foodCount -= 1;
		}
		if (removed.hasProperty(Names.kPropertyPoints)) {
			scoreCount -= removed.getProperty("apply.points", 0, Integer.class);
		}
	}

	public void updateObjects() {
		Set<CellObject> copy = new HashSet<CellObject>(getData().updatables);
		for (CellObject cellObject : copy) {
			Cell cell = cellObject.getCell();

			lingerUpdate(cellObject, cell);

			// decay
			if (cellObject.hasProperty("update.decay")) {
				int points = cellObject.getProperty("apply.points", 0,
						Integer.class);
				int decay = cellObject.getProperty("update.decay", 1,
						Integer.class);
				if (decay >= points) {
					scoreCount -= points;
					cell.removeObject(cellObject);
				} else {
					scoreCount -= decay;
					points -= decay;
					cellObject.setProperty("apply.points", points);
				}
			}
		}

		if (unopenedBoxesTerminal) {
			Iterator<CellObject> iter = unopenedBoxes.iterator();
			while (iter.hasNext()) {
				CellObject box = iter.next();
				if (!isUnopenedBox(box)) {
					iter.remove();
				}
			}
		}
	}

	private boolean isUnopenedBox(CellObject object) {
		if (object.hasProperty(Names.kPropertyBox)) {
			String status = object.getProperty(Names.kPropertyStatus);
			if (status == null || !status.equals(Names.kOpen)) {
				return true;
			}
		}
		return false;
	}

	public int getScoreCount() {
		return scoreCount;
	}

	public int getFoodCount() {
		return foodCount;
	}

	public int getUnopenedBoxCount() {
		return unopenedBoxes.size();
	}
}
