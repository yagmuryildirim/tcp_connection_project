package Server;

import TcpPayload.TcpPayload;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

class ServerThread extends Thread {
    protected DataInputStream is;
    protected DataOutputStream os;
    protected FileWriter fileWriter;
    protected PrintWriter writer;
    protected Socket s;
    private byte[] line;
    String filePath = "../users.txt";
    String tokenFile = "tokens.txt";
    String username = "";
    String token = "";

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s) {
        this.s = s;
    }

    public void SendTcpData(byte phase, byte type, String payload) throws IOException {
        TcpPayload tcpPayload = new TcpPayload(phase, type, payload);
        byte[] bytes = tcpPayload.constructByteArray(tcpPayload);
        os.write(bytes);
        os.flush();
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

    public void createToken() {
        token = "9" + username.substring(0, username.length() - 1) + "9";
    }

    public void challengePassword() {
        String password = receiveTcpData();
        try (BufferedReader bufferedReader2 = new BufferedReader(new FileReader(filePath))) {
            String us2;
            boolean passwordExists = false;
            while ((us2 = bufferedReader2.readLine()) != null) {
                if (us2.equals(password)) {
                    passwordExists = true;
                    break;
                }
            }
            if (passwordExists) {
                createTokenAndAddToFile();
                SendTcpData((byte) 0, (byte) 3, "Successful login, your token is: " + token);
            } else {
                SendTcpData((byte) 0, (byte) 1, "Wrong password, try again");
                password = receiveTcpData();
                try (BufferedReader bufferedReader3 = new BufferedReader(new FileReader(filePath))) {
                    String us3;
                    boolean passwordExists2 = false;
                    while ((us3 = bufferedReader3.readLine()) != null) {
                        if (us3.equals(password)) {
                            passwordExists2 = true;
                            break;
                        }
                    }
                    if (passwordExists2) {
                        createTokenAndAddToFile();
                        SendTcpData((byte) 0, (byte) 3, "Successful login, your token is: " + token);
                    } else {
                        SendTcpData((byte) 0, (byte) 1, "Wrong password, try again");
                        password = receiveTcpData();
                        try (BufferedReader bufferedReader4 = new BufferedReader(new FileReader(filePath))) {
                            String us4;
                            boolean passwordExists3 = false;
                            while ((us4 = bufferedReader4.readLine()) != null) {
                                if (us4.equals(password)) {
                                    passwordExists3 = true;
                                    break;
                                }
                            }
                            if (passwordExists3) {
                                createTokenAndAddToFile();
                                SendTcpData((byte) 0, (byte) 3, "Successful login, your token is: " + token);
                            } else {
                                SendTcpData((byte) 0, (byte) 2, "Failed to login");
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            writer.close();
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createTokenAndAddToFile() throws IOException {
        createToken();
        String toFile = token + " " + s.getRemoteSocketAddress() + " " + s.getLocalPort() + " " + username + "\n";
        Files.write(Paths.get(tokenFile), toFile.getBytes(), StandardOpenOption.APPEND);
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run() {
        try {
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());
            fileWriter = new FileWriter("/Users/ycy/IdeaProjects/Project1-Part2/src/tokens.txt", true);
            writer = new PrintWriter(fileWriter);

        } catch (IOException e) {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try {
            s.setSoTimeout(30000);
            SendTcpData((byte) 0, (byte) 1, "Please enter your username");
            username = receiveTcpData();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
            String us;
            boolean usernameExists = false;
            while ((us = bufferedReader.readLine()) != null) {
                if (us.equals(username)) {
                    usernameExists = true;
                    break;
                }
            }
            if (usernameExists) {
                System.out.println("Username exists");
                SendTcpData((byte) 0, (byte) 1, "Please enter your password");
                challengePassword();
            } else {
                SendTcpData((byte) 0, (byte) 2, "Invalid username");
            }
            //os.write(line);
            //os.flush();

        } catch (IOException e) {
            //line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        } catch (NullPointerException e) {
            //line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally {
            try {
                System.out.println("Closing the connection");
                if (is != null) {
                    is.close();
                    System.err.println(" Socket Input Stream Closed");
                }

                if (os != null) {
                    os.close();
                    System.err.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.err.println("Socket Closed");
                }

            } catch (java.io.InterruptedIOException e) {
                System.err.println("Server timeout at 10 seconds.");
            } catch (IOException ie) {
                System.err.println("Socket Close Error");
            }

        }//end finally
    }
}
