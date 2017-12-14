import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class ArenaModel extends GridWorldModel {

	public static final int WIDTH = 6 + 2; // TODO
	public static final int HEIGHT = 6 + 2; // TODO

	public static final int SCOUT = 0;

	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;

	public static final int POS_LOC = 0x80;

	int[][] count;
	List<Location> possibleVictims;
	List<Character> visited;
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
		/*add(OBSTACLE, 1, 3);
		add(OBSTACLE, 2, 2);
		add(OBSTACLE, 4, 2);
		add(OBSTACLE, 4, 5);
		add(OBSTACLE, 5, 5);*/
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
	 * Get surrounding obstacle data in the map.
	 *
	 * @param pos
	 *            the position
	 * @param dir
	 *            the direction
	 * @return obstacle data in [left, right, front, back]
	 */
	public boolean[] getSurrounds(Location pos, int[] dir) {
		boolean[] surrounds = new boolean[4];
		surrounds[0] = !isFreeOfObstacle(pos.x + dir[1], pos.y - dir[0]);
		surrounds[1] = !isFreeOfObstacle(pos.x - dir[1], pos.y + dir[0]);
		surrounds[2] = !isFreeOfObstacle(pos.x + dir[0], pos.y + dir[1]);
		surrounds[3] = !isFreeOfObstacle(pos.x - dir[0], pos.y - dir[1]);
		return surrounds;
	}

	/**
	 * Reduce the number of possible cells that the robot located in.
	 *
	 * @param remain
	 *            the remaining possible cells with headings
	 * @param obsData
	 *            data of obstacles, in [left, right, front, back]
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
				if (Arrays.equals(obsData, getSurrounds(pos, dir)) && (getObject(pos) == vicData || CLEAN == vicData)) {
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
	 * Choose one side to explore, every time trying to go to the cell with highest
	 * score.
	 *
	 * @param remain
	 *            the remaining possible cells with headings
	 * @param obsData
	 *            data of obstacles, in [left, right, front, back]
	 * @return the side to go, in ('L', 'R', 'F')
	 */
	public char chooseSide(Map<Location, List<int[]>> remain, boolean[] obsData) {
		@SuppressWarnings("unchecked")
		List<boolean[]>[] predictObs = new List[3];
		for (int i = 0; i < 3; i++) {
			predictObs[i] = obsData[i] ? null : new LinkedList<>();
		}
		int[] vicNum = new int[3];
		for (Location pos : remain.keySet()) {
			for (int[] dir : remain.get(pos)) {
				if (!obsData[0]) { // left
					int[] turned = new int[] { dir[1], -dir[0] };
					Location moved = new Location(pos.x + turned[0], pos.y + turned[1]);
					predictObs[0].add(getSurrounds(moved, turned));
					if (hasObject(VIC_POS, moved)) {
						vicNum[0]++;
					}
				}
				if (!obsData[1]) { // right
					int[] turned = new int[] { -dir[1], dir[0] };
					Location moved = new Location(pos.x + turned[0], pos.y + turned[1]);
					predictObs[1].add(getSurrounds(moved, turned));
					if (hasObject(VIC_POS, moved)) {
						vicNum[1]++;
					}
				}
				if (!obsData[2]) { // front
					int[] turned = new int[] { dir[0], dir[1] };
					Location moved = new Location(pos.x + turned[0], pos.y + turned[1]);
					predictObs[2].add(getSurrounds(moved, turned));
					if (hasObject(VIC_POS, moved)) {
						vicNum[2]++;
					}
				}
			}
		}
		double[] scores = new double[3];
		for (int i = 0; i < 3; i++) {
			if (obsData[i]) {
				scores[i] = Double.NEGATIVE_INFINITY;
				continue;
			}
			List<boolean[]> distinct = new LinkedList<>();
			for (boolean[] predict : predictObs[i]) {
				boolean duplicate = false;
				for (boolean[] data : distinct) {
					if (Arrays.equals(predict, data)) {
						duplicate = true;
						break;
					}
				}
				if (!duplicate) {
					distinct.add(predict);
				}
			}
			scores[i] += distinct.size() + vicNum[i] * 0.5;
		}
		double[] sort = Arrays.copyOf(scores, scores.length);
		Arrays.sort(sort);
		double max = sort[scores.length - 1];
		if (scores[2] == max) {
			return 'F';
		} else if (scores[0] == max) {
			return 'L';
		} else if (scores[1] == max) {
			return 'R';
		} else {
			return 'B';
		}
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
		if (possibleVictims.isEmpty()) {
			return new LinkedList<>();
		}
		return new MultiAStar(this).findPath(getAgPos(SCOUT), possibleVictims);
	}

	/**
	 * Travel to the given cell.
	 *
	 * @param loc
	 *            the {@link Location} of cell
	 */
	public void travelTo(Location loc) {
		Location now = getAgPos(SCOUT);
		int[] dir = ((ArenaView) view).heading;
		int[] to = new int[] { loc.x - now.x, loc.y - now.y };
		int sin = dir[0] * to[1] - dir[1] * to[0];
		switch (sin) {
		case -1:
			dir = new int[] { dir[1], -dir[0] };
			break;
		case 1:
			dir = new int[] { -dir[1], dir[0] };
			break;
		}
		((ArenaView) view).heading = dir;
		setAgPos(SCOUT, loc);
		count[loc.x][loc.y]++;
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

	/**
	 * Add counts to the cells that covered during localization.
	 *
	 * @param pos
	 *            the determined position
	 * @param dir
	 *            the determined direction
	 */
	public void addVisitedCount(int[] pos, int[] dir) {
		Location nowPos = new Location(pos[0], pos[1]);
		int[] nowDir = Arrays.copyOf(dir, dir.length);
		Collections.reverse(visited);
		for (char move : visited) {
			nowPos = new Location(nowPos.x - nowDir[0], nowPos.y - nowDir[1]);
			count[nowPos.x][nowPos.y]++;
			switch (move) {
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
	}

}
