package com.azanda.coupon.service.impl;

import com.azanda.coupon.constant.Constant;
import com.azanda.coupon.dao.CouponTemplateDao;
import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.service.IAsyncService;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// 异步的服务接口的class实现
@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    private final CouponTemplateDao templateDao;
    // 说明redis的key and value都是string的值
    private final StringRedisTemplate redisTemplate;
    @Autowired
    public AsyncServiceImpl(CouponTemplateDao templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }



    @Override
    @Async("getAsyncExecutor")
    @SuppressWarnings("all")
    // 特定优惠卷模版生成一定数量的优惠卷 这个数量是在CouponTemplate中的count存储
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();
        // 这里要动态的生成每一个优惠卷的编码 再自定义一个方法 实现后14位的优惠卷码
        // 这里用set是保证一定数量的优惠卷码不会出现重复
        Set<String> couponCodes =buildCouponCode(template);

        // 固定的prefix+id的形式构成redis的key
        String redisKey = String.format("%s%s",
                Constant.RedisPrefix.COUPON_TEMPLATE,template.getId().toString());

        log.info("Push CouponCode to Redis:{}",
                redisTemplate.opsForList().rightPushAll(redisKey,couponCodes));
        // 当CouponTemplate生成了一定数量的coupon后 让template的avaiable设置为true
        template.setAvailable(true);

        // templateDao这是个接口但它还继承了jpa的特性 所以有save方法
        templateDao.save(template);
        watch.stop();
        log.info("Construct CouponCode By Template Cost:{}ms",
                watch.elapsed(TimeUnit.MILLISECONDS));

        //TODO: send message to inform CouponTemplate now is avaiable
        log.info("CouponTemplate({} is available)",
                template.getId());


    }




    // 下面的buildCouponCodeSuffix14方法已经动态的生成了后14位
    // 这个方法是用来组装前面4 + 后面的14位
    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template)
    {
        Stopwatch watch = Stopwatch.createStarted();
        // 获取到这个模版下有都少张优惠卷
        Set<String> result = new HashSet<>(template.getCount());

        // 前4位
        String prefix4 = template.getProductLine().getCode().toString()
                + template.getCategory().getCode();

        // 获取到优惠卷模版创建的日期
        String date = new SimpleDateFormat("yyMMdd")
                .format(template.getCreateTime());

        for(int i=0;i!=template.getCount();i++)
        {
            result.add(prefix4+buildCouponCodeSuffix14(date));
        }

        // 如果一旦出现相同的 不会进入set中 这个时候set的result就会小于getCount()
        while(result.size() < template.getCount())
        {
            result.add(prefix4+buildCouponCodeSuffix14(date));
        }
        // 不必要再写if的判断 如果assert返回true 继续执行 or throw exception
        assert result.size() == template.getCount();

        watch.stop();
        log.info("Build Coupon code cost:{}ms",watch.elapsed(TimeUnit.MILLISECONDS));
        return result;
    }





   // 这个是动态生成每一张优惠卷码的方法
    // params：date是在上面的方法中已经给定的当前date 获取到的是yyMMdd
    // 优惠卷码由18位组成：productLine + productCategory(4) + Date(6) + random(8)
    // 但在random 8 位中 第一位不可以为0 要单独把第一位拿出来
    // 获取到的优惠卷模版创建日期也要进行洗牌
    private String buildCouponCodeSuffix14(String date)
    {
        char[] base = new char[]{'1','2','3','4','5','6','7','8','9'};
        // 中间6位时间的重新洗牌
        List<Character> chars = date.chars()
                .mapToObj(e->(char) e).collect(Collectors.toList());
        // 先通过把这6位放到一个char的list中进行洗牌
        Collections.shuffle(chars);
        //再将洗好之后的list变成一个string
        String mid6 = chars.stream()
                .map(Object::toString).collect(Collectors.joining());


        // 后8位
        String suffix8 = RandomStringUtils.random(1,base)
                + RandomStringUtils.randomNumeric(7);

        return mid6+suffix8;
    }
}
