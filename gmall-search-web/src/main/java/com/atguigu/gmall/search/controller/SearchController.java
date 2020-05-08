package com.atguigu.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.PmsBaseAttrService;
import com.atguigu.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

/**
 * SearchController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-14
 * @Description:
 */
@Controller
public class SearchController {
    @Reference
    SearchService searchService;
    @Reference
    PmsBaseAttrService pmsBaseAttrService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    /**
     * 返回三级id对应的所有的skufo
     *
     * @param pmsSearchParam
     * @return
     */
    @RequestMapping("list.html")
    public String search(PmsSearchParam pmsSearchParam, ModelMap map) {
        List<PmsSearchSkuInfo> searchSkuInfos = searchService.search(pmsSearchParam);
        map.put("skuLsInfoList", searchSkuInfos);
        //根据skuvalueid显示平台属性列表
//        去掉相同的valueid;
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo searchSkuInfo : searchSkuInfos) {
            List<PmsSkuAttrValue> skuAttrValueList = searchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                String valueId = pmsSkuAttrValue.getValueId();
                valueIdSet.add(valueId);
            }
        }
        //放入当前urlParam
        String urlParam = getUrlParamAll(pmsSearchParam);
        map.put("urlParam", urlParam);

        //根据去重结果找平台属性值
        if (valueIdSet != null && valueIdSet.size() > 0) {
            List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrService.getAttrInfoListByValueId(valueIdSet);
            map.put("attrList", pmsBaseAttrInfos);
            //面包屑功能
            if (pmsSearchParam.getValueId() != null && pmsSearchParam.getValueId().length > 0) {
                //面包屑上移功能
                List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
                String[] valueIdForDels = pmsSearchParam.getValueId();
                for (String valueIdForDel : valueIdForDels) {
                    //面包屑
                    PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                    //url
                    pmsSearchCrumb.setUrlParam(getUrlParamAll(pmsSearchParam, valueIdForDel));
                    pmsSearchCrumbs.add(pmsSearchCrumb);
                    //删除平台属性
                    Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
                    while (iterator.hasNext()) {
                        PmsBaseAttrInfo next = iterator.next();
                        List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                            String id = pmsBaseAttrValue.getId();
                            if (valueIdForDel.equals(id)) {
                                //面包屑属性名称，
                                pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                                iterator.remove();
                            }
                        }
                    }
                }
                map.put("attrValueSelectedList", pmsSearchCrumbs);
                //面包屑的删除功能,根据传过来的valueid做对比来删除
//                for (String valueIdForDel : pmsSearchParam.getValueId()) {
//                    Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfos.iterator();
//                    //删除平台属性；
//                    while (iterator.hasNext()) {
//                        PmsBaseAttrInfo next = iterator.next();
//                        List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
//                        for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
//                            if (valueIdForDel.equals(pmsBaseAttrValue.getId())) {
//                                //删除整个属性，删除用iterator
//                                iterator.remove();
//                            }
//                        }
//                    }
//                }
            }


        }
//        String urlParam = getUrlParamAll(pmsSearchParam);
//        map.put("urlParam", urlParam);
//        if (pmsSearchParam.getValueId() != null && pmsSearchParam.getValueId().length > 0) {
//            List<PmsSearchCrumb> pmsSearchCrumbs = new ArrayList<>();
//            String[] valueIdForCrumbs = pmsSearchParam.getValueId();
//            for (String valueId : valueIdForCrumbs) {
//                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
//                //name
//                pmsSearchCrumb.setValueName(valueId);
//                //url
//                pmsSearchCrumb.setUrlParam(getUrlParamAll(pmsSearchParam, valueId));
//                //valueId
//                pmsSearchCrumb.setValueId(valueId);
//                pmsSearchCrumbs.add(pmsSearchCrumb);
//            }
//            map.put("attrValueSelectedList", pmsSearchCrumbs);
//        }

        return "list";
    }

    private String getUrlParamAll(PmsSearchParam pmsSearchParam, String... valueIdForCrumb) {
        String urlParam = "";
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
//        获取当前url+ 商城有连个搜索入口，第一个首页搜索（关键字），第二个通过三级id进入
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if (valueIdForCrumb != null && valueIdForCrumb.length > 0) {
                    if (!valueIdForCrumb[0].equals(valueId)) {
                        urlParam = urlParam + "&" + "valueId=" + valueId;
                    }
                } else {
                    urlParam = urlParam + "&" + "valueId=" + valueId;
                }

            }
        }
        return urlParam;
    }
   /* private String getUrlParamForCrumb(PmsSearchParam pmsSearchParam,String valueIdForCrumb) {
        String urlParam = "";
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
//        获取当前url+ 商城有连个搜索入口，第一个首页搜索（关键字），第二个通过三级id进入
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if( !valueIdForCrumb.equals(valueId)){
                    urlParam = urlParam + "&" + "valueId=" + valueId;
                }
            }
        }
        return urlParam;
    }*/


  /*  private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String urlParam = "";
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
//        获取当前url+ 商城有连个搜索入口，第一个首页搜索（关键字），第二个通过三级id进入
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam = urlParam + "keyword=" + keyword;
        }
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                urlParam = urlParam + "&" + "valueId=" + valueId;
            }
        }
        return urlParam;
    }*/
}
