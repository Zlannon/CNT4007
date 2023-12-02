import java.util.*;
import java.io.*;
import java.nio.*;

public class Message {
    private int messageLength;
    private char messageType;
    private byte[] messagePayload;

    public Message() {

    }

    public Message(char messageType) {
        this.messageType = messageType;
        this.messageLength = 1;
        this.messagePayload = new byte[0];
    }

    public Message(char messageType, byte[] messagePayload) {
        this.messageType = messageType;
        this.messagePayload = messagePayload;
        this.messageLength = this.messagePayload.length + 1;
    }

    public byte[] buildMessage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            byte[] bytes = ByteBuffer.allocate(4).putInt(this.messageLength).array();
            stream.write(bytes);
            stream.write((byte) this.messageType);
            stream.write(this.messagePayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    public void readMessage(int len, byte[] message) {
        this.messageLength = len;
        this.messageType = getMessageType(message, 0);
        this.messagePayload = getPayload(message, 1);
    }

    public int getIntFromByteArray(byte[] message, int start) {
        byte[] len = new byte[4];
        for (int i = 0; i < 4; i++) {
            len[i] = message[i + start];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(len);
        return byteBuffer.getInt();
    }

    public char getMessageType(byte[] message, int index) {
        return (char) message[index];
    }

    public byte[] getPayload(byte[] message, int index) {
        byte[] response = new byte[this.messageLength - 1];
        System.arraycopy(message, index, response, 0, this.messageLength - 1);
        return response;
    }

    public BitSet getBitFieldMessage() {
        BitSet bits = new BitSet();
        bits = BitSet.valueOf(this.messagePayload);
        return bits;
    }

    public int getPieceIndexFromPayload() {
        return getIntFromByteArray(this.messagePayload, 0);
    }

    public byte[] getPieceFromPayload() {
        int size = this.messageLength - 5;
        byte[] piece = new byte[size];
        for (int i = 0; i < size; i++) {
            piece[i] = this.messagePayload[i + 4];
        }
        return piece;
    }
}
