package com.atguigu.gmall.search;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallSearchServiceApplicationTests {
    @Autowired
    JestClient jestClient;
    @Reference
    SkuService skuService;

    @Test
    public void contextLoads() throws Exception {
        Search search = new Search.Builder("{}").addIndex("gmall1015").addType("pmsSearchSkuInfo").build();
        SearchResult execute = jestClient.execute(search);
        Long total = execute.getTotal();
        System.out.println(total + "----------------");
        //查询mysql中的数据
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSkuInfo();

        //转换成pmssearchskuinfo
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);
            pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }
        System.out.println(pmsSearchSkuInfoList.size());
        //导入到es中
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index index = new Index.Builder(pmsSearchSkuInfo).index("gmall1015").type("pmsSearchSkuInfo").id(pmsSearchSkuInfo.getId() + "").build();
            jestClient.execute(index);
        }
    }


}
