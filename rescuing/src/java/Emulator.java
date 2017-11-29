import java.util.Random;

import jason.environment.grid.Location;

/**
 * This {@code Emulator} is used to emulate the actions and movements of robot.
 * So experiments can carry on even without a robot.
 *
 * @author Cong Bao
 */
public class Emulator {

	public static final int[] N = new int[] { 0, -1 };
	public static final int[] S = new int[] { 0, 1 };
	public static final int[] W = new int[] { -1, 0 };
	public static final int[] E = new int[] { 1, 0 };

	private ArenaModel model;

	private int[] pos;
	private int[] dir;

	/**
	 * The constructor.
	 *
	 * @param model
	 *            an instance of {@link ArenaModel}
	 */
	public Emulator(ArenaModel model) {
		this.model = model;
		initRobot();
	}

	/**
	 * Initialize a robot with random location and heading.
	 */
	public void initRobot() {
		Random random = new Random(System.currentTimeMillis());
		while (pos == null) {
			int x = random.nextInt(model.getWidth() - 2) + 1;
			int y = random.nextInt(model.getHeight() - 2) + 1;
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

	/**
	 * If the given cell is free.
	 *
	 * @param x
	 *            x-axis of this cell
	 * @param y
	 *            y-axis of this cell
	 * @return true if there is neither obstacle nor victim
	 */
	public boolean isFree(int x, int y) {
		return model.isFree(x, y) && isFreeOfVictim(x, y);
	}

	/**
	 * If the given cell is free of victim.
	 *
	 * @param x
	 *            x-axis of this cell
	 * @param y
	 *            y-axis of this cell
	 * @return true if there is no victim in this cell
	 */
	public boolean isFreeOfVictim(int x, int y) {
		return model.isFree(ArenaModel.VIC_POS, x, y) && model.isFree(ArenaModel.VIC_CRI, x, y)
				&& model.isFree(ArenaModel.VIC_SER, x, y) && model.isFree(ArenaModel.VIC_MIN, x, y);
	}

	/**
	 * Emulate the distance data return by robot.
	 *
	 * @return distance data of [left, right, front]
	 */
	public boolean[] detectObstacle() {
		boolean[] occupies = new boolean[3];
		occupies[0] = !model.isFreeOfObstacle(pos[0] + dir[1], pos[1] - dir[0]);
		occupies[1] = !model.isFreeOfObstacle(pos[0] - dir[1], pos[1] + dir[0]);
		occupies[2] = !model.isFreeOfObstacle(pos[0] + dir[0], pos[1] + dir[1]);
		return occupies;
	}

	/**
	 * Emulate the victim data return by robot.
	 *
	 * @return the id of victim
	 */
	public int detectVictim() {
		int x = pos[0];
		int y = pos[1];
		if (model.hasObject(ArenaModel.VIC_POS, x, y)) {
			return ArenaModel.VIC_POS;
		} else if (model.hasObject(ArenaModel.VIC_CRI, x, y)) {
			return ArenaModel.VIC_CRI;
		} else if (model.hasObject(ArenaModel.VIC_SER, x, y)) {
			return ArenaModel.VIC_SER;
		} else if (model.hasObject(ArenaModel.VIC_MIN, x, y)) {
			return ArenaModel.VIC_MIN;
		} else {
			return ArenaModel.CLEAN;
		}
	}

	/**
	 * Emulate the movement of robot.
	 *
	 * @param side
	 *            the side to go, in ['L', 'R', 'F']
	 */
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
		default:
			break;
		}
		pos = new int[] { pos[0] + dir[0], pos[1] + dir[1] };
	}

	/**
	 * Emulate the movement of robot.
	 *
	 * @param loc
	 *            the {@link Location} to go
	 */
	public void moveTo(Location loc) {
		int[] to = new int[] { loc.x - pos[0], loc.y - pos[1] };
		int sin = dir[0] * to[1] - dir[1] * to[0];
		switch (sin) {
		case -1:
			moveTo('L');
			break;
		case 1:
			moveTo('R');
			break;
		case 0:
			moveTo('F');
			break;
		default:
			break;
		}
	}

	/**
	 * Print the real data of the robot.
	 */
	public void printRealInfo() {
		System.out.print("[Emulator Robot Real Info] ");
		System.out.print("Pos: (" + pos[0] + ", " + pos[1] + "), ");
		System.out.println("Dir: (" + dir[0] + ", " + dir[1] + ")");
	}

}
