package group11;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.navigation.MovePilot;
import lejos.robotics.Color;

public class Scout implements Robot {

	private Arena arena;

	private PilotRobot robot;
	private MovePilot pilot;

	private GraphicsLCD lcd;

	private boolean firstDetect = true;

	public Scout() {
		arena = new Arena();

		robot = new PilotRobot();
		pilot = robot.getPilot();
		
		RobotConnector rc = new RobotConnector(this, pilot);
		rc.start(); //Ready for commands from the host

		lcd = LocalEV3.get().getGraphicsLCD();

		// listen for the ESCAPE key pressing
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					if (Button.ESCAPE.isDown()) {
						robot.closeRobot();
						System.exit(0);
					}
				}
			}
		}).start();
	}

	/**
	 * Scan for distance.
	 *
	 * @param total
	 *            number of scans
	 * @param sample
	 *            number of valid scans
	 * @param delay
	 *            delay between every two scans
	 * @return mean of valid scans
	 */
	public float scan(int total, int sample, int delay) {
		float[] data = new float[total];
		for (int i = 0; i < total; i++) {
			data[i] = robot.getDistance();
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			if (Float.isInfinite(data[i])) {
				data[i] = (float) Arena.ARENA_DEPTH;
			}
		}
		Arrays.sort(data);
		float sum = 0;
		int diff = total - sample;
		int min = diff / 2;
		int max = diff % 2 == 0 ? total - min : total - min - 1;
		for (int i = min; i < max; i++) {
			sum += data[i];
		}
		return sum / sample;
	}

	/**
	 * Scan for the given direction, in ('L', 'R', 'F').
	 * This method will update arena info.
	 *
	 * @param dir
	 *            the direction to scan
	 * @return the distance
	 */
	public float scanFor(char dir) {
		float dis = 0.0f;
		switch (dir) {
		case 'L':
			Motor.A.rotate(90);
			dis = scan(7, 5, 100);
			Motor.A.rotate(-90);
			break;
		case 'R':
			Motor.A.rotate(-90);
			dis = scan(7, 5, 100);
			Motor.A.rotate(90);
			break;
		case 'F':
			dis = scan(7, 5, 100);
			break;
		default:
			break;
		}
		return dis;
	}

	public float[] scanAround() {
		float[] dis = new float[3];
		dis[0] = scanFor('L');
		dis[1] = scanFor('R');
		dis[2] = scanFor('F');
		return dis;
	}

	/**
	 * Travel 1 grid unit
	 *
	 * @param forth
	 *            specifies forward direction (true) or backward
	 */
	public void travelAnUnit(boolean forth) {
		// Move forward/backward until we reach the black grid boundary
		// Then move forward half a grid cell, or backward ? a grid cell
		if(forth)
			pilot.getPilot().forward();
		else
			pilot.getPilot().backward();
		
		while(pilot.getColour() != Color.BLACK){
			// Wait until we reach the boundary...
		}
		
		pilot.getPilot().stop();
		
		// Now move forward/backward half a cell (depends on heading)
		// TODO: width/depth depending on heading, exact distances to move,
		//       needs testing with robot
		
		double distance;

		if(forth)
			distance = Arena.UNIT_WIDTH / 2;
		else
			distance = -(Arena.UNIT_WIDTH / 4);
		
		pilot.getPilot().travel(distance);
		
		while(pilot.getPilot().isMoving()){
			// Wait for completion
		}
		
		pilot.getPilot().stop();
	}

	@Override
	public void updateArenaInfo(int[][] data) {
		arena.setMap(data);
	}

	@Override
	public void updateRobotInfo(int[] pos, int[] dir) {
		arena.setAgtPos(pos);
		arena.setAgtDir(dir);
	}

	@Override
	public boolean[] detectObstacle() {
		final double threshold = 0.3 * (Arena.UNIT_DEPTH + Arena.UNIT_WIDTH);
		boolean[] obsData = new boolean[4];
		for (int i = 0; i < 3; i++) {
			obsData[i] = scanAround()[i] < threshold;
		}
		if (firstDetect) {
			pilot.rotate(-90); // turn left
			obsData[3] = scanFor('L') < threshold;
			firstDetect = false;
			pilot.rotate(90); // turn right
		} else {
			obsData[3] = false;
		}
		return obsData;
	}

	/**
	 * Use colour sensor to detect victim in current cell
	 *
	 * @return severity ID for current victim
	 */
	@Override
	public int detectVictim() {
		int colour = pilot.getColour();
		
		switch(colour){
			case Color.RED:
				return Arena.VIC_CRI;
				
			case Color.BLUE:
				return Arena.VIC_SER;
				
			case Color.GREEN:
				return Arena.VIC_MIN;
				
			default:
				return Arena.EMPTY;
		}
	}

	@Override
	public void moveTo(char side) {
		switch (side) {
		case 'L':
			pilot.rotate(-90);
			break;
		case 'R':
			pilot.rotate(90);
			break;
		case 'F':
		case 'B':
		default:
			break;
		}
		switch (side) {
		case 'L':
		case 'R':
		case 'F':
			travelAnUnit(true);
			break;
		case 'B':
			travelAnUnit(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void moveTo(int[] target) {
		int[] pos = arena.getAgtPos();
		int[] dir = arena.getAgtDir();
		if (Arrays.equals(pos, Arena.UNKNOWN) || Arrays.equals(dir, Arena.UNKNOWN)) {
			return;
		}
		int[] to = new int[] { target[0] - pos[0], target[1] - pos[1] };
		if (Arrays.equals(dir, to)) {
			moveTo('F');
			return;
		}
		int sin = dir[0] * to[1] - dir[1] * to[0];
		switch (sin) {
		case -1:
			moveTo('L');
			break;
		case 1:
			moveTo('R');
			break;
		case 0:
			moveTo('B');
			break;
		default:
			break;
		}
	}

	public static void main(String[] args) {
		Scout scout = new Scout();
		scout.lcd.drawString("Press any button to start", 0, 0, 0);
		Button.waitForAnyPress();
		scout.lcd.clear();
	}

}
