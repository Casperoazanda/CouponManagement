package com.azanda.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 用于微服务之间的传递优惠卷模版的信息
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponTemplateSDK {
    // 优惠卷主键
    private Integer id;
    private String name;
    private String logo;
    private String desc;
    private String category;
    private Integer productLine;
    private String key;
    private Integer target;
    private TemplateRule rule;
}
