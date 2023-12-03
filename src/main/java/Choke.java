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
        //init class variables
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
            //make a tmp hash set for storing interested peers
            HashSet<String> tmp = new HashSet<>();
            //get the list of unchoked peers
            HashSet<String> unchokedList = new HashSet<>(this.peer.getUnchokedList());
            //get the list of interested peers
            List<String> interestedList = new ArrayList<>(this.peer.getInterestedList());
            //check if interested peers exist
            if (interestedList.size() > 0) {
                //get the min between preferred neighbors and interested
                int size = Math.min(this.numPreferredNeighbors, interestedList.size());
                if (this.peer.getCompletedPieceCount() == this.peer.getPieceCount()) {
                    //loop for min between pref neighbors and interested
                    for (int i = 0; i < size; i++) {
                        //get random interested peer
                        String currentPeer = interestedList.get(this.rand.nextInt(interestedList.size()));
                        //get the handler for selected peer
                        PeerHandler currentHandler = this.peer.getPeerHandler(currentPeer);
                        //select a new peer that tmp does not contain
                        while (tmp.contains(currentPeer)) {
                            currentPeer = interestedList.get(this.rand.nextInt(interestedList.size()));
                            currentHandler = this.peer.getPeerHandler(currentPeer);
                        }
                        //send an unchoked message if selected peer does not exist in unchoked list
                        if (!unchokedList.contains(currentPeer)) {
                            if (this.peer.getOptUnchokedPeer() == null || this.peer.getOptUnchokedPeer().compareTo(currentPeer) != 0) {
                                currentHandler.sendUnChokedMessage();
                            }
                        } else {
                            //if it does exist, remove it from the list
                            unchokedList.remove(currentPeer);
                        }
                        //add the selected peer to tmp
                        tmp.add(currentPeer);
                        //reset the download rate
                        currentHandler.resetDownloadRate();
                    }
                } else {
                    //get the download rate
                    Map<String, Integer> downloads = new HashMap<>(this.peer.getDownloadRates());
                    //get the rates from downloads by descending order
                    Map<String, Integer> rates = downloads.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
                    //iterator for rates
                    Iterator<Map.Entry<String, Integer>> iterator = rates.entrySet().iterator();
                    int counter = 0;
                    //loop while rates exist
                    while (counter < size && iterator.hasNext()) {
                        //get next rate
                        Map.Entry<String, Integer> entry = iterator.next();
                        //check if peer in interested list
                        if (interestedList.contains(entry.getKey())) {
                            //if exists get the handler for it
                            PeerHandler currentHandler = this.peer.getPeerHandler(entry.getKey());
                            //check if in unchoked list
                            if (!unchokedList.contains(entry.getKey())) {
                                //if not in list send unchoked message
                                String optUnchoke = this.peer.getOptUnchokedPeer();
                                if (optUnchoke == null || optUnchoke.compareTo(entry.getKey()) != 0) {
                                    currentHandler.sendUnChokedMessage();
                                }
                            } else {
                                //remove peer from unchoked list
                                unchokedList.remove(entry.getKey());
                            }
                            //add peer to tmp var
                            tmp.add(entry.getKey());
                            //reset download rate for current peer
                            currentHandler.resetDownloadRate();
                            //increment counter
                            counter++;
                        }
                    }
                }
                //update unchoked list with the tmp var
                this.peer.updateUnchokedList(tmp);
                //check if items exist in tmp
                if(tmp.size() > 0){
                    //log the preferred neighbors
                    this.peer.getLogger().logPreferredNeighbors(new ArrayList<>(tmp));
                }
                //send a choke message to every peer in unchoked list
                for (String peer : unchokedList) {
                    PeerHandler handler = this.peer.getPeerHandler(peer);
                    handler.sendChokedMessage();
                }
            } else {
                //empty the unchoked list
                this.peer.clearUnchokedList();
                //send a choke message to every peer in unchoked list
                for (String peer : unchokedList) {
                    PeerHandler handler = this.peer.getPeerHandler(peer);
                    handler.sendChokedMessage();
                }
                //check if all peers have finished
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
        //shutdown scheduler
        this.scheduler.shutdownNow();
    }
}
