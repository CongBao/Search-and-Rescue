import jason.environment.grid.Location;

public interface Robot {

	int[] N = new int[] { 0, -1 };
	int[] S = new int[] { 0, 1 };
	int[] W = new int[] { -1, 0 };
	int[] E = new int[] { 1, 0 };

	/**
	 * If there is an obstacle at [left, right, front].
	 *
	 * @return obstacle data of [left, right, front]
	 */
	boolean[] detectObstacle();

	/**
	 * The id of victim and current position.
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

}
