import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import lejos.hardware.motor.Motor;

/**
 * Robot Connector
 * 
 * HOW TO USE:
 * RobotConnector rc = new RobotConnector(robot);
 * rc.start(); //all done
 * 
 * @author Chaoyi Han
 */
public class RobotConnector extends Thread {
    
    private PilotRobot robot;
    
    /**
     * Constructor
     * @param robotInput the robot controller
     */
    public RobotConnector(PilotRobot robotInput) {
        robot = robotInput;
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
                String[] parsed = str.split(":");
                switch(parsed[0]) {
                    case "GO":
                        robot.getPilot().travel(Double.parseDouble(parsed[1]));
                        while(robot.getPilot().isMoving()) {
                            Thread.yield();
                        }
                        sendOut.println("");
                        break;
                    case "TURN":
                        robot.getPilot().rotate(Double.parseDouble(parsed[1]));
                        while(robot.getPilot().isMoving()) {
                            Thread.yield();
                        }
                        sendOut.println("");
                        break;
                    case "COLOUR":
                        sendOut.println(String.valueOf(robot.getColour()));
                        break;
                    case "DISTANCE":
                        sendOut.println(String.valueOf(robot.getDistance()));
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
