package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseSaleAttr;
import com.atguigu.gmall.bean.PmsProductInfo;
import com.atguigu.gmall.manager.util.GmallUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * SpuController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-04
 * @Description:
 */
@RestController
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo) {
        spuService.saveSpuInfo(pmsProductInfo);
        return "success";
    }


    /**
     * 返回销售属性一级
     *
     * @return
     */
    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> pmsBaseSaleAttr = spuService.baseSaleAttrList();
        return pmsBaseSaleAttr;
    }

    /**
     * 查询spu列表
     *
     * @param catalog3Id
     * @return
     */
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id) {
        List<PmsProductInfo> pmsProductInfos = spuService.spuList(catalog3Id);
        return pmsProductInfos;
    }

    @RequestMapping("/fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) {
        //上传图片
        String url = GmallUploadUtil.imageUpload(multipartFile);
        return url;
    }
}