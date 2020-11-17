package com.azanda.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// 结算时候的总信息 写成一个对象 可以返回给前端
// 1 userId
// 2 商品信息 列表
// 3 coupon list
// 4 结算后的结果金额
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SettlementInfo {
    private Long userId;
    // 商品信息列表
    private List<GoodsInfo> goodsInfos;
    // 优惠卷列表 不仅包括coupon还包括couponTemplate
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;

    // 是否让结算生效
    private Boolean employ;

    // 最后用完coupon的总支付金额
    private Double cost;




    // coupon和对应的couponTemplate的信息
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponAndTemplateInfo
    {
        // coupon 的主键id
        private Integer id;
        // coupon 对应的couponTemplate
        private CouponTemplateSDK template;
    }





}
