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
            String optUnchokedPeer = this.peer.getOptUnchokedPeer();
            List<String> interestedList = new ArrayList<>(this.peer.getInterestedList());
            interestedList.remove(optUnchokedPeer);
            int size = interestedList.size();
            if (size > 0) {
                String currentPeer = interestedList.get(rand.nextInt(size));
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
                this.peer.setOptimisticUnchokedPeer(currentPeer);
                if(currentPeer != null) {
                    PeerHandler currentHandler = this.peer.getPeerHandler(currentPeer);
                    currentHandler.sendUnChokedMessage();
                    this.peer.getLogger().logOptimisticUnchokedNeighbor(this.peer.getOptUnchokedPeer());
                }
                if (optUnchokedPeer != null && !this.peer.getUnchokedList().contains(optUnchokedPeer)) {
                    this.peer.getPeerHandler(optUnchokedPeer).sendChokedMessage();
                }
            } else {
                String currentOpt = this.peer.getOptUnchokedPeer();
                this.peer.setOptimisticUnchokedPeer(null);
                if (currentOpt != null && !this.peer.getUnchokedList().contains(currentOpt)) {
                    PeerHandler currentHandler = this.peer.getPeerHandler(currentOpt);
                    currentHandler.sendChokedMessage();
                }
                if(this.peer.checkAllDone()) {
                    this.peer.stopAll();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.scheduler.shutdownNow();
    }
}
