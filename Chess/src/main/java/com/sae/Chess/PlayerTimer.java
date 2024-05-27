import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerTimer {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> future;
    private long timeLeft;
    private final long increment;
    private final Runnable timeOutAction;

    public PlayerTimer(long initialTime, long increment, Runnable timeOutAction) {
        this.timeLeft = initialTime;
        this.increment = increment; // 0 valid
        this.timeOutAction = timeOutAction;
    }

    public void start() {
        future = scheduler.scheduleAtFixedRate(() -> {
                timeLeft -= 1000;
                if (timeLeft <= 0) {
                    future.cancel(true);
                    timeOutAction.run();
                }
            }, 1, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        timeLeft += increment;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public void reset(long newTime) {
        stop();
        this.timeLeft = newTime;
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
