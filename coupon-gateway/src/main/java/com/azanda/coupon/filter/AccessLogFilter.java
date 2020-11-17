package com.azanda.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

// 记录用户日志 记录用户请求的时间
@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter {
    // 找出返回response的order -1 就是需要把当前gateway在返回response之前计算
    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER -1;
    }

    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        // 从PreRequestFilter中获取设置的请求时间
        // context 本身就是用于各个filter之间传递消息
        // 这里get里面的变量值一定要和 PreRequestFilter中的是一样的
        long startTime = (Long) context.get("startTime");
        String uri = request.getRequestURI();
        long duration = System.currentTimeMillis() - startTime;
        // 从网关这里直接打印日志记录 uri+duration
        log.info("uri:{}, duration:{}",uri,duration);
        return success();
    }
}
