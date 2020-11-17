package com.azanda.coupon.converter;
import com.azanda.coupon.constant.ProductLine;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ProductLineConverter implements AttributeConverter<ProductLine,Integer> {
    @Override
    public Integer convertToDatabaseColumn(ProductLine productLine) {
        return productLine.getCode();
    }

    @Override
    public ProductLine convertToEntityAttribute(Integer code) {
        return ProductLine.isExist(code);
    }
}
