package com.azanda.coupon.service;

import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.vo.TemplateRequest;

// 优惠卷模版接口
// 这里的request对应的是vo中的Template request
// return 回去的是一个CouponTemplate的类型
// 其实就是相当于把开发者传递进来的coupon的参数包装了一下
public interface IBuildTemplateService {
    CouponTemplate buildTemplate(TemplateRequest request) throws CouponException;
}
