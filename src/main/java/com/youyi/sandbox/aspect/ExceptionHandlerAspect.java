package com.youyi.sandbox.aspect;

import com.youyi.sandbox.base.Result;
import com.youyi.sandbox.enums.ReturnCodeEnum;
import com.youyi.sandbox.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Component
@Order(1)
@Aspect
public class ExceptionHandlerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAspect.class);

    @Pointcut("execution(* com.youyi.example.controller..*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (BusinessException e) {
            LOGGER.warn("BUSINESS ERROR", e);
            return Result.fail(e.getReturnCodeEnum());
        } catch (Throwable e) {
            LOGGER.error("Request ERROR", e);
            return Result.fail(ReturnCodeEnum.SYSTEM_ERROR);
        }
    }
}
