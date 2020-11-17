package com.azanda.coupon.serialization;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.entity.CouponTemplate;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.SimpleDateFormat;

// 这个是为了查询到数据之后把数据变成可以供前端看的数据
// 在这里要制定是哪个类的数据要serialize
public class CouponTemplateSerialize extends JsonSerializer<CouponTemplate> {
    @Override
    public void serialize(CouponTemplate template,
                          JsonGenerator generator,
                          SerializerProvider serializerProvider)
            throws IOException {
        // 开始serialize
        generator.writeStartObject();

        generator.writeStringField("id",template.getId().toString());
        generator.writeStringField("name",template.getName());
        generator.writeStringField("logo",template.getLogo());
        generator.writeStringField("desc",template.getDesc());
        generator.writeStringField("category",template.getCategory().getDescription());
        generator.writeStringField("productLine",template.getProductLine().getDescription());
        generator.writeStringField("count",template.getCount().toString());
        generator.writeStringField("createTime",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(template.getCreateTime()));
        generator.writeStringField("userId",template.getUserId().toString());
        generator.writeStringField("key",
                template.getKey()+ String.format("%04d",template.getId()));
        generator.writeStringField("target",template.getTarget().getDescription());
        generator.writeStringField("rule", JSON.toJSONString(template.getRule()));

        // 结束序列化对象
        generator.writeEndObject();

    }
}
