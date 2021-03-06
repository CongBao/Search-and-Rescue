import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Logger;

import jason.environment.grid.Location;

/**
 * A class represents the remote robot.
 *
 * @author Cong Bao
 * @author Chaoyi Han
 * @author Samuel David Brundell
 */
public class RemoteRobot implements Robot {

	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	private Logger logger = Logger.getLogger("rescuing." + RescueEnv.class.getName());

	/**
	 * Construct the remote robot with host and port.
	 *
	 * @param host
	 *            the IP address of remote robot
	 * @param port
	 *            the port of remote robot
	 */
	public RemoteRobot(String host, int port) {
		try {
			socket = new Socket(host, port);
			logger.info("Connected.");
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
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
	 * Invoke a remote method and wait for returning.
	 *
	 * @param method
	 *            the name of method
	 * @param params
	 *            optional parameters
	 * @return the return value of the method
	 * @throws IOException
	 *             exceptions in connections
	 */
	public synchronized String invoke(String method, String... params) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(method);
		sb.append("&");
		for (String arg : params) {
			sb.append(arg);
			sb.append("#");
		}
		sb.deleteCharAt(sb.length() - 1);
		out.writeUTF(sb.toString());
		out.flush();
		String result = null;
		while (socket.isConnected() && !socket.isClosed()) {
			result = in.readUTF();
			if (getMethod(result).equals(method)) {
				break;
			}
		}
		return getResult(result);
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
	 * Get the return value from encoded string.
	 *
	 * @param line
	 *            the string
	 * @return the return value of method
	 */
	public String getResult(String line) {
		String[] parts = line.split("&");
		return parts[1];
	}

	@Override
	public void updateArenaInfo(int[][] data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				sb.append(data[i][j]);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(";");
		}
		sb.deleteCharAt(sb.length() - 1);
		try {
			invoke("updateArenaInfo", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateRobotInfo(Location loc, int[] dir) {
		try {
			invoke("updateRobotInfo", loc.x + "," + loc.y, dir[0] + "," + dir[1]);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean[] detectObstacle() {
		String result = null;
		try {
			result = invoke("detectObstacle");
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean[] obsData = new boolean[4];
		if (result != null) {
			String[] parts = result.split(",");
			for (int i = 0; i < 4; i++) {
				obsData[i] = Boolean.parseBoolean(parts[i]);
			}
		}
		return obsData;
	}

	@Override
	public int detectVictim() {
		String result = null;
		try {
			result = invoke("detectVictim");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (result != null) {
			return Integer.parseInt(result);
		}
		return 0;
	}

	@Override
	public void moveTo(char side) {
		try {
			invoke("moveToSide", String.valueOf(side));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void moveTo(Location loc) {
		try {
			invoke("moveToLoc", loc.x + "," + loc.y);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void complete() {
		try {
			invoke("complete");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
