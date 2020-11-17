package com.azanda.coupon.feign.hystrix;

import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.feign.SettlementClient;
import com.azanda.coupon.vo.CommonResponse;
import com.azanda.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 如果coupon-settlement失败了的兜底策略
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {

    // coupon规则计算
    @Override
    public CommonResponse<SettlementInfo> computeRule(SettlementInfo settlement) throws CouponException {
        log.error("[eureka-client-coupon-settlement] computeRule" +
                "request error");
        // 是否要用当前的coupon
        settlement.setEmploy(false);
        // 使用或者不使用coupon后的cost
        settlement.setCost(-1.0);

        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-settlement] request error",
                settlement
        );
    }
}
