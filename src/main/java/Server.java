import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server implements Runnable {
    private ServerSocket listener;
    private Peer peer;

    public Server(ServerSocket listener, Peer peer) {
        this.listener = listener;
        this.peer = peer;
    }

    public void run() {
        while (true) {
            try {
                Socket neighbor = this.listener.accept();
                PeerHandler peerHandler = new PeerHandler(neighbor, this.peer);
                new Thread(peerHandler).start();
            }
            catch (SocketException e) {
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
