package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsProductImage;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SkuController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
@RestController
@CrossOrigin
public class SkuController {
    @Reference
    SpuService spuService;
    @Reference
    SkuService skuService;

    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo skuInfo) {
        skuService.saveSkuInfo(skuInfo);
        return "success";
    }

    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId) {
        List<PmsProductImage> productImages = spuService.spuImageList(spuId);
        return productImages;
    }

    /**
     * 查询销售属性
     *
     * @param spuId
     * @return
     */
    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

}
