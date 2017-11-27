package group11;

import static group11.Model.*;

public class Arena {

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
