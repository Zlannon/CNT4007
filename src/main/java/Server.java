import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.rmi.server.ExportException;
import java.util.*;

public class Server {

    private static final int sPort = 8000;

    // Logger is unique to each peer so
    // Logger logger = new Logger(peerID);

    // Log a TCP connection
   // logger.logTCPConnection(peerID1, peerID2, incoming);

// Log changing preferred neighbors
//logger.logPreferredNeighbors(peerID, preferredNeighbors);

// Log unchoking
//logger.logUnchoking(peerID1, peerID2);

// ... and so on for other events


    public static void main(String[] args) throws Exception {
        System.out.println("The server is running.");
        ServerSocket listener = new ServerSocket(sPort);
        int clientNum = 1;
        try {
            while (true) {
                new Handler(listener.accept(), clientNum).start();
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }

    }


    public Server(int pID, int sPort, int clientID) throws Exception {
        ServerSocket listener = new ServerSocket(sPort);
        while(true) {
            new Handler(listener.accept(), clientID).start();
        }
    }

    /**
     * A handler thread class.  Handlers are spawned from the listening
     * loop and are responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private String message;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private Socket connection;
        private ObjectInputStream in;    //stream read from the socket
        private ObjectOutputStream out;    //stream write to the socket
        private int peerID;
        private Logger logger;
        private int no;

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        public void run() {
            try {
                //initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                boolean handshakeComplete = false;
                String handshakeMsg = "";
                int msgLen;
                try {
                    while (!handshakeComplete) {
                        handshakeMsg = (String) in.readObject();

                        System.out.println(handshakeMsg);

                        if (!handshakeMsg.isEmpty()) {
                            handshakeComplete = Objects.equals(handshakeMsg.substring(0, 28), "P2PFILESHARINGPROJ0000000000");
                        }
                    }

                    sendHandshake();

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                //Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        void sendHandshake() throws IOException {
            String msg = "P2PFILESHARINGPROJ0000000000";
            out.writeObject(msg);
            out.flush();
        }

        //send a message to the output stream
        public void sendMessage(Message message) {
            try {
                // Serialize and send the message
                out.writeObject(message);
                out.flush();
                System.out.println("Send message: " + message.getType() + " to Client " + no);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}

//127 + 156 + 30 + 29 + 126 + 81 + 39 + 62 + 214