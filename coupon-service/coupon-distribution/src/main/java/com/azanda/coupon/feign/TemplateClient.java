package com.azanda.coupon.feign;
// 调用coupon-template微服务的接口
// 需要用到coupon-template中的controller
// 在controller中的方法都已经写在了service中

import com.azanda.coupon.feign.hystrix.TemplateClientHystrix;
import com.azanda.coupon.vo.CommonResponse;
import com.azanda.coupon.vo.CouponTemplateSDK;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// value的值是想要调用微服务的名字 定义在yml中的application name中
// context-path: 定义的是访问的前缀url
@FeignClient(value = "eureka-client-coupon-template",fallback = TemplateClientHystrix.class)
public interface TemplateClient
{
    // 查找所有可以用的coupon-template
    // 这个返回值要和controller中对应方法的返回值保持一致
    // 是一个get方法
    @RequestMapping(value = "/coupon-template/template/sdk/all",method = RequestMethod.GET)
    CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate();


    //获取template ids -> CouponTemplateSDK的映射
    // 需要id id一定是传来的参数
    @RequestMapping(value = "/coupon-template/template/sdk/infos",method = RequestMethod.GET)
    CommonResponse<Map<Integer,CouponTemplateSDK>> findIds2TemplateSDK(
            @RequestParam("ids") Collection<Integer> ids );
}
