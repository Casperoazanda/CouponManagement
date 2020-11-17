package com.azanda.coupon.vo;

import com.azanda.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

// 这个是coupon 规则 写到vo中的都是java object 之后都是要转换为json数据的
// 这个类里会再定义几个不同的类 并各自实现各自类中的方法
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRule {
    // 优惠卷过期的规则 这个是下面刚刚创建好的子类
    private Expiration expiration;
    // 折扣的规则
    private Discount discount;
    // 每个人最多领几张coupon的限制
    private Integer limitation;
    // 使用范围的限制：地域+商品类型
    private Usage usage;
    // 权重（可以和哪些优惠卷叠加使用 同一类的coupon不可以一起用：List[] coupon的唯一编码）
    private String weight;

    public boolean validate()
    {
        return expiration.validate() && discount.validate()
                && limitation>0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    // 定义的第一个子类：有效期的规则
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Expiration
    {
        // 对应constant中periodType中的code
        private Integer PeriodCode;
        // 只适用于变动的有效期
        private Integer gap;
        // 使用于所有的有效期规则
        private Long deadline;
        boolean validate()
        {
            return PeriodType.isExist(PeriodCode)!=null && gap>0 && deadline>0;
        }

    }
    // 定义的第二个子类：折扣的规则指定,需要和优惠卷类型绑定
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Discount
    {
        // 这个是优惠的额度 对应的是 满减(20), 折扣(85), 立减(10)
        private Integer quota;
        // 这个是满减的base
        private Integer base;
        boolean validate()
        {
            return quota>0 && base>0;
        }
    }
    // 定义的第三个子类：coupon的使用范围是水果类 还是家居用品类
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage
    {
        private String province;
        private String city;
        private String productType;
        boolean validate(){
            return StringUtils.isNotBlank(province)
                    && StringUtils.isNotBlank(city)
                    && StringUtils.isNotBlank(productType);
        }

    }
}
