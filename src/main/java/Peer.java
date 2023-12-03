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
        //setup class variables
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
            //load config files
            this.config.loadCommonFile();
            this.config.loadConfigFile();
            //get the number of pieces
            this.pieceCount = this.calcPieceCount();
            //init requested info
            this.requestedInfo = new String[this.pieceCount];
            //get the peer config
            this.peerConfig = this.config.getPeerConfig(this.peerID);
            //get the peer info
            this.peerInfoMap = this.config.getPeerInfoMap();
            //get the peer list
            this.peerList = this.config.getPeerList();
            //make file directory if it doesnt exist
            String filepath = "peer_" + this.peerID;
            File file = new File(filepath);
            file.mkdir();
            String filename = filepath + "/" + getFileName();
            file = new File(filename);
            if (!hasFile()) {
                //create a file to store the pieces
                file.createNewFile();
            }
            //init raf file
            this.raf = new RandomAccessFile(file, "rw");
            if (!hasFile()) {
                this.raf.setLength(this.getFileSize());
            }
            this.initPieceAvailability();
            //Make connections
            this.startServer();
            this.connectNeighbors();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            //create socket
            this.socket = new ServerSocket(this.peerConfig.peerPort);
            this.server = new Server(this.socket, this);
            //create thread
            this.serverThread = new Thread(this.server);
            //start thread
            this.serverThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectNeighbors() {
        try {
            //temporarily block thread
            Thread.sleep(5000);
            //for every peer, if we dont have a connection to them,
            //create it
            for (String peerID : this.peerList) {
                //if were already connected, move on
                if (peerID.equals(this.peerID)) {
                    break;
                } else {
                    //create connection to peer if not already connected
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
        //loop through all peers in info map
        for (String peerID : this.peerInfoMap.keySet()) {
            //set up bitset
            BitSet availability = new BitSet(this.pieceCount);
            //init bitset based on if peer has the file or not
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
            //go to position of piece
            int position = this.getPieceSize() * index;
            this.raf.seek(position);
            //write the data
            this.raf.write(data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized byte[] readFromFile(int index) {
        try {
            //go to position of piece
            int position = this.getPieceSize() * index;
            int size = this.getPieceSize();
            if (index == getPieceCount() - 1) {
                size = this.getFileSize() % this.getPieceSize();
            }
            this.raf.seek(position);
            //read the piece data
            byte[] data = new byte[size];
            this.raf.read(data);
            //return piece data
            return data;
        }
        catch (Exception e) {
            e.printStackTrace();

        }
        return new byte[0];
    }
    public HashMap<String, Integer> getDownloadRates() {
        //init hashmap for rates
        HashMap<String, Integer> rates = new HashMap<>();
        for (String key : this.joinedPeers.keySet()) {
            //put download rate for all peers into hashmap
            rates.put(key, this.joinedPeers.get(key).getDownloadRate());
        }
        //return rates
        return rates;
    }
    public synchronized void sendHave(int pieceIndex) {
        //send have message to all joined peers
        for (String key : this.joinedPeers.keySet()) {
            this.joinedPeers.get(key).sendHaveMessage(pieceIndex);
        }
    }
    public synchronized void updatePieceAvailability(String peerID, int index) {
        //set acquired piece to available
        this.piecesAvailable.get(peerID).set(index);
    }
    public synchronized void updateBitset(String peerID, BitSet bitset) {
        //update bitset
        this.piecesAvailable.remove(peerID);
        this.piecesAvailable.put(peerID, bitset);
    }
    public synchronized void addJoinedPeer(PeerHandler p, String otherPeerID) {
        //add a joined peer
        this.joinedPeers.put(otherPeerID, p);
    }
    public synchronized void addJoinedThreads(String otherPeerID, Thread th) {
        //add a joined thread
        this.joinedThreads.put(otherPeerID, th);
    }
    public PeerHandler getPeerHandler(String peerID) {
        //return the PeerHandler for specified peerID
        return this.joinedPeers.get(peerID);
    }
    public BitSet getAvailabilityOf(String peerID) {
        //return pieces available for given peer id
        return this.piecesAvailable.get(peerID);
    }
    public synchronized boolean checkInterested(String otherPeerID) {
        //get the bitsets
        BitSet other = this.getAvailabilityOf(otherPeerID);
        BitSet mine = this.getAvailabilityOf(this.peerID);
        for (int i = 0; i < other.size() && i < this.pieceCount; i++) {
            //if peer doesnt have piece, its interested
            if (other.get(i) && !mine.get(i)) {
                return true;
            }
        }
        //not interested
        return false;
    }
    public synchronized void setRequestedInfo(int id, String peerID) {
        this.requestedInfo[id] = peerID;
    }
    public synchronized int checkRequested(String otherPeerID) {
        //get bitsets
        BitSet other = this.getAvailabilityOf(otherPeerID);
        BitSet mine = this.getAvailabilityOf(this.peerID);
        //loop through pieces
        for (int i = 0; i < other.size() && i < this.pieceCount; i++) {
            //if peer doesnt have piece, update requested info and return piece index
            if (other.get(i) && !mine.get(i) && this.requestedInfo[i] == null) {
                setRequestedInfo(i, otherPeerID);
                return i;
            }
        }
        return -1;
    }
    public synchronized void clearRequested(String otherPeerID) {
        //set requested info to null
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
        //file size divided by piece size
        int len = (getFileSize() / getPieceSize());
        if (getFileSize() % getPieceSize() != 0) {
            //if remainder, add an extra piece
            len += 1;
        }
        //return number of pieces
        return len;
    }
    public int getPieceCount() {
        //return piece count
        return this.pieceCount;
    }

    public int getCompletedPieceCount() {
        //return completed piece count
        return this.piecesAvailable.get(this.peerID).cardinality();
    }

    public synchronized void addToInterestedList(String otherPeerID) {
        //add peer to interested list
        this.interestedList.add(otherPeerID);
    }

    public synchronized void removeFromInterestedList(String otherPeerID) {
        //remove peer from interested list
        if (this.interestedList != null) {
            this.interestedList.remove(otherPeerID);
        }
    }
    public String getPeerID() {
        //return peer id
        return this.peerID;
    }

    public PeerLogger getLogger() {
        //return logger
        return this.logger;
    }
    public int getNumPreferredNeighbors() {
        //return number of preferred neighbors
        return this.config.numPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        //get unchoking interval
        return this.config.UnchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        //return opt unchoking interval
        return this.config.OptUnchokingInterval;
    }

    public String getFileName() {
        //return file name
        return this.config.FileName;
    }

    public int getFileSize() {
        //return file size
        return this.config.FileSize;
    }

    public int getPieceSize() {
        //return piece size
        return this.config.PieceSize;
    }
    public synchronized void clearInterestedList() {
        //empty interested list
        this.interestedList.clear();
    }

    public synchronized HashSet<String> getInterestedList() {
        //return interested list
        return this.interestedList;
    }

    public synchronized HashSet<String> getUnchokedList() {
        //return unchoked list
        return this.unChokedList;
    }

    public synchronized void clearUnchokedList() {
        //empty unchoked list
        this.unChokedList.clear();
    }

    public synchronized void updateUnchokedList(HashSet<String> newSet) {
        //update unchoked list
        this.unChokedList = newSet;
    }

    public synchronized void setOptimisticUnchokedPeer(String peerid) {
        //set new opt unchoked peer
        this.optUnchokedPeer = peerid;
    }

    public synchronized String getOptUnchokedPeer() {
        //return opt unchoked peer
        return this.optUnchokedPeer;
    }
    public synchronized OptUnchoke getOptUnchoke() {
        //return opt unchoke object
        return this.optUnchoke;
    }

    public synchronized Choke getChoke() {
        //return choke object
        return this.choke;
    }

    public synchronized RandomAccessFile getRafFile() {
        //return random access file
        return this.raf;
    }

    public synchronized ServerSocket getSocket() {
        //return socket
        return this.socket;
    }

    public synchronized Thread getServerThread() {
        //return thread
        return this.serverThread;
    }

    public synchronized Boolean checkDone() {
        //return done
        return this.done;
    }

    public synchronized boolean checkAllDone() {
        //check if all done
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
