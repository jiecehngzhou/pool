package com;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pojo.User;
import com.pool.ApplicationThreadPool;
import com.service.UserService;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


import java.util.concurrent.CountDownLatch;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MillionDataApplicationTest {

    @Autowired
    UserService userService;

    @Autowired
    ApplicationThreadPool applicationThreadPool;

    @Value("${excel.filePath}")
    private String EXCEL_WRITER_PATH;

    @Test
    public void test() {
        System.out.println(userService.list(new QueryWrapper<User>().le("id", 100)));
    }

    @Test
    public void testMillionDataWrite() {
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            String fileName = EXCEL_WRITER_PATH + "excel/testMillionDataWrite" + finalI + ".xlsx";
            EasyExcel.write(fileName, User.class)
                    .sheet("模板" + finalI + 1)
                    .doWrite(() -> {
                        // 分页查询数据
                        return userService.list(new QueryWrapper<User>().gt("id", 100000 * finalI).le("id", 100000 * (finalI + 1)));
                    });
            System.out.println("完成" + finalI);
        }
    }


    /**
     * 无法同时写一个excel文件 有需要可以用POI
     */
    @Test
    public void testMillionDataDataSourceWrite() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            new Thread(() -> {
                String fileName = EXCEL_WRITER_PATH + "excel/testMillionDataWrite" + finalI + ".xlsx";
                EasyExcel.write(fileName, User.class)
                        .sheet("模板" + finalI + 1)
                        .doWrite(() -> {
                            // 分页查询数据
                            return userService.list(new QueryWrapper<User>().gt("id", 100000 * finalI).le("id", 100000 * (finalI + 1)));
                        });
                latch.countDown();
                System.out.println("完成" + finalI);
            }).start();
        }
        latch.await();
    }


    @Test
    /**
     * 单挑插入 太长
     * 批量插入 224651
     * 多线程批量插入 2个线程：123872 5个线程：72036
     */
    public void testMillionDataDataSourceRead() {
        String fileName = EXCEL_WRITER_PATH + "simpleWrite" + ".xlsx";
        long startTime = System.currentTimeMillis();
        EasyExcel.read(fileName, User.class, new DataListener(userService, applicationThreadPool)).sheet().doRead();
        long endTime = System.currentTimeMillis();
        System.out.println("花费时间：" + (endTime-startTime));
    }



}
