import java.io.*;
import java.nio.ByteBuffer;

public class Message {
    private MessageType type;
    private byte[] payload;

    public Message(MessageType type, byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public static Message createChokeMessage() {
        return new Message(MessageType.CHOKE, new byte[0]);
    }

    public static Message createUnchokeMessage() {
        return new Message(MessageType.UNCHOKE, new byte[0]);
    }

    public static Message createInterestedMessage() {
        return new Message(MessageType.INTERESTED, new byte[0]);
    }

    public static Message createNotInterestedMessage() {
        return new Message(MessageType.NOT_INTERESTED, new byte[0]);
    }

    public static Message createHaveMessage(int pieceIndex) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(pieceIndex);
        return new Message(MessageType.HAVE, buffer.array());
    }

    public static Message createBitfieldMessage(byte[] bitfield) {
        return new Message(MessageType.BITFIELD, bitfield);
    }

    public static Message createRequestMessage(int pieceIndex) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(pieceIndex);
        return new Message(MessageType.REQUEST, buffer.array());
    }

    public static Message createPieceMessage(int pieceIndex, byte[] pieceData) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(pieceIndex);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(buffer.array());
            outputStream.write(pieceData);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Message(MessageType.PIECE, outputStream.toByteArray());
    }

    //toByteArray() method
    // Serialize the message to a byte array
    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
            objStream.writeObject(this);
            return byteStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Deserialize a byte array into a Message object
    public static Message fromByteArray(byte[] byteArray) {
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            return (Message) objStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
