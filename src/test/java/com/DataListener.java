package com;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.pojo.User;
import com.pool.ApplicationThreadPool;
import com.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
//不能被spring管理 每次操作数据需要创建新的DataListener
//数量限制不是越大越好 可能会造成缓存数据OOM，cpu切片频繁，线程池任务爆满
public class DataListener implements ReadListener<User> {

    UserService userService;

    ApplicationThreadPool applicationThreadPool;

    /**
     * 单次缓存的数据量
     */
    public static final int BATCH_COUNT = 100000;
    /**
     * 单次插入的数据量
     */
    public static final int BATCH_SIZE = 20000;
    /**
     *临时存储
     */
    private  List<User> cachedDataList = ListUtils.newArrayListWithCapacity(BATCH_COUNT);
    /**
     *消费本次缓存记录的线程大小
     */
    private static final int THREAD_NUM = 5;

    public DataListener(UserService userService, ApplicationThreadPool applicationThreadPool){
        this.userService = userService;
        this.applicationThreadPool = applicationThreadPool;
    }

    public DataListener(UserService userService){
        this.userService = userService;
    }

    @Override
    public void invoke(User data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            /*saveBatchData();*/
            /*saveMillionData();*/
            // 存储完成清理 list
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //确保上次遗留数据
        saveData();
    }

    private void saveData() {
        cachedDataList.forEach(user->userService.save(user));
    }

    private void saveBatchData() {
        userService.saveBatch(cachedDataList, BATCH_SIZE);
    }

    private void saveMillionData() {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
        for (int i = 0; i < THREAD_NUM; i++) {
            List<User> saveUserList = new ArrayList<>();
            int pageSize = cachedDataList.size() / THREAD_NUM;
            cachedDataList.stream().skip(pageSize*i).limit(pageSize).forEach(saveUserList::add);
            Runnable runnable = () -> {
                userService.saveBatch(saveUserList, BATCH_SIZE);
                countDownLatch.countDown();
            };
            applicationThreadPool.submit(runnable);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("导入异常："+e.getMessage());
        }
    }

}
