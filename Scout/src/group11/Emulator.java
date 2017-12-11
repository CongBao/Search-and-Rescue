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

	private long moveDelay = 500L;
	private long turnDelay = 500L;
	private long scanDelay = 1000L;

	private int posVicCount = 2;
	private int actVicCount = 3;

	private boolean firstDetect = true;
	private boolean determined = false;

	public Emulator() {
		random = new Random(System.currentTimeMillis());
		arena = new Arena();
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
		map[1][6] =  Arena.OBSTACLE;
		map[2][3] =  Arena.OBSTACLE;
		map[3][2] =  Arena.OBSTACLE;
		map[5][2] =  Arena.OBSTACLE;
		map[5][5] =  Arena.OBSTACLE;
		map[6][5] =  Arena.OBSTACLE;
		map[1][1] =  Arena.VIC_POS;
		map[3][3] =  Arena.VIC_POS;
		map[3][5] =  Arena.VIC_POS;
		map[4][4] =  Arena.VIC_POS;
		map[5][1] =  Arena.VIC_POS;
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

	public void travelAnUnit(boolean forth) {
		if (determined) {
			int[] pos = arena.getAgtPos();
			int[] dir = arena.getAgtDir();
			int[] newPos = new int[] { pos[0] + dir[0] * (forth ? 1 : -1), pos[1] + dir[1] * (forth ? 1 : -1) };
			arena.setAgtPos(newPos);
			this.pos = newPos;
		} else {
			pos = new int[] { pos[0] + dir[0] * (forth ? 1 : -1), pos[1] + dir[1] * (forth ? 1 : -1) };
		}
		delay("move");
		System.out.println(toString());
	}

	@Override
	public void updateArenaInfo(int[][] data) {
		arena.setMap(data);
	}

	@Override
	public void updateRobotInfo(int[] pos, int[] dir) {
		arena.setAgtPos(pos);
		arena.setAgtDir(dir);
		determined = true;
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
		delay("scan");
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
			if (determined) {
				arena.setAgtDir(dir);
			}
			break;
		case 'R':
			dir = new int[] { -dir[1], dir[0] };
			if (determined) {
				arena.setAgtDir(dir);
			}
			break;
		case 'F':
		case 'B':
		default:
			break;
		}
		switch (side) {
		case 'L':
		case 'R':
			delay("turn");
		case 'F':
			travelAnUnit(true);
			break;
		case 'B':
			travelAnUnit(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void moveTo(int[] target) {
		int[] pos = arena.getAgtPos();
		int[] dir = arena.getAgtDir();
		if (Arrays.equals(pos, Arena.UNKNOWN) || Arrays.equals(dir, Arena.UNKNOWN)) {
			return;
		}
		int[] to = new int[] { target[0] - pos[0], target[1] - pos[1] };
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

	@Override
	public void complete() {
		System.out.println("Complete.");
	}

	@Override
	public String toString() {
		return "[Emulator Info] Pos: (" + pos[0] + ", " + pos[1] + "), Dir: (" + dir[0] + ", " + dir[1] + ")";
	}

	public static void main(String[] args) {
		System.out.println("Waiting for PC connecting...");
		RemotePC remotePC = new RemotePC(new Emulator(), 10000);
		System.out.println("Connected.");
		try {
			remotePC.listen();
		} catch (EOFException eofe) {
			remotePC.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
