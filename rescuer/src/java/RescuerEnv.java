
/**
 * Environment code for project rescuer
 */

import java.util.logging.Logger;

import jason.asSyntax.Structure;
import jason.environment.Environment;

public class RescuerEnv extends Environment {

	private Logger logger = Logger.getLogger("rescuer." + RescuerEnv.class.getName());

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
		if (true) {
			informAgsEnvironmentChanged();
		}
		return true;
	}

}
