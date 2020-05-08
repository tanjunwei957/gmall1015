package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

/**
 * SearchService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-14
 * @Description:
 */
public interface SearchService {
    List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam);
}

