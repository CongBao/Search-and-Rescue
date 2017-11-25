
/**
 * Environment code for project rescuer
 */

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import jason.NoValueException;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTerm;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import jason.environment.grid.Location;

public class RescueEnv extends Environment {

	private Logger logger = Logger.getLogger("rescuing." + RescueEnv.class.getName());

	private ArenaModel model;
	private ArenaView view;

	@Override
	public void init(String[] args) {
		super.init(args);
		model = new ArenaModel();
		view = new ArenaView(model);
		model.setView(view);
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		logger.info("Agent: " + agName + ", Action: " + action);
		if (action.getFunctor().equals("check")) {
			// TODO find where is the robot
			int x = 1, y = 1; // example
			NumberTerm X = ASSyntax.createNumber(x);
			NumberTerm Y = ASSyntax.createNumber(y);
			addPercept("scout", ASSyntax.createLiteral("pos", X, Y));
			model.setAgPos(ArenaModel.SCOUT, x, y);
		} else if (action.getFunctor().equals("find_path")) {
			// TODO find a path with minimal total cost
			List<Location> path = new AStar(model).findPath(new Location(1, 1), new Location(1, 5)); // example
			List<Term> pathPos = new ArrayList<>();
			for (Location loc : path) {
				NumberTerm x = ASSyntax.createNumber(loc.x);
				NumberTerm y = ASSyntax.createNumber(loc.y);
				Literal l = ASSyntax.createLiteral("pos", x, y);
				pathPos.add(l);
			}
			ListTerm lt = ASSyntax.createList(pathPos);
			addPercept(ASSyntax.createLiteral("path", lt));
		} else if (action.getFunctor().equals("travel")) {
			// TODO travel to the given cell
			try {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.travelTo(new Location(x, y));
			} catch (NoValueException e) {
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		informAgsEnvironmentChanged();
		return true;
	}

}
