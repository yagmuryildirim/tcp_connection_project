package Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        ConnectionToServer connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.Connect();
        System.out.println(new File(".").getAbsolutePath());
        try (
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ) {

            //Username request received
            String usernameRequest = connectionToServer.receiveTcpData();
            System.out.println(usernameRequest);
            //Username acquired and sent
            String userInput = stdIn.readLine();
            connectionToServer.SendTcpData((byte) 0, (byte) 0, userInput);
            //Username response
            String response = connectionToServer.receiveTcpData();
            if (response.equals("Invalid username")) {
                connectionToServer.Disconnect();
            }
            System.out.println(response);
            while (!(response.substring(0, 16).equals("Successful login")) || !response.equals("Failed to login")) {
                if (userInput.equals("quit")) {
                    connectionToServer.Disconnect();
                }
                userInput = stdIn.readLine();
                //Password sent
                connectionToServer.SendTcpData((byte) 0, (byte) 0, userInput);
                response = connectionToServer.receiveTcpData();
                System.out.println(response);
            }
            connectionToServer.Disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Send Username


        connectionToServer.Disconnect();
    }
}
