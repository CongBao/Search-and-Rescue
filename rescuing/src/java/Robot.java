import jason.environment.grid.Location;

public interface Robot {

	int[] N = new int[] { 0, -1 };
	int[] S = new int[] { 0, 1 };
	int[] W = new int[] { -1, 0 };
	int[] E = new int[] { 1, 0 };

	/**
	 * Update the information shown on robot's LCD screen.
	 *
	 * @param data
	 *            arena data
	 */
	void updateArenaInfo(int[][] data);

	/**
	 * Update the position and orientation information of robot.
	 *
	 * @param loc
	 *            the {@link Location} of robot
	 * @param dir
	 *            the orientation of robot
	 */
	void updateRobotInfo(Location loc, int[] dir);

	/**
	 * Whether there is an obstacle at [left, right, front, back] or not.
	 *
	 * @return obstacle data of [left, right, front, back]
	 */
	boolean[] detectObstacle();

	/**
	 * Detect the color of victim at current position.
	 *
	 * @return the id of victim
	 */
	int detectVictim();

	/**
	 * Move to the given side, in ['L', 'R', 'F', 'B'].
	 *
	 * @param side
	 *            the side to go
	 */
	void moveTo(char side);

	/**
	 * Move to the given location.
	 *
	 * @param loc
	 *            the {@link Location} to go
	 */
	void moveTo(Location loc);

	/**
	 * The mission completes.
	 */
	void complete();

}
