package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 两个enum 是用户自己领取：single user 或者是大批量的分发： multiple User
@Getter
@AllArgsConstructor
public enum DistributeTarget {
    SINGLE("SINGLE",1),
    MULTIPLE("MULTIPLE",2);

    private String description;
    private Integer code;

    public static DistributeTarget isExist(Integer code)
    {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean-> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code +"not exists"));
    }
}
