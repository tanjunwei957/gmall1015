package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.service.OrderItemService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;

/**
 * OrderServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-22
 * @Description:
 */
@Service
public class OrderServiceImpl implements OrderItemService {
    @Autowired
    RedisUtil redisUtil;

    @Override
    public String createTradeCode(String userId) {
        //生成交易码
        String tradeCode = UUID.randomUUID().toString();
        //放入缓存
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:" + userId + ":tradCode", 60 * 30, tradeCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCode) {
        boolean b = false;
        if (StringUtils.isNotBlank(tradeCode)) {
            Jedis jedis = null;
            try {
                jedis = redisUtil.getJedis();
                    //要删除交易码，两种解决方案，第一种，lua脚本，第二种通过删除交易码，如果删除成功就返回true;
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    Long eval = (Long) jedis.eval(script, Collections.singletonList("user:" + userId + ":tradeCode"),
                            Collections.singletonList(tradeCode));
                    if (eval == 1){
                        b = true;
                    }
                /**
                 * 第二种方法
                 * String tradeCodeFromCache=jedis.get("user:"+userId+"tradeCode");
                 * if(StringUtils.isNotBlank("tradeCodeFromCache")){
                 *     if(tradeCode.equals(tradeCodeFromCache)){
                 *         String del=jedis.del("user:"+userId+"tradeCode");
                 *         if(del==1){
                 *             b=true;
                 *         }
                 *     }
                 * }
                 */
            }catch(Exception e){

                }finally{
                 jedis.close();
                }
            }
            return b;
        }
    }
