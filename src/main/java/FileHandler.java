import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileHandler
{
    private final String baseDirectory; // Base directory where all peer subdirectories are located
    public FileHandler(String baseDirectory)
    {
        this.baseDirectory = baseDirectory;
    }

    public void createPeerDirectory(int peerID)
    {
        File peerDirectory = new File(baseDirectory + "peer_" + peerID);
        if (!peerDirectory.exists())
        {
            if (peerDirectory.mkdirs())
            {
                System.out.println("Created directory for Peer " + peerID);
            } else {
                System.err.println("Failed to create directory for Peer " + peerID);
            }
        }
    }

    public void saveFile(int peerID, String fileName, byte[] data) throws IOException
    {
        File peerDirectory = new File(baseDirectory + "peer_" + peerID);
        if (!peerDirectory.exists() || !peerDirectory.isDirectory())
        {
            System.err.println("Peer directory does not exist for Peer " + peerID);
            return;
        }

        File file = new File(peerDirectory, fileName);
        File parentDirectory = file.getParentFile();

        if (!parentDirectory.exists() && !parentDirectory.mkdirs())
        {
            throw new IOException("Failed to create parent directory for file: " + fileName);
        }

        java.nio.file.Files.write(file.toPath(), data);
        System.out.println("Saved file " + fileName + " for Peer " + peerID);
    }

    public boolean doesPeerDirectoryExist(int peerID)
    {
        File peerDirectory = new File(baseDirectory + "peer_" + peerID);
        return peerDirectory.exists() && peerDirectory.isDirectory();
    }

    public boolean doesFileExist(int peerID, String fileName) {
        File peerDirectory = new File(baseDirectory + "peer_" + peerID);
        if (peerDirectory.exists() && peerDirectory.isDirectory()) {
            File file = new File(peerDirectory, fileName);
            return file.exists();
        }
        return false;
    }
}
