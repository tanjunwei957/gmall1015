package com.atguigu.gmall.manager.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manager.mapper.*;
import com.atguigu.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * SpuImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
@Service
public class SpuImpl implements SpuService {
    @Autowired
    private PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    private PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    @Autowired
    private PmsProductImageMapper pmsProductImageMapper;
    @Autowired
    private PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    private PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage productImage = new PmsProductImage();
        productImage.setProductId(spuId);
        List<PmsProductImage> productImages = pmsProductImageMapper.select(productImage);
        return productImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListBysql(String productId, String skuId) {
        //实现多表查询
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.spuSaleAttrListBysql(productId, skuId);
        return pmsProductSaleAttrs;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {
        List<PmsSkuInfo> pmsSkuInfos = pmsProductSaleAttrMapper.selectSkuSaleAttrValueListBySpu(productId);
        return pmsSkuInfos;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        //查询销售属性集合
        //得到销售属性对象
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        //再将属性值装入
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue saleAttrValue = new PmsProductSaleAttrValue();
            saleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            saleAttrValue.setProductId(spuId);
            List<PmsProductSaleAttrValue> values = pmsProductSaleAttrValueMapper.select(saleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(values);
        }
        return pmsProductSaleAttrs;
    }

    @Override
    public void saveSpuInfo(PmsProductInfo pmsProductInfo) {
        //保存spu信息
        pmsProductInfoMapper.insertSelective(pmsProductInfo);
        //保存图片的集合
        String id = pmsProductInfo.getId();
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage pmsProductImage : spuImageList) {
            pmsProductImage.setProductId(id);
            pmsProductImageMapper.insertSelective(pmsProductImage);
        }
        //保存销售属性和销售属性值
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();
        //保存属性
        for (PmsProductSaleAttr pmsProductSaleAttr : spuSaleAttrList) {
            pmsProductSaleAttr.setProductId(id);
            pmsProductSaleAttrMapper.insertSelective(pmsProductSaleAttr);
            //保存属性值
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = pmsProductSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue pmsProductSaleAttrValue : spuSaleAttrValueList) {
                pmsProductSaleAttrValue.setProductId(id);
                pmsProductSaleAttrValueMapper.insertSelective(pmsProductSaleAttrValue);
            }

        }
    }


    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        return pmsBaseSaleAttrMapper.selectAll();

    }

    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo info = new PmsProductInfo();
        info.setCatalog3Id(catalog3Id);
        return pmsProductInfoMapper.select(info);
    }


}
