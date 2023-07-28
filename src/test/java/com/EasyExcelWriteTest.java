package com;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pojo.User;
import com.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EasyExcelWriteTest {

    @Autowired
    UserService userService;

    @Value("${excel.filePath}")
    private String EXCEL_WRITER_PATH;

    /**
     * 最简单的对象写
     */
    @Test
    public void simpleWrite() {
        // 注意 simpleWrite在数据量不大的情况下可以使用（5000以内，具体也要看实际情况），数据量大参照 重复多次写入

        // 写法1 JDK8+
        // since: 3.0.0-beta1
        String fileName = EXCEL_WRITER_PATH + "simpleWrite" + ".xlsx";
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        EasyExcel.write(fileName, User.class)
            .sheet("模板")
            .doWrite(() -> {
                // 分页查询数据
                return userService.list(new QueryWrapper<User>().le("id",2000));
            });

        // 写法2
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        // 如果这里想使用03 则 传入excelType参数即可
        /*EasyExcel.write(fileName, User.class)
                .sheet("模板")
                .doWrite(userService.list(new QueryWrapper<User>().le("id",2000)));*/
    }

    /**
     * 不创建对象的写
     */
    @Test
    public void noModelWrite() {
        // 写法1
        String fileName = EXCEL_WRITER_PATH + "noModelWrite" + ".xlsx";

        List<List<String>> headList = ListUtils.newArrayList();
        List<String> head0 = ListUtils.newArrayList();
        head0.add("id");
        List<String> head1 = ListUtils.newArrayList();
        head1.add("姓名");
        List<String> head2 = ListUtils.newArrayList();
        head2.add("年龄");
        List<String> head3 = ListUtils.newArrayList();
        head3.add("性别");
        List<String> head4 = ListUtils.newArrayList();
        head4.add("创建日期");
        headList.add(head0);
        headList.add(head1);
        headList.add(head2);
        headList.add(head3);
        headList.add(head4);
        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
        EasyExcel.write(fileName).head(headList)
                .sheet("模板")
                .doWrite(userService.list(new QueryWrapper<User>().le("id",100)));
    }


    /**
     * 重复多次写入
     */
    @Test
    public void repeatedWrite() {
        // 方法1: 如果写到同一个sheet
        String fileName = EXCEL_WRITER_PATH + "repeatedWrite1" + ".xlsx";
        // 这里 需要指定写用哪个class去写
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, User.class).build()) {
            // 这里注意 如果同一个sheet只要创建一次
            WriteSheet writeSheet = EasyExcel.writerSheet("模板").build();
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来
            for (int i = 0; i < 5; i++) {
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<User> data = userService.list(new QueryWrapper<User>().gt("id",100*i).le("id",100*(i+1)));
                excelWriter.write(data, writeSheet);
            }
        }

        // 方法2: 如果写到不同的sheet 同一个对象
        fileName = EXCEL_WRITER_PATH + "repeatedWrite2" + ".xlsx";
        // 这里 指定文件
        try (ExcelWriter excelWriter = EasyExcel.write(fileName, User.class).build()) {
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<User> data = userService.list(new QueryWrapper<User>().gt("id",100*i).le("id",100*(i+1)));
                excelWriter.write(data, writeSheet);
            }
        }

        // 方法3 如果写到不同的sheet 不同的对象
        fileName = EXCEL_WRITER_PATH + "repeatedWrite3" + ".xlsx";
        // 这里 指定文件
        try (ExcelWriter excelWriter = EasyExcel.write(fileName).build()) {
            // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
            for (int i = 0; i < 5; i++) {
                // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。这里注意User.class 可以每次都变，我这里为了方便 所以用的同一个class
                // 实际上可以一直变
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "模板" + i).head(User.class).build();
                // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                List<User> data = userService.list(new QueryWrapper<User>().gt("id",100*i).le("id",100*(i+1)));
                excelWriter.write(data, writeSheet);
            }
        }
    }

}