import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class OptUnchoke implements Runnable {
    private int interval;
    private Peer peer;
    private Random rand = new Random();
    private ScheduledExecutorService scheduler = null;

    OptUnchoke(Peer peer) {
        this.peer = peer;
        this.interval = peer.getOptimisticUnchokingInterval();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startJob() {
        this.scheduler.scheduleAtFixedRate(this, 6, this.interval, TimeUnit.SECONDS);
    }

    public void run() {
        try {
            //get optimistically unchoked peer
            String optUnchokedPeer = this.peer.getOptUnchokedPeer();
            //get the interested peer list
            List<String> interestedList = new ArrayList<>(this.peer.getInterestedList());
            //remove the opt unchoked peer from interested list
            interestedList.remove(optUnchokedPeer);
            //get the number of interested peers
            int size = interestedList.size();
            //if iterested peers exist
            if (size > 0) {
                //get a random peer
                String currentPeer = interestedList.get(rand.nextInt(size));
                //while peer is in the unchoked list, remove it from interested and grab a new peer
                while (this.peer.getUnchokedList().contains(currentPeer)) {
                    interestedList.remove(currentPeer);
                    size = size - 1;
                    if(size > 0) {
                        currentPeer = interestedList.get(rand.nextInt(size));
                    } else {
                        currentPeer = null;
                        break;
                    }
                }
                //set the opt unchoked peer
                this.peer.setOptimisticUnchokedPeer(currentPeer);
                if(currentPeer != null) {
                    //get the handler for the selected peer
                    PeerHandler currentHandler = this.peer.getPeerHandler(currentPeer);
                    //send unchoke message
                    currentHandler.sendUnChokedMessage();
                    //log the opt unchoked peer
                    this.peer.getLogger().logOptimisticUnchokedNeighbor(this.peer.getOptUnchokedPeer());
                }
                //check if peer is in unchoked list
                if (optUnchokedPeer != null && !this.peer.getUnchokedList().contains(optUnchokedPeer)) {
                    //if not send choke message
                    this.peer.getPeerHandler(optUnchokedPeer).sendChokedMessage();
                }
            } else {
                //get the opt unchoked peer
                String currentOpt = this.peer.getOptUnchokedPeer();
                //set unchoked peer to null
                this.peer.setOptimisticUnchokedPeer(null);
                //check if opt unchoked peer is in unchoked list
                if (currentOpt != null && !this.peer.getUnchokedList().contains(currentOpt)) {
                    //if not send choked message
                    PeerHandler currentHandler = this.peer.getPeerHandler(currentOpt);
                    currentHandler.sendChokedMessage();
                }
                //check if all peers are done
                if(this.peer.checkAllDone()) {
                    //stop everything
                    this.peer.stopAll();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        //stop scheduler
        this.scheduler.shutdownNow();
    }
}
