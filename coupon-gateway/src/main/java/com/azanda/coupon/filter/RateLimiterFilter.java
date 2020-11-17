package com.azanda.coupon.filter;
// 这是一个限制流量的过滤器 先通过token过滤判断了user的身份之后 再限制每分钟可以访问的几个user


import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
@SuppressWarnings("all")
public class RateLimiterFilter extends AbstractPreZuulFilter {
    // 限定每秒可以获取到两个token
    RateLimiter rateLimiter = RateLimiter.create(2.0);

    @Override
    public int filterOrder() {
        return 2;
    }
  // 再preZull中 设置了success和fail都是object的返回类型
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        if(rateLimiter.tryAcquire())
        {
            log.info("get rate token successfully");
            return success();
        } else
        {
            log.error("rate limit:{}",request.getRequestURI());
            return fail(402,"error: rate limit");
        }

    }
}
