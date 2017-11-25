
/**
 * Environment code for project rescuer
 */

import java.util.LinkedList;
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

	public static final String DOCTOR = "doctor";
	public static final String SCOUT = "scout";

	private Logger logger = Logger.getLogger("rescuing." + RescueEnv.class.getName());

	private ArenaModel model;
	private ArenaView view;

	@Override
	public void init(String[] args) {
		super.init(args);
		model = new ArenaModel();
		view = new ArenaView(model);
		model.setView(view);
		initVictims();
	}

	public void initVictims() {
		List<Term> possibleVic = new LinkedList<>();
		for (Location loc : model.possibleVictims) {
			NumberTerm x = ASSyntax.createNumber(loc.x);
			NumberTerm y = ASSyntax.createNumber(loc.y);
			Literal l = ASSyntax.createLiteral("pos", x, y);
			possibleVic.add(l);
		}
		ListTerm lt = ASSyntax.createList(possibleVic);
		addPercept(DOCTOR, ASSyntax.createLiteral("vic_pos", lt));
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		logger.info("Agent: " + agName + ", Action: " + action);
		if (action.getFunctor().equals("localize")) {
			// TODO find where is the robot
			Location loc = model.localize();
			NumberTerm x = ASSyntax.createNumber(loc.x);
			NumberTerm y = ASSyntax.createNumber(loc.y);
			addPercept(SCOUT, ASSyntax.createLiteral("pos", x, y));
			model.setAgPos(ArenaModel.SCOUT, loc);
		} else if (action.getFunctor().equals("find_path")) {
			List<Location> optimalPath = model.findOptimalPath();
			List<Term> path = new LinkedList<>();
			for (Location loc : optimalPath) {
				NumberTerm x = ASSyntax.createNumber(loc.x);
				NumberTerm y = ASSyntax.createNumber(loc.y);
				Literal l = ASSyntax.createLiteral("pos", x, y);
				path.add(l);
			}
			ListTerm lt = ASSyntax.createList(path);
			addPercept(ASSyntax.createLiteral("total_path", lt));
		} else if (action.getFunctor().equals("travel")) {
			// TODO travel to the given cell
			try {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.travelTo(new Location(x, y));
			} catch (NoValueException e) {
				e.printStackTrace();
			}
		} else if (action.getFunctor().equals("check_vic")) {
			// TODO check the cell and rescue the victim if there is
			try {
				int x = (int) ((NumberTerm) action.getTerm(0)).solve();
				int y = (int) ((NumberTerm) action.getTerm(1)).solve();
				model.checkAndRescue(new Location(x, y));
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
