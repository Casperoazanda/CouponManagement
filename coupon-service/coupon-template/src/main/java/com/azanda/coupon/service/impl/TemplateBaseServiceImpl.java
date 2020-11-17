package com.azanda.coupon.service.impl;

import com.azanda.coupon.dao.CouponTemplateDao;
import com.azanda.coupon.entity.CouponTemplate;
import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.service.ITemplateBaseService;
import com.azanda.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// 通过TemplateDao获取到一些基础的查找功能
// 通过给定的id查找模版 通过数据库中的几列查找信息
@Slf4j
@Service
public class TemplateBaseServiceImpl implements ITemplateBaseService {
    private final CouponTemplateDao templateDao;
    @Autowired
    public TemplateBaseServiceImpl(CouponTemplateDao templateDao) {
        this.templateDao = templateDao;
    }

    @Override
    // 根据couponTemplate Id获取couponTemplate information
    public CouponTemplate buildTemplateInfo(Integer id) throws CouponException {
        // optional是一个容器 里面可以存放null
        // 方法isPresent
        Optional<CouponTemplate> template = templateDao.findById(id);
        // 若查找的id不存在对应couponTemplate
        if(!template.isPresent())
        {
            throw new CouponException("Template is not exist:"+id);
        }
        // 若template中有值 就把这个值取出来
        return template.get();
    }

    @Override
    // 查找所有可用的couponTemplate
    public List<CouponTemplateSDK> findAllUsableTemplate() {
       List<CouponTemplate> templates =
               templateDao.findAllByAvailableAndExpired(true,false);
       return templates.stream()
               .map(this::template2TemplateSDK).collect(Collectors.toList());
    }

    @Override
    // 获取couponTemplate id 到CouponTemplateSDK的映射
    // return 的map<key:id,value:CouponTemplateSDK>
    public Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids) {
        List<CouponTemplate> templates = templateDao.findAllById(ids);
        return templates.stream().map(this::template2TemplateSDK)
                .collect(Collectors.toMap(
                        CouponTemplateSDK::getId, Function.identity()
                ));
    }

    private CouponTemplateSDK template2TemplateSDK(CouponTemplate template)
    {
        return new CouponTemplateSDK(
                template.getId(),
                template.getName(),
                template.getLogo(),
                template.getDesc(),
                template.getCategory().getCode(),
                template.getProductLine().getCode(),
                template.getKey(),// 这个key并不是couponTemplate拼装好的key 已经取出来了id 没必要重新组装
                template.getTarget().getCode(),
                template.getRule()

        );
    }
}
