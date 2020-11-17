package com.azanda.coupon.service;

import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// 优惠卷模版基础曾删改查服务的接口
public interface ITemplateBaseService {
    // 根据优惠卷模版id获取优惠卷的模版信息
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;
    // 查找所有可用的优惠卷模版
    List<CouponTemplateSDK> findAllUsableTemplate();
    // 获取优惠券模版id到 CouponTemplateSDK的映射 Map<key:id, Value: CouponTemplateSDK>
    Map<Integer,CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
