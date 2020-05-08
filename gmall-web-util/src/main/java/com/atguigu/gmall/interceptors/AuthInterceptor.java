package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequired;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpclientUtil;
import com.atguigu.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

            HandlerMethod handlerMethod = (HandlerMethod)handler;

            // 判断当前应用是否需要认证
            LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
            if(loginRequired==null){
                return true;
            }

            // 获得用户的token
            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);// 之前写入的老token
            String newToken = request.getParameter("newToken");// 未经一次验证的新token
            String token = "";
            if(StringUtils.isNotBlank(oldToken)){
                token = oldToken;
            }
            if(StringUtils.isNotBlank(newToken)){
                token = newToken;
            }

            if(StringUtils.isNotBlank(token)){
                // 有token凭证，用户已登录
                /***
                 * 第一种认证方案：cas验证，远程调用认证中心，验证token,webservice的调用passport的认证接口
                 */

                String ip = request.getRemoteAddr();//直接获得请求ip,负载均衡ip
                String headerIp = request.getHeader("X-forwarded-for");//客户端请求ip
                String resultMapStr = HttpclientUtil.doGet("http://passport.gmall.com:8086/verify?token="+token+"&currentIp="+ip);
                Map<String,Object> resultMap = new HashMap<>();
                resultMap = JSON.parseObject(resultMapStr, resultMap.getClass());
                String success = resultMap.get("status").toString();

                /***
                 * 第二种认证方案：去中心化认证方案
                 */
                // jwt算法解析代替远程访问cas
                // 服务器密钥
                String serverKey = "comatguigugmall";
                // 盐值
                String salt = request.getRemoteAddr();// 可以加入自定义的加密处理算法
                // jwt区中心化，本地verify
                Map decode = JwtUtil.decode(serverKey, token, salt);

                if(success.equals("success")){
                    //不set到request中，按理说，我resultmap中也有这两个属性；,不行，不放没有
                    request.setAttribute("userId",resultMap.get("userId"));
                    request.setAttribute("nickname",resultMap.get("nickname"));
                    // 验证通过，写入cookie，更新token
                    CookieUtil.setCookie(request,response,"oldToken",token,60*60*1,true);
                    return true;
                }
            }

            boolean neededSuccess = loginRequired.isNeededSuccess();
            // 验证不通过，判断拦截注解的属性，如果是必须登录的属性则踢回认证中心，如果是可以不登陆的属性(购物车)则放行
            if(neededSuccess){
                // 必须登录
                String ReturnUrl = request.getRequestURL().toString();
                response.sendRedirect("http://passport.gmall.com:8086/index?ReturnUrl="+ReturnUrl);
                return false;
            }

            return true;
        }
}