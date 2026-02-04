public class Packet {
    private String data;
    private String destinationAddress;
    private String sourceAddress;

    public Packet(String rawFrame) {
        String[] splitFrame = rawFrame.split(":");

        destinationAddress = splitFrame[0];
        sourceAddress = splitFrame[1];
        data = splitFrame[2];
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getData() {
        return data;
    }

    public String createFrameString() {
        return sourceAddress + ":" + destinationAddress + ":" + data;
}
}