package com.atguigu.gmall.manager.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.PmsSkuAttrValue;
import com.atguigu.gmall.bean.PmsSkuImage;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.bean.PmsSkuSaleAttrValue;
import com.atguigu.gmall.manager.mapper.PmsSkuAttrValueMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuImageMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuInfoMapper;
import com.atguigu.gmall.manager.mapper.PmsSkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * SkuImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-05
 * @Description:
 */
@Service
public class SkuImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper skuInfoMapper;
    @Autowired
    PmsSkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    PmsSkuImageMapper skuImageMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void saveSkuInfo(PmsSkuInfo skuInfo) {
        //保存sku商品
        skuInfoMapper.insertSelective(skuInfo);
        String infoId = skuInfo.getId();
        //保存sku的图片表
        List<PmsSkuImage> skuImageList = skuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage : skuImageList) {
            pmsSkuImage.setSkuId(infoId);
            skuImageMapper.insertSelective(pmsSkuImage);
        }
        //保存平台和其对应中间表
        List<PmsSkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
            pmsSkuAttrValue.setSkuId(infoId);
            skuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //保存销售属性及其属性值
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(infoId);
            skuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }
    }

    /**
     * 查询单个sku商品信息。
     *
     * @param skuId
     * @return
     */
    @Override
    public PmsSkuInfo getSkuInfoById(String skuId) {
//        System.out.println(Thread.currentThread().getName()+"请求进入商品详情");
        PmsSkuInfo skuInforesult = new PmsSkuInfo();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            //判断redis里是否有值
            String json = jedis.get("skuId:" + skuId + ":Info");
            if (StringUtils.isBlank(json)) {
//              System.out.println("缓存中没有数据，申请进入数据库");
                //从数据库查询(受限)
                //加入分布式锁,这边使用同一个redis，实际应用单独做一个锁的redis
                String uuid = UUID.randomUUID().toString();
                String OK = jedis.set("skuId:" + skuId + ":lock", "uuid", "nx", "px", 10000);
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")) {
//                    System.out.println("分布式锁申请成功");
                    skuInforesult = getSkuInfoByIdfromDb(skuId);
                    if (skuInforesult != null) {
                        //同步redis
//                        System.out.println("同步缓存");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        jedis.set("skuId:" + skuId + ":Info", JSON.toJSONString(skuInforesult));
                    }
                   /* String uuiduse=jedis.get("skuId:"+skuId +":lock");
                    if(StringUtils.isNotBlank(uuiduse)&&uuiduse.equals(uuid)){
                        //解锁
                        jedis.del("skuId:"+skuId +":lock");
                    }*/
                    //使用lua脚本（完美）解决这个第一个锁刚好过期，然后删掉第二个锁的问题，（导致第三个锁进来）；
//                    System.out.println("删除锁");
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("skuId:" + skuId + ":Info"), Collections.singletonList(uuid));

                } else {
//                    System.out.println("分布式锁获得失败，进入自旋");
                    //没有获得锁，等待三秒，并自旋；即递归， return告诉它；
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuInfoById(skuId);
                }
            } else {
                //有值
                skuInforesult = JSON.parseObject(json, PmsSkuInfo.class);
            }
        } finally {
            jedis.close();
        }
        return skuInforesult;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        //数据库查询所有skuinfo
        List<PmsSkuInfo> pmsSkuInfos = skuInfoMapper.selectAll();
        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfos) {
            String skuId = pmsSkuInfo.getId();
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValues = skuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValues);
        }
        return pmsSkuInfos;
    }

    private PmsSkuInfo getSkuInfoByIdfromDb(String skuId) {
        //通过mysql查询sku内容
        PmsSkuInfo skuInfo = new PmsSkuInfo();
        skuInfo.setId(skuId);
        PmsSkuInfo skuInforesult = skuInfoMapper.selectOne(skuInfo);
        //查询图片
        PmsSkuImage skuImage = new PmsSkuImage();
        skuImage.setSkuId(skuId);
        List<PmsSkuImage> skuImages = skuImageMapper.select(skuImage);
        skuInforesult.setSkuImageList(skuImages);
        return skuInforesult;
    }
}
