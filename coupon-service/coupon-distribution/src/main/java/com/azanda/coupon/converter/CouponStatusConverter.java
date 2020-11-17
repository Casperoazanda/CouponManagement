package com.azanda.coupon.converter;

import com.azanda.coupon.constant.CouponStatus;


import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// coupon status enum converter
// 由enum转化成Integer
// 由Integer 转化成 enum
@Converter
public class CouponStatusConverter implements
        AttributeConverter<CouponStatus,Integer> {
    // 在这个里面输入的是enum的写在前面的三个值
    // 能获取到对应值的对应code和desc
    @Override
    public Integer convertToDatabaseColumn(CouponStatus status) {
        return status.getCode();
    }

    @Override
    public CouponStatus convertToEntityAttribute(Integer code) {
        return CouponStatus.isExist(code);
    }
}
