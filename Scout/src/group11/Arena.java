package group11;

/**
 * A class represents the arena.
 *
 * @author Cong Bao
 *
 */
public class Arena {

	public static final double ARENA_WIDTH = 150;
	public static final double ARENA_DEPTH = 190;
	public static final int WIDTH = 6;
	public static final int DEPTH = 6;
	public static final double UNIT_WIDTH = ARENA_WIDTH / WIDTH;
	public static final double UNIT_DEPTH = ARENA_DEPTH / DEPTH;

	public static final int[] NORTH = new int[] { 0, -1 };
	public static final int[] SOUTH = new int[] { 0, 1 };
	public static final int[] WEST = new int[] { -1, 0 };
	public static final int[] EAST = new int[] { 1, 0 };

	public static final int[] UNKNOWN = new int[] { -1, -1 };

	public static final int CLEAN = 0x00;
	public static final int AGENT = 0x02;
	public static final int OBSTACLE = 0x04;
	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;
	public static final int POS_LOC = 0x80;

	private int[][] map;

	private int[] agtDir;
	private int[] agtPos;

	public Arena() {
		map = new int[WIDTH + 2][DEPTH + 2];
		agtDir = agtPos = UNKNOWN;
	}

	/**
	 * Whether the given side is occupied by an obstacle or not.
	 *
	 * @param side
	 *            one side of current place, in ('L', 'R', 'F', 'B')
	 * @return whether the side is occupied
	 */
	public Boolean isOccupied(char side) {
		switch (side) {
		case 'L':
			return isOccupied(agtPos[0] + agtDir[1], agtPos[1] - agtDir[0]);
		case 'R':
			return isOccupied(agtPos[0] - agtDir[1], agtPos[1] + agtDir[0]);
		case 'F':
			return isOccupied(agtPos[0] + agtDir[0], agtPos[1] + agtDir[1]);
		case 'B':
			return isOccupied(agtPos[0] - agtDir[0], agtPos[1] - agtDir[1]);
		default:
			return null;
		}
	}

	/**
	 * Whether the given place is occupied by an obstacle or not.
	 *
	 * @param x
	 *            x-axis
	 * @param y
	 *            y-axis
	 * @return whether the place is occupied or not
	 */
	public boolean isOccupied(int x, int y) {
		return hasObject(OBSTACLE, x, y);
	}

	/**
	 * Whether there is a given object at the given place.
	 *
	 * @param obj
	 *            the id of this object
	 * @param x
	 *            x-axis
	 * @param y
	 *            y-axis
	 * @return whether there is the object or not
	 */
	public boolean hasObject(int obj, int x, int y) {
		return (map[x][y] & obj) != 0;
	}

	public int[][] getMap() {
		return map;
	}

	public void setMap(int[][] map) {
		this.map = map;
	}

	public int[] getAgtDir() {
		return agtDir;
	}

	public void setAgtDir(int[] agtDir) {
		this.agtDir = agtDir;
	}

	public int[] getAgtPos() {
		return agtPos;
	}

	public void setAgtPos(int[] agtPos) {
		this.agtPos = agtPos;
	}

}
