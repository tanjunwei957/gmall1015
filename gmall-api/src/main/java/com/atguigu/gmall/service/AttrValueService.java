package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrValue;

import java.util.List;

/**
 * AttrValueService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
public interface AttrValueService {
    List<PmsBaseAttrValue> getAttrValueList(String attrId);


}

