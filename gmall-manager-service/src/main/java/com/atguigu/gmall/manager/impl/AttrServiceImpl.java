package com.atguigu.gmall.manager.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import com.atguigu.gmall.bean.PmsBaseAttrValue;
import com.atguigu.gmall.manager.mapper.AttrInfoMapper;
import com.atguigu.gmall.manager.mapper.AttrinfoValueMapper;
import com.atguigu.gmall.service.AttrValueService;
import com.atguigu.gmall.service.PmsBaseAttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

/**
 * AttrServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-03
 * @Description:
 */
@Service
public class AttrServiceImpl implements PmsBaseAttrService, AttrValueService {
    @Autowired
    AttrInfoMapper attrInfoMapper;
    @Autowired
    AttrinfoValueMapper attrinfoValueMapper;


    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        //查询平台属性
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = attrInfoMapper.select(pmsBaseAttrInfo);
        //每个平台属性里的值，即包装。
        for (PmsBaseAttrInfo baseAttrInfo : pmsBaseAttrInfos) {
            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            String attrInfoId = baseAttrInfo.getId();
            pmsBaseAttrValue.setAttrId(attrInfoId);
            List<PmsBaseAttrValue> pmsBaseAttrValues = attrinfoValueMapper.select(pmsBaseAttrValue);
            baseAttrInfo.setAttrValueList(pmsBaseAttrValues);
        }

        return pmsBaseAttrInfos;
    }

    @Override
    public void saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        if (pmsBaseAttrInfo.getId() == null) {
            //保存平台属性
            attrInfoMapper.insertSelective(pmsBaseAttrInfo);
            //返回平台属性主键
            String attrInfoId = pmsBaseAttrInfo.getId();
            //保存平台属性值表
            List<PmsBaseAttrValue> attrValues = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue : attrValues) {
                attrValue.setAttrId(attrInfoId);
                attrinfoValueMapper.insertSelective(attrValue);
            }
        } else {
            List<PmsBaseAttrValue> arrtValue2 = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : arrtValue2) {
                //先进行删除
                attrinfoValueMapper.delete(pmsBaseAttrValue);
                //再插入，先获取平台属性id
                String attrInfoId = pmsBaseAttrInfo.getId();
                pmsBaseAttrValue.setAttrId(attrInfoId);
                //再进行插入
                attrinfoValueMapper.insertSelective(pmsBaseAttrValue);
            }


        }
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrInfoListByValueId(Set<String> valueIdSet) {
        //
        String join = StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfos=attrInfoMapper.selectAttrInfoListByValueId(join);
        return pmsBaseAttrInfos;
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        return attrinfoValueMapper.select(pmsBaseAttrValue);
    }


}
