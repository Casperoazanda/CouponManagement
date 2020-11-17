package com.azanda.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

// 商品都有什么类型
@Getter
@AllArgsConstructor
public enum GoodsType {

    ENTERTAINMENT("ENTERTAINMENT",1),
    FRESH("FRESH",2),
    FURNITURE("FURNITURE",3),
    OTHERS("OTHERS",4),
    ALL("ALL",5);

    private String description;

    private Integer code;

    public static GoodsType isExist(Integer code)
    {
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean->bean.code.equals(code))
                .findAny()
                .orElseThrow(
                        ()-> new IllegalArgumentException(code+"not exists")
                );
    }
}
