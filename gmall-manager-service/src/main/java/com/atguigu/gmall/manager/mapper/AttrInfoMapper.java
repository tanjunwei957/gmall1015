package com.atguigu.gmall.manager.mapper;

import com.atguigu.gmall.bean.PmsBaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * AttrInfoMapper
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-03
 * @Description:
 */
public interface AttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectAttrInfoListByValueId(@Param("valueIdSet") String valueIdSet);
}

