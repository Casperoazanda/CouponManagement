package com.azanda.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.service.IBuildTemplateService;
import com.azanda.coupon.service.ITemplateBaseService;
import com.azanda.coupon.vo.CouponTemplateSDK;
import com.azanda.coupon.vo.TemplateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// 和couponTemplate相关的controller 其实controller的用处就是调用在service中已经写好的方法
@Slf4j
@RestController
public class CouponTemplateController {

    // 在service中的接口 调用创建template
    private final IBuildTemplateService buildTemplateService;
    // 在service中的接口 调用对数据库中的template的基础curd服务
    private final ITemplateBaseService templateBaseService;
    @Autowired
    public CouponTemplateController(IBuildTemplateService buildTemplateService, ITemplateBaseService templateBaseService)
    {
        this.buildTemplateService = buildTemplateService;
        this.templateBaseService = templateBaseService;
    }


    // 通过前端已有的数据构造新的couponTemplate
    // 表单的形式 需要用post请求
    // 127.0.0.1:7001/coupon-template/template/build 这个是单独访问
    // 127.0.0.1:9000/coupon-template/template/build 通过网关转发访问
    // @RequestBody这个和js中的差不多 参数需要从request的body中解析出来
    @PostMapping("/template/build")
    public CouponTemplate buildTemplate(@RequestBody TemplateRequest request)
            throws CouponException
    {
        log.info("Build Template:{}", JSON.toJSONString(request));
        return buildTemplateService.buildTemplate(request);
    }

    // 根据id查询template详情
    // 127.0.0.1:7001/coupon-template/template/info?id=1
    // 127.0.0.1:9000/coupon-template/template/info?id=1s
    @GetMapping("/template/info")
    public CouponTemplate buildTemplateInfo(@RequestParam("id") Integer id)
            throws CouponException
    {
        log.info("Build Template Info for:{}",id);
        return templateBaseService.buildTemplateInfo(id);
    }


    // 查找所有可用的template 也用于service之间的传递
    // 127.0.0.1:7001/coupon-template/template/sdk/all
    // 127.0.0.1:9000/coupon-template/template/sdk/all
    @GetMapping("/template/sdk/all")
    public List<CouponTemplateSDK> findAllUsableTemplate()
    {
        log.info("Find All Usable Template");
        return templateBaseService.findAllUsableTemplate();
    }

    //获取template ids-> CouponTemplateSDK的映射
    // 127.0.0.1:7001/coupon-template/template/sdk/infos
    // 127.0.0.1:9000/coupon-template/template/sdk/infos
    @GetMapping("/template/sdk/infos")
    public Map<Integer,CouponTemplateSDK> findIds2TemplateSDK(
            @RequestParam("ids") Collection<Integer> ids)
    {
        log.info("FindIds2TemplateSDK:{}",JSON.toJSONString(ids));
        return templateBaseService.findIds2TemplateSDK(ids);
    }
}
