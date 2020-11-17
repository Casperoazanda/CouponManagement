package com.azanda.coupon.service;
// 用户服务相关的接口
// 1 用户三类优惠卷状态查询：已用 未用 未用已过期
// 2 查看用户当前可以领取的couponTemplate
// 3 用户要领取coupon
// 4 用户消费coupon -- coupon-settlement

import com.azanda.coupon.entity.Coupon;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.vo.AcquireTemplateRequest;
import com.azanda.coupon.vo.CouponTemplateSDK;
import com.azanda.coupon.vo.SettlementInfo;

import java.util.List;

public interface IUserService {

    // 1 需要用户id在数据库中查询 status是在constant中的status
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;

    // 2 需要用户id查找可以领取的couponTemplate
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    // 3. 用户领取coupon request是专门的request模版
    // request 是vo下的 AcquireTemplateRequest
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    // 4 用了coupon之后的结算信息
    SettlementInfo settlement(SettlementInfo info) throws CouponException;





}
