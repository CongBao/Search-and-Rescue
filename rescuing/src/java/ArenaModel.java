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
	public static final int HEIGHT = 6 + 2;

	public static final int SCOUT = 0;

	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;

	public static final int POS_LOC = 0x80;

	int[][] count;
	List<Location> possibleVictims;
	List<Character> visited; // TODO
	List<Map<Integer, List<Character>>> encounters;

	public ArenaModel() {
		super(WIDTH, HEIGHT, 2);
		count = new int[width][height];
		// walls
		addWall(0, 0, WIDTH - 1, 0);
		addWall(0, 0, 0, HEIGHT - 1);
		addWall(0, HEIGHT - 1, WIDTH - 1, HEIGHT - 1);
		addWall(WIDTH - 1, 0, WIDTH - 1, HEIGHT - 1);
		// obstacles
		add(OBSTACLE, 1, 6);
		add(OBSTACLE, 2, 3);
		add(OBSTACLE, 3, 2);
		add(OBSTACLE, 5, 2);
		add(OBSTACLE, 5, 5);
		add(OBSTACLE, 6, 5);
		// possible victims
		possibleVictims = new LinkedList<>();
		possibleVictims.add(new Location(1, 1));
		possibleVictims.add(new Location(3, 3));
		possibleVictims.add(new Location(3, 5));
		possibleVictims.add(new Location(4, 4));
		possibleVictims.add(new Location(5, 1));
		for (Location loc : possibleVictims) {
			add(VIC_POS, loc);
		}
		visited = new LinkedList<>();
		encounters = new LinkedList<>();
	}

	/**
	 * Return the model data.
	 *
	 * @return a 2-d array stored data
	 */
	public int[][] getModelData() {
		return data;
	}

	/**
	 * Get the object located at given cell.
	 *
	 * @param loc
	 *            the {@link Location} of the cell
	 * @return the value of this object
	 */
	public int getObject(Location loc) {
		return getObject(loc.x, loc.y);
	}

	/**
	 * Get the object located at given cell.
	 *
	 * @param x
	 *            x-axis of this cell
	 * @param y
	 *            y-axis of this cell
	 * @return the value of this object
	 */
	public int getObject(int x, int y) {
		if (inGrid(x, y)) {
			return data[x][y];
		}
		return -1;
	}

	/**
	 * Reduce the number of possible cells that the robot located in.
	 *
	 * @param remain
	 *            the remaining possible cells with headings
	 * @param obsData
	 *            data of obstacles, in [left, right, front]
	 * @param vicData
	 *            data of victim
	 * @return a new map of remaining possible cells
	 */
	public Map<Location, List<int[]>> localize(Map<Location, List<int[]>> remain, boolean[] obsData, int vicData) {
		for (Location pos : remain.keySet()) {
			remove(POS_LOC, pos);
		}
		Map<Location, List<int[]>> possible = new HashMap<>();
		for (Location pos : remain.keySet()) {
			List<int[]> posDir = new LinkedList<>();
			for (int[] dir : remain.get(pos)) {
				boolean[] real = new boolean[4];
				real[0] = !isFreeOfObstacle(pos.x + dir[1], pos.y - dir[0]);
				real[1] = !isFreeOfObstacle(pos.x - dir[1], pos.y + dir[0]);
				real[2] = !isFreeOfObstacle(pos.x + dir[0], pos.y + dir[1]);
				real[3] = !isFreeOfObstacle(pos.x - dir[0], pos.y - dir[1]);
				if (Arrays.equals(obsData, real) && (getObject(pos) == vicData || CLEAN == vicData)) {
					if (hasObject(VIC_POS, pos)) {
						checkAndRescue(VIC_POS);
					}
					posDir.add(dir);
				}
			}
			if (posDir.size() > 0) {
				possible.put(pos, posDir);
			}
		}
		for (Location pos : possible.keySet()) {
			((ArenaView) view).remain = possible;
			add(POS_LOC, pos);
		}
		return possible;
	}

	/**
	 * Update the map of remaining possible cells after each moving.
	 *
	 * @param remain
	 *            the remaining possible cells with headings
	 * @param side
	 *            the side to go, in ['L', 'R', 'F']
	 * @return a new map of remaining possible cells
	 */
	public Map<Location, List<int[]>> updateRemain(Map<Location, List<int[]>> remain, char side) {
		for (Location pos : remain.keySet()) {
			remove(POS_LOC, pos);
		}
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
		for (Location pos : updated.keySet()) {
			((ArenaView) view).remain = updated;
			add(POS_LOC, pos);
		}
		return updated;
	}

	/**
	 * Find an optimal path covering all victims.
	 *
	 * @return a list of locations the robot should go
	 */
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

	/**
	 * Use A* algorithm to find a nearest neighbor in a list of locations
	 *
	 * @param start
	 *            the start point
	 * @param neighbors
	 *            a list of neighbor locations
	 * @return the nearest location in list
	 */
	public Location findNearestTarget(final Location start, final List<Location> neighbors) {
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

	/**
	 * Travel to the given cell.
	 *
	 * @param loc
	 *            the {@link Location} of cell
	 */
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

	/**
	 * Check the given cell, if there is a victim, rescue him.
	 *
	 * @param loc
	 *            the {@link Location} of cell
	 * @param vic
	 *            the id of the victim
	 */
	public void checkAndRescue(Location loc, int vic) {
		if (hasObject(VIC_POS, loc)) {
			remove(VIC_POS, loc);
			possibleVictims.remove(loc);
			if (vic > VIC_POS) {
				add(vic, loc);
			}
		}
	}

	/**
	 * Check the given victim value, and record it for future use
	 *
	 * @param vic
	 *            the id of the victim
	 */
	public void checkAndRescue(int vic) {
		Map<Integer, List<Character>> record = new HashMap<>(1, 2);
		record.put(vic, new LinkedList<>());
		encounters.add(record);
	}

	/**
	 * Remove victims found during localization from possible victim list
	 *
	 * @param pos
	 *            the determined position
	 * @param dir
	 *            the determined direction
	 */
	public void removeCheckedVic(int[] pos, int[] dir) {
		for (Map<Integer, List<Character>> record : encounters) {
			int vic = record.keySet().iterator().next();
			List<Character> steps = record.values().iterator().next();
			Location nowPos = new Location(pos[0], pos[1]);
			int[] nowDir = Arrays.copyOf(dir, dir.length);
			Collections.reverse(steps);
			for (char c : steps) {
				nowPos = new Location(nowPos.x - nowDir[0], nowPos.y - nowDir[1]);
				switch (c) {
				case 'L':
					nowDir = new int[] { -nowDir[1], nowDir[0] };
					break;
				case 'R':
					nowDir = new int[] { nowDir[1], -nowDir[0] };
					break;
				case 'F':
					break;
				default:
					break;
				}
			}
			if (hasObject(VIC_POS, nowPos)) {
				remove(VIC_POS, nowPos);
				possibleVictims.remove(nowPos);
				if (vic > VIC_POS) {
					add(vic, nowPos);
				}
			}
		}
	}

	public void addVisitedCount(int[] pos, int[] dir) {
		// TODO
	}

}
