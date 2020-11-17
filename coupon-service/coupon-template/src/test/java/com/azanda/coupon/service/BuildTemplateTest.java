package com.azanda.coupon.service;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.constant.CouponCategory;
import com.azanda.coupon.constant.DistributeTarget;
import com.azanda.coupon.constant.PeriodType;
import com.azanda.coupon.constant.ProductLine;
import com.azanda.coupon.vo.TemplateRequest;
import com.azanda.coupon.vo.TemplateRule;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

// couponTemplate construction test
@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {
    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception
    {
        System.out.println(JSON.toJSONString(
                buildTemplateService.buildTemplate(fakeTemplateRequest())));
        // 在buildTemplateService中调用了IAsyncService 异步的生成coupon code
        // 也就是说另外开辟了一个thread 但另外的这个thread必须依靠main thread 一旦main shut down
        // vice thread也会shut down 所以需要一个sleep的时间来执行vice thread
        Thread.sleep(5000);
    }


    private TemplateRequest fakeTemplateRequest()
    {
        TemplateRequest request = new TemplateRequest();
        request.setName("CouponTemplate-"+ new Date().getTime());
        request.setLogo("http://www.google.com");
        request.setDesc("A coupon template");
        request.setCategory(CouponCategory.DOLLAR_DISCOUNT_WITH_LIMITATION.getCode());
        request.setProductLine(ProductLine.PRODUCT_A.getCode());
        request.setCount(100);
        request.setUserId(1001L); // fake long user id
        request.setTarget(DistributeTarget.SINGLE.getCode());

        // 创建一个templateRule的对象
        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.CHANGEABLE.getCode(),
                1,
                DateUtils.addDays(new Date(),60).getTime()
        ));

        rule.setDiscount(new TemplateRule.Discount(5,1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage(
                "BC","Vancouver",
                JSON.toJSONString(Arrays.asList("entertainment","furniture"))
        ));

        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        request.setRule(rule);
        return request;
    }











}
