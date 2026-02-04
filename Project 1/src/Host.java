import java.util.Scanner;
import java.net.*;
import java.util.HashMap;
import java.io.*;


public class Host {
    private String macAddress;
    private InetAddress ipAddress;
    private int port;
    private DatagramSocket socket;
    private HashMap<String, Port> neighbors;

    public Host(String macAddress, File config) throws UnknownHostException, FileNotFoundException {
        this.macAddress = macAddress;
        Parser parser = new Parser(config);
        Port portInfo = parser.parseMac(macAddress);
        this.ipAddress = InetAddress.getByName(portInfo.getIpAddress());
        this.port = Integer.parseInt(portInfo.getUdpPort());
        this.neighbors = parser.getNeighbors(macAddress);
    }

    public void start() throws IOException {
        socket = new DatagramSocket(port);
        System.out.println("Host " + macAddress + " started at " + ipAddress.getHostAddress() + ":" + port);
        System.out.println("MAC: " + macAddress);
        System.out.println("Connected to: " + neighbors.keySet());
    }

    public void send(String data, String destinationMac) throws IOException {
        String frame = destinationMac + ":" + this.macAddress + ":" + data;
        Packet packet = new Packet(frame);
        System.out.println("[" + macAddress + "] Sending:");
        System.out.println("  Src: " + packet.getSourceAddress());
        System.out.println("  Dst: " + packet.getDestinationAddress());
        System.out.println("  Data: " + packet.getData());

        for (Port neighborPort : neighbors.values()) {
            byte[] sendData = frame.getBytes();
            InetAddress destAddress = InetAddress.getByName(neighborPort.getIpAddress());
            int destPort = Integer.parseInt(neighborPort.getUdpPort());
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destAddress, destPort);
            socket.send(sendPacket);
            System.out.println("  -> " + destAddress.getHostAddress() + ":" + destPort);
            break;
        }
    }

    public void receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
        System.out.println("[" + macAddress + "] Listening...");
        socket.receive(recvPacket);
        String frame = new String(recvPacket.getData(), 0, recvPacket.getLength());
        Packet p = new Packet(frame);

        System.out.println("[" + macAddress + "] Received:");
        System.out.println("  Src: " + p.getSourceAddress());
        System.out.println("  Dst: " + p.getDestinationAddress());
        System.out.println("  Data: " + p.getData());
    }

    public String getMacAddress() {
        return macAddress;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java Host <MAC_ADDRESS>");
            return;
        }

        String macAddress = args[0];
        File config = new File("Project 1/src/config.txt");
        Host host = new Host(macAddress, config);
        host.start();

        Scanner keyboard = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Send\n2. Receive\n3. Exit");
            System.out.print("Choice: ");
            String choice = keyboard.nextLine();

            if (choice.equals("1")) {
                System.out.print("Destination (A/B/C/D): ");
                String dest = keyboard.nextLine().toUpperCase();
                System.out.print("Message: ");
                String msg = keyboard.nextLine();
                host.send(msg, dest);
            } else if (choice.equals("2")) {
                host.receive();
            } else if (choice.equals("3")) {
                host.close();
                break;
            }
        }
        keyboard.close();
    }
}
