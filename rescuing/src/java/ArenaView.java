import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

public class ArenaView extends GridWorldView {

	private static final long serialVersionUID = 1L;

	Map<Location, List<int[]>> remain; // used for draw possible location and heading

	public ArenaView(ArenaModel model) {
		super(model, "Arena Model", 700);
		defaultFont = new Font("Consolas", Font.BOLD | Font.ITALIC, 20);
		setVisible(true);
		repaint();
	}

	@Override
	public void draw(Graphics g, int x, int y, int object) {
		super.draw(g, x, y, object);
		switch (object) {
		case ArenaModel.OBSTACLE:
			drawObstacle(g, x, y);
			break;
		case ArenaModel.VIC_POS:
			drawVic(g, x, y, Color.lightGray);
			break;
		case ArenaModel.VIC_CRI:
			drawVic(g, x, y, Color.red);
			break;
		case ArenaModel.VIC_SER:
			drawVic(g, x, y, Color.blue);
			break;
		case ArenaModel.VIC_MIN:
			drawVic(g, x, y, Color.green);
			break;
		case ArenaModel.POS_LOC:
			drawRemain(g, Color.yellow);
			break;
		default:
			break;
		}
	}

	@Override
	public void drawAgent(Graphics g, int x, int y, Color c, int id) {
		super.drawAgent(g, x, y, Color.magenta, -1);
		g.setColor(Color.black);
		drawString(g, x, y, defaultFont, "Scout");
	}

	public void drawVic(Graphics g, int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
	}

	public void drawRemain(Graphics g, Color c) {
		g.setColor(c);
		for (Location pos : remain.keySet()) {
			for (int[] dir : remain.get(pos)) {
				int x = pos.x;
				int y = pos.y;
				Polygon p = new Polygon();
				if (Arrays.equals(dir, new int[] { 0, -1 })) { // N
					p.addPoint(x * cellSizeW + cellSizeW / 2, y * cellSizeH + 1);
					p.addPoint(x * cellSizeW + 1, y * cellSizeH + cellSizeH - 1);
					p.addPoint(x * cellSizeW + cellSizeW - 1, y * cellSizeH + cellSizeH - 1);
				} else if (Arrays.equals(dir, new int[] { 0, 1 })) { // S
					p.addPoint(x * cellSizeW + 1, y * cellSizeH + 1);
					p.addPoint(x * cellSizeW + cellSizeW - 1, y * cellSizeH + 1);
					p.addPoint(x * cellSizeW + cellSizeW / 2, y * cellSizeH + cellSizeH - 1);
				} else if (Arrays.equals(dir, new int[] { -1, 0 })) { // W
					p.addPoint(x * cellSizeW + 1, y * cellSizeH + cellSizeH / 2);
					p.addPoint(x * cellSizeW + cellSizeW - 1, y * cellSizeH + 1);
					p.addPoint(x * cellSizeW + cellSizeW - 1, y * cellSizeH + cellSizeH - 1);
				} else if (Arrays.equals(dir, new int[] { 1, 0 })) { // E
					p.addPoint(x * cellSizeW + 1, y * cellSizeH + 1);
					p.addPoint(x * cellSizeW + 1, y * cellSizeH + cellSizeH - 1);
					p.addPoint(x * cellSizeW + cellSizeW - 1, y * cellSizeH + cellSizeH / 2);
				}
				g.fillPolygon(p);
				g.setColor(Color.black);
				g.drawPolygon(p);
				g.setColor(c);
			}
		}
	}

}
