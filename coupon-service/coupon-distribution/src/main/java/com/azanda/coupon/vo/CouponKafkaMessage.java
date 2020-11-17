package com.azanda.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
// coupon kafka 消息对象定义
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponKafkaMessage {

    // coupon status
    private Integer status;
    // coupon id
    private List<Integer> ids;
}
