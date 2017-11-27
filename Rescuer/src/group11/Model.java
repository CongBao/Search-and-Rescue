package group11;

public interface Model {

	double ARENA_WIDTH = 150;
	double ARENA_DEPTH = 190;
	int WIDTH = 5;
	int DEPTH = 6;
	double UNIT_WIDTH = ARENA_WIDTH / WIDTH;
	double UNIT_DEPTH = ARENA_DEPTH / DEPTH;

	int[] NORTH = new int[] { 0, 1 };
	int[] SOUTH = new int[] { 0, -1 };
	int[] EAST = new int[] { 1, 0 };
	int[] WEST = new int[] { -1, 0 };

	int[] UNKNOWN = new int[] { -1, -1 };

	int EMPTY = 0x00;
	int AGENT = 0x02;
	int OBSTACLE = 0x04;
	int VIC_POS = 0x08;
	int VIC_CRI = 0x10;
	int VIC_SER = 0x20;
	int VIC_MIN = 0x40;

}
