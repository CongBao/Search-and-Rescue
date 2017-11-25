import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class ArenaModel extends GridWorldModel {

	public static final int WIDTH = 5 + 2;
	public static final int HEIGHT = 6 + 2;

	public static final int SCOUT = 0;

	public static final int VIC_POS = 0x08;
	public static final int VIC_CRI = 0x10;
	public static final int VIC_SER = 0x20;
	public static final int VIC_MIN = 0x40;

	public ArenaModel() {
		super(WIDTH, HEIGHT, 2);
		// position of agent
		setAgPos(SCOUT, 1, 1);
		// walls
		addWall(0, 0, WIDTH - 1, 0);
		addWall(0, 0, 0, HEIGHT - 1);
		addWall(0, HEIGHT - 1, WIDTH - 1, HEIGHT - 1);
		addWall(WIDTH - 1, 0, WIDTH - 1, HEIGHT - 1);
		// obstacles
		add(OBSTACLE, 2, 4);
		add(OBSTACLE, 3, 3);
		add(OBSTACLE, 4, 4);
		add(OBSTACLE, 4, 5);
		// possible victims
		add(VIC_POS, 2, 2);
		add(VIC_POS, 3, 4);
		add(VIC_CRI, 2, 5);
		add(VIC_SER, 5, 2);
		add(VIC_MIN, 1, 3);
	}

	public void travelTo(Location loc) {
		Location now = getAgPos(SCOUT);
		if (now.x < loc.x) {
			now.x++;
		} else if (now.x > loc.x) {
			now.x--;
		}
		if (now.y < loc.y) {
			now.y++;
		} else if (now.y > loc.y) {
			now.y--;
		}
		setAgPos(SCOUT, now);
	}

}
