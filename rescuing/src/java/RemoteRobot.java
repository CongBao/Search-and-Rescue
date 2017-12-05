import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import jason.environment.grid.Location;

public class RemoteRobot implements Robot {

	private ServerSocket server;
	private Socket socket;
	private DataOutputStream out;
	private DataInputStream in;

	private Logger logger = Logger.getLogger("rescuing." + RescueEnv.class.getName());

	public RemoteRobot(int port) {
		try {
			server = new ServerSocket(port);
			logger.info("Waiting for remote robot connecting...");
			socket = server.accept();
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void closeServer() {
		if (server.isClosed()) {
			return;
		}
		try {
			socket.close();
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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

	public String getMethod(String line) {
		String[] parts = line.split("&");
		return parts[0];
	}

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

}
