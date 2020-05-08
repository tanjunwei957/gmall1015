package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

/**
 * UmsMemberService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-20
 * @Description:
 */
public interface UmsMemberService {
    UmsMember getLoginUser(UmsMember umsMember);

    void putUserToken(UmsMember loginUser, String token);

    UmsMember checkUserToken(String token);


    List<UmsMemberReceiveAddress> getAddressByUser(String userId);

    void saveVUser(UmsMember vUser);
}

