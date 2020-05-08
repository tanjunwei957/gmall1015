package com.atguigu.gmall.search;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.service.CatalogService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchWebApplicationTests {
    @Reference
    CatalogService catalogService;
    @Test
     public void contextLoads() throws Exception {
        //生成页面的静态json文件，生成catlog文件
        List<PmsBaseCatalog1> pmsBaseCatalog1List=catalogService.catlogFindAll();
        //得到三层集合， 准备文件流
        File file = new File("d:/catalog.json");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        String json = JSON.toJSONString(pmsBaseCatalog1List);
        fileOutputStream.write(json.getBytes());

    }

}
