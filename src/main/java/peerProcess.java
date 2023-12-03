
public class peerProcess {
    public static void main(String[] args) {
        //get peerID from command line arg
        String peerID = args[0];
        //create new peer
        new Peer(peerID);
    }
}
