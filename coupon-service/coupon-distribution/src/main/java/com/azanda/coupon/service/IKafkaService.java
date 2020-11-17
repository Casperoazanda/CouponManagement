package com.azanda.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

// 从kafka中消费队列中的数据
public interface IKafkaService {
    // ConsumerRecord是一个kafka队列中的类型
    // 这里用到了generic 指定这里面所操作的数据类型指定为这个参数
    void consumeCouponKafkaMessage(ConsumerRecord<?,?> record);
}
