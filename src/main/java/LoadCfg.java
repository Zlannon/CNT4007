import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Scanner;


public class LoadCfg {
    public int numPreferredNeighbors;
    public int UnchokingInterval;
    public int OptUnchokingInterval;
    public String FileName;
    public int FileSize;
    public int PieceSize;
    private HashMap<String,PeerInfo> peerInfoMap;
    private ArrayList<String> peerList;

    public LoadCfg() {
        this.peerInfoMap = new HashMap<>();
        this.peerList = new ArrayList<>();
    }

    public void loadCommonFile() {
        try {
            // Read the common config file
            FileReader fobj = new FileReader("Common.cfg");
            Scanner fReader = new Scanner(fobj);
            // Read the file line by line
            while (fReader.hasNextLine()) {
                String line = fReader.nextLine();
                String[] temp = line.split(" ");
                // Store the values in the appropriate variables
                if (temp[0].equals("numPreferredNeighbors")) {
                    this.numPreferredNeighbors = Integer.parseInt(temp[1]);
                } else if (temp[0].equals("UnchokingInterval")) {
                    this.UnchokingInterval = Integer.parseInt(temp[1]);
                } else if (temp[0].equals("OptimisticUnchokingInterval")) {
                    this.OptUnchokingInterval = Integer.parseInt(temp[1]);
                } else if (temp[0].equals("FileName")) {
                    this.FileName = temp[1];
                } else if (temp[0].equals("FileSize")) {
                    this.FileSize = Integer.parseInt(temp[1]);
                } else if (temp[0].equals("PieceSize")) {
                    this.PieceSize = Integer.parseInt(temp[1]);
                }
            }
            fReader.close();
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
    }

    // Load the peer info config file
    public void loadConfigFile()
    {
        String line;
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader("PeerInfo.cfg"));
            // Read the file line by line
            while((line = in.readLine()) != null) {
                // Split the line by whitespace
                String[] tokens = line.split("\\s+");
                // Store the values in the appropriate variables
                this.peerInfoMap.put(tokens[0],new PeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));
                this.peerList.add(tokens[0]);
            }
            in.close();
        }
        catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    // Get the peer info for a specific peer
    public PeerInfo getPeerConfig(String peerID){
        return this.peerInfoMap.get(peerID);
    }

    // Get the peer info map
    public HashMap<String, PeerInfo> getPeerInfoMap(){
        return this.peerInfoMap;
    }

    // Get the peer list
    public ArrayList<String> getPeerList(){
        return this.peerList;
    }
}
