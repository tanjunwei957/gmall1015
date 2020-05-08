package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.OmsOrder;
import com.atguigu.gmall.bean.OmsOrderItem;
import com.atguigu.gmall.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderItemService;
import com.atguigu.gmall.service.UmsMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * OrderController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-20
 * @Description:
 */
@Controller
public class OrderController {
    @Reference
    OrderItemService orderItemService;
    @Reference
    CartService cartService;
    @Reference
    UmsMemberService userService;

    //提交订单
    @LoginRequired(isNeededSuccess = true)
    @RequestMapping("submitOrder")
    public String submitOrder(HttpServletRequest request,String tradeCode,ModelMap modelMap){
        //保存订单
        String userId=(String)request.getAttribute("userId");
        String nickName=(String)request.getAttribute("nickName");
        //校验tradeCode
        boolean b=orderItemService.checkTradeCode(userId,tradeCode);
        //查询用户购物车数据
        List<OmsCartItem> cartListByUser = cartService.getCartListByUser(userId);
        //保存订单的同时删除购物车数据
        List<String> delCarts=new ArrayList<>();
        //保存订单信息
        OmsOrder omsOrder = new OmsOrder();

        omsOrder.setOrderSn(getOrderSn());
        omsOrder.setStatus("0");
        omsOrder.setTotalAmount(getTotalPrice(cartListByUser));
        omsOrder.setMemberId(userId);
        omsOrder.setMemberUsername(nickName);
        omsOrder.setPayAmount(getTotalPrice(cartListByUser));
        omsOrder.setCreateTime(new Date());
        omsOrder.setSourceType(1);
        omsOrder.setPayType(0);
        Calendar calendar = Calendar.getInstance();
//        calendar.add();

        return "重定向到支付页面";
    }


    @RequestMapping("toTrade")
    @LoginRequired(isNeededSuccess = true)
    public String toTrade(HttpServletRequest request, ModelMap modelMap){
        String userId =(String)request.getAttribute("userId");
        String nickname=(String)request.getAttribute("nickname");
        //根据购物车信息，然后转化为订单信息
        List<OmsCartItem> cartListByUser = cartService.getCartListByUser(userId);
        if(cartListByUser!=null&&cartListByUser.size()>0){
            List<OmsOrderItem> omsOrderItems=new ArrayList<>();
            for (OmsCartItem omsCartItem : cartListByUser) {
                if(omsCartItem.getIsChecked().equals("1")){
                    OmsOrderItem omsOrderItem = new OmsOrderItem();

                    omsOrderItem.setOrderSn(getOrderSn());//生成订单号；
                    omsOrderItem.setProductId(omsCartItem.getProductId());
                    omsOrderItem.setProductCategoryId(omsCartItem.getProductCategoryId());
                    omsOrderItem.setProductSkuId(omsCartItem.getProductSkuId());
                    omsOrderItem.setProductQuantity(omsCartItem.getQuantity());
                    omsOrderItem.setProductName(omsCartItem.getProductName());
                    omsOrderItem.setProductPic(omsCartItem.getProductPic());
                    omsOrderItem.setProductPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
                    omsOrderItems.add(omsOrderItem);
                }
            }
            modelMap.put("orderDetailList",omsOrderItems);
        }
        //获取用户的收货地址
        List<UmsMemberReceiveAddress> addresses=userService.getAddressByUser(userId);
        modelMap.put("userAddressList",addresses);
        //计算总金额
//        BigDecimal totalPrice = getTotalPrice(cartListByUser);
//        modelMap.put("totalAmount",totalPrice);
        modelMap.put("totalAmount",getTotalPrice(cartListByUser));
        //生成交易码；
        String tradeCode=orderItemService.createTradeCode(userId);
        modelMap.put("tradeCode",tradeCode);
        return "trade";
    }
    private BigDecimal getTotalPrice(List<OmsCartItem> OmsCartItems){
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsCartItem omsCartItem : OmsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal = bigDecimal.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
        }
        return bigDecimal;
    }
    private String getOrderSn(){
        String atguigu="atguigu";
        Date date = new Date();
        SimpleDateFormat sim = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = sim.format(date);
        long timeMillis = System.currentTimeMillis();
        String orderSn=atguigu+format+timeMillis;
        return orderSn;
    }

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = simpleDateFormat.format(date);
        System.out.println(format);
        System.out.println(System.currentTimeMillis());
    }
}
