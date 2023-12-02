import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class PeerHandler implements Runnable {
    private Socket socket;
    private Peer peer;
    private String otherPeerID;
    private boolean connected = false;
    private boolean initializer = false;
    private Handshake handshake;
    private volatile int downloadRate = 0;
    private volatile ObjectOutputStream out;
    private volatile ObjectInputStream in;

    public PeerHandler(Socket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
        initstreams();
        this.handshake = new Handshake(this.peer.getPeerID());

    }

    public void initstreams() {
        try {
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOtherPeerID(String peerID) {
        this.otherPeerID = peerID;
        this.initializer = true;
    }

    public void run() {
        try {
            byte[] msg = this.handshake.buildHandShake();
            this.out.write(msg);
            this.out.flush();
            while (true) {
                if (!this.connected) {
                    byte[] response = new byte[32];
                    this.in.readFully(response);
                    this.processHandShake(response);
                    if (this.peer.hasFile() || this.peer.getAvailabilityOf(this.peer.getPeerID()).cardinality() > 0) {
                        this.sendBitField();
                    }
                } else {
                    while (this.in.available() < 4) {
                    }
                    int respSize = this.in.readInt();
                    byte[] response = new byte[respSize];
                    this.in.readFully(response);
                    char messageType = (char) response[0];
                    Message message = new Message();
                    message.readMessage(respSize, response);
                    int pieceIndex;
                    int requestindex;
                    switch (messageType) {
                        case '0':
                            this.peer.clearRequested(this.otherPeerID);
                            this.peer.getLogger().logChoking(this.otherPeerID);
                            break;
                        case '1':
                            requestindex = this.peer.checkRequested(this.otherPeerID);
                            if (requestindex == -1) {
                                this.sendNotInterestedMessage();
                            } else {
                                this.sendRequestMessage(requestindex);
                            }
                            this.peer.getLogger().logUnchoking(this.otherPeerID);
                            break;
                        case '2':
                            this.peer.addToInterestedList(this.otherPeerID);
                            this.peer.getLogger().logInterested(this.otherPeerID);
                            break;
                        case '3':
                            this.peer.removeFromInterestedList(this.otherPeerID);
                            this.peer.getLogger().logNotInterested(this.otherPeerID);
                            break;
                        case '4':
                            pieceIndex = message.getPieceIndexFromPayload();
                            this.peer.updatePieceAvailability(this.otherPeerID, pieceIndex);
                            if (this.peer.checkAllDone()) {
                                this.peer.stopAll();
                            }
                            if (this.peer.checkInterested(this.otherPeerID)) {
                                this.sendInterestedMessage();
                            } else {
                                this.sendNotInterestedMessage();
                            }
                            this.peer.getLogger().logHave(this.otherPeerID, pieceIndex);
                            break;
                        case '5':
                            BitSet bitset = message.getBitFieldMessage();
                            this.processBitFieldMessage(bitset);
                            if (!this.peer.hasFile()) {
                                if (this.peer.checkInterested(this.otherPeerID)) {
                                    this.sendInterestedMessage();
                                } else {
                                    this.sendNotInterestedMessage();
                                }
                            }
                            break;
                        case '6':
                            if (this.peer.getUnchokedList().contains(this.otherPeerID) || (this.peer.getOptUnchokedPeer() != null && this.peer.getOptUnchokedPeer().compareTo(this.otherPeerID) == 0)) {
                                pieceIndex = message.getPieceIndexFromPayload();
                                this.sendPieceMessage(pieceIndex, this.peer.readFromFile(pieceIndex));
                            }
                            break;
                        case '7':
                            pieceIndex = message.getPieceIndexFromPayload();
                            byte[] piece = message.getPieceFromPayload();
                            this.peer.writeToFile(piece, pieceIndex);
                            this.peer.updatePieceAvailability(this.peer.getPeerID(), pieceIndex);
                            this.downloadRate = this.downloadRate + 1;
                            Boolean allDone = this.peer.checkAllDone();
                            this.peer.getLogger().logDownloadedPiece(this.otherPeerID, pieceIndex, this.peer.getCompletedPieceCount());
                            this.peer.setRequestedInfo(pieceIndex, null);
                            this.peer.sendHave(pieceIndex);
                            if (this.peer.getAvailabilityOf(this.peer.getPeerID()).cardinality() != this.peer.getPieceCount()) {
                                requestindex = this.peer.checkRequested(this.otherPeerID);
                                if (requestindex != -1) {
                                    this.sendRequestMessage(requestindex);
                                } else {
                                    this.sendNotInterestedMessage();
                                }
                            } else {
                                this.peer.getLogger().logCompletion();
                                if (allDone) {
                                    this.peer.stopAll();
                                }
                                this.sendNotInterestedMessage();
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        catch (SocketException e) {
            System.out.println("Socket exception");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void send(byte[] obj) {
        try {
            this.out.write(obj);
            this.out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChokedMessage() {
        try {
            Message msg = new Message('0');
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUnChokedMessage() {
        try {
            Message msg = new Message('1');
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInterestedMessage() {
        try {
            Message msg = new Message('2');
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotInterestedMessage() {
        try {
            Message msg = new Message('3');
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendHaveMessage(int pieceIndex) {
        try {
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            Message msg = new Message('4', bytes);
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBitField() {
        try {
            BitSet myAvailability = this.peer.getAvailabilityOf(this.peer.getPeerID());
            Message msg = new Message('5', myAvailability.toByteArray());
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRequestMessage(int pieceIndex) {
        try {
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            Message msg = new Message('6', bytes);
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPieceMessage(int pieceIndex, byte[] payload) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            stream.write(bytes);
            stream.write(payload);
            Message msg = new Message('7', stream.toByteArray());
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processHandShake(byte[] message) {
        try {
            this.handshake.readHandShake(message);
            this.otherPeerID = this.handshake.getPeerID();
            this.peer.addJoinedPeer(this, this.otherPeerID);
            this.peer.addJoinedThreads(this.otherPeerID, Thread.currentThread());
            this.connected = true;
            if (this.initializer) {
                this.peer.getLogger().logTCPConnSender(this.otherPeerID);
            } else {
                this.peer.getLogger().logTCPConnReceiver(this.otherPeerID);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processBitFieldMessage(BitSet b) {
        this.peer.updateBitset(this.otherPeerID, b);
    }

    public int getDownloadRate() {
        return this.downloadRate;
    }

    public void resetDownloadRate() {
        this.downloadRate = 0;
    }

}
