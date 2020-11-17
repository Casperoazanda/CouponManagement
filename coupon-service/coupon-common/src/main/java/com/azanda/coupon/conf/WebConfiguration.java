package com.azanda.coupon.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

// 用来定制http消息转换器 让底层的java object -> json
@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 将之前系统默认的converter清空
        converters.clear();
        converters.add(new MappingJackson2HttpMessageConverter());

    }
}
