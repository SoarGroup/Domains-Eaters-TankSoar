package edu.umich.soar.gridmap2d.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.umich.soar.gridmap2d.Direction;
import edu.umich.soar.gridmap2d.Simulation;

public class TaxiMap extends GridMapBase implements GridMap, CellObjectObserver {
	private static final Log logger = LogFactory.getLog(TaxiMap.class);

	public static TaxiMap generateInstance(String mapPath) {
		return new TaxiMap(mapPath);
	}

	private CellObject passenger;
	private int[] passengerLocation;
	private String passengerSourceColor;
	private String passengerDestination;
	private String passengerDefaultDestination;
	private boolean passengerDelivered;

	private List<int[]> destinations = new ArrayList<int[]>();

	private TaxiMap(String mapPath) {
		super(mapPath);

		reset();
	}

	public void reset() {
		destinations.clear();
		passengerDestination = null;
		passengerSourceColor = null;
		super.reload();

		passengerDelivered = false;
		passengerLocation = null;
		passenger = getData().cellObjectManager.createObject("passenger");

		// this can be null
		passengerDefaultDestination = passenger
				.getProperty("passenger-destination");

		int[] dest = destinations.get(Simulation.random.nextInt(destinations
				.size()));
		getCell(dest).addObject(passenger);

		setPassengerDestination();
	}

	private void setPassengerDestination() {
		if (passengerDefaultDestination != null) {
			passengerDestination = passengerDefaultDestination;
		} else {
			int[] dest = destinations.get(Simulation.random
					.nextInt(destinations.size()));
			passengerDestination = getDestinationName(dest);
		}

		logger.info("passenger destination: " + passengerDestination);
	}

	private String getDestinationName(int[] location) {
		CellObject object = getData().cells.getCell(location)
				.getFirstObjectWithProperty("destination");
		if (object == null) {
			return null;
		}
		return object.getProperty("name");
	}

	public boolean isAvailable(int[] location) {
		Cell cell = getData().cells.getCell(location);
		boolean destination = cell.hasObjectWithProperty("destination");
		boolean fuel = cell.hasObjectWithProperty("fuel");
		boolean noPlayer = !cell.hasPlayers();
		return !destination && !fuel && noPlayer;
	}

	public void addStateUpdate(CellObject added) {
		// Update state we keep track of specific to game type
		if (added.hasProperty("destination")) {
			destinations.add(added.getCell().getLocation());
		}

		if (added.hasProperty("passenger")) {
			passengerLocation = added.getCell().getLocation();
			if (passengerSourceColor == null) {
				CellObject dest = getData().cells.getCell(passengerLocation)
						.getFirstObjectWithProperty("destination");
				if (dest != null) {
					passengerSourceColor = dest.getProperty("color");
				}
			}
		}
	}

	public String getPassengerSourceColor() {
		if (passengerSourceColor == null) {
			return "none";
		}
		return passengerSourceColor;
	}

	public void removalStateUpdate(CellObject removed) {
		if (removed.hasProperty("destination")) {
			Iterator<int[]> iter = destinations.iterator();
			while (iter.hasNext()) {
				int[] dest = iter.next();
				if (Arrays.equals(dest, removed.getCell().getLocation())) {
					iter.remove();
					break;
				}
			}
		}

		if (removed.hasProperty("passenger")) {
			this.passengerLocation = null;
		}
	}

	public boolean pickUp(int[] location) {
		if (passengerLocation == null) {
			return false;
		}
		if (Arrays.equals(passengerLocation, location)) {
			return getData().cells.getCell(location).removeObject(passenger);
		}
		return false;
	}

	public boolean putDown(int[] location) {
		if (passengerLocation != null) {
			return false;
		}

		getCell(location).addObject(passenger);
		return true;
	}

	public boolean isFuel(int[] location) {
		return getData().cells.getCell(location).hasObjectWithProperty(
				"fuel");
	}

	public boolean exitable(int[] location, Direction direction) {
		Set<CellObject> walls = getData().cells.getCell(location)
				.getAllObjectsWithProperty("block");
		if (walls.isEmpty()) {
			return true;
		}
		for (CellObject wall : walls) {
			if (direction
					.equals(Direction.valueOf(wall.getProperty("direction")))) {
				return false;
			}
		}
		return true;
	}

	public boolean isPassengerCarried() {
		return passengerLocation == null;
	}

	public boolean isCorrectPassengerDestination(int[] location) {
		if (passengerDestination.equals(getDestinationName(location))) {
			return true;
		}
		return false;
	}

	public boolean isPassengerDelivered() {
		return passengerDelivered;
	}

	public void deliverPassenger() {
		passengerDelivered = true;
	}

	public String getPassengerDestination() {
		assert passengerDestination != null;
		return passengerDestination;
	}

	public String getStringType(int[] location) {
		if (!this.isInBounds(location)) {
			return "none";
		}

		CellObject dest = getData().cells.getCell(location)
				.getFirstObjectWithProperty("destination");
		if (dest != null) {
			return dest.getProperty("color");
		}

		if (getData().cells.getCell(location).hasObjectWithProperty("fuel")) {
			return "fuel";
		}

		return "normal";
	}

	public boolean wall(int[] from, Direction to) {
		Set<CellObject> walls = getData().cells.getCell(from)
				.getAllObjectsWithProperty("block");
		if (!walls.isEmpty()) {
			for (CellObject wall : walls) {
				if (Direction.valueOf(wall.getProperty("direction")).equals(to)) {
					return true;
				}
			}
		}
		return false;
	}
}
