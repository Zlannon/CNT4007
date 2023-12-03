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
        //initialize class variables
        this.socket = socket;
        this.peer = peer;
        initstreams();
        this.handshake = new Handshake(this.peer.getPeerID());

    }

    public void initstreams() {
        try {
            //make input/output streams
            this.out = new ObjectOutputStream(this.socket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(this.socket.getInputStream());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOtherPeerID(String peerID) {
        //sets the other peerID
        this.otherPeerID = peerID;
        this.initializer = true;
    }

    public void run() {
        try {
            //build a handshake
            byte[] msg = this.handshake.buildHandShake();
            //send handshake
            this.out.write(msg);
            this.out.flush();
            while (true) {
                //if not connected
                if (!this.connected) {
                    //process handshake from peer
                    byte[] response = new byte[32];
                    this.in.readFully(response);
                    this.processHandShake(response);
                    if (this.peer.hasFile() || this.peer.getAvailabilityOf(this.peer.getPeerID()).cardinality() > 0) {
                        //send bitfield if peer has file
                        this.sendBitField();
                    }
                } else {
                    while (this.in.available() < 4) {
                    }
                    //get message size
                    int respSize = this.in.readInt();
                    //get the response
                    byte[] response = new byte[respSize];
                    this.in.readFully(response);
                    //get the message type
                    char messageType = (char) response[0];
                    Message message = new Message();
                    //read the message
                    message.readMessage(respSize, response);
                    int pieceIndex;
                    int requestindex;
                    //switch statement based on message type
                    switch (messageType) {
                        case '0':
                            //Choke message
                            this.peer.clearRequested(this.otherPeerID);
                            //loke choke
                            this.peer.getLogger().logChoking(this.otherPeerID);
                            break;
                        case '1':
                            //unchoke message
                            requestindex = this.peer.checkRequested(this.otherPeerID);
                            //send not interested/request message
                            if (requestindex == -1) {
                                this.sendNotInterestedMessage();
                            } else {
                                this.sendRequestMessage(requestindex);
                            }
                            //log unchoke
                            this.peer.getLogger().logUnchoking(this.otherPeerID);
                            break;
                        case '2':
                            //interested message
                            this.peer.addToInterestedList(this.otherPeerID);
                            //log interested
                            this.peer.getLogger().logInterested(this.otherPeerID);
                            break;
                        case '3':
                            //not interested
                            this.peer.removeFromInterestedList(this.otherPeerID);
                            //log not interested
                            this.peer.getLogger().logNotInterested(this.otherPeerID);
                            break;
                        case '4':
                            //have
                            pieceIndex = message.getPieceIndexFromPayload();
                            this.peer.updatePieceAvailability(this.otherPeerID, pieceIndex);
                            //check if all done
                            if (this.peer.checkAllDone()) {
                                //stop everything
                                this.peer.stopAll();
                            }
                            //check if interested and send respective message
                            if (this.peer.checkInterested(this.otherPeerID)) {
                                this.sendInterestedMessage();
                            } else {
                                this.sendNotInterestedMessage();
                            }
                            //log have
                            this.peer.getLogger().logHave(this.otherPeerID, pieceIndex);
                            break;
                        case '5':
                            //bitfield message
                            BitSet bitset = message.getBitFieldMessage();
                            //process bitfield
                            this.processBitFieldMessage(bitset);
                            //send interested/not interested
                            if (!this.peer.hasFile()) {
                                if (this.peer.checkInterested(this.otherPeerID)) {
                                    this.sendInterestedMessage();
                                } else {
                                    this.sendNotInterestedMessage();
                                }
                            }
                            break;
                        case '6':
                            //request message
                            if (this.peer.getUnchokedList().contains(this.otherPeerID) || (this.peer.getOptUnchokedPeer() != null && this.peer.getOptUnchokedPeer().compareTo(this.otherPeerID) == 0)) {
                                //get the piece index
                                pieceIndex = message.getPieceIndexFromPayload();
                                //send a piece message
                                this.sendPieceMessage(pieceIndex, this.peer.readFromFile(pieceIndex));
                            }
                            break;
                        case '7':
                            //piece message
                            pieceIndex = message.getPieceIndexFromPayload();
                            byte[] piece = message.getPieceFromPayload();
                            //write piece to file
                            this.peer.writeToFile(piece, pieceIndex);
                            //update that peer has the piece available now
                            this.peer.updatePieceAvailability(this.peer.getPeerID(), pieceIndex);
                            //increase download rate
                            this.downloadRate = this.downloadRate + 1;
                            Boolean allDone = this.peer.checkAllDone();
                            //log the downloaded piece
                            this.peer.getLogger().logDownloadedPiece(this.otherPeerID, pieceIndex, this.peer.getCompletedPieceCount());
                            //set the requested info and send a have message
                            this.peer.setRequestedInfo(pieceIndex, null);
                            this.peer.sendHave(pieceIndex);
                            if (this.peer.getAvailabilityOf(this.peer.getPeerID()).cardinality() != this.peer.getPieceCount()) {
                                //get the requested index
                                requestindex = this.peer.checkRequested(this.otherPeerID);
                                if (requestindex != -1) {
                                    //send request message
                                    this.sendRequestMessage(requestindex);
                                } else {
                                    //send not interested message
                                    this.sendNotInterestedMessage();
                                }
                            } else {
                                //log finished
                                this.peer.getLogger().logCompletion();
                                if (allDone) {
                                    //stop everything
                                    this.peer.stopAll();
                                }
                                //send not interested message
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
            //send message
            this.out.write(obj);
            this.out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendChokedMessage() {
        try {
            //build the choke message
            Message msg = new Message('0');
            //send the choke message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUnChokedMessage() {
        try {
            //build the unchoke message
            Message msg = new Message('1');
            //send the unchoke message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInterestedMessage() {
        try {
            //build the interested message
            Message msg = new Message('2');
            //send the interested message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotInterestedMessage() {
        try {
            //build the not interested message
            Message msg = new Message('3');
            //send the not interested message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendHaveMessage(int pieceIndex) {
        try {
            //build the have message
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            Message msg = new Message('4', bytes);
            //send the have message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendBitField() {
        try {
            //build the bitfield message
            BitSet myAvailability = this.peer.getAvailabilityOf(this.peer.getPeerID());
            Message msg = new Message('5', myAvailability.toByteArray());
            //send the bitfield message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRequestMessage(int pieceIndex) {
        try {
            //build the request message
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            Message msg = new Message('6', bytes);
            //send the request message
            this.send(msg.buildMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendPieceMessage(int pieceIndex, byte[] payload) {
        try {
            //Build the piece message
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            byte[] bytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
            //send the piece message
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
            //read handshake
            this.handshake.readHandShake(message);
            //get peer id from handshake
            this.otherPeerID = this.handshake.getPeerID();
            //add connected peer
            this.peer.addJoinedPeer(this, this.otherPeerID);
            this.peer.addJoinedThreads(this.otherPeerID, Thread.currentThread());
            this.connected = true;
            //send TCP log message
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
        //update bitset
        this.peer.updateBitset(this.otherPeerID, b);
    }

    public int getDownloadRate() {
        //return download rate
        return this.downloadRate;
    }

    public void resetDownloadRate() {
        //set download rate to 0
        this.downloadRate = 0;
    }

}
