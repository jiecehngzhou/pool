package com.pool;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApplicationThreadPool {

    /**
     * FixedThreadPool 和 SingleThread Pool允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致 OOM。
     * CachedThreadPool 和 ScheduledThreadPool允许的创建线程数量为 Integer.MAX_VALUE 可能会创建大量的线程，从而导致 OOM。
     */
    private ThreadPoolExecutor threadPoolExecutor;

    private LinkedBlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>(100);

    private ThreadFactory threadFactory = new ThreadFactory() {
        private AtomicInteger threadNumber = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r) {
            String threadName = "applicationThreadPool-" + threadNumber.incrementAndGet();
            System.out.println(threadName + "...start");
            return new Thread(r,  threadName);
        }
    };

    /**
     * 线程
     * CPU密集型任务
     * 尽量使用较小的线程池，一般为CPU核心数+1。
     * 因为CPU密集型任务使得CPU使用率很高，若开过多的线程数，只能增加上下文切换的次数，因此会带来额外的开销。
     *
     * IO密集型任务
     * 可以使用较大的线程池，一般CPU核心数 * 2
     * IO密集型CPU使用率不高，可以让CPU等待IO的时候处理别的任务，充分利用cpu时间
     *
     * 混合型任务
     * 可以使用稍大的线程池，参考公式：CPU核数/阻塞系数(请求时间/总时间)。
     * 假设 8核cpu在处理一个请求的过程中，总共耗时100+5=105ms换算为
     * 8/(5/105)=170
     * 阻塞系数为0.8～0.9之间 IO密集型任务CPU使用率并不高，因此可以让CPU在等待IO的时候去处理别的任务，充分利用CPU时间。
     */
    private ApplicationThreadPool(){
        threadPoolExecutor = new ThreadPoolExecutor(11,
                20,
                180,
                TimeUnit.SECONDS,
                workQueue,
                threadFactory,
                new ApplicationRejectedExecutionHandler());
    }


    public void execute(Runnable runnable){
        threadPoolExecutor.execute(runnable);
    }

    public Future<?> submit(Runnable runnable){
        return threadPoolExecutor.submit(runnable);
    }
}
