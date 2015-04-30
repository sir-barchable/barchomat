package sir.barchable.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Sir Barchable
 *         Date: 20/04/15
 */
public class ThreadPools {

    /**
     * Executor that creates threads on demand but does not reuse them.
     *
     * @param prefix thread name prefix (suffix will be the creation count)
     */
    public static ExecutorService newExecutor(String prefix) {
        return new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            0L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactory() {
                private AtomicInteger count = new AtomicInteger();
                public Thread newThread(Runnable r) {
                    return new Thread(r, prefix + "-" + count.incrementAndGet());
                }
            }
        );
    }
}
