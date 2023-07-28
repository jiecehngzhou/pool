package com;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pojo.User;
import com.pool.ApplicationThreadPool;
import com.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class DataSourceTest {

    @Autowired
    UserService userService;

    @Autowired
    ApplicationThreadPool applicationThreadPool;

    AtomicInteger requestNum = new AtomicInteger(0);

    AtomicLong requestTime = new AtomicLong(0);

    CyclicBarrier cyclicBarrier = new CyclicBarrier(CONCURRENT_NUM);

    CountDownLatch countDownLatch = new CountDownLatch(CONCURRENT_NUM);

    private static final int CONCURRENT_NUM = 500;

    /**
     * mysql连接数
     * show variables like '%max_connection%'; 查看最大连接数
     * show global status like 'Max_used_connections'; 查看最大使用数
     * set global max_connections=600;        重新设置最大连接数,在并发的85%最优
     *
     * 连接池数（500并发下）
     * (cpu核心数*2+有效磁盘数)10*2+1与最大连接数500效果对比
     *
     *500个数据库连接
     *第一轮 38007207
     *第二轮 28316658
     *
     *21个数据库连接
     *第一轮 10388054
     *第二轮 10720564
     */
    @Test
    public void testDataSourceNum(){
        for (int i = 0; i < CONCURRENT_NUM; i++) {
            new Thread(()->{
                    try {
                        cyclicBarrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                    long startTime = System.currentTimeMillis();

                    QueryWrapper<User> wrapper = new QueryWrapper();
                    wrapper.eq("id", requestNum.incrementAndGet());
                    wrapper.or();
                    wrapper.eq("name","*****");
                    userService.getOne(wrapper);

                    long endTime = System.currentTimeMillis();
                    requestTime.set(requestTime.get()+(endTime-startTime));
                    countDownLatch.countDown();

            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(requestTime.get());
    }


    @Test
    /**
     * 线程池参数
     */
    public void poolTest() {
        Runnable runnable = () -> {
            try {
                Thread.sleep(1000);
                System.out.println("消费成功...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < CONCURRENT_NUM; i++) {
            new Thread(()->{
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                applicationThreadPool.execute(runnable);
            }).start();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
