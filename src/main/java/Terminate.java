import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Terminate implements Runnable {
    private Peer peer;
    private ScheduledExecutorService scheduler = null;

    Terminate(Peer peer) {

        this.peer = peer;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startJob(int timeinterval) {
        scheduler.scheduleAtFixedRate(this, 30, timeinterval * 2, TimeUnit.SECONDS);
    }

    public void run() {
        try {
            //check if the peer is done
            if(this.peer.checkDone()) {
                //stop the threads
                this.peer.stopThreads();
                //stop the scheduler
                this.stop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        //stop the scheduler
        this.scheduler.shutdownNow();
    }
}
