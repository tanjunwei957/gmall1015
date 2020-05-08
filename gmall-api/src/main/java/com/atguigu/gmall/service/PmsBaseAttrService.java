package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;

import java.util.List;
import java.util.Set;

/**
 * PmsBaseAttrService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-03
 * @Description:
 */
public interface PmsBaseAttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrInfo> getAttrInfoListByValueId(Set<String> valueIdSet);
}

