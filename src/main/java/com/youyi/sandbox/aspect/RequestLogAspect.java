package com.youyi.sandbox.aspect;

import com.alibaba.fastjson2.JSON;
import com.youyi.sandbox.constants.SymbolConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Aspect
@Order(0)
@Component
public class RequestLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogAspect.class);

    @Pointcut("execution(* com.youyi.example.controller..*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Object[] args = joinPoint.getArgs();
            String methodName = joinPoint.getTarget().getClass().getName()
                    + SymbolConstant.DOT + methodSignature.getName();

            StringBuilder requestLogBuilder = new StringBuilder();
            requestLogBuilder
                    .append(SymbolConstant.NEW_LINE)
                    .append(String.format("%s request to %s", "[mock ip]", methodName));

            if (args.length > 0) {
                StringBuilder paramsBuilder = new StringBuilder();
                for (Object param : args) {
                    if (param instanceof MultipartFile) {
                        // skip file
                        continue;
                    }

                    paramsBuilder.append(JSON.toJSONString(param)).append(SymbolConstant.COMMA);
                }
                if (paramsBuilder.length() > 0) {
                    requestLogBuilder
                            .append(SymbolConstant.NEW_LINE)
                            .append("params: ")
                            .append(paramsBuilder.substring(0, paramsBuilder.length() - 1));
                }
            }

            long startTime = System.currentTimeMillis();

            // 理论上这是最外层的切面，不可能抛异常
            Object result = joinPoint.proceed();

            long costTime = System.currentTimeMillis() - startTime;
            if (costTime > 200) {
                LOGGER.warn("耗时接口: {}ms, requestLog: {}", costTime, requestLogBuilder);
            }

            if (result != null) {
                requestLogBuilder
                        .append(SymbolConstant.NEW_LINE)
                        .append("result: ").append(JSON.toJSONString(result))
                        .append(" cost:").append(costTime).append("ms");
            }

            LOGGER.info(requestLogBuilder.toString());
            return result;
        } catch (Throwable e) {
            LOGGER.error("RequestLogAspect ERROR", e);
            // 兜底
            return joinPoint.proceed();
        }
    }
}
