package com.nowcoder.community.util;
//an interface which defines  some constant to declare different activate status
public interface CommunityConstant {
    //activate success
    int ACTIVATION_SUCCESS = 0;
    //repeated activate
    int ACTIVATION_REPEAT = 1;
    //activate failed
    int ACTIVATION_FAILED = 2;
    //Default login credential timeout
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    //remember me status credential timeout
    int REMEMBER_ME_EXPIRED_SECONDS = 3600 * 24 * 100;
}
