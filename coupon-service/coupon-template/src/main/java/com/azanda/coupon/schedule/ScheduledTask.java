package com.azanda.coupon.schedule;

import com.azanda.coupon.dao.CouponTemplateDao;
import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 在这个module下的入口中 就要加入enableScheduling的注解
// 定时清理过期的couponTemplate
// 这个couponTemplate expired了 对应生成的coupon也会expired
@Slf4j
@Component
public class ScheduledTask {

    private final CouponTemplateDao templateDao;
    @Autowired
    public ScheduledTask(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    // 下线已经过期的couponTemplate
    @Scheduled(fixedRate = 60 * 60 *1000)
    public void offlineCouponTemplate()
    {
        log.info("Start to Expire CouponTemplate");
        // 找到还没有过期的的template 找到没有过期的 才可以后面对这个template进行验证
        // find 这个template的deadline是不是超过了当前的时间
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        // 若找到的个数为空 说明当前的template都是过期的
        if(CollectionUtils.isEmpty(templates))
        {
            log.info("Done to Expire CouponTemplate");
            return;
        }

        // 获取当前的日期
        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        // 把expired位置还标志为false的template拿出来 一个个做校验
        // 若deadline的时间已经在当前时间之前了 那么就把这个expired的设置为true
        // 把这条template放入list中 之后在数据库中更新这个list中的所有数据
        templates.forEach(
                t->{
                    TemplateRule rule = t.getRule();
                    if(rule.getExpiration().getDeadline()<cur.getTime())
                    {
                        t.setExpired(true);
                        expiredTemplates.add(t);
                    }
                }
        );

        // 在数据库中更新
        if(CollectionUtils.isNotEmpty(expiredTemplates))
        {
            log.info("Expired CouponTemplate Num:{}",
                    templateDao.saveAll(expiredTemplates));
        }

        log.info("Done to Expire CouponTemplate");




    }
}
