package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.PmsSkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CartController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-16
 * @Description:
 */
@Controller
public class CartController {

    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;
    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(OmsCartItem omsCartItem,HttpServletRequest request, HttpServletResponse response, ModelMap map){
        List<OmsCartItem> omsCartItems=new ArrayList<>();
        //String userId=""; 判断用户是否登录，测试
        //从拦截器中获取用户信息
        String userId =(String)request.getAttribute("userId");
        if(StringUtils.isBlank(userId)){
           //cookie数据
          String cartListCookieStr= CookieUtil.getCookieValue(request,"cartListCookie",true);
           if(StringUtils.isNotBlank(cartListCookieStr)){
               //congcookie中取
               omsCartItems =JSON.parseArray(cartListCookieStr, OmsCartItem.class);
               //选中状态的更新即更新cookie
               for (OmsCartItem cartItem : omsCartItems) {
                   if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                       cartItem.setIsChecked(omsCartItem.getIsChecked());
                   }
               }
           }
           //覆盖浏览器数据
           CookieUtil.setCookie(request,response,"cartListCookie",cartListCookieStr,60*60*24,true);
       }else{
           //db
           omsCartItem.setMemberId(userId);
           cartService.updateCartItem(omsCartItem);
           //db数据
           omsCartItems= cartService.getCartListByUser(userId);
       }
       //将最新数据放入内嵌页
        map.put("cartList",omsCartItems);
       //总价格的计算
        map.put("sumPrice",getSumPrice(omsCartItems));
        return "cartListinner";
    }
    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response,ModelMap map){
        List<OmsCartItem> omsCartItems = new ArrayList<>();
        //登录和未登录
        String userId =(String)request.getAttribute("userId");
        if(StringUtils.isBlank(userId)){
            //用户未登录 cookie数据
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isNotBlank(cartListCookieStr)){
                omsCartItems=JSON.parseArray(cartListCookieStr,OmsCartItem.class);
            }
        }else{
            //db
            omsCartItems=cartService.getCartListByUser(userId);
        }
        map.put("cartList",omsCartItems);
        //总价格的计算
        map.put("sumPrice",getSumPrice(omsCartItems));
        return "cartList";
    }

    private BigDecimal getSumPrice(List<OmsCartItem> omsCartItems) {
        BigDecimal bigDecimal = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItems) {
            if(omsCartItem.getIsChecked().equals("1")){
                bigDecimal.add(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
            }
        }
        return bigDecimal;
    }
    @LoginRequired(isNeededSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(OmsCartItem omsCartItem, HttpServletRequest request, HttpServletResponse response){
        List<OmsCartItem> omsCartItems=new ArrayList<>();
        PmsSkuInfo skuInfoById = skuService.getSkuInfoById(omsCartItem.getProductSkuId());
        omsCartItem.setIsChecked("1");
        omsCartItem.setProductId(skuInfoById.getProductId());
        omsCartItem.setProductPic(skuInfoById.getSkuDefaultImg());
        omsCartItem.setProductCategoryId(skuInfoById.getCatalog3Id());
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setPrice(skuInfoById.getPrice());
        omsCartItem.setProductName(skuInfoById.getSkuName());
        //添加购物车的业务逻辑
        String userId =(String)request.getAttribute("userId");
        if(StringUtils.isBlank(userId)){
            //用户未登录，执行cookie的购物车分支
            //获取cookie
            String cartListCookieStr = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(StringUtils.isBlank(cartListCookieStr)){
                 //之前没有数据,进行添加
                omsCartItems.add(omsCartItem);
            }else{//之前有数据，进行更新操作
                omsCartItems=JSON.parseArray(cartListCookieStr,OmsCartItem.class);
                boolean b=if_cart_exist(omsCartItems,omsCartItem);
                if(b){//之前有相同的东西，更新
                    for (OmsCartItem cartItem : omsCartItems) {
                        cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        cartItem.setTotalPrice(cartItem.getQuantity().multiply(cartItem.getPrice()));
                    }
                }else{//添加
                    omsCartItems.add(omsCartItem);
                }
            }
            //覆盖浏览器cookie
         CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(omsCartItems),60*60*24,true);

        }else{
            //用户已登录，同步到redis中
            //传过来的omscartitem，得设置是哪个member的
            omsCartItem.setMemberId(userId);
            //从数据库查询购物项是否存在，不存在添加，存在更新；
            OmsCartItem omsCartItemfromDb=cartService.is_cart_exist(omsCartItem);
                if (omsCartItemfromDb !=null){
                    //更新，
                    cartService.updateCartItem(omsCartItemfromDb);
                }else{
                    //添加
                    cartService.addCartItem(omsCartItem);
                }
                //同步缓存
            cartService.synchronizeUserCart(userId);

        }
        return "redirect:/success.html";
    }

    private boolean if_cart_exist(List<OmsCartItem> omsCartItems, OmsCartItem omsCartItem) {
        boolean b =false;
        for (OmsCartItem cartItem : omsCartItems) {
            if(cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                //之前有相同的商品
                b=true;
            }
        }
        return b;
    }
}
