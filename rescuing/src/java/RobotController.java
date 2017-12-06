import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import com.google.gson.Gson;
import jason.environment.grid.Location;

/**
 * Robot Remote Controller
 * @author Chaoyi Han
 */
public class RobotController implements Robot {

    private String ip;
    private Gson gson;
    
    /**
     * Constructor
     * @param ipInput connection IP
     */
    public RobotController(String ipInput) {
        ip = ipInput;
        gson = new Gson();
    }
    
    /**
     * Send to the robot.
     * @param info the text want to send
     * @return the data back
     * @throws throw it
    */
    private String sendToRobot(String info) {
        try {
            Socket socket = new Socket(ip, 21900);
            socket.setSoTimeout(20000);
            PrintStream sendOut = new PrintStream(socket.getOutputStream());
            BufferedReader recieved = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendOut.println(info);
            String result = recieved.readLine();
            sendOut.close();
            recieved.close();
            return result;
        } catch(IOException ex) {
            
        }
        return null;
    }
    
    @Override
    public void updateArenaInfo(int[][] data) {
        sendToRobot("updateArenaInfo-cmd->" + gson.toJson(data));
    }
    
    @Override
    public void updateRobotInfo(Location loc, int[] dir) {
        sendToRobot("updateRobotInfo-cmd->" + gson.toJson(loc) + "-param->" + gson.toJson(dir));
    }
    
    @Override
    public boolean[] detectObstacle() {
        return gson.fromJson(sendToRobot("detectObstacle"), boolean[].class);
    }
    
    @Override
    public int detectVictim() {
        return Integer.parseInt(sendToRobot("detectVictim"));
    }
    
    @Override
    public void moveTo(char side) {
        sendToRobot("moveToSide-cmd->" + String.valueOf(side));
    }
    
    @Override
    public void moveTo(Location loc) {
        sendToRobot("moveToLoc-cmd->" + gson.toJson(loc));
    }
    
    /**
     * Let the robot travels.
     * @param distance how long?
     * @throws IOException throw it
     */
    public void travel(double distance) throws IOException {
        sendToRobot("GO-cmd->" + String.valueOf(distance));
    }
    
    /**
     * Let the robot turns.
     * @param angle How much?
     * @throws IOException throw it
     */
    public void rotate(double angle) throws IOException {
        sendToRobot("TURN-cmd->" + String.valueOf(angle));
    }
    
    /**
     * Get colour detected
     * @return colour
     * @throws IOException throw it
     */
    public double getColour() throws IOException {
        return Double.parseDouble(sendToRobot("COLOUR"));
    }
    
    /**
     * Get Distance from the sensor
     * @return the distance
     * @throws IOException throw it
     */
    public double getDistance() throws IOException {
        return Double.parseDouble(sendToRobot("DISTANCE"));
    }
    
    /**
     * Rotate the distance sensor
     * @param angle angle or rotation
     * @throws IOException throw it
     */
    public void rotateSensor(int angle) throws IOException {
    	sendToRobot("DSENSOR-cmd->" + String.valueOf(angle));
    }
}