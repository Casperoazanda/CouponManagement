package com.azanda.coupon.constant;
// 通用常量的定义
public class Constant {
    // kafka消息的topic的名字
    public static final String TOPIC = "user_coupon_op";
    // redis key的前缀定义
    public static class RedisPrefix
    {
        // 优惠卷码key前缀
        public static final String COUPON_TEMPLATE = "coupon_template_code_";

        // 用户当前所有可用的优惠卷key的前缀
        public static final String USER_COUPON_USABLE = "user_coupon_usable_";

        // 用户当前已使用的优惠卷key的前缀
        public static final String USER_COUPON_USED = "user_coupon_used_";

        // 用户当前所有已过期的优惠卷key的前缀
        public static final String USER_COUPON_EXPIRED = "user_coupon_expired_";








    }

}
