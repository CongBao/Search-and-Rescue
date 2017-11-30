import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import jason.environment.grid.GridWorldView;

public class ArenaView extends GridWorldView {

	private static final long serialVersionUID = 1L;

	public ArenaView(ArenaModel model) {
		super(model, "Arena Model", 900);
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

}
