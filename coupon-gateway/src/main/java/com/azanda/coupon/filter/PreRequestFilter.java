package com.azanda.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 在过滤器中最先存储客户端发起请求的时间戳
@Slf4j
@Component
public class PreRequestFilter extends AbstractPreZuulFilter{
    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    protected Object cRun() {
        context.set("startTime",System.currentTimeMillis());
        return success();
    }
}
