import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Handshake {
    private String header;
    private String peerID;

    public Handshake(String peerID) {
        this.header = "P2PFILESHARINGPROJ";
        this.peerID = peerID;
    }

    public String getPeerID(){
        return this.peerID;
    }

    public byte[] buildHandShake() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            stream.write(this.header.getBytes(StandardCharsets.UTF_8));
            stream.write(new byte[10]);
            stream.write(this.peerID.getBytes(StandardCharsets.UTF_8));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return stream.toByteArray();
    }

    public void readHandShake(byte[] message){
        String msg = new String(message,StandardCharsets.UTF_8);
        this.peerID = msg.substring(28,32);
    }
}
