package com.azanda.coupon.service;

import com.azanda.coupon.entity.CouponTemplate;

// 异步服务的接口
public interface IAsyncService {

    // 根据模版异步的创建优惠卷码
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
