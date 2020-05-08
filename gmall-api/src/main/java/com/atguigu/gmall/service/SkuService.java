package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSkuInfo;

import java.util.List;

/**
 * SkuService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
public interface SkuService {
    void saveSkuInfo(PmsSkuInfo skuInfo);

    PmsSkuInfo getSkuInfoById(String skuId);

    List<PmsSkuInfo> getAllSkuInfo();
}

