package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * PmsProductSaleAttrMapper
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
public interface PmsProductSaleAttrMapper extends Mapper<PmsProductSaleAttr> {
    List<PmsProductSaleAttr> spuSaleAttrListBysql(@Param("productId") String productId, @Param("skuId") String skuId);

    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(@Param("productId") String productId);
}

