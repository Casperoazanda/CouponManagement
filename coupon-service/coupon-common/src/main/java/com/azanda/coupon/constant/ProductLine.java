package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 用于定义每一个coupon是属于哪个产品的
@Getter
@AllArgsConstructor
public enum ProductLine {
    PRODUCT_A("PRODUCT_A",1),
    PRODUCT_B("PRODUCT_B",2);

    private String description;
    private Integer code;

    public static ProductLine isExist(Integer code)
    {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(bean->bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code +"not exists"));
    }
}
