package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 用户现在所有的coupon的状态：已用 还未用 没有用但过期的
@Getter
@AllArgsConstructor
public enum CouponStatus {
    USABLE("usable",1),
    USED("used",2),
    EXPIRED("expired",3);

    // coupon status 描述
    private String description;
    // coupon status code
    private Integer code;

    // 根据code获取到couponStatus
    public static CouponStatus isExist(Integer code)
    {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean->bean.code.equals(code))
                .findAny()
                .orElseThrow(()-> new IllegalArgumentException(code + "not exist"));
    }
}
