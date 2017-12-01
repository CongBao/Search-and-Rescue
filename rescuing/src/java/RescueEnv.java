
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
import jason.asSyntax.parser.ParseException;
import jason.environment.Environment;
import jason.environment.grid.Location;

public class RescueEnv extends Environment {

	private Robot robot;

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
		initRemain();

		robot = new Emulator(model); // TODO change to real robot
		logger.info(robot.toString());
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
	public boolean executeAction(String agName, Structure action) {
		logger.info("Agent: " + agName + ", Action: " + action.getFunctor());
		try {
			switch (action.getFunctor()) {
			// actions of doctor
			case "localize": localize(action); break;
			case "find_path": findPath(action); break;
			case "determine": determine(action); break;
			// actions of scout
			case "detect": detect(action); break;
			case "move": move(action); break;
			case "travel": travel(action); break;
			case "check_vic": checkVic(action); break;
			default: break;
			}
		} catch (NoValueException nve) {
			nve.printStackTrace();
			return false;
		}
		robot.updateArenaInfo(model.getModelData());
		try {
			Thread.sleep(500);
		} catch (Exception e) {
		}
		informAgsEnvironmentChanged();
		return true;
	}

	private Map<Location, List<int[]>> getRemain(List<Term> pairs) throws NoValueException {
		removePerceptsByUnif(DOCTOR, Literal.parseLiteral("remain(_)"));
		logger.info("Get remain size: " + pairs.size());
		Map<Location, List<int[]>> remain = new HashMap<>();
		for (Term pair : pairs) {
			Literal l = (Literal) pair;
			Literal pos = (Literal) l.getTerm(0);
			Literal dir = (Literal) l.getTerm(1);
			int	x = (int) ((NumberTerm) pos.getTerm(0)).solve();
			int	y = (int) ((NumberTerm) pos.getTerm(1)).solve();
			int	d1 = (int) ((NumberTerm) dir.getTerm(0)).solve();
			int	d2 = (int) ((NumberTerm) dir.getTerm(1)).solve();
			Location loc = new Location(x, y);
			remain.putIfAbsent(loc, new LinkedList<>());
			remain.get(loc).add(new int[] { d1, d2 });
		}
		return remain;
	}

	private void putRemain(Map<Location, List<int[]>> remain) {
		List<Term> pairs = new LinkedList<>();
		for (Location pos : remain.keySet()) {
			NumberTerm x = ASSyntax.createNumber(pos.x);
			NumberTerm y = ASSyntax.createNumber(pos.y);
			Literal posL = ASSyntax.createLiteral("pos", x, y);
			for (int[] dir : remain.get(pos)) {
				NumberTerm d1 = ASSyntax.createNumber(dir[0]);
				NumberTerm d2 = ASSyntax.createNumber(dir[1]);
				Literal dirL = ASSyntax.createLiteral("dir", d1, d2);
				Literal pair = ASSyntax.createLiteral("pair", posL, dirL);
				pairs.add(pair);
			}
		}
		logger.info("Put remain size: " + pairs.size());
		ListTerm lt = ASSyntax.createList(pairs);
		addPercept(DOCTOR, ASSyntax.createLiteral("remain", lt));
	}

	private void putVictims() {
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

	private void localize(Structure action) throws NoValueException {
		Map<Location, List<int[]>> remain = getRemain((ListTerm) action.getTerm(4));
		boolean[] obsData = new boolean[3];
		for (int i = 0; i < 3; i++) {
			obsData[i] = 1 == (int) ((NumberTerm) action.getTerm(i)).solve();
		}
		int vicData = (int) ((NumberTerm) action.getTerm(3)).solve();
		remain = model.localize(remain, obsData, vicData);
		putRemain(remain);
	}

	private void findPath(Structure action) {
		List<Location> optimalPath = model.findOptimalPath();
		List<Term> path = new LinkedList<>();
		for (Location loc : optimalPath) {
			NumberTerm x = ASSyntax.createNumber(loc.x);
			NumberTerm y = ASSyntax.createNumber(loc.y);
			Literal l = ASSyntax.createLiteral("pos", x, y);
			path.add(l);
		}
		ListTerm lt = ASSyntax.createList(path);
		addPercept(DOCTOR, ASSyntax.createLiteral("total_path", lt));
	}

	private void determine(Structure action) throws NoValueException {
		int x = (int) ((NumberTerm) action.getTerm(0)).solve();
		int y = (int) ((NumberTerm) action.getTerm(1)).solve();
		int d1 = (int) ((NumberTerm) action.getTerm(2)).solve();
		int d2 = (int) ((NumberTerm) action.getTerm(3)).solve();
		model.remove(ArenaModel.POS_LOC, x, y);
		view.update();
		model.removeCheckedVic(new int[] { x, y }, new int[] { d1, d2 });
		putVictims();
		model.setAgPos(ArenaModel.SCOUT, x, y);
		addPercept(DOCTOR, ASSyntax.createLiteral("pos", action.getTerm(0), action.getTerm(1)));
	}

	private void detect(Structure action) {
		boolean[] obsData = robot.detectObstacle();
		int vicData = robot.detectVictim();
		NumberTerm l = ASSyntax.createNumber(obsData[0] ? 1 : 0);
		NumberTerm r = ASSyntax.createNumber(obsData[1] ? 1 : 0);
		NumberTerm f = ASSyntax.createNumber(obsData[2] ? 1 : 0);
		NumberTerm v = ASSyntax.createNumber(vicData);
		removePerceptsByUnif(SCOUT, Literal.parseLiteral("data(_, _, _, _)"));
		addPercept(SCOUT, ASSyntax.createLiteral("data", l, r, f, v));
	}

	private void move(Structure action) throws NoValueException {
		Term left = null, right = null, front = null;
		try {
			left = ASSyntax.parseTerm("left");
			right = ASSyntax.parseTerm("right");
			front = ASSyntax.parseTerm("front");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		char side = 0;
		if (action.getTerm(0).equals(left)) {
			side = 'L';
		} else if (action.getTerm(0).equals(right)) {
			side = 'R';
		} else if (action.getTerm(0).equals(front)) {
			side = 'F';
		}
		robot.moveTo(side);
		logger.info(robot.toString());
		for (Map<Integer, List<Character>> record : model.encounters) {
			record.values().iterator().next().add(side);
		}
		Map<Location, List<int[]>> remain = getRemain((ListTerm) action.getTerm(1));
		remain = model.updateRemain(remain, side);
		putRemain(remain);
	}

	private void travel(Structure action) throws NoValueException {
		int x = (int) ((NumberTerm) action.getTerm(0)).solve();
		int y = (int) ((NumberTerm) action.getTerm(1)).solve();
		Location target = new Location(x, y);
		model.travelTo(target);
		robot.moveTo(target);
		logger.info(robot.toString());
	}

	private void checkVic(Structure action) throws NoValueException {
		if (action.getArity() == 2) {
			int x = (int) ((NumberTerm) action.getTerm(0)).solve();
			int y = (int) ((NumberTerm) action.getTerm(1)).solve();
			int vic = robot.detectVictim();
			model.checkAndRescue(new Location(x, y), vic);
		} else if (action.getArity() == 1) {
			int v = (int) ((NumberTerm) action.getTerm(0)).solve();
			model.checkAndRescue(v);
		}
	}

}
