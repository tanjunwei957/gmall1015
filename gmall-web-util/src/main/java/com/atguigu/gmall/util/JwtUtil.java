package com.atguigu.gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    public static void main(String[] args){

        // 服务器密钥
        String serverKey = "comatguigugmall";

        // 盐值
        String ip = "127.0.0.1";// 可以加入自定义的加密处理算法
//
//        // 用户个人信息
//        Map<String,String> map = new HashMap<>();
//        map.put("userId","1");
//        map.put("nickname","tom");
//
//        // 加密
//        String encode = encode(serverKey, map, ip);
//
//        System.out.println(encode);

        // 生成的字符串
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrbmFtZSI6InRvbSIsInVzZXJJZCI6IjEifQ.zfZLPhVQmke1wmLq47WjDDwA-M95eJuRYmZtw1l2GWs";


        // ip = "12345";
        Map decode = decode(serverKey, token, ip);
        System.out.println(decode);


    }


    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key,Map map,String salt){

        // 加密算法
        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static  Map decode(String key,String token,String salt)throws SignatureException{
        // 加密算法
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        return map;

    }

}
