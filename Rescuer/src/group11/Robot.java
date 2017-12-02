package group11;

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
	 * Whether there is an obstacle at [left, right, front] or not.
	 *
	 * @return obstacle data of [left, right, front]
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
	 * @param pos
	 *            the position to go
	 */
	void moveTo(int[] pos);

}