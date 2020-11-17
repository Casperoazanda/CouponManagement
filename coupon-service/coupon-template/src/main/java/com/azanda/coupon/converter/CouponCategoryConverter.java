package com.azanda.coupon.converter;

import com.azanda.coupon.constant.CouponCategory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

// 优惠卷分类的转换器 转换成数据表中的一个table
// <X,Y> X:是object的类 Y:是数据库中对应column存储的类型
@Converter
public class CouponCategoryConverter implements AttributeConverter<CouponCategory,String> {
    // 将object类的属性转换为可以存到数据库中的数据
    @Override
    public String convertToDatabaseColumn(CouponCategory couponCategory) {
        return couponCategory.getCode();
    }

    @Override
    public CouponCategory convertToEntityAttribute(String code) {
        return CouponCategory.isExist(code);
    }



}
