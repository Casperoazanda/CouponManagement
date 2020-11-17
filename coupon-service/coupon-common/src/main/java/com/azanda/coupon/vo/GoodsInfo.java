package com.azanda.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//  可以传回前端的goods
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {
    // 商品的类型 在constant中的GoodsType
    private Integer code;
    // 商品价格
    private Double price;
    // 商品数量
    private Integer count;


}
