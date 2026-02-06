import java.util.Scanner;
import java.net.*;
import java.util.HashMap;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Host {
    private String macAddress;
    private InetAddress ipAddress;
    private int port;
    private DatagramSocket socket;
    private HashMap<String, Port> neighbors;
    private ExecutorService executorService;

    public Host(String macAddress, File config) throws UnknownHostException, FileNotFoundException {
        this.macAddress = macAddress;
        Parser parser = new Parser(config);
        Port portInfo = parser.parseMac(macAddress);
        this.ipAddress = (portInfo.getIpAddress());
        this.port = (portInfo.getUdpPort());
        this.neighbors = parser.getNeighbors(macAddress);
    }

    public void start() throws IOException {
        socket = new DatagramSocket(port);
        executorService = Executors.newCachedThreadPool();
        System.out.println("Host " + macAddress + " started at " + ipAddress.getHostAddress() + ":" + port);
        System.out.println("MAC: " + macAddress);
        System.out.println("Connected to: " + neighbors.keySet());

        executorService.submit(() -> {
            while (!socket.isClosed()) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
                    socket.receive(recvPacket);
                    String frame = new String(recvPacket.getData(), 0, recvPacket.getLength());
                    Packet p = new Packet(frame);

                    if (p.getDestinationAddress().equals(macAddress)) {
                        System.out.println("\n[" + macAddress + "] Received:");
                        System.out.println("  Src: " + p.getSourceAddress());
                        System.out.println("  Dst: " + p.getDestinationAddress());
                        System.out.println("  Data: " + p.getData());
                    } else {
                        wrongMAC(p);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error receiving: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void send(String data, String destinationMac) {
        executorService.submit(() -> {
            try {
                String frame = this.macAddress + ":" + destinationMac + ":" + data;
                Packet packet = new Packet(frame);
                System.out.println("[" + macAddress + "] Sending:");
                System.out.println("  Src: " + packet.getSourceAddress());
                System.out.println("  Dst: " + packet.getDestinationAddress());
                System.out.println("  Data: " + packet.getData());

                for (Port neighborPort : neighbors.values()) {
                    byte[] sendData = frame.getBytes();
                    InetAddress destAddress = (neighborPort.getIpAddress());
                    int destPort = (neighborPort.getUdpPort());
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destAddress, destPort);
                    socket.send(sendPacket);
                    System.out.println("  -> " + destAddress.getHostAddress() + ":" + destPort);
                    break;
                }
            } catch (IOException e) {
                System.err.println("Error sending: " + e.getMessage());
            }
        });
    }

    public void receive() {
        executorService.submit(() -> {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket recvPacket = new DatagramPacket(buffer, buffer.length);
                System.out.println("[" + macAddress + "] Listening...");
                socket.receive(recvPacket);
                String frame = new String(recvPacket.getData(), 0, recvPacket.getLength());
                Packet p = new Packet(frame);

                        if (p.getDestinationAddress().equals(macAddress)) {
                            System.out.println("\n[" + macAddress + "] Received:");
                            System.out.println("  Src: " + p.getSourceAddress());
                            System.out.println("  Dst: " + p.getDestinationAddress());
                            System.out.println("  Data: " + p.getData());
                        } else {
                            wrongMAC(p);
                        }
            } catch (IOException e) {
                System.err.println("Error receiving: " + e.getMessage());
            }
        });
    }

    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    private void wrongMAC(Packet p) {
        System.err.println("[" + macAddress + "] Packet dropped — destination " + p.getDestinationAddress() + " does not match MAC " + macAddress);
    }

    public static void runHostA() throws Exception {
        File config = new File("config.txt");
        Host host = new Host("A", config);
        host.start();
        host.runInteractive();
    }

    public static void runHostB() throws Exception {
        File config = new File("config.txt");
        Host host = new Host("B", config);
        host.start();
        host.runInteractive();
    }

    public static void runHostC() throws Exception {
        File config = new File("config.txt");
        Host host = new Host("C", config);
        host.start();
        host.runInteractive();
    }

    public static void runHostD() throws Exception {
        File config = new File("config.txt");
        Host host = new Host("D", config);
        host.start();
        host.runInteractive();
    }

    private void runInteractive() {
        Scanner keyboard = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Send\n2. Exit");
            System.out.print("Choice: ");
            String choice = keyboard.nextLine();

            if (choice.equals("1")) {
                System.out.print("Destination (A/B/C/D): ");
                String dest = keyboard.nextLine().toUpperCase();
                System.out.print("Message: ");
                String msg = keyboard.nextLine();
                send(msg, dest);
            } else if (choice.equals("2")) {
                close();
                break;
            }
        }
        keyboard.close();
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
            System.out.println("\n1. Send\n2. Exit");
            System.out.print("Choice: ");
            String choice = keyboard.nextLine();

            if (choice.equals("1")) {
                System.out.print("Destination (A/B/C/D): ");
                String dest = keyboard.nextLine().toUpperCase();
                System.out.print("Message: ");
                String msg = keyboard.nextLine();
                host.send(msg, dest);
            } else if (choice.equals("2")) {
                host.close();
                break;
            }
        }
        keyboard.close();
    }
}
