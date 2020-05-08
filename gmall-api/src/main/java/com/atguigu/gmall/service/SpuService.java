package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;


import java.util.List;

/**
 * SpuService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
public interface SpuService {

    List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    void saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListBysql(String productId, String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);
}

