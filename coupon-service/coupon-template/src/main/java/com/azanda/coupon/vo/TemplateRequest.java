package com.azanda.coupon.vo;

import com.azanda.coupon.constant.CouponCategory;
import com.azanda.coupon.constant.DistributeTarget;
import com.azanda.coupon.constant.ProductLine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;

// coupon 模版创建的请求
// vo都是对应着前端的 只需要写上前端需要的feature
// 如果发起请求创建优惠卷模版的话 这个就是请求的格式
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {

    private String name;
    private String logo;
    private String desc;
    // 这个是产品编号 是string类型的
    private String category;
    private Integer productLine;
    private Integer count;
    private Long userId;
    private Integer target;
    private TemplateRule rule;

    public boolean validate()
    {
        boolean stringValid = StringUtils.isNotEmpty(name)
                && StringUtils.isNotBlank(logo)
                && StringUtils.isNotBlank(desc);
        boolean enumValid = CouponCategory.isExist(category)!=null
                && ProductLine.isExist(productLine)!=null
                && DistributeTarget.isExist(target)!=null;
        boolean numValid = count>0 && userId >0;
        return stringValid && enumValid && numValid && rule.validate();
    }
}
