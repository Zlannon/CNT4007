import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;
import java.text.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class PeerLogger {

    private String logFileName;
    private String peerId;
    private FileHandler peerLogFileHandler;
    private SimpleDateFormat dateFormat = null;
    private Logger peerLogger;

    // Constructor initializes logger with a given peer ID
    public PeerLogger(String peerId) {
        this.peerId = peerId;
        startLogger();
    }

    // Method to initialize and start the logger
    public void startLogger() {
        try {
            // Set date format for logging timestamps
            this.dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            this.logFileName = "log_peer_" + this.peerId + ".log";
            this.peerLogFileHandler = new FileHandler(this.logFileName, false);
            System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
            // Create a FileHandler to write log messages to a file
            this.peerLogFileHandler.setFormatter(new SimpleFormatter());
            this.peerLogger = Logger.getLogger("PeerLogs");
            // Disable logging to parent handlers
            this.peerLogger.setUseParentHandlers(false);
            // Add the file handler to the logger
            this.peerLogger.addHandler(this.peerLogFileHandler);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Log a message when this peer initiates a TCP connection to another peer
    public synchronized void logTCPConnSender(String peer) {
        String msg = String.format("[%s]: Peer %s makes a connection to Peer %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    // Log a message when this peer receives a TCP connection from another peer
    public synchronized void logTCPConnReceiver(String peer) {
        String msg = String.format("[%s]: Peer %s is connected from Peer %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the list of preferred neighbors
    public synchronized void logPreferredNeighbors(List<String> neigbors) {
        String neighborList = "";
        for (String neighbor : neigbors) {
            neighborList += neighbor + ",";
        }
        neighborList = neighborList.substring(0, neighborList.length() - 1);
        String msg = String.format("[%s]: Peer %s has the preferred neighbors %s.", getCurrentTime(), this.peerId, neighborList);
        this.peerLogger.log(Level.INFO, msg);
    }

    // Log the optimistically unchoked neighbor

    public synchronized void logOptimisticUnchokedNeighbor(String peer) {
        String msg = String.format("[%s]: Peer %s has the optimistically unchoked neighbor %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    // Log when a peer is unchoked by another peer

    public synchronized void logUnchoking(String peer) {
        String msg = String.format("[%s]: Peer %s is unchoked by %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log when a peer is choked by another peer
    public synchronized void logChoking(String peer) {
        String msg = String.format("[%s]: Peer %s is choked by %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the receipt of a 'have' message
    public synchronized void logHave(String peer, int index) {
        String msg = String.format("[%s]: Peer received the 'have' message from %s for the piece %s.", getCurrentTime(), this.peerId, peer, index);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the receipt of an 'interested' message
    public synchronized void logInterested(String peer) {
        String msg = String.format("[%s]: Peer %s received the 'interested' message from %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the receipt of a 'not interested' message
    public synchronized void logNotInterested(String peer) {
        String msg = String.format("[%s]: Peer %s received the 'not interested' message from %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the downloading of a piece from a peer

    public synchronized void logDownloadedPiece(String peer, int index, int pieces) {
        String msg = String.format("[%s]: Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.", getCurrentTime(), this.peerId, index, peer, pieces);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Log the completion of a file download
    public synchronized void logCompletion() {
        String msg = String.format("[%s]: Peer %s has downloaded the complete file.", getCurrentTime(), this.peerId);
        this.peerLogger.log(Level.INFO, msg);
    }
    // Get the current time in a specific format
    private synchronized String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
    // Close the logger and its associated file handler

    public void closeLogger() {
        try {
            if (this.peerLogFileHandler != null) {
                this.peerLogFileHandler.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}