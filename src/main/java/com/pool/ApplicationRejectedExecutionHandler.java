package com.pool;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class ApplicationRejectedExecutionHandler implements RejectedExecutionHandler {


    @Override
    /**
     * 自定义拒绝策略
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        throw new RejectedExecutionException("丢弃Task");
    }


}
