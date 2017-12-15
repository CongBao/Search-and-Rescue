package group11;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A class represents the remote computer.
 *
 * @author Cong Bao
 * @author Chaoyi Han
 * @author Samuel David Brundell
 */
public class RemotePC {

	private Robot robot;

	private ServerSocket server;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	/**
	 * Construct the remote computer with the {@link Robot} to control and the port.
	 *
	 * @param robot
	 *            the {@link Robot} to control
	 * @param port
	 *            the port to connect
	 */
	public RemotePC(Robot robot, int port) {
		this.robot = robot;
		try {
			server = new ServerSocket(port);
			socket = server.accept();
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Close the remote connection.
	 */
	public synchronized void close() {
		if (socket.isClosed()) {
			return;
		}
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Listen for remote invocations.
	 *
	 * @throws IOException
	 *             exceptions in connections
	 * @throws EOFException
	 *             exception if remote computer close stream
	 */
	public void listen() throws IOException, EOFException {
		while (socket.isConnected() && !socket.isClosed()) {
			String request = in.readUTF();
			if (request != null) {
				String method = getMethod(request);
				String result = null;
				switch (method) {
				case "updateArenaInfo":
					result = invokeUpdateArenaInfo(getParams(request)[0]);
					break;
				case "updateRobotInfo":
					result = invokeUpdateRobotInfo(getParams(request));
					break;
				case "detectObstacle":
					result = invokeDetectObstacle();
					break;
				case "detectVictim":
					result = invokeDetectVictim();
					break;
				case "moveToSide":
					result = invokeMoveToSide(getParams(request)[0]);
					break;
				case "moveToLoc":
					result = invokeMoveToLoc(getParams(request)[0]);
					break;
				case "complete":
					result = invokeComplete();
					break;
				default:
					break;
				}
				if (result != null) {
					sendResult(method, result);
				}
			}
		}
	}

	/**
	 * Send the result to remote computer.
	 *
	 * @param method
	 *            the name of method
	 * @param result
	 *            the results
	 * @throws IOException
	 *             exception in connection
	 */
	public synchronized void sendResult(String method, String result) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(method);
		sb.append("&");
		sb.append(result);
		out.writeUTF(sb.toString());
		out.flush();
	}

	/**
	 * Get method name from encoded string.
	 *
	 * @param line
	 *            the string
	 * @return the name of method
	 */
	public String getMethod(String line) {
		String[] parts = line.split("&");
		return parts[0];
	}

	/**
	 * Get a list of parameters from encoded string.
	 *
	 * @param line
	 *            the string
	 * @return a array of parameters
	 */
	public String[] getParams(String line) {
		String[] parts = line.split("&");
		return parts[1].split("#");
	}

	// invoke Robot::updateArenaInfo
	private String invokeUpdateArenaInfo(String param) {
		int[][] map = new int[Arena.WIDTH + 2][Arena.DEPTH + 2];
		String[] xAxis = param.split(";");
		for (int i = 0; i < xAxis.length; i++) {
			String[] yAxis = xAxis[i].split(",");
			for (int j = 0; j < yAxis.length; j++) {
				map[i][j] = Integer.parseInt(yAxis[j]);
			}
		}
		robot.updateArenaInfo(map);
		return "Done";
	}

	// invoke Robot::updateRobotInfo
	private String invokeUpdateRobotInfo(String[] params) {
		String[] loc = params[0].split(",");
		String[] ori = params[1].split(",");
		int[] pos = new int[] { Integer.parseInt(loc[0]), Integer.parseInt(loc[1]) };
		int[] dir = new int[] { Integer.parseInt(ori[0]), Integer.parseInt(ori[1]) };
		robot.updateRobotInfo(pos, dir);
		return "Done";
	}

	// invoke Robot::detectObstacle
	private String invokeDetectObstacle() {
		boolean[] obsData = robot.detectObstacle();
		StringBuilder sb = new StringBuilder();
		for (boolean obs : obsData) {
			sb.append(obs);
			sb.append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	// invoke Robot::detectVictim
	private String invokeDetectVictim() {
		return String.valueOf(robot.detectVictim());
	}

	// invoke Robot::moveTo
	private String invokeMoveToSide(String param) {
		robot.moveTo(param.toCharArray()[0]);
		return "Done";
	}

	// invoke Robot::moveTo
	private String invokeMoveToLoc(String param) {
		String[] axis = param.split(",");
		int[] target = new int[] { Integer.parseInt(axis[0]), Integer.parseInt(axis[1]) };
		robot.moveTo(target);
		return "Done";
	}

	// invoke Robot::complete
	private String invokeComplete() {
		robot.complete();
		return "Done";
	}

}
