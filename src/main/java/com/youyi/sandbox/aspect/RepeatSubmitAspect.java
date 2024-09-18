package com.youyi.sandbox.aspect;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.youyi.sandbox.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * @author yoyocraft
 * @date 2024/09/17
 */
@Aspect
@Order(4)
@Component
public class RepeatSubmitAspect {

    private final Cache<String, Object> submitCache = CacheBuilder
            .newBuilder()
            .maximumSize(200)
            .expireAfterAccess(1L, TimeUnit.MINUTES)
            .build();


    @Pointcut("execution (* com.youyi.sandbox.controller..*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null) {
            return joinPoint.proceed();
        }

        for (Object param : args) {
            Field uuidField = ReflectionUtils.findField(param.getClass(), "uuid");
            if (uuidField == null) {
                continue;
            }

            uuidField.setAccessible(true);
            String uuid = (String) ReflectionUtils.getField(uuidField, param);
            if (StringUtils.isBlank(uuid)) {
                break;
            }

            synchronized (this) {
                if (submitCache.getIfPresent(uuid) != null) {
                    throw new BusinessException("重复提交");
                }
                submitCache.put(uuid, uuid);
            }
        }
        return joinPoint.proceed();
    }
}
