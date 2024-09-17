package com.youyi.sandbox.aspect;

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
@Aspect
@Order(2)
@Component
public class LoggedCheckAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggedCheckAspect.class);

    @Pointcut("@annotation(com.youyi.example.aspect.LoggedCheck)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // TODO youyi 2024/9/13 Logged Check
        LOGGER.info("logged check");
        return joinPoint.proceed();
    }
}
