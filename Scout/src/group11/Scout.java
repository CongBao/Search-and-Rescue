package group11;

import java.io.EOFException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.motor.Motor;
import lejos.robotics.geometry.Point;
import lejos.robotics.localization.OdometryPoseProvider;
import lejos.robotics.navigation.MovePilot;

public class Scout implements Robot {

	private Arena arena;

	private PilotRobot robot;
	private MovePilot pilot;
	private OdometryPoseProvider pose;

	private GraphicsLCD lcd;

	private boolean firstDetect = true;
	private boolean determined = false;

	private boolean[] obsData;

	public Scout() {
		arena = new Arena();

		robot = new PilotRobot();
		pilot = robot.getPilot();
		pose = new OdometryPoseProvider(pilot);

		lcd = LocalEV3.get().getGraphicsLCD();
		lcd.setFont(Font.getSmallFont());

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
	 * Draw surrounding obstacles on LCD screen.
	 */
	public void drawNeighbor() {
		if (determined) {
			return;
		}
		final int size = 30;
		final int center_x = lcd.getWidth() / 2;
		final int center_y = lcd.getHeight() / 2;
		lcd.clear();
		lcd.setFont(Font.getDefaultFont());
		lcd.drawRect(center_x - size / 2, center_y - size / 2, size, size); // center
		lcd.drawChar('X', center_x - 1, center_y - 1, 0);
		if (obsData[0]) {
			lcd.fillRect(center_x - 3 * size / 2, center_y - size / 2, size, size);
		} else {
			lcd.drawRect(center_x - 3 * size / 2, center_y - size / 2, size, size);
		}
		if (obsData[1]) {
			lcd.fillRect(center_x + size / 2, center_y - size / 2, size, size);
		} else {
			lcd.drawRect(center_x + size / 2, center_y - size / 2, size, size);
		}
		if (obsData[2]) {
			lcd.fillRect(center_x - size / 2, center_y - 3 * size / 2, size, size);
		} else {
			lcd.drawRect(center_x - size / 2, center_y - 3 * size / 2, size, size);
		}
		if (obsData[3]) {
			lcd.fillRect(center_x - size / 2, center_y + size / 2, size, size);
		} else {
			lcd.drawRect(center_x - size / 2, center_y + size / 2, size, size);
		}
	}

	/**
	 * Draw the map on LCD screen.
	 */
	public void drawMap() {
		if (!determined) {
			return;
		}
		final int size = 16;
		final int box_bias_x = 9;
		final int box_bias_y = 1;
		final int char_bias_x = 11;
		final int char_bias_y = 0;
		int[][] map = arena.getMap();
		lcd.clear();
		lcd.setFont(Font.getDefaultFont());
		for (int j = 0; j < map[0].length; j++) {
			for (int i = 0; i < map.length; i++) {
				if (map[i][j] == Arena.OBSTACLE) {
					lcd.fillRect(box_bias_x + size * i, box_bias_y + size * j, size, size);
				} else {
					lcd.drawRect(box_bias_x + size * i, box_bias_y + size * j, size, size);
				}
				if ((map[i][j] & Arena.VIC_CRI) != 0) {
					lcd.drawChar('R', char_bias_x + size * i, char_bias_y + size * j, 0);
				} else if ((map[i][j] & Arena.VIC_SER) != 0) {
					lcd.drawChar('B', char_bias_x + size * i, char_bias_y + size * j, 0);
				} else if ((map[i][j] & Arena.VIC_MIN) != 0) {
					lcd.drawChar('G', char_bias_x + size * i, char_bias_y + size * j, 0);
				} else if ((map[i][j] & Arena.VIC_POS) != 0) {
					lcd.drawChar('?', char_bias_x + size * i, char_bias_y + size * j, 0);
				}
				if ((map[i][j] & Arena.AGENT) != 0) {
					lcd.drawChar('X', char_bias_x + size * i, char_bias_y + size * j, 0);
				}
			}
		}
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
	 *
	 * @param dir
	 *            the direction to scan
	 * @param params
	 * 			  some optional parameters, in [total, sample, delay]:
	 *            total - number of scans, default 9;
	 *            sample - number of valid scans, default 5;
	 *            delay - delay between every two scans, default 100.
	 * @return the distance
	 */
	public float scanFor(char dir, int... params) {
		int[] args = new int[] { 20, 8, 50 };
		for (int i = 0; i < Math.min(args.length, params.length); i++) {
			args[i] = params[i];
		}
		final int angle = 95;
		float dis = 0.0f;
		switch (dir) {
		case 'L':
			Motor.C.rotate(angle);
			dis = scan(args[0], args[1], args[2]);
			Motor.C.rotate(-angle);
			break;
		case 'R':
			Motor.C.rotate(-angle);
			dis = scan(args[0], args[1], args[2]);
			Motor.C.rotate(angle);
			break;
		case 'F':
			dis = scan(args[0], args[1], args[2]);
			break;
		default:
			break;
		}
		return dis;
	}

	/**
	 * Scan around to detect the distance.
	 *
	 * @return distance data in [left, right, front]
	 */
	public float[] scanAround() {
		float[] dis = new float[3];
		dis[0] = scanFor('L');
		dis[1] = scanFor('R');
		dis[2] = scanFor('F');
		return dis;
	}

	/**
	 * Detect color data in RGB mode.
	 *
	 * @param total
	 *            number of scans
	 * @param sample
	 *            number of valid scans
	 * @param delay
	 *            delay between every two scans
	 * @return mean of valid scans in [R, G, B]
	 */
	public float[] detectRGB(int total, int sample, long delay) {
		float[] rData = new float[total];
		float[] gData = new float[total];
		float[] bData = new float[total];
		for (int i = 0; i < total; i++) {
			float[] data = robot.getColour();
			rData[i] = data[0];
			gData[i] = data[1];
			bData[i] = data[2];
			try {
				Thread.sleep(delay);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
		Arrays.sort(rData);
		Arrays.sort(gData);
		Arrays.sort(bData);
		float[] rgbData = new float[3];
		int diff = total - sample;
		int min = diff / 2;
		int max = diff % 2 == 0 ? total - min : total - min - 1;
		for (int i = min; i < max; i++) {
			rgbData[0] += rData[i];
			rgbData[1] += gData[i];
			rgbData[2] += bData[i];
		}
		for (int i = 0; i < 3; i++) {
			rgbData[i] /= sample;
		}
		return rgbData;
	}

	/**
	 * Travel an unit distance.
	 *
	 * @param forth
	 *            whether move forth or not
	 */
	public void travelAnUnit(boolean forth) {
		if (determined) {
			int[] pos = arena.getAgtPos();
			int[] dir = arena.getAgtDir();
			arena.setAgtPos(new int[] { pos[0] + dir[0] * (forth ? 1 : -1), pos[1] + dir[1] * (forth ? 1 : -1) });
		}
		travelWithBlackLine(forth);
	}

	/**
	 * Travel with the help of black line.
	 *
	 * @param forth
	 *            whether move forth or not
	 * @param params
	 *            some parameters to calculate logistic function, in [min, max, gradient]:
	 *            min - the lower bound rate of unit width of arena, default 0.15;
	 *            max - the upper bound rate of unit depth of arena, default 0.8;
	 *            gradient - the rate of slope of logistic function, default 0.16.
	 */
	public void travelWithBlackLine(boolean forth, double... params) {
		Point start = pose.getPose().getLocation();
		float distance = 0.0f;
		if (forth) {
			pilot.forward();
		} else {
			pilot.backward();
		}
		while (true) {
			float[] colorData = detectRGB(5, 3, 10);
			if (colorData[0] < 0.1 && colorData[1] < 0.1 && colorData[2] < 0.1) {
				pilot.stop();
				distance = pose.getPose().distanceTo(start);
				break;
			}
		}
		// logistic function
		// y = c / (1 + e^(-k * (x + b))) + d
		double [] args = new double[] { 0.04, 0.82, 0.15 }; // TODO (1, 2) 0.04, 0.82, 0.15 (3) 0.05, 0.8, 0.15
		for (int i = 0; i < Math.min(args.length, params.length); i++) {
			args[i] = params[i];
		}
		double b = -0.25 * (Arena.UNIT_DEPTH + Arena.UNIT_WIDTH);
		double c = args[1] * Arena.UNIT_DEPTH - args[0] * Arena.UNIT_WIDTH; // TODO (2, 3)
		//double c = args[1] * Arena.UNIT_WIDTH - args[0] * Arena.UNIT_WIDTH; // TODO (1)
		double d = args[0] * Arena.UNIT_WIDTH;
		double k = args[2];
		double next = distance + b;
		next *= -k;
		next = Math.pow(Math.E, next);
		next += 1;
		next = c / next;
		next += d;
		pilot.travel(next * (forth ? 1 : -1));
	}

	/**
	 * Adjust the padding if there is an obstacle nearby.
	 *
	 * @param side
	 *            the side obstacle locates, in ('L', 'R')
	 */
	public void adjustPadding(char side) {
		if (!determined) {
			return;
		}
		final double len = 7;
		double gap = 12.5;
		if (arena.getAgtDir()[0] == 0) { // N, S
			gap = 11.2; // TODO (1)10.5 (2)11.2 (3)14.5
		} else if (arena.getAgtDir()[1] == 0) { // W, E
			gap = 14.3; // TODO (1)10.5 (2)14.3 (3)15.5
		}
		pilot.travel(-1);
		double data = scanFor(side, 50, 10, 20);
		double diff = side == 'L' ? gap - data : data - gap;
		double degree = Math.toDegrees(Math.atan(diff / len));
		if (arena.isOccupied('F')) {
			pilot.rotate(-degree);
			pilot.travel(-Math.sqrt(diff * diff + len * len));
			pilot.rotate(degree);
			pilot.travel(len);
		} else {
			pilot.rotate(degree);
			pilot.travel(Math.sqrt(diff * diff + len * len));
			pilot.rotate(-degree);
			pilot.travel(-len);
		}
	}

	/**
	 * Adjust the orientation if there is an obstacle nearby.
	 *
	 * @param side
	 *            the side obstacle locates, in ('L', 'R')
	 */
	public void adjustOrientation(char side) {
		if (!determined) {
			return;
		}
		final double backward = arena.isOccupied('F') ? 6 : 0;
		final double forward = arena.isOccupied('F') ? 0 : 6;
		final double total = backward + forward;
		pilot.travel(-backward);
		double back = scanFor(side, 50, 10, 20);
		pilot.travel(total);
		double fore = scanFor(side, 50, 10, 20);
		pilot.travel(-forward);
		double diff = side == 'L' ? back - fore : fore - back;
		double degree = Math.toDegrees(Math.atan(diff / total));
		pilot.rotate(degree);
	}

	@Override
	public void updateArenaInfo(int[][] data) {
		arena.setMap(data);
		drawMap();
	}

	@Override
	public void updateRobotInfo(int[] pos, int[] dir) {
		arena.setAgtPos(pos);
		arena.setAgtDir(dir);
		determined = true;
		obsData = null;
	}

	@Override
	public boolean[] detectObstacle() {
		final double threshold = 0.45 * (Arena.UNIT_DEPTH + Arena.UNIT_WIDTH); // TODO (2, 3)
		//final double threshold = 0.45 * (Arena.UNIT_WIDTH + Arena.UNIT_WIDTH); // TODO (1)
		boolean[] obsData = new boolean[4];
		float[] disData = scanAround();
		for (int i = 0; i < 3; i++) {
			obsData[i] = disData[i] < threshold;
		}
		if (firstDetect) {
			pilot.rotate(-90); // turn left
			obsData[3] = scanFor('L') < threshold;
			firstDetect = false;
			pilot.rotate(90); // turn right
		} else {
			obsData[3] = false;
		}
		this.obsData = obsData;
		drawNeighbor();
		return obsData;
	}

	@Override
	public int detectVictim() {
		float[] colorData = detectRGB(9, 5, 20);
		if (colorData[0] > 0.2 && colorData[1] > 0.2 && colorData[2] > 0.2) {
			return Arena.CLEAN;
		} else if (colorData[0] > 0.15 && colorData[1] < 0.1 && colorData[2] < 0.1) {
			return Arena.VIC_CRI;
		} else if (colorData[0] < 0.1 && colorData[1] > 0.1 && colorData[2] > 0.2) {
			return Arena.VIC_SER;
		} else if (colorData[0] < 0.1 && colorData[1] > 0.1 && colorData[2] < 0.1) {
			return Arena.VIC_MIN;
		}
		return 0;
	}

	@Override
	public void moveTo(char side) {
		// calibrate
		switch (side) {
		case 'L':
			if (determined && arena.isOccupied('R')) {
				//adjustOrientation('R');
				adjustPadding('R');
			}
			break;
		case 'R':
			if (determined && arena.isOccupied('L')) {
				//adjustOrientation('L');
				adjustPadding('L');
			}
			break;
		default:
			break;
		}
		// rotate
		switch (side) {
		case 'L':
			pilot.travel(-2);
			pilot.rotate(-90);
			if (determined) {
				int[] dir = arena.getAgtDir();
				arena.setAgtDir(new int[] { dir[1], -dir[0] });
			}
			break;
		case 'R':
			pilot.travel(-2);
			pilot.rotate(90);
			if (determined) {
				int[] dir = arena.getAgtDir();
				arena.setAgtDir(new int[] { -dir[1], dir[0] });
			}
			break;
		default:
			break;
		}
		// travel
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
		if (!determined) {
			return;
		}
		int[] pos = arena.getAgtPos();
		int[] dir = arena.getAgtDir();
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

	@Override
	public void complete() {
		Sound.beepSequence();
		Sound.beepSequenceUp();
	}

	// test method
	public void testMove() {
		pilot.travel(30);
		pilot.rotate(90);
		pilot.travel(30);
		pilot.rotate(90);
		pilot.travel(30);
		pilot.rotate(90);
		pilot.travel(30);
		pilot.rotate(90);
	}

	// test method
	public void testColor() {
		DecimalFormat df = new DecimalFormat("####0.000");
		lcd.setFont(Font.getSmallFont());
		while (true) {
			lcd.clear();
			float[] colorData = detectRGB(9, 5, 20);
			lcd.drawString("Colour: [" + df.format(colorData[0]) + ", " + df.format(colorData[1]) + ", " + df.format(colorData[2]) + "]", 0, 50, 0);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// test method
	public void testDistance() {
		DecimalFormat df = new DecimalFormat("####0.000");
		lcd.setFont(Font.getSmallFont());
		while (true) {
			lcd.clear();
			float[] disData = scanAround();
			lcd.drawString("Dis: [" + df.format(disData[0]) + ", " + df.format(disData[1]) + ", " + df.format(disData[2]) + "]", 0, 50, 0);
			lcd.drawString("Press for next...", 0, 70, 0);
			Button.waitForAnyPress();
		}
	}

	// test method
	public static void testLogisticFunc(double distance, double... params) {
		// logistic function
		// y = c / (1 + e^(-k * (x + b))) + d
		double [] args = new double[] { 0.15, 0.8, 0.14 };
		for (int i = 0; i < Math.min(args.length, params.length); i++) {
			args[i] = params[i];
		}
		double b = -0.25 * (Arena.UNIT_DEPTH + Arena.UNIT_WIDTH);
		double c = args[1] * Arena.UNIT_DEPTH - args[0] * Arena.UNIT_WIDTH;
		double d = args[0] * Arena.UNIT_WIDTH;
		double k = args[2];
		double next = distance + b;
		next *= -k;
		next = Math.pow(Math.E, next);
		next += 1;
		next = c / next;
		next += d;
		System.out.println(next);
	}

	public static void main(String[] args) {
		Scout scout = new Scout();
		scout.lcd.drawString("Press any button to start", 0, 0, 0);
		Button.waitForAnyPress();
		scout.lcd.clear();
		scout.lcd.drawString("Waiting for PC connecting...", 0, 0, 0);
		RemotePC remote = new RemotePC(scout, 10000);
		scout.lcd.clear();
		try {
			remote.listen();
		} catch (EOFException eofe) {
			remote.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
