package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 不同的优惠卷有不一样的有效期
// 1. fixed: expeir的date是固定的 2. changeable：是一个固定的period 两个月或者XX
@Getter
@AllArgsConstructor
public enum PeriodType {
    FIXED("FIXED",1),
    CHANGEABLE("CHANGEABLE",2);

    private String description;
    private Integer code;

    public static PeriodType isExist(Integer code)
    {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean-> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code+"not exist"));
    }
}
