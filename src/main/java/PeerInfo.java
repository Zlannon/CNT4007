public class PeerInfo {
    public String peerId;
    public String peerAddress;
    public int peerPort;
    public int containsFile;

    public PeerInfo(String pId, String pAddress, String pPort, String cFile) {
        // Store the peer info in the appropriate variables
        this.peerId = pId;
        this.peerAddress = pAddress;
        this.peerPort = Integer.parseInt(pPort);
        this.containsFile = Integer.parseInt(cFile);
    }

}
