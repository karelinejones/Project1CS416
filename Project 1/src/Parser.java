import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.net.InetAddress;

public class Parser {
    File config;

    public Parser (File config){
        this.config = config;
    }

    
    public Port parseMac (String macAddress) throws FileNotFoundException, UnknownHostException {
        Scanner fileReader = new Scanner(config);
        boolean notMac = true;
        Port port = new Port();

        while(notMac) {
            String line = fileReader.nextLine();
            String[] sectionedLine = line.split(" ");
            if (sectionedLine[0].equals(macAddress)) {
                notMac = false;
                port = new Port(Integer.parseInt(sectionedLine[1]), InetAddress.getByName(sectionedLine[2]));
            }
        }

        return port;
    }

    public HashMap<String, Port> getNeighbors (String macAddress) throws FileNotFoundException, UnknownHostException {
        Scanner fileReader = new Scanner(config);
        boolean linksSection = false;
        HashMap<String, Port> neighbors = new HashMap<>();

        while(fileReader.hasNextLine()) {
            String line = fileReader.nextLine();
            if (linksSection) {
                String[] macAddresses = line.split(":");
                if (macAddresses[0].equals(macAddress)) {
                    Port neighborPort = parseMac(macAddresses[1]);
                    neighbors.put(macAddresses[1], neighborPort);
                }
                else if (macAddresses[1].equals(macAddress)){
                    Port neighborPort = parseMac(macAddresses[0]);
                    neighbors.put(macAddresses[0], neighborPort);
                }
            }
            if (line.equals("Links")) {
                linksSection = true;
            }
        }

        return neighbors;
    }
}
