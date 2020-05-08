package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OmsCartItem;
import com.atguigu.gmall.bean.UmsMember;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.UmsMemberService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PassportController
 *
 * @Author: 谭俊伟
 * @CreateTime: 2020-03-20
 * @Description:
 */
@Controller
public class PassportController {
    @Reference
    UmsMemberService umsMemberService;
    @Reference
    CartService cartService;

    @RequestMapping("vlogin")
    public String vlogin(HttpServletRequest request,String code, ModelMap modelMap){
        //已经可以跳转第二个(第一个地址返回的，获取到了code)地址了，解析为accesstoken，最后通过accesstoken获取用户信
        String url3 = "https://api.weibo.com/oauth2/access_token";
        Map<String,String> map = new HashMap<>();
        map.put("client_id","3619260816");// appid
        map.put("client_secret","3072b64f2025ad2301d02a88c236c28c");// app密钥
        map.put("grant_type","authorization_code");// 访问类型
        map.put("redirect_uri","http://passport.gmall.com:8086/vlogin");// 回调地址
        map.put("code",code);// 交易码(授权后，回调携带的)

        String json = HttpclientUtil.doPost(url3, map);
        Map<String,Object> mapJson=new HashMap<>();
        Map<String,Object> mapresult = JSON.parseObject(json, mapJson.getClass());
        String access_token = (String) mapresult.get("access_token");
        String uid = (String) mapresult.get("uid");

        String url4="https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid="+uid;
        String userStr = HttpclientUtil.doGet(url4);
        //临时保存用户信息
        Map memberMap=JSON.parseObject(userStr, Map.class);
        String name = (String) memberMap.get("name");
        Long accessCode = (Long) memberMap.get("id");
        String location = (String) memberMap.get("location");


        UmsMember vUser = new UmsMember();
        vUser.setNickname(name);
        vUser.setAccessCode(accessCode+"");
        vUser.setSourceUid(uid);
        vUser.setGender("1");
        vUser.setCity(location);
        vUser.setAccessToken(access_token);

//        UmsMember loginUser = umsMemberService.getVUser(vUser);
//        if(loginUser== null){
//            umsMemberService.saveVUser(vUser);
//            //将token存入redis，
//            umsMemberService.putUserToken(vUser,access_token);
            //完成重定向首页地址
//        }


        return "redirect:http://search.gmall.com:8083/index";
    }
    @RequestMapping("verify")
    @ResponseBody
    public Map<String,Object> verify(HttpServletRequest request,String ip,String token,ModelMap modelMap){
        //校验token的方法，cas认证中心模式调用，umsMemberService校验token
        UmsMember user= umsMemberService.checkUserToken(token);
        Map<String,Object> resultMap= new HashMap<>();
        if(user !=null){//用户已登录
            resultMap.put("status","success");
            resultMap.put("userId",user.getId());
            resultMap.put("nickname",user.getNickname());

        }
        return resultMap;

    }
    @RequestMapping("index")
    public String index(HttpServletRequest request, String ReturnUrl , ModelMap modelMap){
        modelMap.put("ReturnUrl",ReturnUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UmsMember umsMember, HttpServletRequest request, HttpServletResponse response) {
        //对比登录的用户名和密码，获取token值
        UmsMember loginUser = umsMemberService.getLoginUser(umsMember);
        if (loginUser != null) {
            //用户名密码正确，发送token
            //jwt算法生成token
            String serverkey = "comatguigugmall";//服务器秘钥
            //String headerIp=request.getHeader("X-forward-for");有集群获得其浏览器地址；
            String ip = request.getRemoteAddr();//盐值，如果有集群，这个方法获得是集群的ip,
            //用户
            Map<String, String> map = new HashMap<>();
            map.put("userId", loginUser.getId());
            map.put("nickname", loginUser.getNickname());
            String token = JwtUtil.encode(serverkey, map, ip);
            //将token写入缓存
            umsMemberService.putUserToken(loginUser, token);
            //登录成功后，将存储到cookie的数据存到数据库中，
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if(cartListCookie!=null&&cartListCookie.length()>0){
                List<OmsCartItem> omsCartItems = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //把当前用户Id存储到购物车中
                for (OmsCartItem omsCartItem : omsCartItems) {
                    omsCartItem.setMemberId(umsMember.getId());
                    cartService.addCartItem(omsCartItem);
                }
                //最后删除cookie
                CookieUtil.deleteCookie(request, response,"cartListCookie");
            }
            return token;
        } else {
            return "fail";
        }
    }
}
