package com.azanda.coupon.advice;

import com.azanda.coupon.exception.CouponException;
import com.azanda.coupon.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

// 这个才是真正的异常处理
// 这个注解是对所有的controller进行拦截
@RestControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(HttpServletRequest req, CouponException ex)
    {
        CommonResponse<String> response = new CommonResponse<>(
                -1,"business error");
        response.setData(ex.getMessage());
        return response;
    }
}
