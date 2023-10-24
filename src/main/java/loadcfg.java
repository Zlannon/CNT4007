import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class loadcfg
{

    public ArrayList<String[]> readcfgfile(String filename) throws IOException
    {
        ArrayList<String[]> peers = new ArrayList<String[]>(0);
        String line;
        String[] info;
        //File file = new File(".\\src\\main\\java\\project_config_file_large\\PeerInfo.cfg");
        //for(String filenames : file.list())
        //{
       //     System.out.println(filenames);
       // }
        try
        {
            BufferedReader cfgreader = new BufferedReader(new FileReader(filename));
           while((line = cfgreader.readLine())!= null)
           {
               info = line.split(" ");
               peers.add(info);
           }
        }
        catch (FileNotFoundException e)
        {
            throw new IOException(filename + " does not exist!");
        }

        return peers;

    }
}
