package group11;

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

	public static final int EMPTY = 0x00;
	public static final int AGENT = 0x02;
	public static final int OBSTACLE = 0x04;
	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;

	private int[][] map;

	private int[] agtDir;
	private int[] agtPos;

	public Arena() {
		map = new int[WIDTH + 2][DEPTH + 2];
		for (int i = 0; i < WIDTH + 2; i++) {
			map[i][0] = OBSTACLE;
			map[i][DEPTH + 1] = OBSTACLE;
		}
		for (int i = 0; i < DEPTH + 2; i++) {
			map[0][i] = OBSTACLE;
			map[WIDTH + 1][i] = OBSTACLE;
		}
		agtDir = agtPos = UNKNOWN;
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
