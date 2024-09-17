package com.youyi.sandbox.aspect;

import com.alibaba.fastjson2.JSON;
import com.youyi.sandbox.exception.BusinessException;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Aspect
@Order(3)
@Component
public class ParameterCheckAspect {

    @Pointcut("@annotation(com.youyi.example.aspect.ParameterCheck) || " +
            "execution(* com.youyi.example.controller..*.*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = validatorFactory.getValidator();
            for (Object param : args) {
                Set<ConstraintViolation<Object>> checkResult = validator.validate(param);
                if (CollectionUtils.isEmpty(checkResult)) {
                    continue;
                }

                List<String> resultMsgList = checkResult
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.toList());
                throw new BusinessException(JSON.toJSONString(resultMsgList));
            }
        }

        return joinPoint.proceed();
    }
}
