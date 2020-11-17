package com.azanda.coupon.service;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.constant.CouponCategory;
import com.azanda.coupon.constant.GoodsType;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.executor.ExecuteManager;
import com.azanda.coupon.vo.CouponTemplateSDK;
import com.azanda.coupon.vo.GoodsInfo;
import com.azanda.coupon.vo.SettlementInfo;
import com.azanda.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecuteManagerTest {

    /** fake 一个 UserId */
    private Long fakeUserId = 20001L;

    @Autowired
    private ExecuteManager manager;

    @Test
    public void testComputeRule() throws CouponException {
//         满减优惠券结算测试
//        log.info("ManJian Coupon Executor Test");
//        SettlementInfo manjianInfo = fakeManJianCouponSettlement();
//        SettlementInfo result = manager.computeRule(manjianInfo);
//
//        log.info("{}", result.getCost());
//        log.info("{}", result.getCouponAndTemplateInfos().size());
//        log.info("{}", result.getCouponAndTemplateInfos());

        // 折扣优惠券结算测试
        log.info("ZheKou Coupon Executor Test");
        SettlementInfo zhekouInfo = fakeZheKouCouponSettlement();
        SettlementInfo result = manager.computeRule(zhekouInfo);

        log.info("{}", result.getCost());
        log.info("{}", result.getCouponAndTemplateInfos().size());
        log.info("{}", result.getCouponAndTemplateInfos());
    }

    /**
     * <h2>fake(mock) 满减优惠券的结算信息--dollar discount with limitation</h2>
     * */
    private SettlementInfo fakeManJianCouponSettlement() {

        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setCode(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        // 达到满减标准
        goodsInfo02.setCount(10);
        // 没有达到满减标准
        //goodsInfo02.setCount(5);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setCode(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.DOLLAR_DISCOUNT_WITH_LIMITATION.getCode());
        templateSDK.setKey("100120190801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(20, 199));
        rule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.ENTERTAINMENT.getCode(),
                        GoodsType.FURNITURE.getCode()
                ))));
        templateSDK.setRule(rule);

        ctInfo.setTemplate(templateSDK);

        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }


    /**
     * <h2>fake 折扣优惠券的结算信息--discount</h2>
     * */
    private SettlementInfo fakeZheKouCouponSettlement() {

        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setCode(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setCode(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(2);
        templateSDK.setCategory(CouponCategory.PERCENTAGE_DISCOUNT.getCode());
        templateSDK.setKey("100220190712");

        // 设置 TemplateRule
        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(85, 1));
        rule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.ENTERTAINMENT.getCode(),
                        GoodsType.FURNITURE.getCode()
                ))));
//        rule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
//                JSON.toJSONString(Arrays.asList(
//                        GoodsType.FRESH.getCode(),
//                        GoodsType.FURNITURE.getCode()
//                ))));

        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }


    /**
     * <h2>fake 立减优惠券的结算信息--automatic </h2>
     * */
    private SettlementInfo fakeLiJianCouponSettlement() {

        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setCode(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setCode(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        SettlementInfo.CouponAndTemplateInfo ctInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(3);
        templateSDK.setCategory(CouponCategory.AUTOMATIC_DISCOUNT.getCode());
        templateSDK.setKey("200320190712");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.ENTERTAINMENT.getCode(),
                        GoodsType.FURNITURE.getCode()
                ))));
        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);

        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    /**
     * <h2>fake 满减 + 折扣优惠券结算信息</h2>
     * */
    private SettlementInfo fakeManJianAndZheKouCouponSettlement() {

        SettlementInfo info = new SettlementInfo();
        info.setUserId(fakeUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo01 = new GoodsInfo();
        goodsInfo01.setCount(2);
        goodsInfo01.setPrice(10.88);
        goodsInfo01.setCode(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo02 = new GoodsInfo();
        goodsInfo02.setCount(10);
        goodsInfo02.setPrice(20.88);
        goodsInfo02.setCode(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo01, goodsInfo02));

        // 满减优惠券
        SettlementInfo.CouponAndTemplateInfo manjianInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        manjianInfo.setId(1);

        CouponTemplateSDK manjianTemplate = new CouponTemplateSDK();
        manjianTemplate.setId(1);
        manjianTemplate.setCategory(CouponCategory.DOLLAR_DISCOUNT_WITH_LIMITATION.getCode());
        manjianTemplate.setKey("100120190712");

        TemplateRule manjianRule = new TemplateRule();
        manjianRule.setDiscount(new TemplateRule.Discount(20, 199));
        manjianRule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.ENTERTAINMENT.getCode(),
                        GoodsType.FURNITURE.getCode()
                ))));
        manjianRule.setWeight(JSON.toJSONString(Collections.emptyList()));
        manjianTemplate.setRule(manjianRule);
        manjianInfo.setTemplate(manjianTemplate);

        // 折扣优惠券
        SettlementInfo.CouponAndTemplateInfo zhekouInfo =
                new SettlementInfo.CouponAndTemplateInfo();
        zhekouInfo.setId(1);

        CouponTemplateSDK zhekouTemplate = new CouponTemplateSDK();
        zhekouTemplate.setId(2);
        zhekouTemplate.setCategory(CouponCategory.PERCENTAGE_DISCOUNT.getCode());
        zhekouTemplate.setKey("100220190712");

        TemplateRule zhekouRule = new TemplateRule();
        zhekouRule.setDiscount(new TemplateRule.Discount(85, 1));
        zhekouRule.setUsage(new TemplateRule.Usage("安徽省", "桐城市",
                JSON.toJSONString(Arrays.asList(
                        GoodsType.ENTERTAINMENT.getCode(),
                        GoodsType.FURNITURE.getCode()
                ))));
        zhekouRule.setWeight(JSON.toJSONString(
                Collections.singletonList("1001201907120001")
        ));
        zhekouTemplate.setRule(zhekouRule);
        zhekouInfo.setTemplate(zhekouTemplate);

        info.setCouponAndTemplateInfos(Arrays.asList(
                manjianInfo, zhekouInfo
        ));

        return info;
    }
}
