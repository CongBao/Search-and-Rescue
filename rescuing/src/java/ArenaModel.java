import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class ArenaModel extends GridWorldModel {

	public static final int WIDTH = 5 + 2;
	public static final int HEIGHT = 6 + 2;

	public static final int SCOUT = 0;

	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;

	List<Location> possibleVictims;

	public ArenaModel() {
		super(WIDTH, HEIGHT, 2);
		// position of agent
		// setAgPos(SCOUT, 1, 1);
		// walls
		addWall(0, 0, WIDTH - 1, 0);
		addWall(0, 0, 0, HEIGHT - 1);
		addWall(0, HEIGHT - 1, WIDTH - 1, HEIGHT - 1);
		addWall(WIDTH - 1, 0, WIDTH - 1, HEIGHT - 1);
		// obstacles
		add(OBSTACLE, 1, 4);
		add(OBSTACLE, 3, 3);
		add(OBSTACLE, 4, 4);
		add(OBSTACLE, 4, 5);
		// possible victims
		possibleVictims = new LinkedList<>();
		possibleVictims.add(new Location(2, 2));
		possibleVictims.add(new Location(3, 4));
		possibleVictims.add(new Location(2, 5));
		possibleVictims.add(new Location(5, 2));
		possibleVictims.add(new Location(5, 3));
		for (Location loc : possibleVictims) {
			add(VIC_POS, loc);
		}
	}

	public Location localize() {
		// TODO localize robot
		return new Location(1, 1);
	}

	public List<Location> findOptimalPath() {
		AStar aStar = new AStar(this);
		List<Location> optimalPath = new LinkedList<>();
		List<Location> remain = new LinkedList<>(possibleVictims);
		Location start = getAgPos(SCOUT);
		while (!remain.isEmpty()) {
			Location nearest = findNearestTarget(start, remain);
			optimalPath.addAll(aStar.findPath(start, nearest));
			start = nearest;
			remain.remove(nearest);
		}
		return optimalPath;
	}

	public Location findNearestTarget(Location start, List<Location> neighbors) {
		AStar aStar = new AStar(this);
		Map<Location, Integer> dis = new HashMap<>();
		for (Location loc : neighbors) {
			dis.put(loc, aStar.findPath(start, loc).size());
		}
		List<Map.Entry<Location, Integer>> disList = new LinkedList<>(dis.entrySet());
		Collections.sort(disList, new Comparator<Map.Entry<Location, Integer>>() {
			@Override
			public int compare(Map.Entry<Location, Integer> o1, Map.Entry<Location, Integer> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return disList.get(0).getKey();
	}

	public void travelTo(Location loc) {
		Location now = getAgPos(SCOUT);
		if (now.x < loc.x) {
			now.x++;
		} else if (now.x > loc.x) {
			now.x--;
		}
		if (now.y < loc.y) {
			now.y++;
		} else if (now.y > loc.y) {
			now.y--;
		}
		setAgPos(SCOUT, now);
	}

	public void checkAndRescue(Location loc) {
		// TODO add more function
		if (hasObject(VIC_POS, loc)) {
			remove(VIC_POS, loc);
			System.out.println("A victim is rescued.");
		}
	}

}
