package com.azanda.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.constant.Constant;
import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.entity.Coupon;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// 实现redis接口的类
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    // 先开启一个redis
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    // 根据coupon status获取到对应的redis key 之后要用对应的redisKey在redis中查找coupon
    // 在common中的constant有定义
    // 这个是每一个user下不同status coupon的redisKey
    private String status2RedisKey(Integer status,Long userId)
    {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.isExist(status);

        switch (couponStatus)
        {
            case USABLE:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USABLE,userId);
                break;
            case USED:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_USED,userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s",
                        Constant.RedisPrefix.USER_COUPON_EXPIRED,userId);
                break;
        }
        return redisKey;
    }

    // 获取一个redis中随机的过期时间 每隔一段时间key会失效
    // min and max 都是小时数
    private Long getRandomExpirationTime(Integer min,Integer max)
    {
        return RandomUtils.nextLong(
                min * 60 * 60,
                max * 60 * 60
        );
    }


    // 根据userId 和 状态找到redis中的coupon list
    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache:{},{}",userId,status);
        String redisKey = status2RedisKey(status,userId);
        // 有了rediskey之后就可以在这个key下搜索coupon
        // 不做处理之前返回回来的是List<Object>
        // 需要做处理的转换
        // 从redis返回的是Object-> String -> Coupon
        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream()
                .map(o-> Objects.toString(o,null))
                .collect(Collectors.toList());

        // 判断couponStrs返回回来的是否为空
        if(CollectionUtils.isEmpty(couponStrs))
        {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }

        return couponStrs.stream()
                .map(cs->JSON.parseObject(cs,Coupon.class))
                .collect(Collectors.toList());
    }

    //一个用户对应下面会有很多个不一样的status的coupon 存一个fake的进去 防止缓存穿透
    @Override
    @SuppressWarnings("all")
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save Empty List To Cache For User:{}, Status:{}",
                userId, JSON.toJSONString(status));

        // key是coupon id， value是序列化之后的coupon
        // 用户coupon信息在redis中的存储方式
        // KV
        // K: status-> redisKey
        // V: {coupon-id: 序列化之后的Coupon}
        // 但不管是什么 放到redis中的永远是string
        Map<String,String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1",JSON.toJSONString(Coupon.invalidCoupon()));

        // 使用sessionCallback 把数据命令放到Redis的pipeline
        // 一般来说每一条数据放入redis后 都会有response
        // 现在把一系列的数据放入到pipeline 等都处理完成了之后统一返回response
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
               status.forEach(
                       s->{
                           String redisKey = status2RedisKey(s,userId);
                           // 将redisKey和上面写好的invalidCouponMap一同放入redis中
                            redisOperations.opsForHash().putAll(redisKey,invalidCouponMap);
                       }
               );
               return null;
            }

        };
        log.info("Pipeline Exe Result:{}",JSON.toJSONString(
                redisTemplate.executePipelined(sessionCallback)));

    }
    // 根据templateId的值找到对应的couponTemplate 并取出其中的一个coupon-code
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format(
                "%s%s", Constant.RedisPrefix.COUPON_TEMPLATE,templateId.toString()
        );

        // 因为coupon code不存在顺序关系 左边pop 或者右边pop 没有任何的影响
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);

        log.info("Acquire Coupon Code:{},{},{}",
                templateId,redisKey,couponCode);
        return couponCode;
    }

    // 将特定用户的特定coupon类型保存到redis中
    @Override
    public Integer addCouponToCache(Long useId, List<Coupon> coupons,
                                    Integer status) throws CouponException
    {
        log.info("Add Coupon To Cache:{},{},{}",
                useId,JSON.toJSONString(coupons),status);

        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.isExist(status);

        switch (couponStatus)
        {
            case USABLE:
                result=addCouponToCacheForUsable(useId,coupons);
                break;
            case USED:
                result=addCouponToCacheForUsed(useId,coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(useId,coupons);
                break;
        }

        return result;
    }


    // 1. 将user新领取的coupon加入redis中 现在coupon status一定是usable
    // 每个user都会根据他的userId生成新的列表在redis中
    private Integer addCouponToCacheForUsable(Long userId,List<Coupon> coupons)
    {
        // 如果status是usable 是新增的优惠卷
        // 只会影响一个cache：USER_COUPON_USABLE_userId
        log.debug("Add Coupon To Cache For Usable");
        Map<String,String> needCachedObject = new HashMap<>();
        coupons.forEach(
               c-> needCachedObject.put(
                       c.getId().toString(),
                       JSON.toJSONString(c))
        );
        // 这个是含有userId的redisKey
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey,needCachedObject);
        log.info("Add {} Coupons To Cache: {},{}",
                needCachedObject.size(),userId,redisKey);

        // 在redis中设置过期的时间 防止缓存雪崩
        redisTemplate.expire(
                redisKey,
                getRandomExpirationTime(1,2),
                TimeUnit.SECONDS
        );
        // 返回操作成功的数量
        return needCachedObject.size();
    }


    // 2. 将用户已经使用过的list coupon加入redis中
    // 这里需要影响到两个cache：usable used： 从usable中清楚这些已经使用的 在used中加入这些
    @SuppressWarnings("all")
    private Integer addCouponToCacheForUsed(Long userId,List<Coupon> coupons) throws CouponException
    {
        log.debug("Add Coupon To Cache For Used.");
        // 将需要缓存的的value map 创建 后面需要往这个里面添加值
        Map<String,String> needCachedForUsed = new HashMap<>(coupons.size());
        // 给出usable的redisKey
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        // 给出used的redisKey
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(),userId);

        // 获取当前userId下所有可用的coupon
        List<Coupon> curUsableCoupons = getCachedCoupons(userId,CouponStatus.USABLE.getCode());

        // 因为之前save进去一个empty的coupon 当前可用coupon的个数肯定是大于1的
        assert curUsableCoupons.size() > coupons.size();

        // 将要存到used下面的coupon一个个的put进去刚才创建好的value map
        coupons.forEach(
                c->needCachedForUsed.put(c.getId().toString(),JSON.toJSONString(c))
        );

        // 校验当前给出的used的coupons是不是usable coupons的一个子集
        // 因为信息太多 不能都校验 只提取出来id
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        // 如果需要插入这些优惠卷不是所有优惠卷的子集 抛出异常
        if(!CollectionUtils.isSubCollection(paramIds,curUsableIds))
        {
            log.error("CurCoupons Is Not Equal ToCache:{},{},{}",
                    userId,JSON.toJSONString(curUsableIds),JSON.toJSONString(paramIds));

            throw new CouponException("CurCoupons Is Not Equal To Cache");
        }

        List<String> needCleanKey = paramIds.stream()
                .map(i-> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>()
        {
            @Override
            public Objects execute(RedisOperations redisOperations) throws DataAccessException
            {
                // 将已使用的coupon cache 缓存添加
                redisOperations.opsForHash().putAll(
                        redisKeyForUsed,needCachedForUsed
                );

                // 在可用的coupon那边删除这些已经使用的coupon
                // 只需要传递进去要删除的id
                redisOperations.opsForHash().delete(
                        redisKeyForUsable,needCleanKey.toArray()
                );

                // 重新设置过期的时间
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );

                redisOperations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };

        log.info("Pipeline Exe Result:{}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));

        return coupons.size();



    }


    @SuppressWarnings("all")
    private Integer addCouponToCacheForExpired(Long userId,List<Coupon> coupons) throws CouponException
    {
        // status 是expired 也是影响到了两类的优惠卷 usable expired
        log.debug("Add Coupon To Cache For Expired");

        Map<String,String> needCachedForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);

        List<Coupon> curUsableCoupons = getCachedCoupons(userId,CouponStatus.USABLE.getCode());

        // 之前save了一个empty的coupon
        assert curUsableCoupons.size() > coupons.size();

        // 将map中填充值
        coupons.forEach(
                c-> needCachedForExpired.put(
                        c.getId().toString(),
                        JSON.toJSONString(c)
                )
        );
        // 检查当前的expired的list coupons是否是和原来usable中的相符合
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds,curUsableIds))
        {
            log.error("CurCoupons Is Not Equal To Cache:{},{},{}",
                    userId, JSON.toJSONString(curUsableIds),
                    JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupon Is Not Equal To Cache");
        }

        List<String> needCleanKey = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations redisOperations) throws DataAccessException {
               // 已过期coupon加入cache
                redisOperations.opsForHash().putAll(
                        redisKeyForExpired,needCachedForExpired
                );

                // usable coupons 清理
                redisOperations.opsForHash().delete(
                        redisKeyForUsable,needCleanKey.toArray()
                );

                // 重制过期时间
                redisOperations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );

                redisOperations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1,2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };

        log.info("PipeLine Exe Result:{}",
                JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

}
