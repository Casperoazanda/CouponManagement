package com.azanda.coupon.converter;

import com.azanda.coupon.constant.DistributeTarget;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class DistributeTargetConverter implements AttributeConverter<DistributeTarget,Integer> {
    @Override
    public Integer convertToDatabaseColumn(DistributeTarget distributeTarget) {
        return distributeTarget.getCode();
    }

    @Override
    public DistributeTarget convertToEntityAttribute(Integer code) {
        return DistributeTarget.isExist(code);
    }
}
