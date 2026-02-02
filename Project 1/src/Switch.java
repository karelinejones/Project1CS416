import java.io.File;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Switch {
    public static void main(String[] args) throws Exception, FileNotFoundException {
        String macAddress = args[0];
        System.out.println("Mac Address: " + macAddress);
        File config = new File("Project 1/src/config.txt");
        Parser parser = new Parser(config);
        HashMap<String, Port> neighbors = parser.getNeighbors(macAddress);
        HashMap<String, Integer> switchTable = new HashMap<>();
        DatagramSocket socket = new DatagramSocket();
        DatagramPacket incoming = new DatagramPacket(new byte[1024], 1024);

        while(true) {
            socket.receive(incoming);
            InetAddress incomingIp = incoming.getAddress();
            String incomingMac = getMacFromIp(neighbors, incomingIp);
            if (switchTable.containsKey(incomingMac)) {

            }
            else {
                Port incomingPort = neighbors.get(incomingMac);
                switchTable.put(macAddress, incomingPort.getUdpPort());

            }
        }
    }

    public static String getMacFromIp(HashMap<String, Port> neighbors, InetAddress ipAddress) {
        for (String s : neighbors.keySet()) {
            Port p = neighbors.get(s);
            if (p.getIpAddress().equals(ipAddress)) {
                return s;
            }
        }
        return "None";
    }

    public static void printSwitchTable() {

    }

}
