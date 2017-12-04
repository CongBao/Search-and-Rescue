import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Robot Remote Controller
 * @author Chaoyi Han
 */
public class RobotController {

    private String ip;
    
    /**
     * Constructor
     * @param ipInput connection IP
     * @throws IOException throw it
     */
    public RobotController(String ipInput) throws IOException {
        ip = ipInput;
    }
    
    /**
     * Let the robot travels.
     * @param distance how long?
     * @throws IOException throw it
     */
    public void travel(double distance) throws IOException {
        Socket socket = new Socket(ip, 21900);
        socket.setSoTimeout(20000);
        PrintStream sendOut = new PrintStream(socket.getOutputStream());
        BufferedReader recieved = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendOut.println("GO:" + String.valueOf(distance));
        recieved.readLine();
        sendOut.close();
        recieved.close();
    }
    
    /**
     * Let the robot turns.
     * @param angle How much?
     * @throws IOException throw it
     */
    public void rotate(double angle) throws IOException {
        Socket socket = new Socket(ip, 21900);
        socket.setSoTimeout(20000);
        PrintStream sendOut = new PrintStream(socket.getOutputStream());
        BufferedReader recieved = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendOut.println("TURN:" + String.valueOf(angle));
        recieved.readLine();
        sendOut.close();
        recieved.close();
    }
    
    /**
     * Get colour detected
     * @return colour
     * @throws IOException throw it
     */
    public int getColour() throws IOException {
        Socket socket = new Socket(ip, 21900);
        socket.setSoTimeout(20000);
        PrintStream sendOut = new PrintStream(socket.getOutputStream());
        BufferedReader recieved = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendOut.println("COLOUR");
        String result = recieved.readLine();
        sendOut.close();
        recieved.close();
        return Integer.parseInt(result);
    }
    
    /**
     * Get Distance from the sensor
     * @return the distance
     * @throws IOException throw it
     */
    public double getDistance() throws IOException {
        Socket socket = new Socket(ip, 21900);
        socket.setSoTimeout(20000);
        PrintStream sendOut = new PrintStream(socket.getOutputStream());
        BufferedReader recieved = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        sendOut.println("DISTANCE");
        String result = recieved.readLine();
        sendOut.close();
        recieved.close();
        return Integer.parseInt(result);
    }
}
