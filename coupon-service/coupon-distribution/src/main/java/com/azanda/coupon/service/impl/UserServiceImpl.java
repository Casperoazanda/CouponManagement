package com.azanda.coupon.service.impl;
// 用户服务相关接口的实现
// 所有的操作过程 状态都保存在redis中 并通过kafka传递到mySql中
// 因为在操作过程中会出现失败 使用kafka即使失败了还是可以再次获取message
// 因此不实用springboot中的异步处理

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.constant.Constant;
import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.dao.CouponDao;
import com.azanda.coupon.entity.Coupon;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.feign.SettlementClient;
import com.azanda.coupon.feign.TemplateClient;
import com.azanda.coupon.service.IRedisService;
import com.azanda.coupon.service.IUserService;
import com.azanda.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    // coupon dao的使用
    private final CouponDao couponDao;

    // redis
    private final IRedisService redisService;

    // call coupon-template microservice
    // 调用微服务是需要单独写到feign中的
    // 这个调用的是feign中的Template client
    private final TemplateClient templateClient;

    // call coupon-settlement microservice
    private final SettlementClient settlementClient;

    // 这个是系统中的kafka客户端调用
    private final KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    public UserServiceImpl(CouponDao couponDao, IRedisService redisService, TemplateClient templateClient, SettlementClient settlementClient, KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    // 以下所有的操作都是针对用户的操作 都是需要userId的
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        // 通过getCachedCoupons这个方法之后 若一开始这个状态下没有coupon 则会返回一个空list
        // 且会在这个status下加入一个空的coupon
        List<Coupon> curCached = redisService.getCachedCoupons(userId,status);

        List<Coupon> preTarget;

        // 如果从redis中获得的不是空list 说明之前已经做过操作
        if(CollectionUtils.isNotEmpty(curCached))
        {
            log.debug("Coupon cache is not empty:{},{}",
                    userId,status);
            // 最终把结果存放在preTarget中
            preTarget = curCached;
        }
        else
        {
            // 从redis中获取到的是空list
            log.debug("Coupon cache is empty, get coupon from db:{},{}",
                    userId,status);

            // 从数据库中获取
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(
                    userId, CouponStatus.isExist(status));

            // 如果数据库中也没有记录 那么直接返回就ok
            // 说明当前的user还没有coupon
            // cache中此时已经添加了一张无效的coupon
            if(CollectionUtils.isEmpty(dbCoupons))
            {
                log.debug("Current user do not have coupons:{},{}",
                        userId,status);
                return dbCoupons;
            }

            // 如果数据库中存在记录 因为返回值为Coupon类型
            // Coupon类型下还定义着TemplateSDK 需要手动将TemplateSDK添加值
            // 根据dbCoupons中每一个coupon的templateID 获取对应的templateSDK
            // dbCoupons-> templateId -> id2TemplateSDK -> Map<Integer,CouponTemplateSDK>
            // 为什么这里返回的是CommonResponse的类型： 因为这里用的是feign下的新写的方法 新写的方法返回的是CommonResponse类型
            Map<Integer,CouponTemplateSDK> id2TemplateSDK =
                    templateClient.findIds2TemplateSDK(
                            dbCoupons.stream()
                            .map(Coupon::getTemplateId)
                            .collect(Collectors.toList())
                    ).getData();

            // 前面只是找到他们每个id所对应的sdk 现在是要将sdk设置进去
            dbCoupons.forEach(
                    dc-> dc.setTemplateSDK(
                        id2TemplateSDK.get(dc.getTemplateId())
                    ));

            // 当数据库中存在记录时
            preTarget = dbCoupons;
            // 之前的记录是在db中 重新将记录写入Cache
            redisService.addCouponToCache(userId,preTarget,status);
        }

        // 将无效的coupon剔除 因为会有一个空的coupon
        // 这个方法是配合当从redis中拿出来的数据不为空时
        preTarget = preTarget.stream()
                .filter(c->c.getId()!=-1)
                .collect(Collectors.toList());

        // 如果当前获取到的是usable coupon 需要再次判断是不是expired
        if(CouponStatus.isExist(status)==CouponStatus.USABLE)
        {
            CouponClassify classify = CouponClassify.classify(preTarget);

            // 如果获取已过期的list不是空的 里面有值
            if(CollectionUtils.isNotEmpty(classify.getExpired()))
            {
                log.info("Add Expired Coupons To Cache From FindCouponsByStatus:"
                        + "{},{}", userId,status);

                // 这里因为处理的是expired 就自动处理了expired和usable中的coupons
                redisService.addCouponToCache(userId, classify.getExpired(),CouponStatus.EXPIRED.getCode());

                // 如果expired这个list中不为空 放到kafka中异步的传递给数据库
                // 规定发给哪个topic
                // 参照vo中的CouponKafkaMessage
                kafkaTemplate.send(
                        Constant.TOPIC,
                        JSON.toJSONString(new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                classify.getExpired().stream()
                                        .map(Coupon::getId)
                                        .collect(Collectors.toList())
                        ))
                );

            }

            // 通过classify之后 有的coupon进入到了expired list中 直接返回classify中的usable的list
            return classify.getUsable();
        }
        return preTarget;
    }



    // 因为用户有的已经领取了某个coupon-template下的coupons
    // 但template是有限制的 一个用户可以从一个template领取一定数量的coupons：limitation
    // 1. 将系统中所有未过期的template取出
    // 2. 将user可用的coupon都取出
    // 3. 对比user已使用的template和template的limitation的限制
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        // 取出当前的时间 对template进行二次判断是否expired
        // 清理expired template是定时任务 可能有延迟
        Long curTime = new Date().getTime();

        // 将系统中所有未过期的template取出
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();

        log.debug("Find All Template(From TemplateClient) Count:{}",templateSDKS.size());

        // 再次过滤template
        templateSDKS = templateSDKS.stream().filter(
                t-> t.getRule().getExpiration().getDeadline() > curTime)
                .collect(Collectors.toList());

        log.info("Find Usable Template Count:{}",templateSDKS.size());

        //key 是templateID
        // value->left: template limitation
        // value->right: templateSDK
        Map<Integer, Pair<Integer,CouponTemplateSDK>> limit2Template =
                new HashMap<>(templateSDKS.size());
        // 用templateSDKS中的每一个值去填充limit2Template
        templateSDKS.forEach(
                t-> limit2Template.put(t.getId(),
                        Pair.of(t.getRule().getLimitation(),t)
                ));

        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());

        // 开始寻找当前user所有可用的coupons
        // 这个使用的是第一个方法
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId,CouponStatus.USABLE.getCode());

        log.debug("Current User Has Usable Coupons:{},{}",userId,userUsableCoupons.size());
        // key=templateId
        // List<Coupon> userUsableCoupons
        Map<Integer,List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        // 根据已领取的coupons的个数和limitation的对比
        // templateId-> <limitation,template>
        // templateId-> coupons
        limit2Template.forEach(
                (k,v)-> {
                    int limitation = v.getLeft();
                    CouponTemplateSDK templateSDK = v.getRight();
                    if(templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size()>=limitation)
                    {
                        return;
                    }
                    result.add(templateSDK);
                }
        );
        return result;

    }


    /**
     * <h2>用户领取优惠券</h2>
     * 1. 从 TemplateClient 拿到对应的优惠券, 并检查是否过期
     * 2. 根据 limitation 判断用户是否可以领取
     * 3. save to db
     * 4. 填充 CouponTemplateSDK
     * 5. save to cache
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        Map<Integer, CouponTemplateSDK> id2Template =
                templateClient.findIds2TemplateSDK(
                        Collections.singletonList(
                                request.getTemplateSDK().getId()
                        )
                ).getData();

        // 优惠券模板是需要存在的
        if (id2Template.size() <= 0) {
            log.error("Can Not Acquire Template From TemplateClient: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");
        }

        // 用户是否可以领取这张优惠券
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                request.getUserId(),
                CouponStatus.USABLE.getCode()
        );

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons
                .stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
                request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed Template Assign Limitation: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }

        // 尝试去获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Acquire Coupon Code: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(),
                couponCode, CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);

        // 填充 Coupon 对象的 CouponTemplateSDK, 一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());

        // 放入缓存中
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );

        return newCoupon;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {
        // 当没有传递优惠券时, 直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos =
                info.getCouponAndTemplateInfos();

        if (CollectionUtils.isEmpty(ctInfos)) {

            log.info("Empty Coupons For Settle.");
            double goodsSum = 0.0;

            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() + gi.getCount();
            }

            // 没有优惠券也就不存在优惠券的核销, SettlementInfo 其他的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
        }

        // 校验传递的优惠券是否是用户自己的
        List<Coupon> coupons = findCouponsByStatus(
                info.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, Coupon> id2Coupon = coupons.stream()
                .collect(Collectors.toMap(
                        Coupon::getId,
                        Function.identity()
                ));
        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId)
                        .collect(Collectors.toList()), id2Coupon.keySet()
        )) {
            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream()
                    .map(SettlementInfo.CouponAndTemplateInfo::getId)
                    .collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem, It Is Not SubCollection" +
                    "Of Coupons!");
            throw new CouponException("User Coupon Has Some Problem, " +
                    "It Is Not SubCollection Of Coupons!");
        }

        log.debug("Current Settlement Coupons Is User's: {}", ctInfos.size());

        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));

        // 通过结算服务获取结算信息
        SettlementInfo processedInfo =
                settlementClient.computeRule(info).getData();
        if (processedInfo.getEmploy() && CollectionUtils.isNotEmpty(
                processedInfo.getCouponAndTemplateInfos()
        )) {
            log.info("Settle User Coupon: {}, {}", info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            // 更新缓存
            redisService.addCouponToCache(
                    info.getUserId(),
                    settleCoupons,
                    CouponStatus.USED.getCode()
            );
            // 更新 db
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream().map(Coupon::getId)
                                    .collect(Collectors.toList())
                    ))
            );
        }

        return processedInfo;
    }

    private double retain2Decimals(double value) {

        // BigDecimal.ROUND_HALF_UP 代表四舍五入
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
