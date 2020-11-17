package com.azanda.coupon.service.impl;

import com.azanda.coupon.dao.CouponTemplateDao;
import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.service.IAsyncService;
import com.azanda.coupon.service.IBuildTemplateService;
import com.azanda.coupon.vo.TemplateRequest;
import com.azanda.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 这里我们的传递进来的参数是一个vo中的TemplateRequest的类型
// 将这个参数中传递进来的值进行解析 解析成一个couponTemplate的object并调用dao接口存入到数据库
@Slf4j
@Service
public class BuildTemplateServiceImpl implements IBuildTemplateService {
    // 异步服务
    private final IAsyncService asyncService;
    // CouponTemplate Dao
    private final CouponTemplateDao templateDao;
    @Autowired
    public BuildTemplateServiceImpl(IAsyncService asyncService, CouponTemplateDao templateDao) {
        this.asyncService = asyncService;
        this.templateDao = templateDao;
    }


    @Override
    public CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException {
        // 将request中的参数转换为CouponTemplate的object
        // 先进行validate 判断request中的数据是validated
        if(!request.validate())
        {
            throw new CouponException("Build Template params are not validated");
        }

        // 判断同名字的优惠券模版是否已经存在
        if(templateDao.findByName(request.getName())!=null)
        {
            throw new CouponException("Exist same name template");
        }

        // 构造couponTemplate并保存到数据库中
        CouponTemplate template = requestToTemplate(request);
        // 本来是没有id的 save之后返回的是一个实体类 并加上id
        template = templateDao.save(template);

        // 根据优惠卷模版异步的生成coupon code
        asyncService.asyncConstructCouponByTemplate(template);
        return template;
    }

    // 写一个方法将TemplateRequest类型转换为CouponTemplate类型
    private CouponTemplate requestToTemplate(TemplateRequest request)
    {
        // 直接调用constructor
        return new CouponTemplate(
                request.getName(),
                request.getLogo(),
                request.getDesc(),
                request.getCategory(),
                request.getProductLine(),
                request.getCount(),
                request.getUserId(),
                request.getTarget(),
                request.getRule()
        );
    }
}
