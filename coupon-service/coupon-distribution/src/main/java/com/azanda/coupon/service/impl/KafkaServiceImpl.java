package com.azanda.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.azanda.coupon.constant.Constant;
import com.azanda.coupon.constant.CouponStatus;
import com.azanda.coupon.dao.CouponDao;
import com.azanda.coupon.entity.Coupon;
import com.azanda.coupon.service.IKafkaService;
import com.azanda.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// kafka相关的服务接口实现
// 核心思想：将cache中的coupon status变化后的数据同步到db中
// 从kafka中消费数据
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {

    private final CouponDao couponDao;
    @Autowired
    public KafkaServiceImpl(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Override
    @KafkaListener(topics = {Constant.TOPIC},groupId = "imooc-coupon-1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record)
    {
        // 有kafkaListener 如果发现有消息来的时候就会给到这个record中
        // 从record中获取到消息
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if(kafkaMessage.isPresent())
        {
            // If a value is present in this Optional, returns the value, otherwise throws NoSuchElementException.
            // 这个是get方法的定义 取出来的是一个value
            // 但这一个value包含一个状态值 和多个该状态值下的ids
            Object message = kafkaMessage.get();
            // 利用自己在vo中定义的kafka模版 将获得的消息转化为java object对象
            CouponKafkaMessage couponInfo = JSON.parseObject(
                    message.toString(),
                    CouponKafkaMessage.class
            );

            log.info("Received CouponKafkaMessage:{}",
                    message.toString());
            CouponStatus status = CouponStatus.isExist(couponInfo.getStatus());
            // 不需要处理usable 只需要在数据库中存储used和expired
            // 能生成usable coupon说明db中已经存了coupon 才会有返回回来的id
            switch (status)
            {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo,status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo,status);
                    break;
            }

        }
    }
    // kafkaMessage 是从kafka队列中接受到值后转化为自定义的java object couponKafkaMessage
    private void processCouponsByStatus(CouponKafkaMessage kafkaMessage,
                                        CouponStatus status)
    {
        List<Coupon> coupons = couponDao.findAllById(
                    kafkaMessage.getIds()
        );

        if(CollectionUtils.isEmpty(coupons) ||
                coupons.size()!= kafkaMessage.getIds().size())
        {
            log.error("Can not Find Right Coupon Info:{}",
                    JSON.toJSONString(kafkaMessage));
            // TODO: send email
            return;
        }

        coupons.forEach( c->c.setStatus(status));
        // 最后将status全部改了的coupon在数据库中保存
        log.info("CouponKafkaMessage Op Coupon Count:{}",
                couponDao.saveAll(coupons).size());

    }

    // 处理已使用的用户coupon
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage,
                                    CouponStatus status)
    {
        // TODO: send text to uses
        processCouponsByStatus(kafkaMessage,status);
    }

    // 处理过期的用户coupon
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage,
                                       CouponStatus status)
    {
        processCouponsByStatus(kafkaMessage, status);
    }


}
