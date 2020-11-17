package com.azanda.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 请求分配coupon的request模版
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcquireTemplateRequest {

    private Long userId;
    // 要指定要领取的是哪一个couponTemplate
    private CouponTemplateSDK templateSDK;
}
