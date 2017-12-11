package group11;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RemotePC {

	private Robot robot;

	private ServerSocket server;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

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

	public synchronized void sendResult(String method, String result) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(method);
		sb.append("&");
		sb.append(result);
		out.writeUTF(sb.toString());
		out.flush();
	}

	public String getMethod(String line) {
		String[] parts = line.split("&");
		return parts[0];
	}

	public String[] getParams(String line) {
		String[] parts = line.split("&");
		return parts[1].split("#");
	}

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

	private String invokeUpdateRobotInfo(String[] params) {
		String[] loc = params[0].split(",");
		String[] ori = params[1].split(",");
		int[] pos = new int[] { Integer.parseInt(loc[0]), Integer.parseInt(loc[1]) };
		int[] dir = new int[] { Integer.parseInt(ori[0]), Integer.parseInt(ori[1]) };
		robot.updateRobotInfo(pos, dir);
		return "Done";
	}

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

	private String invokeDetectVictim() {
		return String.valueOf(robot.detectVictim());
	}

	private String invokeMoveToSide(String param) {
		robot.moveTo(param.toCharArray()[0]);
		return "Done";
	}

	private String invokeMoveToLoc(String param) {
		String[] axis = param.split(",");
		int[] target = new int[] { Integer.parseInt(axis[0]), Integer.parseInt(axis[1]) };
		robot.moveTo(target);
		return "Done";
	}

	private String invokeComplete() {
		robot.complete();
		return "Done";
	}

}
