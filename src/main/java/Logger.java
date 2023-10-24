import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final String logFileName;

    public Logger(int peerID) {
        this.logFileName = "log_peer_" + peerID + ".log";
    }

    public void logTCPConnection(int peerID1, int peerID2, boolean incoming) {
        String logMessage;
        if (incoming) {
            logMessage = String.format("[%s]: Peer %d is connected from Peer %d.%n", getCurrentTime(), peerID1, peerID2);
        } else {
            logMessage = String.format("[%s]: Peer %d makes a connection to Peer %d.%n", getCurrentTime(), peerID1, peerID2);
        }
        writeLog(logMessage);
    }

    public void logPreferredNeighbors(int peerID, String preferredNeighbors) {
        String logMessage = String.format("[%s]: Peer %d has the preferred neighbors %s.%n", getCurrentTime(), peerID, preferredNeighbors);
        writeLog(logMessage);
    }

    public void logOptimisticUnchokedNeighbor(int peerID, int neighborID) {
        String logMessage = String.format("[%s]: Peer %d has the optimistically unchoked neighbor %d.%n", getCurrentTime(), peerID, neighborID);
        writeLog(logMessage);
    }

    public void logUnchoking(int peerID1, int peerID2) {
        String logMessage = String.format("[%s]: Peer %d is unchoked by %d.%n", getCurrentTime(), peerID1, peerID2);
        writeLog(logMessage);
    }

    public void logChoking(int peerID1, int peerID2) {
        String logMessage = String.format("[%s]: Peer %d is choked by %d.%n", getCurrentTime(), peerID1, peerID2);
        writeLog(logMessage);
    }

    public void logHave(int peerID1, int peerID2, int pieceIndex) {
        String logMessage = String.format("[%s]: Peer %d received the 'have' message from %d for the piece %d.%n", getCurrentTime(), peerID1, peerID2, pieceIndex);
        writeLog(logMessage);
    }

    public void logInterested(int peerID1, int peerID2) {
        String logMessage = String.format("[%s]: Peer %d received the 'interested' message from %d.%n", getCurrentTime(), peerID1, peerID2);
        writeLog(logMessage);
    }

    public void logNotInterested(int peerID1, int peerID2) {
        String logMessage = String.format("[%s]: Peer %d received the 'not interested' message from %d.%n", getCurrentTime(), peerID1, peerID2);
        writeLog(logMessage);
    }

    public void logDownloadedPiece(int peerID1, int peerID2, int pieceIndex, int numberOfPieces) {
        String logMessage = String.format("[%s]: Peer %d has downloaded the piece %d from %d. Now the number of pieces it has is %d.%n", getCurrentTime(), peerID1, pieceIndex, peerID2, numberOfPieces);
        writeLog(logMessage);
    }

    public void logCompletion(int peerID) {
        String logMessage = String.format("[%s]: Peer %d has downloaded the complete file.%n", getCurrentTime(), peerID);
        writeLog(logMessage);
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    private void writeLog(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName, true))) {
            writer.write(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
