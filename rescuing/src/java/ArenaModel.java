import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class ArenaModel extends GridWorldModel {

	public static final int WIDTH = 6 + 2;
	public static final int HEIGHT = 7 + 2;

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
		add(OBSTACLE, 1, 6);
		add(OBSTACLE, 2, 3);
		add(OBSTACLE, 2, 4);
		add(OBSTACLE, 3, 3);
		add(OBSTACLE, 4, 4);
		add(OBSTACLE, 4, 5);
		add(OBSTACLE, 4, 7);
		add(OBSTACLE, 5, 2);
		add(OBSTACLE, 6, 4);
		// possible victims
		possibleVictims = new LinkedList<>();
		possibleVictims.add(new Location(1, 2));
		possibleVictims.add(new Location(2, 5));
		possibleVictims.add(new Location(3, 4));
		possibleVictims.add(new Location(4, 3));
		possibleVictims.add(new Location(5, 5));
		for (Location loc : possibleVictims) {
			add(VIC_POS, loc);
		}
	}

	public Map<Location, List<int[]>> localize(Map<Location, List<int[]>> remain, boolean[] obsData, int vicData) {
		Map<Location, List<int[]>> possible = new HashMap<>();
		for (Location pos : remain.keySet()) {
			List<int[]> posDir = new LinkedList<>();
			for (int[] dir : remain.get(pos)) {
				boolean[] real = new boolean[3];
				real[0] = !isFreeOfObstacle(pos.x + dir[1], pos.y - dir[0]);
				real[1] = !isFreeOfObstacle(pos.x - dir[1], pos.y + dir[0]);
				real[2] = !isFreeOfObstacle(pos.x + dir[0], pos.y + dir[1]);
				if (Arrays.equals(obsData, real) && data[pos.x][pos.y] == vicData) {
					posDir.add(dir);
				}
			}
			if (posDir.size() > 0) {
				possible.put(pos, posDir);
			}
		}
		return possible;
	}

	public Map<Location, List<int[]>> updateRemain(Map<Location, List<int[]>> remain, char side) {
		Map<Location, List<int[]>> updated = new HashMap<>();
		for (Location pos : remain.keySet()) {
			for (int[] dir : remain.get(pos)) {
				int[] turned = null;
				switch (side) {
				case 'L':
					turned = new int[] { dir[1], -dir[0] };
					break;
				case 'R':
					turned = new int[] { -dir[1], dir[0] };
					break;
				case 'F':
					turned = new int[] { dir[0], dir[1] };
					break;
				default:
					break;
				}
				Location loc = new Location(pos.x + turned[0], pos.y + turned[1]);
				updated.putIfAbsent(loc, new LinkedList<>());
				updated.get(loc).add(turned);
			}
		}
		return updated;
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
