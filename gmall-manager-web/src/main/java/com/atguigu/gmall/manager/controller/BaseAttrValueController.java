package com.atguigu.gmall.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.service.AttrValueService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * BaseAttrValueController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
@RestController
@CrossOrigin
public class BaseAttrValueController {
    @Reference
    AttrValueService attrValueService;

    /**
     * 根据id获得平台属性值
     *
     * @param attrId
     * @return
     */
    @RequestMapping("getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        List<PmsBaseAttrValue> pmsBaseAttrValues = attrValueService.getAttrValueList(attrId);
        return pmsBaseAttrValues;
    }
}
