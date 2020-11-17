package com.azanda.coupon.service;

import com.azanda.coupon.entity.Coupon;
import com.azanda.coupon.exception.CouponException;

import java.util.List;

// redis相关操作的服务接口 之后会在impl中实现这个接口
//
public interface IRedisService {
    // 根据userId 和 coupon status 在redis中找到coupon list
    // user可能会领取很多种类的coupon 每一种coupon都有status
    List<Coupon> getCachedCoupons(Long userId,Integer status);

    // 保存空的coupon list到redis中 不返回任何值
    void saveEmptyCouponListToCache(Long userId,List<Integer> status);

    // 这个方法是针对用户的 为用户在所需要的couponTemplate中领取一张coupon
    // return coupon code
    String tryToAcquireCouponCodeFromCache(Integer templateId);


    // 之前是已经生成了很多coupon code 用户在领取过后 这个coupon code就是用户的了
    // 要将coupon 连接user的信息一起保存到redis中去
    // return 保存成功的个数
    Integer addCouponToCache(Long useId, List<Coupon> coupons,
                             Integer status) throws CouponException;





}
