package com.azanda.coupon.controller;

import com.azanda.coupon.exception.CouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 这是一个健康检查的接口
@Slf4j
@RestController
// RestController 是没有对应视图反射的
public class HealthCheck {
    // 在springCloud中 除了server之外的所有都是client
    // 因为微服务之间是完全独立的 client需要将自己注册到server中
    // 也可以从server中获取到当前client的元信息

    // client会向eureka server中发起注册
    // 通过DiscoveryClient获取保存在server中的client信息
    private final DiscoveryClient client;

    // 服务注册 并返回一个id
    private final Registration registration;

    public HealthCheck(DiscoveryClient client, Registration registration) {
        this.client = client;
        this.registration = registration;
    }

    // 将康检查的接口 127.0.0.1:7001/coupon-template/health
    // 7001是在yml中定义的当前microservice的port
    // coupon-template是yml中定义的 context-path
    @GetMapping("/health")
    public String health()
    {
        log.debug("view health api");
        return "Coupon Template Is Ok!";
    }

    // 异常测试接口 127.0.0.1:7001/coupon-template/exception
    @GetMapping("/exception")
    public String exception() throws CouponException
    {
        log.debug("View exception api");
        throw new CouponException("Problem with CouponTemplate");
    }

    // 获取Eureka server上当前client的元信息
    // 因为当前client 可能还会有很多个instance
    // 127.0.0.1:7001/coupon-template/info
    @GetMapping("/info")
    public List<Map<String,Object>> info()
    {
        // 注册后 大约需要等待2min才可以获取到注册的信息
        List<ServiceInstance> instances = client.getInstances(registration.getServiceId());

        List<Map<String,Object>> result = new ArrayList<>(instances.size());
        // 把从server中获取到的所有service的instance全部转换成一个map的格式
        // 并把每一条都加入到result中
        // 最终返回result
        instances.forEach(
                i->{
                    Map<String,Object> info = new HashMap<>();
                    info.put("serviceId",i.getServiceId());
                    info.put("instanceId",i.getInstanceId());
                    info.put("port",i.getPort());

                    result.add(info);
                });
        return result;
    }

}
