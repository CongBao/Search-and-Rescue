package group11;

import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.navigation.MovePilot;

public class Scout implements Robot {

	private Arena arena;

	private PilotRobot robot;
	private MovePilot pilot;

	private GraphicsLCD lcd;

	public Scout() {
		arena = new Arena();

		robot = new PilotRobot();
		pilot = robot.getPilot();

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

	@Override
	public void updateArenaInfo(int[][] data) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean[] detectObstacle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int detectVictim() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveTo(char side) {
		// TODO Auto-generated method stub
	}

	@Override
	public void moveTo(int[] pos) {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args) {
		Scout scout = new Scout();
		scout.lcd.drawString("Press any button to start", 0, 0, 0);
		Button.waitForAnyPress();
		scout.lcd.clear();
	}

}
