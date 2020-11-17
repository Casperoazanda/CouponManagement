package com.azanda.coupon.dao;

import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 对coupon表的curd查询
public interface CouponDao extends JpaRepository<Coupon,Integer> {
    // 根据userID + coupon status查询coupon
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
