package com.nowcoder.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

//Utility Class used for
public class CommunityUtil {
    //生成随机字符串
    public static String generateUUid(){
        return UUID.randomUUID().toString().replace("-","");
    }
    //MD5加密
    //只能加密，不能解密
    //添加salt拼接，防止弱密码破解
    public static String md5(String key){
        //先判断key是否为空
        //apache lang包，判断为空 nulL，空串，只有空格都判定为空
        if(StringUtils.isBlank(key)){
            return null;
        }
        //使用Spring封装的工具，可以方便的使用MD5加密
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
