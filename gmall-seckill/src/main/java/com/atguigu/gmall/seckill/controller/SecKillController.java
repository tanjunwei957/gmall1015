package com.atguigu.gmall.seckill.controller;

import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Collections;
import java.util.List;

/**
 * SecKillController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-30
 * @Description:
 */
@Controller
public class SecKillController {
    @Autowired
    RedisUtil redisUtil;

    @RequestMapping("secKillOne")
    @ResponseBody
    public String secKillOne() {
        //用户在15分钟之内只能成功抢购一件商品；解决方案redis锁；
        String userId = "";
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String OK = jedis.set("user:" + userId + ":seckill", "1", "nx", "px", 60 * 15);
//            if(StringUtils.isNotBlank(OK)&&OK.equals("OK")){
            //减库存
            String sto = jedis.get("sku:" + "113" + ":sto");
            long l = Long.parseLong(sto);
            //抢库存

            if (l > 0) {
                //监控
                jedis.watch("sku:" + "113" + ":sto");
                //开启事务
                Transaction multi = jedis.multi();
                //对库存进行消耗；
                Response<Long> longResponse = multi.decrBy("sku:" + "113" + ":sto", 1);
                //执行,要判断执行结果是否为空；
                List<Object> exec = multi.exec();
                if (exec != null && exec.size() > 0) {
                    //有剩余库存，抢购成功，发送消息队列，生成订单消息
                    System.out.println("congratulation，抢购成功。");
                    System.out.println("剩余库存数" + longResponse.get() + "");
                    return longResponse.get() + "";
                } else {
                    System.out.println("有库存，但是抢购失败");
                    //并且删除锁  jedis.del("sku:" + "113" + ":sto");
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    jedis.eval(script, Collections.singletonList("user:" + userId + ":seckill"), Collections.singletonList("1"));
                    return "又库存，但是抢购失败";
                }
//            }else{
//                return null;
//            }
            } else {
                System.out.println("商品已被抢购，请稍后查看是否有退货的");
                return "抢购失败";
            }
        } finally {
            jedis.close();
        }

    }
}
