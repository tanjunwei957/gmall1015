package com.atguigu.gmall.cart.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.cart.mapper.CartItemMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CartServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-17
 * @Description:
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartItemMapper cartItemMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public OmsCartItem is_cart_exist(OmsCartItem omsCartItem) {
        //查询是数据库是否有此购物项
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setMemberId(omsCartItem.getMemberId());
        cartItem.setProductSkuId(omsCartItem.getProductSkuId());
        OmsCartItem omsCartItemResult = cartItemMapper.selectOne(cartItem);
        return omsCartItemResult;
    }

    @Override
    public void updateCartItem(OmsCartItem omsCartItemfromDb) {
        //更新数据库
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItemfromDb.getMemberId()).andEqualTo("productSkuId",omsCartItemfromDb.getProductSkuId());
        OmsCartItem omsCartItemForUpdate = new OmsCartItem();
        if(omsCartItemfromDb.getQuantity()!=null){
            omsCartItemForUpdate.setQuantity(omsCartItemfromDb.getQuantity());
            omsCartItemForUpdate.setTotalPrice(omsCartItemForUpdate.getQuantity().multiply(omsCartItemForUpdate.getPrice()));
        }
        if(StringUtils.isNotBlank(omsCartItemfromDb.getIsChecked())){
            omsCartItemForUpdate.setIsChecked(omsCartItemfromDb.getIsChecked());
        }
        cartItemMapper.updateByExampleSelective(omsCartItemForUpdate,example);
        //同步缓存
        synchronizeUserCart(omsCartItemfromDb.getMemberId());
    }



    @Override
    public void addCartItem(OmsCartItem omsCartItem) {
        cartItemMapper.insertSelective(omsCartItem);
        //同步缓存
        Jedis jedis = redisUtil.getJedis();
        jedis.hset("user:"+omsCartItem.getMemberId()+":cart",omsCartItem.getProductSkuId(), JSON.toJSONString(omsCartItem));
        jedis.close();
    }

    @Override
    public void synchronizeUserCart(String userId) {
        //同步缓存,同步所有的购物项
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(userId);
        List<OmsCartItem> omsCartItems = cartItemMapper.select(omsCartItem);
        if(omsCartItems !=null &&omsCartItems.size()>0){
            Map<String,String> map=new HashMap<>();
            for (OmsCartItem cartItem : omsCartItems) {
                map.put(cartItem.getProductSkuId(),JSON.toJSONString(cartItem));
                Jedis jedis = redisUtil.getJedis();
                jedis.hmset("user:"+userId+":cart",map);
                jedis.close();
            }
        }


    }

    @Override
    public List<OmsCartItem> getCartListByUser(String userId) {
        List<OmsCartItem> OmsCartItemResult=new ArrayList<>();
        //查询购物车
       //先查询缓存
        Jedis jedis = redisUtil.getJedis();
        List<String> cartStr = jedis.hvals("user:" + userId + ":cart");
        if(cartStr!=null&&cartStr.size()>0){
            //在缓存中取
            for (String cartitem : cartStr) {
                OmsCartItem omsCartItem = JSON.parseObject(cartitem, OmsCartItem.class);
                OmsCartItemResult.add(omsCartItem);
            }
        }else{
            //再数据库中查询
            OmsCartItem omsCartItem = new OmsCartItem();
            omsCartItem.setMemberId(userId);
            OmsCartItemResult = cartItemMapper.select(omsCartItem);
            if(OmsCartItemResult !=null&&OmsCartItemResult.size()>0){
                synchronizeUserCart(userId);
            }
        }
        jedis.close();

        return OmsCartItemResult;
    }
}
