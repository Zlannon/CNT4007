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

    public PeerLogger(String peerId) {
        this.peerId = peerId;
        startLogger();
    }

    public void startLogger() {
        try {
            this.dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            this.logFileName = "log_peer_" + this.peerId + ".log";
            this.peerLogFileHandler = new FileHandler(this.logFileName, false);
            System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");
            this.peerLogFileHandler.setFormatter(new SimpleFormatter());
            this.peerLogger = Logger.getLogger("PeerLogs");
            this.peerLogger.setUseParentHandlers(false);
            this.peerLogger.addHandler(this.peerLogFileHandler);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void logTCPConnSender(String peer) {
        String msg = String.format("[%s]: Peer %s makes a connection to Peer %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logTCPConnReceiver(String peer) {
        String msg = String.format("[%s]: Peer %s is connected from Peer %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logPreferredNeighbors(List<String> neigbors) {
        String neighborList = "";
        for (String neighbor : neigbors) {
            neighborList += neighbor + ",";
        }
        neighborList = neighborList.substring(0, neighborList.length() - 1);
        String msg = String.format("[%s]: Peer %s has the preferred neighbors %s.", getCurrentTime(), this.peerId, neighborList);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logOptimisticUnchokedNeighbor(String peer) {
        String msg = String.format("[%s]: Peer %s has the optimistically unchoked neighbor %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logUnchoking(String peer) {
        String msg = String.format("[%s]: Peer %s is unchoked by %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logChoking(String peer) {
        String msg = String.format("[%s]: Peer %s is choked by %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logHave(String peer, int index) {
        String msg = String.format("[%s]: Peer received the 'have' message from %s for the piece %s.", getCurrentTime(), this.peerId, peer, index);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logInterested(String peer) {
        String msg = String.format("[%s]: Peer %s received the 'interested' message from %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logNotInterested(String peer) {
        String msg = String.format("[%s]: Peer %s received the 'not interested' message from %s.", getCurrentTime(), this.peerId, peer);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logDownloadedPiece(String peer, int index, int pieces) {
        String msg = String.format("[%s]: Peer %s has downloaded the piece %s from %s. Now the number of pieces it has is %s.", getCurrentTime(), this.peerId, index, peer, pieces);
        this.peerLogger.log(Level.INFO, msg);
    }

    public synchronized void logCompletion() {
        String msg = String.format("[%s]: Peer %s has downloaded the complete file.", getCurrentTime(), this.peerId);
        this.peerLogger.log(Level.INFO, msg);
    }

    private synchronized String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

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