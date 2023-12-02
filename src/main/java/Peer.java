import java.io.File;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

public class Peer {
    private String peerID;
    private PeerInfo peerConfig;
    private HashMap<String, PeerInfo> peerInfoMap;
    private ArrayList<String> peerList;
    private Server server;
    private volatile ServerSocket socket;
    private LoadCfg config;
    private volatile PeerLogger logger;
    private volatile HashMap<String, PeerHandler> joinedPeers;
    private volatile HashMap<String, Thread> joinedThreads;
    private volatile HashMap<String, BitSet> piecesAvailable;
    private volatile String[] requestedInfo;
    private volatile HashSet<String> unChokedList;
    private volatile String optUnchokedPeer;
    private volatile OptUnchoke optUnchoke;
    private volatile RandomAccessFile raf;
    private volatile HashSet<String> interestedList;
    private int pieceCount;
    private volatile Choke choke;
    private volatile Terminate terminate;
    private Thread serverThread;
    private volatile Boolean done;

    public Peer(String peerID) {
        this.peerID = peerID;
        this.peerInfoMap = new HashMap<>();
        this.piecesAvailable = new HashMap<>();
        this.peerList = new ArrayList<>();
        this.joinedPeers = new HashMap<>();
        this.joinedThreads = new HashMap<>();
        this.config = new LoadCfg();
        this.logger = new PeerLogger(this.peerID);
        this.unChokedList = new HashSet<>();
        this.interestedList = new HashSet<>();
        this.initPeer();
        this.choke = new Choke(this);
        this.optUnchoke = new OptUnchoke(this);
        this.terminate = new Terminate(this);
        this.done = false;
        this.choke.startJob();
        this.optUnchoke.startJob();
    }

    public void initPeer() {
        try {
            this.config.loadCommonFile();
            this.config.loadConfigFile();
            this.pieceCount = this.calcPieceCount();
            this.requestedInfo = new String[this.pieceCount];
            this.peerConfig = this.config.getPeerConfig(this.peerID);
            this.peerInfoMap = this.config.getPeerInfoMap();
            this.peerList = this.config.getPeerList();
            String filepath = "peer_" + this.peerID;
            File file = new File(filepath);
            file.mkdir();
            String filename = filepath + "/" + getFileName();
            file = new File(filename);
            if (!hasFile()) {
                file.createNewFile();
            }
            this.raf = new RandomAccessFile(file, "rw");
            if (!hasFile()) {
                this.raf.setLength(this.getFileSize());
            }
            this.initPieceAvailability();
            this.startServer();
            this.connectNeighbors();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            this.socket = new ServerSocket(this.peerConfig.peerPort);
            this.server = new Server(this.socket, this);
            this.serverThread = new Thread(this.server);
            this.serverThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectNeighbors() {
        try {
            Thread.sleep(5000);
            for (String peerID : this.peerList) {
                if (peerID.equals(this.peerID)) {
                    break;
                } else {
                    PeerInfo peer = this.peerInfoMap.get(peerID);
                    Socket tmp = new Socket(peer.peerAddress, peer.peerPort);
                    PeerHandler peerHandler = new PeerHandler(tmp, this);
                    peerHandler.setOtherPeerID(peerID);
                    this.addJoinedPeer(peerHandler, peerID);
                    Thread thread = new Thread(peerHandler);
                    this.addJoinedThreads(peerID, thread);
                    thread.start();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void initPieceAvailability() {
        for (String peerID : this.peerInfoMap.keySet()) {
            BitSet availability = new BitSet(this.pieceCount);
            if (this.peerInfoMap.get(peerID).containsFile == 1) {
                availability.set(0, this.pieceCount);
                this.piecesAvailable.put(peerID, availability);
            } else {
                availability.clear();
                this.piecesAvailable.put(peerID, availability);
            }
        }
    }
    public synchronized void writeToFile(byte[] data, int index) {
        try {
            int position = this.getPieceSize() * index;
            this.raf.seek(position);
            this.raf.write(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized byte[] readFromFile(int index) {
        try {
            int position = this.getPieceSize() * index;
            int size = this.getPieceSize();
            if (index == getPieceCount() - 1) {
                size = this.getFileSize() % this.getPieceSize();
            }
            this.raf.seek(position);
            byte[] data = new byte[size];
            this.raf.read(data);
            return data;
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return new byte[0];
    }
    public HashMap<String, Integer> getDownloadRates() {
        HashMap<String, Integer> rates = new HashMap<>();
        for (String key : this.joinedPeers.keySet()) {
            rates.put(key, this.joinedPeers.get(key).getDownloadRate());
        }
        return rates;
    }
    public synchronized void sendHave(int pieceIndex) {
        for (String key : this.joinedPeers.keySet()) {
            this.joinedPeers.get(key).sendHaveMessage(pieceIndex);
        }
    }
    public synchronized void updatePieceAvailability(String peerID, int index) {
        this.piecesAvailable.get(peerID).set(index);
    }
    public synchronized void updateBitset(String peerID, BitSet bitset) {
        this.piecesAvailable.remove(peerID);
        this.piecesAvailable.put(peerID, bitset);
    }
    public synchronized void addJoinedPeer(PeerHandler p, String otherPeerID) {
        this.joinedPeers.put(otherPeerID, p);
    }
    public synchronized void addJoinedThreads(String epeerid, Thread th) {
        this.joinedThreads.put(epeerid, th);
    }
    public PeerHandler getPeerHandler(String peerid) {
        return this.joinedPeers.get(peerid);
    }
    public BitSet getAvailabilityOf(String pid) {
        return this.piecesAvailable.get(pid);
    }
    public synchronized boolean checkInterested(String otherPeerID) {
        BitSet other = this.getAvailabilityOf(otherPeerID);
        BitSet mine = this.getAvailabilityOf(this.peerID);
        for (int i = 0; i < other.size() && i < this.pieceCount; i++) {
            if (other.get(i) && !mine.get(i)) {
                return true;
            }
        }
        return false;
    }
    public synchronized void setRequestedInfo(int id, String peerID) {
        this.requestedInfo[id] = peerID;
    }
    public synchronized int checkRequested(String otherPeerID) {
        BitSet end = this.getAvailabilityOf(otherPeerID);
        BitSet mine = this.getAvailabilityOf(this.peerID);
        for (int i = 0; i < end.size() && i < this.pieceCount; i++) {
            if (end.get(i) && !mine.get(i) && this.requestedInfo[i] == null) {
                setRequestedInfo(i, otherPeerID);
                return i;
            }
        }
        return -1;
    }
    public synchronized void clearRequested(String otherPeerID) {
        for (int i = 0; i < this.requestedInfo.length; i++) {
            if (this.requestedInfo[i] != null && this.requestedInfo[i].compareTo(otherPeerID) == 0) {
                setRequestedInfo(i, null);
            }
        }
    }
    public boolean hasFile() {
        return this.peerConfig.containsFile == 1;
    }
    public int calcPieceCount() {
        int len = (getFileSize() / getPieceSize());
        if (getFileSize() % getPieceSize() != 0) {
            len += 1;
        }
        return len;
    }
    public int getPieceCount() {
        return this.pieceCount;
    }

    public int getCompletedPieceCount() {
        return this.piecesAvailable.get(this.peerID).cardinality();
    }

    public synchronized void addToInterestedList(String otherPeerID) {
        this.interestedList.add(otherPeerID);
    }

    public synchronized void removeFromInterestedList(String otherPeerID) {
        if (this.interestedList != null) {
            this.interestedList.remove(otherPeerID);
        }
    }
    public String getPeerID() {
        return this.peerID;
    }

    public PeerLogger getLogger() {
        return this.logger;
    }
    public int getNumPreferredNeighbors() {
        return this.config.numPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return this.config.UnchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return this.config.OptUnchokingInterval;
    }

    public String getFileName() {
        return this.config.FileName;
    }

    public int getFileSize() {
        return this.config.FileSize;
    }

    public int getPieceSize() {
        return this.config.PieceSize;
    }
    public synchronized void clearInterestedList() {
        this.interestedList.clear();
    }

    public synchronized HashSet<String> getInterestedList() {
        return this.interestedList;
    }

    public synchronized HashSet<String> getUnchokedList() {
        return this.unChokedList;
    }

    public synchronized void clearUnchokedList() {
        this.unChokedList.clear();
    }

    public synchronized void updateUnchokedList(HashSet<String> newSet) {
        this.unChokedList = newSet;
    }

    public synchronized void setOptimisticUnchokedPeer(String peerid) {
        this.optUnchokedPeer = peerid;
    }

    public synchronized String getOptUnchokedPeer() {
        return this.optUnchokedPeer;
    }
    public synchronized OptUnchoke getOptUnchoke() {
        return this.optUnchoke;
    }

    public synchronized Choke getChoke() {
        return this.choke;
    }

    public synchronized RandomAccessFile getRafFile() {
        return this.raf;
    }

    public synchronized ServerSocket getSocket() {
        return this.socket;
    }

    public synchronized Thread getServerThread() {
        return this.serverThread;
    }

    public synchronized Boolean checkDone() {
        return this.done;
    }

    public synchronized boolean checkAllDone() {
        for (String peer : this.piecesAvailable.keySet()) {
            if (this.piecesAvailable.get(peer).cardinality() != this.pieceCount) {
                return false;
            }
        }
        return true;
    }
    public synchronized void stopThreads() {
        for (String peer : this.joinedThreads.keySet()) {
            this.joinedThreads.get(peer).stop();
        }
    }

    public synchronized void stopAll() {
        try {
            this.getChoke().stop();
            this.getOptUnchoke().stop();
            this.clearUnchokedList();
            this.setOptimisticUnchokedPeer(null);
            this.clearInterestedList();
            this.getRafFile().close();
            this.getLogger().closeLogger();
            this.getSocket().close();
            this.getServerThread().stop();
            this.done = true;
            this.terminate.startJob(6);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
