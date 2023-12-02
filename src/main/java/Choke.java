import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static java.util.stream.Collectors.toMap;

public class Choke implements Runnable {
    private int interval;
    private int numPreferredNeighbors;
    private Peer peer;
    private Random rand = new Random();
    private ScheduledExecutorService scheduler = null;

    Choke(Peer peer) {
        this.peer = peer;
        this.interval = peer.getUnchokingInterval();
        this.numPreferredNeighbors = peer.getNumPreferredNeighbors();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startJob() {
        this.scheduler.scheduleAtFixedRate(this, 6, this.interval, TimeUnit.SECONDS);
    }

    public void run() {
        try {
            HashSet<String> tmp = new HashSet<>();
            HashSet<String> unchokedList = new HashSet<>(this.peer.getUnchokedList());
            List<String> interestedList = new ArrayList<>(this.peer.getInterestedList());
            if (interestedList.size() > 0) {
                int size = Math.min(this.numPreferredNeighbors, interestedList.size());
                if (this.peer.getCompletedPieceCount() == this.peer.getPieceCount()) {
                    for (int i = 0; i < size; i++) {
                        String currentPeer = interestedList.get(this.rand.nextInt(interestedList.size()));
                        PeerHandler currentHandler = this.peer.getPeerHandler(currentPeer);
                        while (tmp.contains(currentPeer)) {
                            currentPeer = interestedList.get(this.rand.nextInt(interestedList.size()));
                            currentHandler = this.peer.getPeerHandler(currentPeer);
                        }
                        if (!unchokedList.contains(currentPeer)) {
                            if (this.peer.getOptUnchokedPeer() == null || this.peer.getOptUnchokedPeer().compareTo(currentPeer) != 0) {
                                currentHandler.sendUnChokedMessage();
                            }
                        } else {
                            unchokedList.remove(currentPeer);
                        }
                        tmp.add(currentPeer);
                        currentHandler.resetDownloadRate();
                    }
                } else {
                    Map<String, Integer> downloads = new HashMap<>(this.peer.getDownloadRates());
                    Map<String, Integer> rates = downloads.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    Iterator<Map.Entry<String, Integer>> iterator = rates.entrySet().iterator();
                    int counter = 0;
                    while (counter < size && iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        if (interestedList.contains(entry.getKey())) {
                            PeerHandler currentHandler = this.peer.getPeerHandler(entry.getKey());
                            if (!unchokedList.contains(entry.getKey())) {
                                String optUnchoke = this.peer.getOptUnchokedPeer();
                                if (optUnchoke == null || optUnchoke.compareTo(entry.getKey()) != 0) {
                                    currentHandler.sendUnChokedMessage();
                                }
                            } else {
                                unchokedList.remove(entry.getKey());
                            }
                            tmp.add(entry.getKey());
                            currentHandler.resetDownloadRate();
                            counter++;
                        }
                    }
                }
                this.peer.updateUnchokedList(tmp);
                if(tmp.size() > 0){
                    this.peer.getLogger().logPreferredNeighbors(new ArrayList<>(tmp));
                }
                for (String peer : unchokedList) {
                    PeerHandler handler = this.peer.getPeerHandler(peer);
                    handler.sendChokedMessage();
                }
            } else {
                this.peer.clearUnchokedList();
                for (String peer : unchokedList) {
                    PeerHandler handler = this.peer.getPeerHandler(peer);
                    handler.sendChokedMessage();
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
