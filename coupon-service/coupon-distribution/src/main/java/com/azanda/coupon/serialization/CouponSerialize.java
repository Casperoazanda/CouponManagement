package com.azanda.coupon.serialization;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.entity.Coupon;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;

// 将coupon实体类 转换为json对象
// json的key 和value都是string类型
// 和entity中的coupon object配合在一起用
// 在返回给front-end的时候会将object -> json
public class CouponSerialize extends JsonSerializer<Coupon> {
    @Override
    public void serialize(Coupon coupon,
                          JsonGenerator generator,
                          SerializerProvider serializers)
            throws IOException {
        generator.writeStartObject();
        generator.writeStringField("id",coupon.getId().toString());
        generator.writeStringField("templateId",coupon.getTemplateId().toString());
        generator.writeStringField("userId",coupon.getUserId().toString());
        generator.writeStringField("couponCode",coupon.getCouponCode());
        generator.writeStringField("assignTime",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                        coupon.getAssignTime()
                ));
        // 下面的由调用sdk展示template的信息
        generator.writeStringField("name",coupon.getTemplateSDK().getName());
        generator.writeStringField("logo",coupon.getTemplateSDK().getLogo());
        generator.writeStringField("desc",coupon.getTemplateSDK().getDesc());
        generator.writeStringField("status",coupon.getStatus().getDescription());
        // 下面的expiration不是一个常规类型 是专门的一个class在templateRule这个class中
        // 解析出来的是一个对象 要将他json
        generator.writeStringField("expiration",
                JSON.toJSONString(coupon.getTemplateSDK().getRule().getExpiration()));

        generator.writeStringField("discount",JSON.toJSONString(
                coupon.getTemplateSDK().getRule().getDiscount()
        ));

        generator.writeStringField("usage",JSON.toJSONString(
                coupon.getTemplateSDK().getRule().getUsage()
        ));

        generator.writeEndObject();

    }
}
