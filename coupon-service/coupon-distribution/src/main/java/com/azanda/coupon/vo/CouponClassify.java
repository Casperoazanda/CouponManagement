package com.azanda.coupon.vo;
// 这个class的意义在于 传递进来一个coupon list 判断这些coupon哪些是used usable expired
// 根据coupon status加入到不同的list中 最后再把这个list返回

import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.constant.PeriodType;
import com.azanda.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {
    // 可以使用的
    private List<Coupon> usable;
    // 已使用的
    private List<Coupon> used;

    // 已经过期的
    private List<Coupon> expired;

    //一个class内的方法给list of coupons进行分类
    public static CouponClassify classify(List<Coupon> coupons)
    {
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());

        coupons.forEach(
                c-> {
                    // 判断优惠卷是否过期
                    boolean isTimeExpire;
                    Long curTime = new Date().getTime();

                    // 如果当前template采用的是过期时间fixed的方式 需要判断过期日期和当前日期的大小
                    // 设定的过期时间应该是大于现在的时间 如果小于现在的时间 则过期了
                    if(c.getTemplateSDK().getRule().getExpiration().getPeriodCode().equals(
                            PeriodType.FIXED.getCode()))
                    {
                        // 设定的过期时间是否超过了当前的时间
                        isTimeExpire = c.getTemplateSDK().getRule().getExpiration().getDeadline() <=curTime;
                    }
                    else
                    {
                        // 如果不是fixed的deadline 则需要用gap和assignTime来算出具体的过期时间和现在的时间的关系
                        isTimeExpire = DateUtils.addDays(
                                c.getAssignTime(),
                                c.getTemplateSDK().getRule().getExpiration().getGap()
                        ).getTime() <= curTime;
                    }

                    if(c.getStatus() == CouponStatus.USED)
                    {
                        used.add(c);
                    }
                    else if(c.getStatus() == CouponStatus.EXPIRED || isTimeExpire)
                    {
                        expired.add(c);
                    }
                    else
                    {
                        usable.add(c);
                    }

                });

            return new CouponClassify(usable,used,expired);
    }







}
