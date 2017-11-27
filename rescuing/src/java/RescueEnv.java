
/**
 * Environment code for project rescuer
 */

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
		initRemain();
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

	public void initRemain() {
		List<Literal> remainCell = new LinkedList<>();
		for (int i = 0; i < model.getWidth(); i++) {
			for (int j = 0; j < model.getHeight(); j++) {
				if (model.isFreeOfObstacle(i, j)) {
					NumberTerm x = ASSyntax.createNumber(i);
					NumberTerm y = ASSyntax.createNumber(j);
					Literal l = ASSyntax.createLiteral("pos", x, y);
					remainCell.add(l);
				}
			}
		}
		List<Literal> dirs = new LinkedList<>();
		NumberTerm zero = ASSyntax.createNumber(0);
		NumberTerm one = ASSyntax.createNumber(1);
		NumberTerm ngone = ASSyntax.createNumber(-1);
		dirs.add(ASSyntax.createLiteral("dir", ngone, zero));
		dirs.add(ASSyntax.createLiteral("dir", one, zero));
		dirs.add(ASSyntax.createLiteral("dir", zero, ngone));
		dirs.add(ASSyntax.createLiteral("dir", zero, one));
		List<Term> pairs = new LinkedList<>();
		for (Literal pos : remainCell) {
			for (Literal dir : dirs) {
				pairs.add(ASSyntax.createLiteral("pair", pos, dir));
			}
		}
		ListTerm lt = ASSyntax.createList(pairs);
		addPercept(DOCTOR, ASSyntax.createLiteral("remain", lt));
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		logger.info("Agent: " + agName + ", Action: " + action);
		if (action.getFunctor().equals("localize")) {
			Map<Location, List<int[]>> remain = new HashMap<>();
			List<Term> pairs = (ListTerm) action.getTerm(0);
			for (Term pair : pairs) {
				Literal l = (Literal) pair;
				Literal pos = (Literal) l.getTerm(0);
				Literal dir = (Literal) l.getTerm(1);
				int x = 0, y = 0, d1 = 0, d2 = 0;
				try {
					x = (int) ((NumberTerm) pos.getTerm(0)).solve();
					y = (int) ((NumberTerm) pos.getTerm(1)).solve();
					d1 = (int) ((NumberTerm) dir.getTerm(0)).solve();
					d2 = (int) ((NumberTerm) dir.getTerm(1)).solve();
				} catch (NoValueException e) {
					e.printStackTrace();
				}
				Location loc = new Location(x, y);
				remain.putIfAbsent(loc, new LinkedList<>());
				remain.get(loc).add(new int[] { d1, d2 });
			}
			System.out.println(remain.size());
			remain = model.localize(remain);
			System.out.println(remain.size());
			/*Location loc = new Location(1, 2);
			NumberTerm x = ASSyntax.createNumber(loc.x);
			NumberTerm y = ASSyntax.createNumber(loc.y);
			addPercept(SCOUT, ASSyntax.createLiteral("pos", x, y));
			model.setAgPos(ArenaModel.SCOUT, loc);*/
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
