import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Dictionary;

public class peerProcess {

    public static void main(String[] args) throws Exception {
        String st;
        Dictionary<String, String[]> peerInfo = new Hashtable<>();
        Dictionary<String, String[]> cfgInfo = new Hashtable<>();

        if(args.length == 0) {
            System.exit(0);
        }

        loadcfg configReader = new loadcfg();
        peerInfo = configReader.readcfgfile(".\\src\\main\\java\\project_config_file_large\\PeerInfo.cfg");
        cfgInfo = configReader.readcfgfile(".\\src\\main\\java\\project_config_file_large\\Common.cfg");


        Server server = new Server(Integer.parseInt(peerInfo.get(args[0])[0]), 8000, 1001);

        Client client = new Client();
        client.startConnection("localhost", 8000);

        System.out.println(peerInfo.get(args[0])[0] + " " + peerInfo.get(args[0])[1] + " " + peerInfo.get(args[0])[2] + " " + peerInfo.get(args[0])[3]);
    }
}
