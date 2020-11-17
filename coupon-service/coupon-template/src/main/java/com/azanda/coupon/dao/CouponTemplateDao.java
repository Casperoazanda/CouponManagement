package com.azanda.coupon.dao;

import com.azanda.coupon.entity.CouponTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 用jpa来查询数据库中的couponTemplate的表格 是一个接口
// <CouponTemplate,Integer> 想要查询table对应的class 和这个表格的主键类型
public interface CouponTemplateDao
        extends JpaRepository<CouponTemplate, Integer> {
    // 根据模版名称来查询模版 并返回一个couponTemplate的值

    CouponTemplate findByName (String name);

    // 根据给定的available 和 expired 这两个table中的属性 查找 自己制定true 或者 false
    List<CouponTemplate> findAllByAvailableAndExpired (
      Boolean available, Boolean expired
    );
    // 根据expired查询模版记录
    List<CouponTemplate> findAllByExpired (Boolean Expired);


}
