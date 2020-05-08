package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.service.PmsBaseAttrService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * PmsBaseAttrController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-03
 * @Description:
 */
@RestController
@CrossOrigin
public class PmsBaseAttrController {
    @Reference
    PmsBaseAttrService pmsBaseAttrService;

    /**
     * 保存平台属性
     *
     * @param pmsBaseAttrInfo
     * @return
     */
    @RequestMapping("/saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo) {
        pmsBaseAttrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    /**
     * 查询平台属性列表
     *
     * @param catalog3Id
     * @return
     */
    @RequestMapping("/attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        List<PmsBaseAttrInfo> attrInfos = pmsBaseAttrService.attrInfoList(catalog3Id);
        return attrInfos;
    }
}
