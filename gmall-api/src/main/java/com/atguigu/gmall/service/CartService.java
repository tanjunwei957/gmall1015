package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OmsCartItem;

import java.util.List;

/**
 * CartService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-17
 * @Description:
 */
public interface CartService {


    OmsCartItem is_cart_exist(OmsCartItem omsCartItem);

    void updateCartItem(OmsCartItem omsCartItemfromDb);

    void addCartItem(OmsCartItem omsCartItem);

    void synchronizeUserCart(String userId);

    List<OmsCartItem> getCartListByUser(String userId);
}

