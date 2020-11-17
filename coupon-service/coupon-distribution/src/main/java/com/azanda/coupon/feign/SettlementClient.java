package com.azanda.coupon.feign;

import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.feign.hystrix.SettlementClientHystrix;
import com.azanda.coupon.vo.CommonResponse;
import com.azanda.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


//调用结算微服务的接口
@FeignClient(value = "eureka-client-coupon-settlement",fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    // 优惠卷规则计算
    @RequestMapping(value = "/coupon-settlement/settlement/compute",method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computeRule(
            @RequestBody SettlementInfo settlement ) throws CouponException;

}
