package com;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.fastjson.JSON;
import com.pojo.User;
import com.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class EasyExcelReadTest {

    @Autowired
    UserService userService;

    @Value("${excel.filePath}")
    private String EXCEL_WRITER_PATH;

    /**
     *  最简单的对象读
     */
    @Test
    public void simpleRead() {
        String fileName = EXCEL_WRITER_PATH + "simpleWrite" + ".xlsx";
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName , User.class, new PageReadListener<User>(dataList -> {
            for (User demoData : dataList) {
                log.info("读取到一条数据{}", JSON.toJSONString(demoData));
            }
        })).sheet().doRead();

        // 写法2：
        // 匿名内部类 不用额外写一个DemoDataListener
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 文件流会自动关闭
        EasyExcel.read(fileName, User.class, new DataListener(userService)).sheet().doRead();
    }


    /**
     * 指定列的下标或者列名
     * 创建excel对应的实体对象,并使用ExcelProperty
     */
    @Test
    public void indexOrNameRead() {
        String fileName = EXCEL_WRITER_PATH + "noModelWrite" + ".xlsx";
        // 这里默认读取第一个sheet
        EasyExcel.read(fileName, User.class, new DataListener(userService)).sheet().doRead();
    }

    /**
     * 读多个或者全部sheet,这里注意一个sheet不能读取多次，多次读取需要重新读取文件
     * 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器
     */
    @Test
    public void repeatedRead() {
        String fileName = EXCEL_WRITER_PATH + "repeatedWrite2" + ".xlsx";
        // 读取全部sheet
        // 这里需要注意 DemoDataListener的doAfterAllAnalysed 会在每个sheet读取完毕后调用一次。然后所有sheet都会往同一个DemoDataListener里面写
        EasyExcel.read(fileName, User.class, new DataListener(userService)).doReadAll();

        // 写法1
        try (ExcelReader excelReader = EasyExcel.read(fileName).build()) {
            // 这里为了简单 所以注册了 同样的head 和Listener 自己使用功能必须不同的Listener
            ReadSheet readSheet1 =
                    EasyExcel.readSheet(0).head(User.class).registerReadListener(new DataListener(userService)).build();
            ReadSheet readSheet2 =
                    EasyExcel.readSheet(1).head(User.class).registerReadListener(new DataListener(userService)).build();
            // 这里注意 一定要把sheet1 sheet2 一起传进去，不然有个问题就是03版的excel 会读取多次，浪费性能
            excelReader.read(readSheet1, readSheet2);
        }
    }
}
