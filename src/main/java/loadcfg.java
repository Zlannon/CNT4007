import java.io.*;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Scanner;

public class loadcfg
{

    public Dictionary<String, String[]> readcfgfile(String filename) throws IOException
    {
        Dictionary<String, String[]> cfgInfo = new Hashtable<>();
        String line;
        String[] info;
        //File file = new File(".\\src\\main\\java\\project_config_file_large\\PeerInfo.cfg");
        //for(String filenames : file.list())
        //{
       //     System.out.println(filenames);
       // }
        try
        {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while((line = in.readLine()) != null)
            {
                String[] tokens = line.split("\\s+");
                cfgInfo.put(tokens[0], tokens);
            }

            in.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return cfgInfo;

    }
}
