package com.azanda.coupon.feign.hystrix;

import com.azanda.coupon.feign.TemplateClient;
import com.azanda.coupon.vo.CommonResponse;
import com.azanda.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

// coupon-template微服务熔断了之后兜底策略
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {

    // 查找可用的coupon-tempalte
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate" +
                "request error");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error",
                Collections.emptyList()
        );
    }

    // 获取template ids -> CouponTemplateSDK 的映射
    // 如果在接口那里调用coupon-template失败 用fallback回到这里调用
    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK" +
                "request error");
        // 最下面的数据类型 要和方法中限定的数据类型是一致的
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] request error",
                new HashMap<>()
        );
    }
}
