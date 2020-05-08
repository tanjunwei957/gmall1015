package com.atguigu.gmall.user.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.user.mapper.UserAdressMapper;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * UserServiceImpl
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-20
 * @Description:
 */
@Service
public class UserServiceImpl implements UmsMemberService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    UserAdressMapper userAdressMapper;
    @Override
    public UmsMember getLoginUser(UmsMember umsMember) {
        //获取登录用户
        UmsMember userResult=null;
        UmsMember userParam = new UmsMember();
        userParam.setUsername(umsMember.getUsername());
        userParam.setPassword(umsMember.getPassword());
        userResult= userMapper.selectOne(userParam);
        return userResult;
    }

    @Override
    public void putUserToken(UmsMember loginUser, String token) {
        Jedis jedis = redisUtil.getJedis();
        //将用户凭证的token放入缓存
        jedis.setex("user:"+loginUser.getId()+":token",60*60*2,token);
        //还要通过token去校验，所以还要set东西进去
        UmsMember umsMember = new UmsMember();
        umsMember.setId(loginUser.getId());
        jedis.setex("token:"+token+":user",60*60*2, JSON.toJSONString(userMapper.selectOne(umsMember)));
        jedis.close();
    }

    @Override
    public UmsMember checkUserToken(String token) {
        //校验token值,返回用户信息
        UmsMember umsMember = new UmsMember();
        Jedis jedis = redisUtil.getJedis();
        String userStr = jedis.get("token:" + token + ":user");
        if(StringUtils.isNotBlank(userStr)){
            //用户信息不为空，既已经登录成功了
            umsMember = JSON.parseObject(userStr, UmsMember.class);
        }
        jedis.close();
        return umsMember;
    }

    @Override
    public List<UmsMemberReceiveAddress> getAddressByUser(String userId) {
        //获得用户的收货地址集合
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(userId);
        List<UmsMemberReceiveAddress> receiveAddresses = userAdressMapper.select(umsMemberReceiveAddress);
        return receiveAddresses;
    }

    @Override
    public void saveVUser(UmsMember vUser) {
         userMapper.insertSelective(vUser);
    }
}
