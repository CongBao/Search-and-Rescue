package group11;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class Emulator implements Robot {

	private Random random;

	private Arena arena;

	private int[][] map;

	private int[] pos;
	private int[] dir;

	private long moveDelay = 400L;
	private long turnDelay = 100L;
	private long scanDelay = 200L;

	private int posVicCount = 2;
	private int actVicCount = 3;

	private boolean firstDetect = true;

	public Emulator() {
		random = new Random(System.currentTimeMillis());
		initMap();
		initRobot();
	}

	public void initMap() {
		map = new int[Arena.WIDTH + 2][Arena.DEPTH + 2];
		for (int i = 0; i < Arena.WIDTH + 2; i++) {
			map[i][0] = Arena.OBSTACLE;
			map[i][Arena.DEPTH + 1] = Arena.OBSTACLE;
		}
		for (int i = 0; i < Arena.DEPTH + 2; i++) {
			map[0][i] = Arena.OBSTACLE;
			map[Arena.WIDTH + 1][i] = Arena.OBSTACLE;
		}
		map[2][2] =  Arena.OBSTACLE;
		map[2][4] =  Arena.OBSTACLE;
		map[2][6] =  Arena.OBSTACLE;
		map[4][2] =  Arena.OBSTACLE;
		map[4][4] =  Arena.OBSTACLE;
		map[6][4] =  Arena.OBSTACLE;
		map[1][2] =  Arena.VIC_POS;
		map[2][5] =  Arena.VIC_POS;
		map[3][4] =  Arena.VIC_POS;
		map[4][3] =  Arena.VIC_POS;
		map[5][5] =  Arena.VIC_POS;
	}

	public void initRobot() {
		while (pos == null) {
			int x = random.nextInt(Arena.WIDTH) + 1;
			int y = random.nextInt(Arena.DEPTH) + 1;
			if (isFree(x, y)) {
				pos = new int[] { x, y };
			}
		}
		dir = new int[2];
		dir[0] = random.nextInt(2);
		dir[1] = Math.abs(dir[0] - 1);
		dir[0] = random.nextBoolean() ? dir[0] : -dir[0];
		dir[1] = random.nextBoolean() ? dir[1] : -dir[1];
	}

	public boolean isFree(int x, int y) {
		return (map[x][y] & Arena.OBSTACLE) == 0 && (map[x][y] & Arena.VIC_POS) == 0;
	}

	public boolean isFreeOfObstacle(int x, int y) {
		return (map[x][y] & Arena.OBSTACLE) == 0;
	}

	public boolean hasObject(int obj, int x, int y) {
		return (map[x][y] & obj) != 0;
	}

	public void delay(String action) {
		try {
			switch (action) {
			case "move":
				Thread.sleep(moveDelay);
				break;
			case "turn":
				Thread.sleep(turnDelay);
				break;
			case "scan":
				Thread.sleep(scanDelay);
				break;
			default:
				break;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateArenaInfo(int[][] data) {
		arena.setMap(data);
	}

	@Override
	public void updateRobotInfo(int[] pos, int[] dir) {
		arena.setAgtPos(pos);
		arena.setAgtDir(dir);
	}

	@Override
	public boolean[] detectObstacle() {
		delay("scan");
		boolean[] occupies = new boolean[4];
		occupies[0] = !isFreeOfObstacle(pos[0] + dir[1], pos[1] - dir[0]);
		occupies[1] = !isFreeOfObstacle(pos[0] - dir[1], pos[1] + dir[0]);
		occupies[2] = !isFreeOfObstacle(pos[0] + dir[0], pos[1] + dir[1]);
		if (firstDetect) {
			delay("turn");
			occupies[3] = !isFreeOfObstacle(pos[0] - dir[0], pos[1] - dir[1]);
			firstDetect = false;
		} else {
			occupies[3] = false;
		}
		return occupies;
	}

	@Override
	public int detectVictim() {
		if (hasObject(Arena.VIC_POS, pos[0], pos[1])) {
			int rand = random.nextInt(posVicCount + actVicCount);
			if (rand < posVicCount) {
				posVicCount--;
				return Arena.VIC_POS;
			}
			actVicCount--;
			return (int) Math.pow(2, random.nextInt(3) + 4);
		}
		return Arena.CLEAN;
	}

	@Override
	public void moveTo(char side) {
		switch (side) {
		case 'L':
			dir = new int[] { dir[1], -dir[0] };
			break;
		case 'R':
			dir = new int[] { -dir[1], dir[0] };
			break;
		case 'F':
			break;
		case 'B':
			dir = new int[] { -dir[0], -dir[1] };
			break;
		default:
			break;
		}
		switch (side) {
		case 'L':
		case 'R':
			delay("turn");
			break;
		default:
			break;
		}
		pos = new int[] { pos[0] + dir[0], pos[1] + dir[1] };
		delay("move");
	}

	@Override
	public void moveTo(int[] loc) {
		int[] to = new int[] { loc[0] - pos[0], loc[1] - pos[1] };
		if (Arrays.equals(dir, to)) {
			moveTo('F');
			return;
		}
		int sin = dir[0] * to[1] - dir[1] * to[0];
		switch (sin) {
		case -1:
			moveTo('L');
			break;
		case 1:
			moveTo('R');
			break;
		case 0:
			moveTo('B');
			break;
		default:
			break;
		}
	}

	public static void main(String[] args) {
		RemotePC remotePC = new RemotePC(new Emulator(), 10000);
		try {
			remotePC.listen();
		} catch (EOFException eofe) {
			System.out.println("Complete.");
			remotePC.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
