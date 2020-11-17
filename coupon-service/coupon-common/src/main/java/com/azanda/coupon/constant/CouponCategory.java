package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 优惠卷分类 优惠卷有三种类型： 满减 折扣 立减
// 在这里一个enum的类 如果new一个它的instance
// CouponCategory cc = Coupon.DOLLAR_DISCOUNT
// 1. dollar discount with total value over specific dollars 2. percentage discount with no limitation 3. automatic discount with no limitation
@Getter
@AllArgsConstructor
public enum CouponCategory {
    DOLLAR_DISCOUNT_WITH_LIMITATION("LIMITATION","001"),
    PERCENTAGE_DISCOUNT("PERCENTAGE","002"),
    AUTOMATIC_DISCOUNT("AUTOMATIC","003");

    // 里面定义的变量是上面enum值的两个属性 要先把这两个值给定义出来
    private String description;
    private String code;
    // 这个是一个方法 判断这个code是不是在预先设定好的couponCategory中
    public static CouponCategory isExist(String code)
    {
        Objects.requireNonNull(code);
        // 这个values返回的当前所有enum的数组 并尽心过滤
        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code + "not exists"));
    }

}
