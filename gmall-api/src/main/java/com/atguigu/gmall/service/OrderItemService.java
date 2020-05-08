package com.atguigu.gmall.service;

/**
 * OrderItemService
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-22
 * @Description:
 */
public interface OrderItemService {
    String createTradeCode(String userId);

    boolean checkTradeCode(String userId, String tradeCode);
}

