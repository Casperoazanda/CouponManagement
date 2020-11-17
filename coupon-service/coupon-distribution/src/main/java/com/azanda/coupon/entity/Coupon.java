package com.azanda.coupon.entity;

import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.converter.CouponStatusConverter;
import com.azanda.coupon.serialization.CouponSerialize;
import com.azanda.coupon.vo.CouponTemplateSDK;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

// 用户coupon的实体类（用户领取coupon的记录）
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "coupon")
@JsonSerialize(using = CouponSerialize.class)
public class Coupon {
    // 自增主键id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    private Integer id;

    // 关联的couponTemplate 逻辑外键
    @Column(name = "template_id",nullable = false)
    private Integer templateId;

    // 领取的用户id
    @Column(name = "user_id",nullable = false)
    private Long userId;

    // coupon code（String）
    @Column(name = "coupon_code",nullable = false)
    private String couponCode;

    // 领取时间：是jpa审计功能实现的自动把创建时间给到变量中
    @CreatedDate
    @Column(name = "assign_time",nullable = false)
    private Date assignTime;

    // coupon现在的状态
    @Basic
    @Column(name = "status",nullable = false)
    @Convert(converter = CouponStatusConverter.class)
    private CouponStatus status;

    // 用户优惠卷对应的模版信息 transient是不需要真的用jpa体现在数据库表格中的
    @Transient
    private CouponTemplateSDK templateSDK;


    // 为了测试 返回一个无效的coupon对象
    // 在类里返回一个当前类的类型的一个方法 如下 是无法调用到自身的
    public static Coupon invalidCoupon()
    {
        Coupon coupon = new Coupon();
        coupon.setId(-1);
        return coupon;
    }

    // 构造一个coupon
    public Coupon(Integer templateId,Long userId,String couponCode,
                  CouponStatus status)
    {
        this.templateId = templateId;
        this.userId = userId;
        this.couponCode = couponCode;
        this.status = status;
    }

}
