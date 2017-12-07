package group11;

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import lejos.hardware.Button;
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

	public void drawNeighbor(boolean[] obsData) {
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
	public float scan(int total, int sample, long delay) {
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
	 * Scan for the given direction, in ('L', 'R', 'F'). This method will update
	 * arena info.
	 *
	 * @param dir
	 *            the direction to scan
	 * @return the distance
	 */
	public float scanFor(char dir) {
		float dis = 0.0f;
		switch (dir) {
		case 'L':
			Motor.C.rotate(90);
			dis = scan(9, 5, 100);
			Motor.C.rotate(-90);
			break;
		case 'R':
			Motor.C.rotate(-90);
			dis = scan(9, 5, 100);
			Motor.C.rotate(90);
			break;
		case 'F':
			dis = scan(9, 5, 100);
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
			arena.setAgtPos(new int[] { pos[0] + dir[0] * (forth ? 1 : -1), pos[1] + dir[1] * (forth ? 1 : -1)});
		}
		travelWithBlackLine(forth);
	}

	/**
	 * Travel with the help of black line.
	 *
	 * @param forth
	 *            whether move forth or not
	 */
	public void travelWithBlackLine(boolean forth) {
		Point start = pose.getPose().getLocation();
		float distance = 0.0f;
		if (forth) {
			pilot.forward();
		} else {
			pilot.backward();
		}
		while (true) {
			float[] colorData = detectRGB(5, 3, 10);
			if (colorData[0] < 0.05 && colorData[1] < 0.05 && colorData[2] < 0.05) {
				pilot.stop();
				distance = pose.getPose().distanceTo(start);
				break;
			}
		}
		if (distance > 0.85 * Arena.UNIT_DEPTH) {
			distance *= 0.85;
		}
		if (distance < 0.15 * Arena.UNIT_WIDTH) {
			distance /= 0.85;
		}
		pilot.travel(distance * (forth ? 1 : -1) * 0.9);
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
	}

	@Override
	public boolean[] detectObstacle() {
		final double threshold = 0.4 * (Arena.UNIT_DEPTH + Arena.UNIT_WIDTH);
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
		drawNeighbor(obsData);
		return obsData;
	}

	/*
	 * Tested RGB data
	 * R 0.275, 0.049, 0.032
	 * G 0.052, 0.108, 0.055
	 * B 0.036, 0.124, 0.216
	 * W 0.276, 0.226, 0.237
	 * B 0.024, 0.026, 0.024
	 */
	@Override
	public int detectVictim() {
		float[] colorData = detectRGB(9, 5, 100);
		if (colorData[0] > 0.2 && colorData[1] > 0.2 && colorData[2] > 0.2) {
			return Arena.CLEAN;
		} else if (colorData[0] > 0.2 && colorData[1] < 0.1 && colorData[2] < 0.1) {
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
		switch (side) {
		case 'L':
			pilot.rotate(-90);
			if (determined) {
				int[] dir = arena.getAgtDir();
				arena.setAgtDir(new int[] { dir[1], -dir[0] });
			}
			break;
		case 'R':
			pilot.rotate(90);
			if (determined) {
				int[] dir = arena.getAgtDir();
				arena.setAgtDir(new int[] { -dir[1], dir[0] });
			}
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
		scout.lcd.drawString("Waiting for PC connecting...", 0, 0, 0);
		RemotePC remote = new RemotePC(scout, 10000);
		scout.lcd.clear();
		try {
			remote.listen();
		} catch (EOFException eofe) {
			System.out.println("Complete.");
			remote.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
