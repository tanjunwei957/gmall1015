package com.atguigu.gmall.search.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.PmsSearchParam;
import com.atguigu.gmall.bean.PmsSearchSkuInfo;
import com.atguigu.gmall.service.SearchService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SearchServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-14
 * @Description:
 */
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    JestClient jestClient;

    @Override
    public List<PmsSearchSkuInfo> search(PmsSearchParam pmsSearchParam) {

        String dsl = getDsl(pmsSearchParam);
        //查询命令对象
        Search build = new Search.Builder(dsl).addIndex("gmall1015").addType("pmsSearchSkuInfo").build();
        //导入到es中
        SearchResult execute = null;
        try {
            execute = jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //得到es中的结果，解析结果集
        List<PmsSearchSkuInfo> searchSkuInfos = new ArrayList<>();
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            //解析和替换高亮
            if (StringUtils.isNotBlank(pmsSearchParam.getKeyword())) {
                Map<String, List<String>> highlight = hit.highlight;
                String skuName = highlight.get("skuName").get(0);
                source.setSkuName(skuName);
            }
            searchSkuInfos.add(source);
        }

        return searchSkuInfos;
    }

    private String getDsl(PmsSearchParam pmsSearchParam) {
        //查询内容， 先过滤后搜索
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueIds = pmsSearchParam.getValueId();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(200);
        // 三级分类id ：bool
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        //term
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", catalog3Id);
            //filter
            queryBuilder.filter(termQueryBuilder);
        }
            //关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            //match
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", keyword);
            //must
            queryBuilder.must(matchQueryBuilder);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='font-weight:bolder;color:red'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlight(highlightBuilder);
        }
        //平台属性进行搜索
        if(valueIds!=null && valueIds.length>0){
            for (String valueId : valueIds) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                //filter
                queryBuilder.filter(termQueryBuilder);
            }
        }

        //query
        searchSourceBuilder.query(queryBuilder);
        return searchSourceBuilder.toString();
    }
}
