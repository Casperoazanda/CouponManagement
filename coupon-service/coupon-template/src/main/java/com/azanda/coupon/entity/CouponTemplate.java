package com.azanda.coupon.entity;

import com.azanda.coupon.constant.CouponCategory;
import com.azanda.coupon.constant.DistributeTarget;
import com.azanda.coupon.constant.ProductLine;
import com.azanda.coupon.converter.CouponCategoryConverter;
import com.azanda.coupon.converter.DistributeTargetConverter;
import com.azanda.coupon.converter.ProductLineConverter;
import com.azanda.coupon.converter.RuleConverter;
import com.azanda.coupon.serialization.CouponTemplateSerialize;
import com.azanda.coupon.vo.TemplateRule;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

// coupon模版的实体类 根据总的TemplateRule来制定每一类的coupon
// 再根据每一个类的coupon来生成优惠卷
// 整个这个类对应的是数据库中的一个表格
// Entity 是jpa中数据表的映射
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name="coupon_template")
@JsonSerialize(using = CouponTemplateSerialize.class)
public class CouponTemplate implements Serializable {
    // 自增的主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    // 是否是可用的状态
    @Column(name = "available",nullable = false)
    private Boolean available;

    //是否过期
    @Column(name = "expired",nullable = false)
    private Boolean expired;

    // 优惠卷的名称
    @Column(name = "name",nullable = false)
    private String name;

    // 优惠卷logo
    @Column(name = "logo",nullable = false)
    private String logo;

    // 优惠卷描述
    @Column(name = "intro",nullable = false)
    private String desc;

    // 优惠卷分类
    @Column(name = "category",nullable = false)
    @Convert(converter = CouponCategoryConverter.class)
    private CouponCategory category;

    // 产品线
    @Column(name = "product_line",nullable = false)
    @Convert(converter = ProductLineConverter.class)
    private ProductLine productLine;

    // 一种优惠卷的总数
    @Column(name = "coupon_count",nullable = false)
    private Integer count;

    // 创建时间
    @CreatedDate
    @Column(name = "create_time",nullable = false)
    private Date createTime;

    // 创建用户
    @Column(name = "user_id",nullable = false)
    private Long userId;

    // 优惠卷模版编码
    @Column(name = "template_key",nullable = false)
    private String key;

    // 目标的用户
    @Column(name = "target",nullable = false)
    @Convert(converter = DistributeTargetConverter.class)
    private DistributeTarget target;

    // 优惠卷的规则
    @Column(name = "rule",nullable = false)
    @Convert(converter = RuleConverter.class)
    private TemplateRule rule;

    // 自定义构造函数
    public CouponTemplate(String name, String logo, String desc,
                          String category, Integer productLine,
                          Integer count, Long userId, Integer target,
                          TemplateRule rule)
    {
        // 因为表格还没有创建 现在这个constructor不可以用
        this.available=false;
        this.expired= false;
        this.name=name;
        this.logo=logo;
        this.desc=desc;
        this.category = CouponCategory.isExist(category);
        this.productLine = ProductLine.isExist(productLine);
        this.count = count;
        this.userId=userId;

        // 优惠卷模版的唯一编码 = 4(产品线1 类型3)+8(date)+4(id)
        this.key = productLine.toString() + category +
                new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.target = DistributeTarget.isExist(target);
        this.rule = rule;
    }


}
