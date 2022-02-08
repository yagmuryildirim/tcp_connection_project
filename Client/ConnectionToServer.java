package Client;

import TcpPayload.TcpPayload;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.logging.Filter;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 4444;
    private Socket s;
    //private BufferedReader br;
    protected DataInputStream is;
    protected DataOutputStream os;

    protected String serverAddress;
    protected int serverPort;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public ConnectionToServer(String address, int port) {
        serverAddress = address;
        serverPort = port;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void Connect() {
        try {
            s = new Socket(serverAddress, serverPort);
            //br= new BufferedReader(new InputStreamReader(System.in));
            /*
            Read and write buffers on the socket
             */
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    public void SendTcpData(byte phase, byte type, String payload) {
        try {
            TcpPayload tcpPayload = new TcpPayload(phase, type, payload);
            byte[] bytes = tcpPayload.constructByteArray(tcpPayload);
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
    }

    public String receiveTcpData() {
        String payload = "";
        try {
            byte[] responseBytes = is.readNBytes(6); //Size bytes acquired
            TcpPayload tcp = new TcpPayload(responseBytes[0], responseBytes[1]);
            int payloadSize = tcp.calculateSize(responseBytes);
            byte[] payloadBytes = is.readNBytes(payloadSize);
            TcpPayload tcpWithPayload = new TcpPayload(responseBytes[0], responseBytes[1], payloadBytes);
            payload = tcpWithPayload.convertByteArrayToPayload(payloadBytes);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return payload;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void Disconnect() {
        try {
            is.close();
            os.close();
            //br.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
