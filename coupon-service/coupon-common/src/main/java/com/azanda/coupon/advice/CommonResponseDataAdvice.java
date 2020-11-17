package com.azanda.coupon.advice;

import com.azanda.coupon.annotation.IgnoreResponseAdvice;
import com.azanda.coupon.vo.CommonResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

// 用来判断是不是需要对响应作出处理
// 在响应返回之前都要做些什么
// 各种advice 都是要先有一个基本的类 然后再延伸出的CommonResponseDataAdvice
// 这些类单独定义在一个包里
@RestControllerAdvice
public class CommonResponseDataAdvice implements ResponseBodyAdvice<Object> {
    // 判断是否需要对response进行加工处理
    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        // 如果当前的类标志了 @IgnoreResponseAdvice 就不再需要处理了
        if(methodParameter.getDeclaringClass().isAnnotationPresent(IgnoreResponseAdvice.class))
        {
            return false;
        }
        // 如果当前的方法标志了 @IgnoreResponseAdvice 就不再需要处理了
        else if(methodParameter.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class))
        {
            return false;
        }
        // 需要对response进行处理 继续执行 beforeBodyWrite
        return true;
    }
    // response返回之前需要做的处理
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 定义最终要返回的对象
        CommonResponse<Object> response = new CommonResponse<>(0,"");
        // 如果Object o是null response不需要设置data
        if(o==null)
        {
            return response;
        }
        // 如果 o 已经是一个CommonResponse 不需要再次处理
        else if(o instanceof  CommonResponse)
        {
            response = (CommonResponse<Object>) o;
        }
        // 否则 把response 作为commonResponse的data
        else
        {
            response.setData(o);
        }
        return response;

    }
}
