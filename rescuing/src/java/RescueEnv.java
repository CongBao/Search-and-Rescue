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

/**
 * Environment code for project rescuer.
 *
 * @author Cong Bao
 */
public class RescueEnv extends Environment {

	public static final String DOCTOR = "doctor";
	public static final String SCOUT = "scout";

	private Logger logger = Logger.getLogger("rescuing." + RescueEnv.class.getName());

	private ArenaModel model;
	private ArenaView view;

	private Robot robot;

	@Override
	public void init(String[] args) {
		super.init(args);
		model = new ArenaModel();
		view = new ArenaView(model);
		model.setView(view);
		initRemain();

		robot = new RemoteRobot("127.0.0.1", 10000); // TODO
	}

	@Override
	public void stop() {
		((RemoteRobot) robot).close();
		super.stop();
	}

	/**
	 * Initialize the remain percept with all possible locations and headings.
	 */
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

	/**
	 * Get a percept from agents.
	 *
	 * @param agName
	 *            agent name
	 * @param percept
	 *            the percept name
	 * @return a {@link Literal} if exists, otherwise null
	 */
	public Literal getPercept(String agName, String percept) {
		for (Literal l : consultPercepts(agName)) {
			if (l.getFunctor().equals(percept)) {
				return l;
			}
		}
		return null;
	}

	/**
	 * Create a time stamp to prevent the same percept.
	 *
	 * @return a {@link NumberTerm} of time stamp
	 */
	public NumberTerm getTimeStamp() {
		return ASSyntax.createNumber(System.currentTimeMillis());
	}

	@Override
	public boolean executeAction(String agName, Structure action) {
		logger.info("Agent: " + agName + ", Action: " + action.getFunctor());
		try {
			switch (action.getFunctor()) {
			// actions of doctor
			case "localize": localize(action); break;
			case "determine": determine(action); break;
			case "choose": choose(action); break;
			case "find_path": findPath(action); break;
			case "complete": complete(action); break;
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

	// get the remain percept from doctor
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

	// put new remain percept to doctor
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

	// put the victim percept to doctor
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

	// reduce the possible positions in remain percept
	private void localize(Structure action) throws NoValueException {
		Map<Location, List<int[]>> remain = getRemain((ListTerm) action.getTerm(5));
		boolean[] obsData = new boolean[4];
		for (int i = 0; i < 4; i++) {
			obsData[i] = 1 == (int) ((NumberTerm) action.getTerm(i)).solve();
		}
		int vicData = (int) ((NumberTerm) action.getTerm(4)).solve();
		remain = model.localize(remain, obsData, vicData > 0 ? ArenaModel.VIC_POS : 0);
		putRemain(remain);
	}

	// once doctor find where scout is, prepare for path finding
	private void determine(Structure action) throws NoValueException {
		int x = (int) ((NumberTerm) action.getTerm(0)).solve();
		int y = (int) ((NumberTerm) action.getTerm(1)).solve();
		int d1 = (int) ((NumberTerm) action.getTerm(2)).solve();
		int d2 = (int) ((NumberTerm) action.getTerm(3)).solve();
		int[] pos = new int[] { x, y };
		int[] dir = new int[] { d1, d2 };
		view.update();
		view.heading = dir;
		model.setAgPos(ArenaModel.SCOUT, x, y);
		model.remove(ArenaModel.POS_LOC, x, y);
		model.removeCheckedVic(pos, dir);
		model.addVisitedCount(pos, dir);
		robot.updateRobotInfo(new Location(x, y), dir);
		putVictims();
		addPercept(DOCTOR, ASSyntax.createLiteral("pos", action.getTerm(0), action.getTerm(1)));
	}

	// choose a side to further explore
	private void choose(Structure action) throws NoValueException {
		boolean[] obsData = new boolean[4];
		for (int i = 0; i < 4; i++) {
			obsData[i] = 1 == (int) ((NumberTerm) action.getTerm(i)).solve();
		}
		Map<Location, List<int[]>> remain = getRemain((ListTerm) action.getTerm(4));
		char side = model.chooseSide(remain, obsData);
		removePerceptsByUnif(DOCTOR, Literal.parseLiteral("choose(_, _)"));
		Term sideTerm = null;
		try {
			switch (side) {
			case 'L':
				sideTerm = ASSyntax.parseTerm("left");
				break;
			case 'R':
				sideTerm = ASSyntax.parseTerm("right");
				break;
			case 'F':
				sideTerm = ASSyntax.parseTerm("front");
				break;
			case 'B':
				sideTerm = ASSyntax.parseTerm("back");
				break;
			default:
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		addPercept(DOCTOR, ASSyntax.createLiteral("choose", sideTerm, action.getTerm(4)));
	}

	// find a total path passing all remaining possible victims
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

	// mission completes
	private void complete(Structure action) {
		robot.complete();
	}

	// detect obstacle and victim data from robot
	private void detect(Structure action) {
		boolean[] obsData = robot.detectObstacle();
		int vicData = robot.detectVictim();
		NumberTerm l = ASSyntax.createNumber(obsData[0] ? 1 : 0);
		NumberTerm r = ASSyntax.createNumber(obsData[1] ? 1 : 0);
		NumberTerm f = ASSyntax.createNumber(obsData[2] ? 1 : 0);
		NumberTerm b = ASSyntax.createNumber(obsData[3] ? 1 : 0);
		NumberTerm v = ASSyntax.createNumber(vicData);
		removePerceptsByUnif(SCOUT, Literal.parseLiteral("data(_, _, _, _, _, _)"));
		addPercept(SCOUT, ASSyntax.createLiteral("data", l, r, f, b, v, getTimeStamp()));
	}

	// move to a given side, in [left, right, front]
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
		for (Map<Integer, List<Character>> record : model.encounters) {
			record.values().iterator().next().add(side);
		}
		model.visited.add(side); // TODO
		Map<Location, List<int[]>> remain = getRemain((ListTerm) action.getTerm(1));
		remain = model.updateRemain(remain, side);
		putRemain(remain);
		removePerceptsByUnif(SCOUT, Literal.parseLiteral("arrive(_)"));
		addPercept(SCOUT, ASSyntax.createLiteral("arrive", getTimeStamp()));
	}

	// travel to a given location
	private void travel(Structure action) throws NoValueException {
		int x = (int) ((NumberTerm) action.getTerm(0)).solve();
		int y = (int) ((NumberTerm) action.getTerm(1)).solve();
		Location target = new Location(x, y);
		robot.moveTo(target);
		model.travelTo(target);
		removePerceptsByUnif(SCOUT, Literal.parseLiteral("at(_, _, _)"));
		addPercept(SCOUT, ASSyntax.createLiteral("at", action.getTerm(0), action.getTerm(1), getTimeStamp()));
	}

	// check whether there is a victim or not, if there is, rescue him
	private void checkVic(Structure action) throws NoValueException {
		int numRescued = 0;
		Literal rescued = getPercept(DOCTOR, "vic_rescued");
		if (rescued != null) {
			numRescued = (int) ((NumberTerm) rescued.getTerm(0)).solve();
			removePerceptsByUnif(DOCTOR, Literal.parseLiteral("vic_rescued(_)"));
		}
		int vic = 0;
		if (action.getArity() == 1) { // localization
			vic = (int) ((NumberTerm) action.getTerm(0)).solve();
			model.checkAndRescue(vic);
		} else if (action.getArity() == 2) { // path finding
			int x = (int) ((NumberTerm) action.getTerm(0)).solve();
			int y = (int) ((NumberTerm) action.getTerm(1)).solve();
			vic = robot.detectVictim();
			model.checkAndRescue(new Location(x, y), vic);
		}
		if (vic > ArenaModel.VIC_POS) {
			numRescued++;
		}
		NumberTerm num = ASSyntax.createNumber(numRescued);
		addPercept(DOCTOR, ASSyntax.createLiteral("vic_rescued", num));
	}

}
