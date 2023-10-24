import java.io.IOException;
import java.util.ArrayList;

public class peerslist
{
        class peer
        {
            private int peerid;
            private String hostname;
            private int listeningport;
            private int hasfileornot;
            public void setpeerid(int peerid)
            {
                 this.peerid = peerid;
            }
            public void setpeerhostname(String hostname)
            {
                this.hostname = hostname;
            }
            public void setpeerlisteningport(int listeningport)
            {
                this.listeningport = listeningport;
            }
            public void setpeerfileornot(int hasfileornot)
            {
                this.hasfileornot = hasfileornot;
            }
            public int getpeerid()
            {
                return peerid;
            }
            public String getpeerhostname(int id)
            {
                return hostname;
            }
            public int getpeerlisteningport()
            {
                return listeningport;
            }
            public int getpeerfileornot()
            {
                return hasfileornot;
            }
        }
        private ArrayList<peer> peerslist = new ArrayList<peer>(0);

        public void addpeer(String[] peerinfo) throws Exception
        {
            if(peerinfo.length != 4)
            {
                throw new IOException("This is not a correct peer string array!");
            }
            else
            {
                peer pp = new peer();
                pp.setpeerid(Integer.parseInt(peerinfo[0]));
                pp.setpeerhostname(peerinfo[1]);
                pp.setpeerlisteningport(Integer.parseInt(peerinfo[2]));
                pp.setpeerfileornot(Integer.parseInt(peerinfo[3]));
                peerslist.add(pp);
            }
        }



}
