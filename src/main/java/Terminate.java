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
            if(this.peer.checkDone()) {
                this.peer.stopThreads();
                this.stop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.scheduler.shutdownNow();
    }
}
