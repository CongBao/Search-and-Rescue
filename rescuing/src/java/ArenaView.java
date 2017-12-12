import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

public class ArenaView extends GridWorldView {

	private static final long serialVersionUID = 1L;

	Map<Location, List<int[]>> remain; // used to draw possible location and heading
	int[] heading; // used to draw robot's heading

	public ArenaView(ArenaModel model) {
		super(model, "Arena Model", 750);
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
			drawRemain(g);
			break;
		default:
			break;
		}
	}

	@Override
	public void drawAgent(Graphics g, int x, int y, Color c, int id) {
		Polygon p = getTriangle(new int[] { x, y }, heading);
		g.setColor(Color.magenta);
		g.fillPolygon(p);
		g.setColor(Color.black);
		g.drawPolygon(p);
		drawCount(g);
	}

	public void drawVic(Graphics g, int x, int y, Color c) {
		g.setColor(c);
		g.fillRect(x * cellSizeW + 1, y * cellSizeH + 1, cellSizeW - 1, cellSizeH - 1);
		g.setColor(Color.black);
		g.drawRect(x * cellSizeW + 2, y * cellSizeH + 2, cellSizeW - 4, cellSizeH - 4);
	}

	public void drawRemain(Graphics g) {
		for (Location pos : remain.keySet()) {
			for (int[] dir : remain.get(pos)) {
				Polygon p = getTriangle(new int[] { pos.x, pos.y },  dir);
				g.setColor(Color.yellow);
				g.fillPolygon(p);
				g.setColor(Color.black);
				g.drawPolygon(p);
			}
		}
	}

	public void drawCount(Graphics g) {
		int[][] count = ((ArenaModel) model).count;
		for (int i = 0; i < count.length; i++) {
			for (int j = 0; j < count[i].length; j++) {
				if (count[i][j] > 0) {
					String s = String.valueOf(count[i][j]);
					g.setFont(defaultFont);
					FontMetrics metrics = g.getFontMetrics();
					int width = metrics.stringWidth(s);
					int height = metrics.getHeight();
					g.drawString(s, i * cellSizeW + width, j * cellSizeH + height);
				}
			}
		}
	}

	private Polygon getTriangle(int[] pos, int[] dir) {
		Polygon p = new Polygon();
		int x = pos[0];
		int y = pos[1];
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
		return p;
	}

}
