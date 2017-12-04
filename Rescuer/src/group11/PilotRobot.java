package group11;

import lejos.hardware.Brick;
import lejos.hardware.BrickFinder;
import lejos.hardware.motor.Motor;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;

public class PilotRobot {

	private EV3TouchSensor leftBump, rightBump;
	private EV3UltrasonicSensor usSensor;
	private EV3ColorSensor cSensor;
	private SampleProvider leftSP, rightSP, distSP, colourSP;
	private float[] leftSample, rightSample, distSample, colourSample;
	private MovePilot pilot;

	public PilotRobot() {
		Brick myEV3 = BrickFinder.getDefault();

		leftBump = new EV3TouchSensor(myEV3.getPort("S1"));
		rightBump = new EV3TouchSensor(myEV3.getPort("S4"));
		usSensor = new EV3UltrasonicSensor(myEV3.getPort("S3"));
		cSensor = new EV3ColorSensor(myEV3.getPort("S2"));

		leftSP = leftBump.getTouchMode();
		rightSP = rightBump.getTouchMode();
		distSP = usSensor.getDistanceMode();
		colourSP = cSensor.getRGBMode();

		leftSample = new float[leftSP.sampleSize()]; // Size is 1
		rightSample = new float[rightSP.sampleSize()]; // Size is 1
		distSample = new float[distSP.sampleSize()]; // Size is 1
		colourSample = new float[colourSP.sampleSize()]; // Size is 1 for Red Mode, 3 for RGB Mode

		Wheel leftWheel = WheeledChassis.modelWheel(Motor.B, 3.3).offset(-9.65);
		Wheel rightWheel = WheeledChassis.modelWheel(Motor.D, 3.3).offset(9.65);
		Chassis myChassis = new WheeledChassis(new Wheel[] { leftWheel, rightWheel }, WheeledChassis.TYPE_DIFFERENTIAL);

		pilot = new MovePilot(myChassis);
		pilot.setAngularSpeed(pilot.getMaxAngularSpeed() * 0.5);
	}

	public synchronized void closeRobot() {
		leftBump.close();
		rightBump.close();
		usSensor.close();
		cSensor.close();
	}

	public boolean isLeftBumpPressed() {
		leftSP.fetchSample(leftSample, 0);
		return (leftSample[0] == 1.0);
	}

	public boolean isRightBumpPressed() {
		rightSP.fetchSample(rightSample, 0);
		return (rightSample[0] == 1.0);
	}

	public float getDistance() {
		distSP.fetchSample(distSample, 0);
		return distSample[0] * 100; // meter -> centimeter
	}

	public float[] getColour() {
		colourSP.fetchSample(colourSample, 0);
		return colourSample;
	}

	public MovePilot getPilot() {
		return pilot;
	}
}