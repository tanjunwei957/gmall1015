package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsProductSaleAttr;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
@Controller
@CrossOrigin
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    //第二种商品详情功能，利用查询整个spu放在页面隐藏域中， 再利用前端代码与之匹配
    @RequestMapping("{skuId}.html")
    public String item (@PathVariable String skuId, ModelMap map){
        //查询商品详情
         PmsSkuInfo skuInfo=skuService.getSkuInfoById(skuId);
         map.put("skuInfo",skuInfo);
         //查出销售属性列表，并根据根据查询的sku固定住选中边框颜色
         List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListBysql(skuInfo.getProductId(),skuInfo.getId());
         map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
         //切换销售属性时切换到当前sku下，第二种方法，通过查询spu销售属性，返回到页面隐藏域中，进行切换
         List<PmsSkuInfo> pmsSkuInfos=spuService.getSkuSaleAttrValueListBySpu(skuInfo.getProductId());
         Map<String, String> storagemap = new HashMap<>();
         for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String v=pmsSkuInfo.getId();
            String k="";
            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                String saleAttrValueId = pmsSkuSaleAttrValue.getSaleAttrValueId();
                k=k +"|" + saleAttrValueId;
            }
             storagemap.put(k,v);
        }
        String skuSaleAttrValueJson= JSON.toJSONString(storagemap);
         map.put("skuSaleAttrValueJson",skuSaleAttrValueJson);
        return "item";
    }
    @RequestMapping("{skuId}.html2")
    public String item2 (@PathVariable String skuId, ModelMap map){
        //查询商品详情
        PmsSkuInfo skuInfo=skuService.getSkuInfoById(skuId);
        map.put("skuInfo",skuInfo);
        //查出销售属性列表，并根据根据查询的sku固定住选中边框颜色
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrListBysql(skuInfo.getProductId(),skuInfo.getId());
        map.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrs);
        //直接通过和静态资源中的json文件进行比对
       map.put("spuId",skuInfo.getProductId());

        return "item";
    }
    @RequestMapping("item")
    public String item(){
        return "item";
    }
  /*  @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId){
        return "item";
    }*/
}
