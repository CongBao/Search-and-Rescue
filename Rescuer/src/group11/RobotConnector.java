package group11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson;
import jason.environment.grid.Location;
import lejos.hardware.motor.Motor;

/**
 * Robot Connector
 * 
 * HOW TO USE:
 * RobotConnector rc = new RobotConnector(robot, pilot);
 * rc.start(); //all done
 * 
 * @author Chaoyi Han
 */
public class RobotConnector extends Thread {
    
    private PilotRobot pilot;
    private Robot robot;
    private Gson gson;
    
    /**
     * 
     * @param robotInput
     * @param pilotInput 
     */
    public RobotConnector(Robot robotInput, PilotRobot pilotInput) {
        robot = robotInput;
        pilot = pilotInput;
        gson = new Gson();
    }
    
    /**
     * The runnable
     */
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(21900);
            Socket client;
            PrintStream sendOut;
            BufferedReader recieved;
            while(true) {
                client = server.accept();
                sendOut = new PrintStream(client.getOutputStream());
                recieved =  new BufferedReader(new InputStreamReader(client.getInputStream()));
                String str = recieved.readLine();
                String[] parsed = str.split("-cmd->");
                switch(parsed[0]) {
                    case "updateArenaInfo":
                        robot.updateArenaInfo(gson.fromJson(parsed[1], int[][].class));
                        sendOut.println("");
                        break;
                    case "updateRobotInfo":
                        String[] params = parsed[1].split("-param->");
                        robot.updateRobotInfo(gson.fromJson(params[0], Location.class), gson.fromJson(params[1], int[].class));
                        sendOut.println("");
                        break;
                    case "detectObstacle":
                        sendOut.println(gson.toJson(robot.detectObstacle()));
                        break;
                    case "detectVictim":
                        sendOut.println(String.valueOf(robot.detectObstacle()));
                        break;
                    case "moveToSide":
                        robot.moveTo(parsed[1].charAt(0));
                        sendOut.println("");
                        break;
                    case "moveToLoc":
                        robot.moveTo(gson.fromJson(parsed[1], Location.class));
                        sendOut.println("");
                        break;
                    case "GO":
                        pilot.getPilot().travel(Double.parseDouble(parsed[1]));
                        while(pilot.getPilot().isMoving()) {
                            Thread.yield();
                        }
                        sendOut.println("");
                        break;
                    case "TURN":
                        pilot.getPilot().rotate(Double.parseDouble(parsed[1]));
                        while(pilot.getPilot().isMoving()) {
                            Thread.yield();
                        }
                        sendOut.println("");
                        break;
                    case "COLOUR":
                        sendOut.println(String.valueOf(pilot.getColour()));
                        break;
                    case "DISTANCE":
                        sendOut.println(String.valueOf(pilot.getDistance()));
                        break;
                    case "DSENSOR":
                    	Motor.C.rotate(Integer.parseInt(parsed[1]));
                    	sendOut.println("");
                    	break;
                }
                sendOut.close();  
                client.close();
            }
        } catch(IOException e) {
            
        }
    }
}
