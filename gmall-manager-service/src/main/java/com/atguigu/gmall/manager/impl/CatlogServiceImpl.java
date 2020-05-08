package com.atguigu.gmall.manager.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseCatalog1;
import com.atguigu.gmall.bean.PmsBaseCatalog2;
import com.atguigu.gmall.bean.PmsBaseCatalog3;
import com.atguigu.gmall.manager.mapper.PmsBaseCatalog1Mapper;
import com.atguigu.gmall.manager.mapper.PmsBaseCatalog2Mapper;
import com.atguigu.gmall.manager.mapper.PmsBaseCatalog3Mapper;
import com.atguigu.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * CatlogServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-02
 * @Description:
 */
@Service
public class CatlogServiceImpl implements CatalogService {
    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;
    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;
    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;

    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
        pmsBaseCatalog2.setCatalog1Id(catalog1Id);
        return pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
        pmsBaseCatalog3.setCatalog2Id(catalog2Id);
        return pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
    }

    @Override
    public List<PmsBaseCatalog1> catlogFindAll() {
        //生成.json文件
        List<PmsBaseCatalog1> catalog1s = pmsBaseCatalog1Mapper.selectAll();
        for (PmsBaseCatalog1 catalog1 : catalog1s) {
            String catalog1Id = catalog1.getId();
            PmsBaseCatalog2 pmsBaseCatalog2 = new PmsBaseCatalog2();
            pmsBaseCatalog2.setCatalog1Id(catalog1Id);
            List<PmsBaseCatalog2> pmsBaseCatalog2s = pmsBaseCatalog2Mapper.select(pmsBaseCatalog2);
            catalog1.setCatalog2s(pmsBaseCatalog2s);
            for (PmsBaseCatalog2 baseCatalog2 : pmsBaseCatalog2s) {
                String catalog2Id = baseCatalog2.getId();
                PmsBaseCatalog3 pmsBaseCatalog3 = new PmsBaseCatalog3();
                pmsBaseCatalog3.setCatalog2Id(catalog2Id);
                List<PmsBaseCatalog3> pmsBaseCatalog3s = pmsBaseCatalog3Mapper.select(pmsBaseCatalog3);
                baseCatalog2.setCatalog3List(pmsBaseCatalog3s);
            }
        }

        return catalog1s;
    }
}
