import java.io.IOException;
import java.util.ArrayList;

public class peerslist
{
        static class peer
        {
            private int peerid;
            private String hostname;
            private int listeningport;
            private int hasfileornot;
            private float datarate;
            private boolean ischoked;
            private boolean isoptimisticneighbor;
            private boolean ispreferredneighbor;
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
            public void setdatarate(float datarate)
            {
                this.datarate = datarate;
            }
            public void setischoked(boolean choked)
            {
                this.ischoked = choked;
            }
            public void setisoptimisticneighbor(boolean opt)
            {
                this.isoptimisticneighbor = opt;
            }
            public void setispreferredneighbor(boolean preferred)
            {
                this.ispreferredneighbor = preferred;
            }
            public int getpeerid()
            {
                return peerid;
            }
            public String getpeerhostname()
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
            public float getdatarate()
            {
                return datarate;
            }
            public boolean getchoked()
            {
                return ischoked;
            }
            public boolean getisoptimisticneighbor()
            {
                return isoptimisticneighbor;
            }
            public boolean getispreferredneighbor()
            {
                return ispreferredneighbor;
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
                pp.setischoked(true);
                pp.setdatarate(0.0f);
                pp.setisoptimisticneighbor(false);
                pp.setispreferredneighbor(false);
                peerslist.add(pp);
            }
        }

        public peer getpeer(int peerid)
        {
            peer pp = new peer();
            for (int i =0; i < peerslist.size(); i++)
            {
                if(peerid == peerslist.get(i).getpeerid())
                {
                    pp = peerslist.get(i);
                }
            }
            return pp;
        }

        public ArrayList<peer> findpreferredneighbors()
        {
            float smallest = 0.0f;
            ArrayList<peer> pp = new ArrayList<peer>(0);
            for(int i =0; i < this.peerslist.size(); i++)
            {
                if(peerslist.get(i).getisoptimisticneighbor() == true)
                {
                    smallest = peerslist.get(i).getdatarate();
                    pp.add(peerslist.get(i));
                    break;
                }
            }
            for(int i =0; i < this.peerslist.size()-1; i++)
            {
                if(peerslist.get(i).getdatarate() >= smallest)
                {
                    peerslist.get(i).setischoked(false);
                    pp.add(peerslist.get(i));
                }
            }
            for(int i =0; i < pp.size(); i++ )
            {
                if(pp.get(i).getpeerid() != peerslist.get(i).getpeerid())
                {
                    peerslist.get(i).setischoked(true);
                }
            }
            return pp;
        }
        public ArrayList<peer> getpeerslist()
        {
            return this.peerslist;
        }
}
