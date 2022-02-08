package TcpPayload;

import java.nio.charset.StandardCharsets;

public class TcpPayload {

    private byte phase;
    private byte type;
    private String payload;
    private int size;
    private byte[] payloadBytes;


    public TcpPayload(byte phase, byte type) {
        this.phase = phase;
        this.type = type;
    }

    public TcpPayload(byte phase, byte type, String payload) {
        this.phase = phase;
        this.type = type;
        this.payload = payload;
        this.size = payload.length();
    }

    public TcpPayload(byte phase, byte type, byte[] payloadBytes) {
        this.phase = phase;
        this.type = type;
        this.payloadBytes = payloadBytes;
    }


    public int calculateSize(byte[] bytes) {
        int size = 0;
        if (bytes[0] == (byte) 0) {
            //Initialization
            //Type can be 0,1,2,3
            if (bytes[1] == (byte) 0 || bytes[1] == (byte) 1 || bytes[1] == (byte) 2 || bytes[1] == (byte) 3) {
                //Type is request
                //Convert username to string
                byte[] sizeBytes = {bytes[2], bytes[3], bytes[4], bytes[5]};
                size = convertByteArrayToSize(sizeBytes);
            }
        }
        return size;
    }


    public int convertByteArrayToSize(byte[] byteArray) {
        return byteArray[0] << 24 | (byteArray[1] & 0xFF) << 16 | (byteArray[2] & 0xFF) << 8 | (byteArray[3] & 0xFF);
    }

    public String convertByteArrayToPayload(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public byte[] sizeToByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    public byte[] payloadToByteArray(String value) {
        return value.getBytes();
    }

    public byte[] constructByteArray(TcpPayload tcp) {
        byte[] bytes = new byte[6 + tcp.getSize()];
        byte[] sizeToBytes = tcp.sizeToByteArray(tcp.getSize());
        byte[] payloadToBytes = tcp.payloadToByteArray(tcp.getPayload());
        for (int i = 0; i < bytes.length; i++) {
            if (i == 0) {
                bytes[i] = tcp.getPhase();
            } else if (i == 1) {
                bytes[i] = tcp.getType();
            } else if (i == 2 || i == 3 || i == 4 || i == 5) {
                bytes[i] = sizeToBytes[i - 2];
            } else if (i >= 6) {
                bytes[i] = payloadToBytes[i - 6];
            }
        }

        return bytes;
    }

    public byte getPhase() {
        return phase;
    }

    public void setPhase(byte phase) {
        this.phase = phase;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
